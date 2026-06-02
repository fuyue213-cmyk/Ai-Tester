import argparse
import json
from datetime import datetime
from pathlib import Path


STATE_FILE = Path(__file__).resolve().parent.parent / "todo_state.json"


def utc_now():
    return datetime.utcnow().replace(microsecond=0).isoformat() + "Z"


def load_state():
    if not STATE_FILE.exists():
        return None
    state = json.loads(STATE_FILE.read_text(encoding="utf-8"))
    if not state.get("task_name") or not isinstance(state.get("items"), list) or not state["items"]:
        return None
    return state


def save_state(state):
    STATE_FILE.write_text(
        json.dumps(state, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def normalize_items(raw_items, start_index=1):
    if not isinstance(raw_items, list) or not raw_items:
        raise ValueError("items must be a non-empty list")

    items = []
    for offset, item in enumerate(raw_items, start=start_index):
        if not isinstance(item, str) or not item.strip():
            raise ValueError("every todo item must be a non-empty string")
        items.append(
            {
                "id": offset,
                "title": item.strip(),
                "done": False,
                "created_at": utc_now(),
                "completed_at": None,
            }
        )
    return items


def resolve_raw_items(items_json, item_args):
    if item_args:
        return item_args
    if items_json:
        raw_items = json.loads(items_json)
        if not isinstance(raw_items, list):
            raise ValueError("items-json must decode to a JSON array")
        return raw_items
    raise ValueError("provide --item at least once or pass --items-json")


def markdown_table(state):
    lines = [
        f"### Todo: {state['task_name']}",
        "",
        "| ID | Status | Subtask | Updated |",
        "| --- | --- | --- | --- |",
    ]
    for item in state["items"]:
        status = "[x]" if item["done"] else "[ ]"
        updated = item["completed_at"] or item["created_at"]
        lines.append(
            f"| {item['id']} | {status} | {item['title']} | {updated} |"
        )
    return "\n".join(lines)


def build_output(state):
    completed = sum(1 for item in state["items"] if item["done"])
    total = len(state["items"])
    pending = total - completed
    return {
        "task_name": state["task_name"],
        "completed": completed,
        "pending": pending,
        "total": total,
        "all_done": pending == 0,
        "markdown_table": markdown_table(state),
    }


def cmd_init(args):
    state = {
        "task_name": args.task_name.strip(),
        "created_at": utc_now(),
        "items": normalize_items(resolve_raw_items(args.items_json, args.item)),
    }
    save_state(state)
    print(json.dumps(build_output(state), ensure_ascii=False))


def cmd_complete(args):
    state = load_state()
    if state is None:
        raise ValueError("todo state does not exist")

    target = None
    for item in state["items"]:
        if item["id"] == args.id:
            target = item
            break

    if target is None:
        raise ValueError(f"todo item {args.id} does not exist")

    target["done"] = True
    target["completed_at"] = utc_now()
    save_state(state)
    print(json.dumps(build_output(state), ensure_ascii=False))


def cmd_append(args):
    state = load_state()
    if state is None:
        raise ValueError("todo state does not exist")

    next_index = max((item["id"] for item in state["items"]), default=0) + 1
    raw_items = resolve_raw_items(args.items_json, args.item)
    state["items"].extend(normalize_items(raw_items, start_index=next_index))
    save_state(state)
    print(json.dumps(build_output(state), ensure_ascii=False))


def cmd_show(_args):
    state = load_state()
    if state is None:
        raise ValueError("todo state does not exist")
    print(json.dumps(build_output(state), ensure_ascii=False))


def cmd_reset(_args):
    if STATE_FILE.exists():
        STATE_FILE.unlink()
    print(json.dumps({"reset": True, "state_file": str(STATE_FILE)}, ensure_ascii=False))


def main():
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers(dest="action", required=True)

    init_parser = subparsers.add_parser("init")
    init_parser.add_argument("--task-name", required=True)
    init_parser.add_argument("--items-json")
    init_parser.add_argument("--item", action="append")
    init_parser.set_defaults(handler=cmd_init)

    complete_parser = subparsers.add_parser("complete")
    complete_parser.add_argument("--id", required=True, type=int)
    complete_parser.set_defaults(handler=cmd_complete)

    append_parser = subparsers.add_parser("append")
    append_parser.add_argument("--items-json")
    append_parser.add_argument("--item", action="append")
    append_parser.set_defaults(handler=cmd_append)

    show_parser = subparsers.add_parser("show")
    show_parser.set_defaults(handler=cmd_show)

    reset_parser = subparsers.add_parser("reset")
    reset_parser.set_defaults(handler=cmd_reset)

    args = parser.parse_args()
    args.handler(args)


if __name__ == "__main__":
    main()
