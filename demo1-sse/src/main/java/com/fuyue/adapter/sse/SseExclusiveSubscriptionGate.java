package com.fuyue.adapter.sse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

/**
 * 同一路由键下仅保留最近一次 SSE 对上游 {@link Flux} 的订阅。
 * <p>
 * 背景：任务步骤执行流通过 {@code ITaskStreamManager} 对同一 {@code taskId-stageId} 复用同一
 * {@code Flux}（断线重连）。若旧 SSE 仍未断开时再次发起请求，会对该 {@code Flux} 产生多个订阅者，
 * Reactor 会将同一元素推送给所有订阅者，表现为多端重复输出。新请求到达时取消上一路订阅，可保证
 * 任意时刻仅有一个活跃消费者（与「刷新重连」场景兼容：旧连接断开后仅新连接存在）。
 * </p>
 *
 * @author AI Tester
 */
@Slf4j
@Component
public class SseExclusiveSubscriptionGate {

    private final ConcurrentHashMap<String, Runnable> activeCancellers = new ConcurrentHashMap<>();

    /**
     * 对 {@code source} 做互斥订阅包装：同一 {@code routeKey} 上新订阅建立时，取消该键下上一路订阅。
     *
     * @param routeKey 路由键（建议包含业务前缀，避免不同接口互相抢占）
     * @param source   上游流
     * @param <T>      元素类型
     * @return 包装后的流
     */
    public <T> Flux<T> exclusive(String routeKey, Flux<T> source) {
        AtomicReference<Runnable> selfCancelRef = new AtomicReference<>();
        return source
                .doOnSubscribe(subscription -> {
                    Runnable cancelMe = subscription::cancel;
                    selfCancelRef.set(cancelMe);
                    Runnable previous = activeCancellers.put(routeKey, cancelMe);
                    if (previous != null) {
                        log.info("取消同键上一路 SSE 订阅: routeKey={}", routeKey);
                        previous.run();
                    }
                })
                .doFinally(signalType -> {
                    Runnable me = selfCancelRef.get();
                    if (me != null) {
                        activeCancellers.remove(routeKey, me);
                    }
                });
    }
}
