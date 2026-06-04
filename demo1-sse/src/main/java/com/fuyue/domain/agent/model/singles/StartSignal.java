package com.fuyue.domain.agent.model.singles;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 开始流程标签
 * <p>
 * 用于传输LLM流式输出的增量文本内容
 *
 * @author AI Tester
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StartSignal extends StreamSignal {

    /**
     * 完成消息（可选）
     */
    private String message;

    /**
     * 完成类型（可选，用于区分不同的完成场景）
     */
    private String startType;

    public StartSignal(String message, String startType) {
        super();
        this.type = SignalType.START;
        this.message = message;
        this.startType = startType;
    }
}
