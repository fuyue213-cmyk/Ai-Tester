package com.fuyue.app.converter;

import com.fuyue.client.vo.StreamEventVo;
import com.fuyue.domain.agent.model.singles.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 流式事件转换器
 *
 * 将StreamSignal转换为简化的StreamEventVo，参考Python实现的扁平化数据结构
 *
 * @author AI Tester
 */
@Slf4j
@Component
public class StreamEventConverter {

    /**
     * 将StreamSignal转换为StreamEventVo
     *
     * @param signal StreamSignal对象
     * @return StreamEventVo对象
     */
    public StreamEventVo toStreamEventVo(StreamSignal signal) {
        if (signal == null) return null;

        StreamEventVo.StreamEventVoBuilder builder = StreamEventVo.builder();
        // 根据信号类型进行转换
        switch (signal.getType()) {
            case START:
                if (signal instanceof StartSignal startSignal) {
                    if (startSignal.getMessage() != null && !startSignal.getMessage().isEmpty()) {
                        builder.chunk(startSignal.getMessage());
                    }
                    builder.start(true);
                    builder.status(startSignal.getStartType() != null ? startSignal.getStartType() : "started");
                }
                break;
            case TEXT_CHUNK:
                if (signal instanceof TextChunkSignal textChunk) {
                    builder.chunk(textChunk.getText());
                    // finished字段不映射到VO，因为VO中没有对应字段
                    // 如果需要表示完成，可以通过done字段
                }
                break;

            case THINKING:
                if (signal instanceof ThinkingSignal thinking) {
                    builder.chunk(thinking.getContent());
                    // 如果思考完成，可以设置done
                    if (thinking.isFinished()) {
                        builder.done(true);
                    }
                }
                break;

            case SEPARATOR:
                if (signal instanceof SeparatorSignal separator) {
                    // 分隔符可以作为chunk内容，也可以设置fileStart表示新阶段开始
                    if (separator.getText() != null && !separator.getText().isEmpty()) {
                        builder.chunk(separator.getText());
                    }
                    // 分隔符通常表示新阶段开始
                    builder.fileStart(true);
                    // 分隔符类型透传，前端根据 separatorType（如 STEP11_LINK_PARAM）判断是否绘制桑基图
                    if (separator.getSeparatorType() != null) {
                        builder.separatorType(separator.getSeparatorType());
                    }
                }
                break;

            case ERROR:
                if (signal instanceof ErrorSignal error) {
                    // 构建错误消息
                    StringBuilder errorMsg = new StringBuilder();
                    if (error.getMessage() != null) {
                        errorMsg.append(error.getMessage());
                    }
                    if (error.getCode() != null) {
                        if (!errorMsg.isEmpty()) {
                            errorMsg.append(" [").append(error.getCode()).append("]");
                        } else {
                            errorMsg.append("Error Code: ").append(error.getCode());
                        }
                    }
                    if (error.getDetails() != null) {
                        if (!errorMsg.isEmpty()) {
                            errorMsg.append("\nDetails: ").append(error.getDetails());
                        } else {
                            errorMsg.append(error.getDetails());
                        }
                    }
                    builder.error(errorMsg.toString());
                    builder.done(true); // 错误通常表示流结束
                    builder.status("failed");
                }
                break;

            case COMPLETE:
                if (signal instanceof CompleteSignal complete) {
                    // 完成消息可以作为chunk
                    if (complete.getMessage() != null && !complete.getMessage().isEmpty()) {
                        builder.chunk(complete.getMessage());
                    }
                    builder.done(true);
                    // 使用completeType作为status，如果没有则默认为"completed"
                    builder.status(complete.getCompleteType() != null ? complete.getCompleteType() : "completed");
                }
                break;

            case TOOL_CALL:
                if (signal instanceof ToolCallSignal toolCall) {
                    // 工具调用可以格式化为文本
                    String toolCallText = String.format("Tool Call: %s (id: %s)",
                            toolCall.getToolName(), toolCall.getCallId());
                    builder.chunk(toolCallText);
                }
                break;

            case TOOL_RESULT:
                if (signal instanceof ToolResultSignal toolResult) {
                    // 工具结果可以格式化为文本
                    String toolResultText = String.format("Tool Result: %s (id: %s) - %s",
                            toolResult.getToolName(),
                            toolResult.getCallId(),
                            toolResult.isSuccess() ? "Success" : "Failed");
                    if (toolResult.getResult() != null) {
                        toolResultText += "\n" + toolResult.getResult();
                    }
                    if (toolResult.getError() != null) {
                        toolResultText += "\nError: " + toolResult.getError();
                    }
                    builder.chunk(toolResultText);
                }
                break;

            default:
                log.warn("未知的信号类型: {}", signal.getType());
                break;
        }

        return builder.build();
    }
}
