package com.fuyue.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式事件视图对象（简化版）
 *
 * 用于SSE响应，参考Python实现的扁平化数据结构，但保留SSE规范（event、id、retry在SSE层面处理）
 *
 * 数据结构说明：
 * - chunk: 文本内容（用于TEXT_CHUNK、THINKING、SEPARATOR等文本类型）
 * - error: 错误信息（用于ERROR类型）
 * - done: 是否完成（用于COMPLETE类型或表示流结束）
 * - status: 状态（可选，用于COMPLETE类型，如"completed"、"failed"等）
 * - file_start: 是否文件开始（可选，用于标识新文件或新阶段的开始）
 *
 * @author AI Tester
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamEventVo {
    /**
     * 文本内容（增量）
     * 用于TEXT_CHUNK、THINKING、SEPARATOR等类型
     */
    private String chunk;

    /**
     * 错误信息
     * 用于ERROR类型
     */
    private String error;

    /**
     * 是否完成
     * true表示流结束或某个阶段完成
     */
    private Boolean done;

    /**
     * 状态
     * 用于COMPLETE类型，如"completed"、"failed"等
     */
    private String status;

    /**
     * 是否文件开始
     * true表示新文件或新阶段的开始
     */
    private Boolean fileStart;

    /**
     * 是否步骤开始
     * true表示步骤开始
     */
    private boolean start;
    /**
     * 分隔符类型（可选）
     * 用于前端区分结构化数据。例如 "STEP11_LINK_PARAM" 表示订阅关系 JSON，可据此绘制桑基图。
     */
    private String separatorType;
}
