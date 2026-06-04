package com.fuyue.domain.agent.model.singles;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 文本块信号
 * 
 * 用于传输LLM流式输出的增量文本内容
 * 
 * @author AI Tester
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TextChunkSignal extends StreamSignal {
    
    /**
     * 文本内容（增量）
     */
    private String text;
    
    /**
     * 是否为完成标志（true表示这是该段文本的最后一块）
     */
    private boolean finished;

    public TextChunkSignal(String text) {
        this(text, false);
    }

    public TextChunkSignal(String text, boolean finished) {
        super();
        this.type = SignalType.TEXT_CHUNK;
        this.text = text;
        this.finished = finished;
    }
}
