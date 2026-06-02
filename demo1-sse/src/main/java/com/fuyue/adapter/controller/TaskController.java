package com.fuyue.adapter.controller;

import java.time.Duration;

import com.fuyue.adapter.sse.SseExclusiveSubscriptionGate;
import com.fuyue.app.service.TaskAppService;
import com.fuyue.client.vo.StreamEventVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * demo1-sse 控制器。
 *
 * <p>只测试两件事：</p>
 * <p>1. Last-Event-ID 断线重连。</p>
 * <p>2. 同一路由键的互斥订阅。</p>
 */
@Slf4j
@Tag(name = "任务")
@AllArgsConstructor
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskAppService taskAppService;
    private final SseExclusiveSubscriptionGate sseExclusiveSubscriptionGate;

    @Operation(summary = "执行步骤（SSE）", description = "仅用于测试断线重连和互斥订阅。")
    @PutMapping(value = "/{taskId}/{stageId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<ServerSentEvent<StreamEventVo>>> executeStep(
            @PathVariable("taskId") String taskId,
            @PathVariable("stageId") long stageId,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        log.info("接收执行步骤请求: taskId={}, stageId={}, lastEventId={}", taskId, stageId, lastEventId);
        long lastSeenSequence = parseLastSeenSequence(lastEventId, stageId);

        Flux<StreamEventVo> eventVoFlux = sseExclusiveSubscriptionGate.exclusive(
                "sse:step:" + taskId + ":" + stageId,
                taskAppService.execute(taskId, stageId));

        Flux<ServerSentEvent<StreamEventVo>> sseFlux = eventVoFlux
                .index()
                .filter(tuple -> tuple.getT1() + 1 > lastSeenSequence)
                .map(tuple -> {
                    long index = tuple.getT1();
                    StreamEventVo eventVo = tuple.getT2();
                    String eventId = stageId + "-" + (index + 1);

                    return ServerSentEvent.<StreamEventVo>builder()
                            .id(eventId)
                            .event(resolveEventType(eventVo))
                            .data(eventVo)
                            .retry(Duration.ofMillis(3000))
                            .build();
                });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_EVENT_STREAM);
        headers.setCacheControl("no-cache");
        headers.set("Connection", "keep-alive");
        headers.set("X-Accel-Buffering", "no");

        return ResponseEntity.ok()
                .headers(headers)
                .body(sseFlux);
    }

    private String resolveEventType(StreamEventVo eventVo) {
        if (eventVo.isStart()) {
            return "started";
        }
        if (eventVo.getError() != null) {
            return "error";
        }
        if (Boolean.TRUE.equals(eventVo.getDone())) {
            return "complete";
        }
        if (eventVo.getChunk() != null) {
            return "chunk";
        }
        return "message";
    }

    private long parseLastSeenSequence(String lastEventId, long stageId) {
        if (lastEventId == null || lastEventId.isBlank()) {
            return 0;
        }
        String prefix = stageId + "-";
        if (!lastEventId.startsWith(prefix)) {
            log.warn("忽略无法识别的 Last-Event-ID: stageId={}, lastEventId={}", stageId, lastEventId);
            return 0;
        }
        try {
            return Long.parseLong(lastEventId.substring(prefix.length()));
        } catch (NumberFormatException ex) {
            log.warn("忽略非法的 Last-Event-ID: stageId={}, lastEventId={}", stageId, lastEventId);
            return 0;
        }
    }
}
