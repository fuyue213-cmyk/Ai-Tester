---
name: todo
description: Generate and maintain a visible todo log table for complex multi-step work. Use when Codex receives a task that needs decomposition into multiple sub-tasks, phased execution, checkpoints, or progress reporting. After splitting the work, create a persistent todo table, mark items complete as each sub-task finishes, and show the updated table in user-facing replies so the checklist also becomes part of the conversation context.
---

# Todo

Track execution of complex work with a persistent checklist. Keep the checklist on disk, update it after each completed sub-task, and show the latest markdown table in replies so the user always sees current status and the table stays in the conversation history.

## Workflow

1. Decide whether the task is complex enough to need decomposition.
Complex means there are multiple concrete deliverables, dependencies, phases, or validation steps.

2. Break the task into short actionable sub-tasks.
Write items as imperative actions such as `Inspect API client`, `Patch retry logic`, `Run targeted tests`.

3. Initialize the todo log immediately after planning.
Run:

```powershell
python .codex/skills/todo/scripts/todo_tracker.py init --task-name "<main task>" --item "<subtask 1>" --item "<subtask 2>"
```

Example:

```powershell
python .codex/skills/todo/scripts/todo_tracker.py init --task-name "Fix flaky login flow" --item "Inspect auth flow" --item "Patch timeout handling" --item "Run login tests" --item "Summarize results"
```

4. Show the generated markdown table in the next user-facing reply.
Do not summarize the table away. Paste it so it becomes part of the visible conversation context.

5. Mark each sub-task complete as soon as it is actually finished.
Run:

```powershell
python .codex/skills/todo/scripts/todo_tracker.py complete --id <subtask-id>
```

6. After every completion, show the refreshed markdown table again.
This is mandatory. The updated table is the authoritative task ledger for the current complex task.

7. If the task changes materially, append or reset the list before continuing.
Use `append` for new sub-tasks discovered during execution. Use `reset` only when replacing the entire current checklist.

## Required Behavior

- Initialize a todo log whenever a task needs meaningful decomposition.
- Keep item titles short and outcome-oriented.
- Use the script output instead of hand-editing the table.
- Do not mark a step complete before the work and any needed validation are done.
- Include the updated markdown table in replies after initialization, completion, append, or explicit status requests.
- Treat the displayed table as the mechanism that places the current todo state into conversation context.

## Commands

Use the helper script at `scripts/todo_tracker.py`.

- Initialize:

```powershell
python .codex/skills/todo/scripts/todo_tracker.py init --task-name "<main task>" --item "<subtask 1>" --item "<subtask 2>"
```

- Complete one item:

```powershell
python .codex/skills/todo/scripts/todo_tracker.py complete --id <subtask-id>
```

- Append extra items:

```powershell
python .codex/skills/todo/scripts/todo_tracker.py append --item "<subtask n+1>" --item "<subtask n+2>"
```

- Show current table:

```powershell
python .codex/skills/todo/scripts/todo_tracker.py show
```

- Reset current state:

```powershell
python .codex/skills/todo/scripts/todo_tracker.py reset
```

## Response Pattern

When this skill is active for a complex task:

1. Briefly state the current action or result.
2. Paste the latest markdown todo table from the script output.
3. Continue with execution or the next concrete update.

If the user only asks for a simple one-step task, do not create a todo log.
