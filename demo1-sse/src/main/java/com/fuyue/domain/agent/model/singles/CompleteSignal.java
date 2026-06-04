package com.fuyue.domain.agent.model.singles;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 完成信号
 *
 * 用于标识某个处理阶段或整个任务完成
 *
 * @author AI Tester
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CompleteSignal extends StreamSignal {

    /**
     * 完成消息（可选）
     */
    private String message;

    /**
     * 完成类型（可选，用于区分不同的完成场景）
     */
    private String completeType;

    public CompleteSignal(String message, String completeType) {
        super();
        this.type = SignalType.COMPLETE;
        this.message = message;
        this.completeType = completeType;
    }
}
