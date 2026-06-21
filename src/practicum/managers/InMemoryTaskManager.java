package practicum.managers;

import practicum.enums.Status;
import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private final long SORTING_SAVING_MILLIS = 0;
    public HistoryManager historyManager = Managers.getDefaultHistory();
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;
    private ArrayList<Task> sortedTasks = new ArrayList<>();
    private long lastSortingTime;
    private int id = 1;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        lastSortingTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    // основные методы
    @Override
    public boolean add(Task task, int id) {
        var isContains = contains(id);
        if (id == 0 || isContains) {
            task.setId(this.id);
            id = this.id;
            this.id++;
        } else {
            task.setId(id);
        }


        if (!task.getClass().getSimpleName().equals("Epic"))
            if (!validateTask(task))
                return false;

        switch (getClassName(task)) {
            case "Subtask" -> {
                int epicId = ((Subtask) task).getEpicId();
                if (!epics.containsKey(epicId))
                    return false;
                Task epic = find(epicId);
                if (!getClassName(epic).equals("Epic"))
                    return false;
                subtasks.put(id, (Subtask) task);
                ((Epic) epic).addSubtaskId(id);
                updateEpicStatus((Epic) epic);
            }
            case "Epic" -> epics.put(id, (Epic) task);
            default -> tasks.put(id, task);
        }

        var tasks = getAllTasks();
        if (tasks.size() != 0) {
            var lastTask = tasks.get(tasks.size() - 1);
            this.id = lastTask.getId() + 1;
        } else {
            this.id++;
        }

        return true;
    }

    @Override
    public Task find(int id) {
        Task task;
        if (tasks.containsKey(id)) {
            task = tasks.get(id);
        } else if (epics.containsKey(id)) {
            task = epics.get(id);
        } else if (subtasks.containsKey(id)) {
            task = subtasks.get(id);
        } else {
            return null;
        }

        historyManager.add(task);
        return task;
    }

    public int size() {
        return getAllTasks().size();
    }

    @Override
    public boolean change(Task task) {
        int taskId = task.getId();

        if (!validateTask(task))
            return false;

        if (contains(taskId)) {
            Task temp = find(taskId);
            temp.setName(task.getName());
            temp.setDescription(task.getDescription());
            temp.setStatus(task.getStatus());

            if (!isEpic(temp) && !isEpic(task)) {
                temp.setStartTime(task.getStartTime());
                temp.setDuration(task.getDuration());
            }

            if (isSubtask(temp) && isSubtask(task)) {
                int epicId = ((Subtask) task).getEpicId();

                ((Subtask) temp).setEpicId(((Subtask) task).getEpicId());

                if (!epics.containsKey(epicId))
                    return false;

                var epic = (Epic) find(epicId);
                updateEpicStatus(epic);
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean remove(int id) {
        if (!contains(id))
            return false;

        var task = find(id);
        if (task != null) {
            historyManager.remove(id);
        }
        if (isEpic(task)) {
            for (Subtask subtask : getSubtasks(id)) {
                remove(subtask.getId());
            }
            epics.remove(id);
        } else if (isSubtask(task)) {
            int epicId = ((Subtask) task).getEpicId();
            var epic = (Epic) find(epicId);
            epic.removeSubtaskId(task.getId());
            updateEpicStatus(epic);
            subtasks.remove(id);
        } else if (isTask(task)) {
            tasks.remove(id);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void clear() {
        clearTasks();
        clearEpics();
        id = 1;
    }

    @Override
    public void clearTasks() {
        var tasks = getTasks();
        for (Task task : tasks) {
            remove(task.getId());
        }
    }

    @Override
    public void clearEpics() {
        for (Epic epic : getEpics()) {
            remove(epic.getId());
        }
    }

    @Override
    public void clearSubtasks() {
        var subtasks = getSubtasks();
        for (Subtask subtask : subtasks) {
            remove(subtask.getId());
        }

        var epics = getEpics();
        for (Epic epic : epics) {
            updateEpicStatus(epic);
        }
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Task> getAllTasks() {
        var result = new ArrayList<Task>();
        result.addAll(tasks.values());
        result.addAll(epics.values());
        result.addAll(subtasks.values());
        result.sort(Comparator.comparingInt(Task::getId));
        return result;
    }

    public ArrayList<Task> getPrioritizedTasks() {
        var tasks = getAllTasks();
        var now = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        if (now >= lastSortingTime + SORTING_SAVING_MILLIS) {
            tasks.sort((Task first, Task second) ->
                    compareDates(first.getStartTime(), second.getStartTime()));
            sortedTasks = tasks;
            lastSortingTime = now;
        }
        return new ArrayList<>(sortedTasks);
    }

    public ArrayList<Task> getPrioritizedTasksWithoutEpics() {
        var tasks = getPrioritizedTasks();
        var result = new ArrayList<Task>();
        for (Task task : tasks) {
            if (!getClassName(task).equals("Epic")) {
                result.add(task);
            }
        }
        return result;
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks(int epicId) {
        var task = find(epicId);
        if (task == null) {
            return new ArrayList<>();
        } else if (!isEpic(task)) {
            return new ArrayList<>();
        } else {
            var result = new ArrayList<Subtask>();
            var ids = ((Epic) task).getTaskIds();
            for (int id : ids) {
                var foundTask = find(id);
                if (foundTask != null) {
                    if (foundTask.getClass().getSimpleName().equals("Subtask")) {
                        result.add((Subtask) foundTask);
                    }
                }
            }
            return result;
        }
    }

    @Override
    public String getTasksInfo() {
        String result = "";
        if (tasks.size() != 0) {
            result += "Обычные задачи:\n";
            for (Task task : tasks.values()) {
                result += task + "\n";
            }
        }

        if (epics.size() != 0) {
            result += "Эпики:\n";
            for (Epic epic : epics.values()) {
                result += epic + "\n";
            }
        }

        if (subtasks.size() != 0) {
            result += "Подзадачи:\n";
            for (Subtask subtask : subtasks.values()) {
                result += subtask + "\n";
            }
        }

        return result;
    }

    // вспомогательные методы
    private int compareDates(LocalDateTime firstStartTime, LocalDateTime secondStartTime) {
        if (firstStartTime.isAfter(secondStartTime)) {
            return 1;
        } else if (secondStartTime.equals(firstStartTime)) {
            return 0;
        } else {
            return -1;
        }
    }

    private boolean validateTask(Task task) {
        var tasks = getPrioritizedTasksWithoutEpics();
        var startTime = task.getStartTime();
        var endTime = task.getEndTime();

        for (Task currentTask : tasks) {
            var currentStartTime = currentTask.getStartTime();
            var currentEndTime = currentTask.getEndTime();

            var startTimeIsAfterStart = startTime.isAfter(currentStartTime);
            var startTimeIsBeforeEnd = startTime.isBefore(currentEndTime);

            if (startTimeIsAfterStart && startTimeIsBeforeEnd) {
                System.out.println("Валидация задачи " +
                        task.getName() +
                        " не прошла, т.к. она пересекается с задачей " +
                        currentTask.getName() +
                        " по времени начала");
                return false;
            }

            var currentStartTimeIsAfterStart = currentStartTime.isAfter(startTime);
            var currentStartTimeIsBeforeEnd = endTime.isBefore(currentEndTime);

            if (currentStartTimeIsAfterStart && currentStartTimeIsBeforeEnd) {
                System.out.println("Валидация задачи " +
                        task.getName() +
                        " не прошла, т.к. она пересекается с задачей " +
                        currentTask.getName() +
                        " по времени конца");
                return false;
            }
        }
        return true;
    }

    private boolean isTask(Task task) {
        return getClassName(task).equals("Task");
    }

    private boolean isEpic(Task task) {
        return getClassName(task).equals("Epic");
    }

    private boolean isSubtask(Task task) {
        return getClassName(task).equals("Subtask");
    }

    private boolean contains(int id) {
        return tasks.containsKey(id) || epics.containsKey(id) || subtasks.containsKey(id);
    }

    public String getClassName(Task task) {
        return task.getClass().getSimpleName();
    }

    public void updateEpicStatus(Epic epic) {
        var ids = epic.getTaskIds();

        if (ids.size() == 0) {
            epic.setStatus(Status.NEW);
            return;
        }

        var allIsDone = true;
        var allIsNew = true;

        for (int subtaskId : ids) {
            var subtask = find(subtaskId);

            if (subtask == null) {
                epic.removeSubtaskId(subtaskId);
                continue;
            }

            var status = subtask.getStatus();

            if (status != Status.DONE) {
                allIsDone = false;
            }

            if (status != Status.NEW) {
                allIsNew = false;
            }

            if (!allIsDone && !allIsNew) {
                break;
            }
        }

        if (allIsDone) {
            epic.setStatus(Status.DONE);
        } else if (allIsNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}
