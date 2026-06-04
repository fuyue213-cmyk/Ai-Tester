package com.fuyue.domain.agent.model.singles;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 错误信号
 * 
 * 用于传输错误信息
 * 
 * @author AI Tester
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ErrorSignal extends StreamSignal {
    
    /**
     * 错误消息
     */
    private String message;
    
    /**
     * 错误代码（可选）
     */
    private String code;
    
    /**
     * 错误详情（可选）
     */
    private String details;
    
    public ErrorSignal(String message, String code, String details) {
        super();
        this.type = SignalType.ERROR;
        this.message = message;
        this.code = code;
        this.details = details;
    }
}
