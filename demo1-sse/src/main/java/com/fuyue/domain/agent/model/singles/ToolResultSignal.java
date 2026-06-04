package com.fuyue.domain.agent.model.singles;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 工具执行结果信号
 * 
 * 用于传输工具执行后的返回结果
 * 
 * @author AI Tester
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ToolResultSignal extends StreamSignal {
    
    /**
     * 工具名称
     */
    private String toolName;
    
    /**
     * 工具调用ID
     */
    private String callId;
    
    /**
     * 执行结果
     */
    private String result;
    
    /**
     * 是否执行成功
     */
    private boolean success;
    
    /**
     * 错误信息（如果执行失败）
     */
    private String error;
    
    public ToolResultSignal(String toolName, String callId, String result, boolean success, String error) {
        super();
        this.type = SignalType.TOOL_RESULT;
        this.toolName = toolName;
        this.callId = callId;
        this.result = result;
        this.success = success;
        this.error = error;
    }
}
