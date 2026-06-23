# java-kanban

> 🇬🇧 English | [🇷🇺 Русский](README.ru.md)

A Kanban-style task tracker. Manages three types of entities, keeps a view history,
sorts tasks by start time, persists data to a CSV file or an external KV server over HTTP,
and exposes a REST-like HTTP API.

## Features

- Create, update, delete, and look up tasks of three types: Task, Epic, and Subtask.
- Automatic Epic status recalculation based on its Subtasks' statuses.
- Epic duration and start/end time derived from its Subtasks.
- Overlap validation when adding or updating tasks.
- Browsing history (no duplicates, O(1) per operation).
- Tasks sorted by start time (prioritised view).
- Three storage backends:
  - `InMemoryTaskManager` — in-memory only;
  - `FileBackedTasksManager` — auto-saves to a CSV file;
  - `HttpTaskManager` — persists to an external KV server over HTTP.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| HTTP server | `com.sun.net.httpserver` (built-in) |
| JSON | Gson 2.9.0 |
| Tests | JUnit 5.8.1 |
| Build | Manual `javac` / IntelliJ IDEA |

## Requirements

- **JDK 17+** (text blocks, switch expressions, `var`, try-with-resources are used).
- `lib/gson-2.9.0.jar` — bundled in the repository.
- `lib/junit-*.jar` — bundled in the repository (tests only).

> ⚠️ A JRE 8 installation is **not enough** — a full JDK 17+ (with `javac`) is required.
> The easiest path is to open the project in IntelliJ IDEA: the module config (`java-kanban.iml`)
> and libraries are already set up.

## Installation

### IntelliJ IDEA (recommended)

1. Open the project directory as an IntelliJ IDEA project.
2. Set the SDK to JDK 17+ (**File → Project Structure → Project SDK**).
3. Run the `Main` class.

### Command line

```bash
# From the project root
javac -encoding UTF-8 -cp "lib/gson-2.9.0.jar" -d out/production/java-kanban \
      $(find src -name "*.java" -not -path "*/tests/*")

# Windows
java -cp "out/production/java-kanban;lib/gson-2.9.0.jar" Main

# Linux / macOS
java -cp "out/production/java-kanban:lib/gson-2.9.0.jar" Main
```

On startup, `KVServer` (port 8078) and `HttpTaskServer` (port 8080) are started,
a set of sample tasks is created, and the interactive console menu is shown.

## Running Tests

Open the project in IntelliJ IDEA, right-click the `src/practicum/tests/` directory,
and choose **Run tests**.

> Tests that involve `HttpTaskManagerTest`, `HttpTaskServerTest`, and `KVServerTest`
> start real network servers — ports **8078** and **8080** must be free before running them.

## Project Structure

```
src/
├── Main.java                        — console entry point
└── practicum/
    ├── adapters/                    — Gson TypeAdapters (JSON serialisation)
    │   └── Adapter, TaskAdapter, SubtaskAdapter, EpicAdapter
    ├── api/
    │   ├── HttpTaskServer           — REST-like task server (port 8080)
    │   ├── KVServer                 — key-value storage server (port 8078)
    │   ├── KVTaskClient             — HTTP client for KVServer
    │   └── handlers/                — HTTP endpoint handlers
    ├── collections/
    │   └── CustomLinkedList         — doubly-linked list for history
    ├── enums/Status
    ├── managers/
    │   ├── TaskManager (interface)  / InMemoryTaskManager
    │   ├── FileBackedTasksManager   — extends InMemoryTaskManager
    │   ├── HttpTaskManager          — extends FileBackedTasksManager
    │   ├── HistoryManager (interface) / InMemoryHistoryManager
    │   └── Managers                 — manager factory
    ├── model/                       — Task, Epic, Subtask, Node
    └── tests/                       — JUnit 5 test suite
```

Manager hierarchy: `HttpTaskManager` → `FileBackedTasksManager` → `InMemoryTaskManager`.
Each level adds persistence on top of the previous one.

## HTTP API

`HttpTaskServer` listens on `http://localhost:8080`. Request and response bodies are JSON.

| Method | Path | Action |
|--------|------|--------|
| GET | `/tasks/task` | List all tasks |
| GET | `/tasks/task?id={id}` | Get task by id |
| POST | `/tasks/task?id={id}` | Create (id=0/new) or update (existing id) a task |
| DELETE | `/tasks/task?id={id}` | Delete task; omit `id` to clear all tasks |
| GET/POST/DELETE | `/tasks/subtask[?id=]` | Same for subtasks |
| GET/POST/DELETE | `/tasks/epic[?id=]` | Same for epics |
| GET | `/tasks/subtask/epic?id={epicId}` | Subtasks of a given epic |
| GET | `/tasks/history` | View history |
| GET | `/tasks/` | All tasks sorted by start time |

Response codes: `200` — OK, `400` — bad request / wrong method,
`404` — not found, `500` — internal error.

## KV Storage Server

`KVServer` (`http://localhost:8078`) is a simple key-value store with token-based auth:

| Method | Path | Action |
|--------|------|--------|
| GET | `/register` | Issue an `API_TOKEN` |
| POST | `/save/{key}?API_TOKEN=...` | Store a value |
| GET | `/load/{key}?API_TOKEN=...` | Read a value (`null` if key absent) |
| DELETE | `/clear/?API_TOKEN=...` | Clear the store |

`HttpTaskManager` serialises each task to JSON and stores it under its `id` as key;
the `ids` key holds the list of `id_fullyQualifiedClassName` entries and the view history
(delimiter `|`).

## CSV Format

`FileBackedTasksManager` saves data in CSV with the following column order:

```
id,type,name,status,description,duration,start_time,epic
```

- `epic` — the epic's `id` for a subtask, `null` for all other types;
- after the task block there is a blank line followed by a comma-separated list of history ids;
- timestamp format: `dd.MM.yyyy HH:mm:ss`.

Example:

```
id,type,name,status,description,duration,start_time,epic
1,Epic,Epic 1,IN_PROGRESS,description,0,29.03.2023 00:00:00,null
2,Subtask,Subtask 1,IN_PROGRESS,description,30,29.03.2023 10:00:00,1

2,1,
```

## License

[MIT](LICENSE)
