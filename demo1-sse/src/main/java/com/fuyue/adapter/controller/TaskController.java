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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@Tag(name = "任务")
@AllArgsConstructor
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskAppService taskAppService;
    private final SseExclusiveSubscriptionGate sseExclusiveSubscriptionGate;

    /**
     * 执行步骤（SSE 流式推送）。
     * <p>
     * 页面刷新后，前端通过相同的 taskId + stageId 再次请求，直接复用当前正在执行的步骤流。
     * 这里不再依赖 Last-Event-ID 做事件级续传，只保留 SSE 的 event/id/retry/data 输出格式。
     * </p>
     */
    @Operation(
            summary = "执行步骤（SSE）",
            description = "支持 SSE 流式推送、刷新后基于相同 taskId + stageId 继续订阅当前步骤流，以及同一路由键的互斥订阅。"
    )
    @PutMapping(value = "/{taskId}/{stageId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<ServerSentEvent<StreamEventVo>>> executeStep(
            @PathVariable("taskId") String taskId,
            @PathVariable("stageId") long stageId) {
        log.info("接收执行步骤请求: taskId={}, stageId={}", taskId, stageId);

        Flux<StreamEventVo> eventVoFlux = sseExclusiveSubscriptionGate.exclusive(
                "sse:step:" + taskId + ":" + stageId,
                taskAppService.execute(taskId, stageId));

        Flux<ServerSentEvent<StreamEventVo>> sseFlux = eventVoFlux
                .index()
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
}
