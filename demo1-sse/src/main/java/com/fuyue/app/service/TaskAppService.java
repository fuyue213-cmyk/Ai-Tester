package com.fuyue.app.service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import com.fuyue.client.vo.StreamEventVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;


/**
 * demo1-sse 专用的最小任务流服务。
 *
 * <p>只保留两件事：</p>
 * <p>1. 相同 taskId + stageId 复用同一条事件流，便于断线重连。</p>
 * <p>2. 让控制层可以对同一条流做互斥订阅测试。</p>
 */
@Slf4j
@Service
public class TaskAppService {

    private final ConcurrentHashMap<String, Flux<StreamEventVo>> runningStreams = new ConcurrentHashMap<>();

    public Flux<StreamEventVo> execute(String taskId, long stageId) {
        String streamKey = buildStreamKey(taskId, stageId);
        Flux<StreamEventVo> existing = runningStreams.get(streamKey);
        if (existing != null) {
            log.info("复用已有演示流: streamKey={}", streamKey);
            return existing;
        }
        return runningStreams.computeIfAbsent(streamKey, key -> createDemoStream(taskId, stageId, key));
    }

    private Flux<StreamEventVo> createDemoStream(String taskId, long stageId, String streamKey) {
        log.info("创建新的演示流: streamKey={}", streamKey);

        Flux<StreamEventVo> source = Flux.concat(
                        Flux.just(StreamEventVo.builder()
                                .start(true)
                                .status("running")
                                .chunk("开始执行测试流: taskId=%s, stageId=%s".formatted(taskId, stageId))
                                .build()),
                        Flux.interval(Duration.ofSeconds(1))
                                .take(8)
                                .map(index -> StreamEventVo.builder()
                                        .status("running")
                                        .chunk("第 %d 条流事件，验证断线重连和互斥订阅".formatted(index + 1))
                                        .build()),
                        Flux.just(StreamEventVo.builder()
                                .done(true)
                                .status("completed")
                                .chunk("演示流结束")
                                .build()))
                .doOnSubscribe(subscription -> log.info("演示流开始执行: streamKey={}", streamKey))
                .doFinally(signalType -> {
                    runningStreams.remove(streamKey);
                    log.info("演示流结束并移除缓存: streamKey={}, signalType={}", streamKey, signalType);
                });

        ConnectableFlux<StreamEventVo> replayable = source.replay(32);
        replayable.connect();
        return replayable;
    }

    private String buildStreamKey(String taskId, long stageId) {
        return taskId + ":" + stageId;
    }
}
