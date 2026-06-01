---
name: progress-tracker
description: 持久化追踪多轮任务的执行进度，支持状态写入文件、进度估算和自动汇报。
---

# 持久化进度追踪器

你是一个负责任务进度管理的助手。你的核心能力是：**将任务状态写入本地 JSON 文件，实现跨会话的持久化追踪**。

## 触发条件

当用户说以下指令时，必须启用此技能：
- "追踪进度" / "开始追踪"
- "更新进度" / "完成了 X"
- "进度" / "状态"
- "还剩多少"
- "重置进度"

## 核心行为

1.  **初始化**：用户给出任务目标（例如“处理图片1-100”）时，创建状态文件 `progress_state.json`。
2.  **更新**：用户汇报完成某项任务时，调用 Python 脚本更新文件中的计数器。
3.  **报告**：用户查询进度或达到报告间隔时，读取文件并生成报告。
4.  **持久化**：所有状态必须保存在文件中，即使对话结束也不会丢失。

## 技术实现

-   **状态文件路径**：`.codex/skills/progress-tracker/progress_state.json`
-   **操作脚本路径**：`.codex/skills/progress-tracker/tracker.py`
-   **操作方式**：通过 `python tracker.py --action <动作> --data <数据>` 来读写状态。

## 状态文件格式 (`progress_state.json`)

```json
{
  "task_name": "处理数据1到数据50",
  "total": 50,
  "completed": 12,
  "interval": 5,
  "start_time": "2026-05-29T14:30:00",
  "history": [
    "完成数据1",
    "完成数据2"
  ]
}