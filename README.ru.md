# java-kanban

> [🇬🇧 English](README.md) | 🇷🇺 Русский

Трекер задач в стиле доски Kanban. Управляет тремя типами сущностей, хранит историю
просмотров, сортирует задачи по времени начала, сохраняет данные в CSV-файл или на
внешний KV-сервер по HTTP и предоставляет REST-подобный HTTP API.

## Возможности

- Создание, изменение, удаление и поиск задач трёх типов: Task, Epic, Subtask.
- Автоматический пересчёт статуса эпика по статусам его подзадач.
- Длительность и время начала/окончания эпика вычисляются по подзадачам.
- Проверка пересечения задач по времени при добавлении и изменении.
- История последних просмотренных задач (без дубликатов, O(1) на операцию).
- Список задач, отсортированный по времени начала (приоритизация).
- Три варианта хранилища:
  - `InMemoryTaskManager` — только в оперативной памяти;
  - `FileBackedTasksManager` — с автосохранением в CSV-файл;
  - `HttpTaskManager` — с сохранением на внешний KV-сервер по HTTP.

## Технологии

| Слой | Технология |
|---|---|
| Язык | Java 17 |
| HTTP-сервер | `com.sun.net.httpserver` (встроенный) |
| JSON | Gson 2.9.0 |
| Тесты | JUnit 5.8.1 |
| Сборка | `javac` вручную / IntelliJ IDEA |

## Требования

- **JDK 17+** (используются текстовые блоки, `switch`-выражения, `var`, try-with-resources).
- `lib/gson-2.9.0.jar` — входит в репозиторий.
- `lib/junit-*.jar` — входит в репозиторий (только для тестов).

> ⚠️ Установленной JRE 8 **недостаточно** — нужен полноценный JDK 17+ (с `javac`).
> Проще всего открыть проект в IntelliJ IDEA: конфигурация модуля (`java-kanban.iml`)
> и библиотеки уже подключены.

## Установка

### IntelliJ IDEA (рекомендуется)

1. Открыть каталог проекта как проект IntelliJ IDEA.
2. Указать SDK = JDK 17+ (**File → Project Structure → Project SDK**).
3. Запустить класс `Main`.

### Командная строка

```bash
# из корня проекта
javac -encoding UTF-8 -cp "lib/gson-2.9.0.jar" -d out/production/java-kanban \
      $(find src -name "*.java" -not -path "*/tests/*")

# Windows
java -cp "out/production/java-kanban;lib/gson-2.9.0.jar" Main

# Linux / macOS
java -cp "out/production/java-kanban:lib/gson-2.9.0.jar" Main
```

При старте поднимаются `KVServer` (порт 8078) и `HttpTaskServer` (порт 8080),
создаётся набор тестовых задач, после чего выводится консольное меню.

## Запуск тестов

Откройте проект в IntelliJ IDEA, нажмите ПКМ по каталогу `src/practicum/tests/`
и выберите **Run tests**.

> Тесты `HttpTaskManagerTest`, `HttpTaskServerTest` и `KVServerTest` поднимают реальные
> сетевые серверы — перед запуском порты **8078** и **8080** должны быть свободны.

## Структура проекта

```
src/
├── Main.java                        — консольная точка входа
└── practicum/
    ├── adapters/                    — Gson TypeAdapter'ы (сериализация в JSON)
    │   └── Adapter, TaskAdapter, SubtaskAdapter, EpicAdapter
    ├── api/
    │   ├── HttpTaskServer           — REST-подобный сервер задач (порт 8080)
    │   ├── KVServer                 — key-value сервер хранения (порт 8078)
    │   ├── KVTaskClient             — HTTP-клиент к KVServer
    │   └── handlers/                — обработчики HTTP-эндпоинтов
    ├── collections/
    │   └── CustomLinkedList         — двусвязный список для истории
    ├── enums/Status
    ├── managers/
    │   ├── TaskManager (интерфейс)  / InMemoryTaskManager
    │   ├── FileBackedTasksManager   — расширяет InMemoryTaskManager
    │   ├── HttpTaskManager          — расширяет FileBackedTasksManager
    │   ├── HistoryManager (интерфейс) / InMemoryHistoryManager
    │   └── Managers                 — фабрика менеджеров
    ├── model/                       — Task, Epic, Subtask, Node
    └── tests/                       — JUnit 5 тесты
```

Иерархия менеджеров: `HttpTaskManager` → `FileBackedTasksManager` → `InMemoryTaskManager`.
Каждый уровень добавляет персистентность поверх предыдущего.

## HTTP API

`HttpTaskServer` слушает `http://localhost:8080`. Тело запросов и ответов — JSON.

| Метод | Путь | Действие |
|-------|------|----------|
| GET | `/tasks/task` | Список обычных задач |
| GET | `/tasks/task?id={id}` | Задача по id |
| POST | `/tasks/task?id={id}` | Создать (id=0/новый) или изменить (существующий id) задачу |
| DELETE | `/tasks/task?id={id}` | Удалить задачу; без `id` — очистить все обычные задачи |
| GET/POST/DELETE | `/tasks/subtask[?id=]` | То же для подзадач |
| GET/POST/DELETE | `/tasks/epic[?id=]` | То же для эпиков |
| GET | `/tasks/subtask/epic?id={epicId}` | Подзадачи указанного эпика |
| GET | `/tasks/history` | История просмотров |
| GET | `/tasks/` | Все задачи, отсортированные по времени |

Коды ответов: `200` — успех, `400` — некорректный запрос/метод,
`404` — не найдено, `500` — внутренняя ошибка.

## KV-сервер хранения

`KVServer` (`http://localhost:8078`) — простое key-value хранилище с авторизацией по токену:

| Метод | Путь | Действие |
|-------|------|----------|
| GET | `/register` | Выдать `API_TOKEN` |
| POST | `/save/{key}?API_TOKEN=...` | Сохранить значение |
| GET | `/load/{key}?API_TOKEN=...` | Прочитать значение (`null`, если ключа нет) |
| DELETE | `/clear/?API_TOKEN=...` | Очистить хранилище |

`HttpTaskManager` сериализует каждую задачу в JSON и кладёт её по ключу-`id`, а в ключе
`ids` хранит список `id_полноеИмяКласса` и историю просмотров (разделитель `|`).

## Формат CSV

`FileBackedTasksManager` сохраняет данные в CSV. Порядок колонок:

```
id,type,name,status,description,duration,start_time,epic
```

- `epic` — `id` эпика для подзадачи, `null` для остальных типов;
- после блока задач идёт пустая строка, затем строка с `id` истории через запятую;
- формат времени: `dd.MM.yyyy HH:mm:ss`.

Пример:

```
id,type,name,status,description,duration,start_time,epic
1,Epic,Эпик 1,IN_PROGRESS,описание,0,29.03.2023 00:00:00,null
2,Subtask,Подзадача 1,IN_PROGRESS,описание,30,29.03.2023 10:00:00,1

2,1,
```

## Лицензия

[MIT](LICENSE)
