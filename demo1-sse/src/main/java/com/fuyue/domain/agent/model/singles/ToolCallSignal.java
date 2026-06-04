package com.fuyue.domain.agent.model.singles;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 工具调用信号
 * 
 * 用于传输Agent请求调用工具的信息
 * 
 * @author AI Tester
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ToolCallSignal extends StreamSignal {
    
    /**
     * 工具名称
     */
    private String toolName;
    
    /**
     * 工具调用ID
     */
    private String callId;
    
    /**
     * 工具参数
     */
    private Map<String, Object> arguments;
    
    public ToolCallSignal(String toolName, String callId, Map<String, Object> arguments) {
        super();
        this.type = SignalType.TOOL_CALL;
        this.toolName = toolName;
        this.callId = callId;
        this.arguments = arguments;
    }
}
