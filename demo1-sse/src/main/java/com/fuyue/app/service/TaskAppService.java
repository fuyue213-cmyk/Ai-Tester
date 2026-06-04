package com.fuyue.app.service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.fuyue.app.converter.StreamEventConverter;
import com.fuyue.client.vo.StreamEventVo;
import com.fuyue.domain.agent.model.singles.StreamSignal;
import com.fuyue.domain.task.ability.TaskStageDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;


/**
 * 任务应用服务
 * <p>
 * 负责任务的创建和执行，协调领域服务完成业务逻辑。
 *
 * @author JarkimZhu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskAppService {
    private final TaskStageDomainService taskStageDomainService;
    private final StreamEventConverter streamEventConverter;

    private final ConcurrentHashMap<String, Flux<StreamEventVo>> runningStreams = new ConcurrentHashMap<>();

    /**
     * 执行任务步骤
     * <p>
     * 支持断线重连：如果该步骤已经有正在执行的流，则直接返回该流。
     * 将领域层的StreamSignal转换为应用层的StreamEventVo。
     *
     * @param taskId  任务ID
     * @param stageId 步骤ID
     * @return 流式事件VO流，用于 SSE 推送
     */
    @Transactional(rollbackFor = Throwable.class)
    public Flux<StreamEventVo> execute(String taskId, long stageId) {
        log.info("执行任务步骤: taskId={}, stageId={}", taskId, stageId);

        // 获取领域层的StreamSignal流
        Flux<StreamSignal> signalFlux = taskStageDomainService.execute(taskId, stageId)
                .cast(StreamSignal.class);

        // 转换为应用层的StreamEventVo
        return signalFlux
                .map(streamEventConverter::toStreamEventVo)
                .filter(Objects::nonNull); // 过滤掉null值
    }


}
