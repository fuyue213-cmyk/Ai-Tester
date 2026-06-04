package com.fuyue.domain.agent.model.singles;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 流式信号基类
 * 用于前端展示的流式输出信号，统一封装不同类型的输出内容
 * 注意：序列化由adapter层处理，client层只定义数据结构
 *
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StreamSignal {

    /**
     * 信号类型
     */
    protected SignalType type;

    /**
     * 时间戳
     */
    protected LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 节点名称（可选，用于标识信号来源）
     */
    protected String node;

    /**
     * 信号类型枚举
     */
    public enum SignalType {
        /**
         * 开始输出信号
         */
        START,
        /**
         * 文本块（LLM流式输出的增量内容）
         */
        TEXT_CHUNK,

        /**
         * 思考过程（模型的推理过程，如DeepSeek的Thinking）
         */
        THINKING,

        /**
         * 工具调用请求
         */
        TOOL_CALL,

        /**
         * 工具执行结果
         */
        TOOL_RESULT,

        /**
         * 分隔符（用于区分不同sheet或阶段）
         */
        SEPARATOR,

        /**
         * 错误信息
         */
        ERROR,

        /**
         * 完成信号
         */
        COMPLETE
    }
}
