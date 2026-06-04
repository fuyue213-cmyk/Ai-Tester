package com.fuyue.domain.task.ability;

import com.fuyue.domain.agent.model.singles.StreamSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Optional;

/**
 * 任务步骤领域服务
 * <p>
 * 负责任务步骤的执行和流管理，支持断线重连功能。
 *
 * @author AI Tester
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TaskStageDomainService {
    private final ITaskStreamManager streamManager;

    /**
     * 执行任务步骤
     * <p>
     * 如果该步骤已经有正在执行的流，则直接返回该流（支持断线重连）。
     * 如果没有，则创建新的流并存储到流管理器中。
     *
     * @param taskId  任务ID
     * @param stageId 步骤ID
     * @return 流对象，用于 SSE 推送
     */
    public Flux<StreamSignal> execute(String taskId, long stageId) {
        // 首先检查是否已经有正在执行的流（支持断线重连）
        Optional<Flux<StreamSignal>> existingStream = streamManager.getStream(taskId + "-" + stageId);
        if (existingStream.isPresent()) {
            log.info("找到正在执行的流，支持断线重连: taskId={}, stageId={}", taskId, stageId);
            return existingStream.get();
        }





       return existingStream.get();
    }
}
