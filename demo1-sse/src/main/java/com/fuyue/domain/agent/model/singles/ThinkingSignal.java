package com.fuyue.domain.agent.model.singles;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 思考过程信号
 * 
 * 用于传输模型的推理过程（如DeepSeek-R1的Thinking内容）
 * 
 * @author AI Tester
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ThinkingSignal extends StreamSignal {
    
    /**
     * 思考内容
     */
    private String content;
    
    /**
     * 是否为完成标志
     */
    private boolean finished;
    
    public ThinkingSignal(String content, boolean finished) {
        super();
        this.type = SignalType.THINKING;
        this.content = content;
        this.finished = finished;
    }
}
