
---

### 2. 配套 Python 脚本 `tracker.py`

将这个文件放在与 `SKILL.md` 相同的目录下：`.codex/skills/progress-tracker/tracker.py`

```python
import os
import json
import argparse
from datetime import datetime

# 定义状态文件的路径 (与脚本在同一目录)
STATE_FILE = os.path.join(os.path.dirname(__file__), "progress_state.json")

def load_state():
    """从文件加载状态，如果文件不存在则返回 None"""
    if not os.path.exists(STATE_FILE):
        return None
    with open(STATE_FILE, 'r', encoding='utf-8') as f:
        return json.load(f)

def save_state(state):
    """保存状态到文件"""
    with open(STATE_FILE, 'w', encoding='utf-8') as f:
        json.dump(state, f, ensure_ascii=False, indent=2)

def init_task(total, name, interval=5):
    """初始化新任务"""
    if load_state() is not None:
        print("⚠️ 已有进行中的任务，请先使用 reset 重置")
        return

    state = {
        "task_name": name,
        "total": total,
        "completed": 0,
        "interval": interval,
        "start_time": datetime.now().isoformat(),
        "history": []
    }
    save_state(state)
    print(f"✅ 任务已创建: {name} (总计 {total} 项，每 {interval} 轮汇报一次)")

def complete_task(item_description):
    """标记完成一项任务"""
    state = load_state()
    if not state:
        print("❌ 没有进行中的任务，请先使用 init 初始化")
        return

    state["completed"] += 1
    state["history"].append(item_description)
    # 保持历史记录最多 20 条，避免文件过大
    if len(state["history"]) > 20:
        state["history"] = state["history"][-20:]

    save_state(state)
    
    # 判断是否完成任务
    if state["completed"] >= state["total"]:
        end_time = datetime.now()
        start_time = datetime.fromisoformat(state["start_time"])
        elapsed = end_time - start_time
        print(f"🎉 恭喜！任务 '{state['task_name']}' 已完成！")
        print(f"   总用时: {elapsed}")
        # 可选：删除或重命名状态文件
        # os.remove(STATE_FILE)
    else:
        print(f"✅ 已完成: {item_description}")
        print(f"   进度: {state['completed']}/{state['total']}")

def show_status():
    """显示当前进度"""
    state = load_state()
    if not state:
        print("❌ 无进行中的任务")
        return

    now = datetime.now()
    start = datetime.fromisoformat(state["start_time"])
    elapsed = now - start
    elapsed_min = elapsed.total_seconds() / 60

    completed = state["completed"]
    total = state["total"]
    
    if completed > 0:
        rate = completed / elapsed_min
        remaining_min = (total - completed) / rate if rate > 0 else 0
    else:
        rate = 0
        remaining_min = 0

    percent = (completed / total) * 100 if total > 0 else 0
    
    # 生成简单进度条
    bar_len = 20
    filled = int(bar_len * completed / total)
    bar = '█' * filled + '░' * (bar_len - filled)

    # 输出 JSON 格式，方便 AI 解析
    result = {
        "task_name": state["task_name"],
        "completed": completed,
        "total": total,
        "percent": round(percent, 1),
        "elapsed_min": round(elapsed_min, 1),
        "remaining_min": round(remaining_min, 1),
        "rate": round(rate, 2),
        "history": state["history"][-5:],  # 最近5条
        "next_index": completed + 1,
        "progress_bar": bar
    }
    print(json.dumps(result, ensure_ascii=False))

def reset_task():
    """重置当前任务"""
    if os.path.exists(STATE_FILE):
        os.remove(STATE_FILE)
        print("🗑️ 任务已重置")
    else:
        print("❌ 没有进行中的任务")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--action", required=True, choices=["init", "complete", "status", "reset"])
    parser.add_argument("--total", type=int)
    parser.add_argument("--name", type=str)
    parser.add_argument("--interval", type=int, default=5)
    parser.add_argument("--item", type=str)
    
    args = parser.parse_args()
    
    if args.action == "init":
        if not args.total or not args.name:
            print("❌ init 需要 --total 和 --name 参数")
        else:
            init_task(args.total, args.name, args.interval)
    elif args.action == "complete":
        if not args.item:
            print("❌ complete 需要 --item 参数")
        else:
            complete_task(args.item)
    elif args.action == "status":
        show_status()
    elif args.action == "reset":
        reset_task()