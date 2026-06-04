package com.fuyue.domain.agent.model.singles;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 分隔符信号
 * 
 * 用于区分不同的处理阶段或sheet
 * 
 * @author AI Tester
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SeparatorSignal extends StreamSignal {
    
    /**
     * 分隔符文本（可选）
     */
    private String text;
    
    /**
     * 分隔符类型（可选，用于前端区分不同的分隔场景）
     */
    private String separatorType;
    
    public SeparatorSignal(String text, String separatorType) {
        super();
        this.type = SignalType.SEPARATOR;
        this.text = text;
        this.separatorType = separatorType;
    }
}
