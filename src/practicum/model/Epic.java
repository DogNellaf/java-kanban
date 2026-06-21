package practicum.model;

import practicum.enums.Status;
import practicum.managers.InMemoryTaskManager;
import practicum.managers.TaskManager;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    /**
     * Manager used to resolve subtasks when computing the epic's duration / start / end time.
     * Defaults to a standalone in-memory manager so that loading the {@code Epic} class never
     * triggers a network call; the application and tests assign the real manager explicitly.
     */
    public static TaskManager DEFAULT_MANAGER = new InMemoryTaskManager();

    private final ArrayList<Integer> tasks;

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
        this.tasks = new ArrayList<>();
    }

    public Epic(String name, String describe) {
        super(name, describe);
        this.tasks = new ArrayList<>();
    }

    public ArrayList<Integer> getTaskIds() {
        return tasks;
    }

    public boolean contains(int id) {
        return tasks.contains(id);
    }

    public void removeSubtaskId(int id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i) == id) {
                var task = DEFAULT_MANAGER.find(tasks.get(i));
                tasks.remove(i);
                if (task != null) {
                    super.addDuration(-1 * task.getDuration());
                }
                return;
            }
        }
    }

    public void addSubtaskId(int id) {
        if (!tasks.contains(id)) {
            tasks.add(id);
        }
    }

    public void cleanSubtaskIds() {
        tasks.clear();
    }

    @Override
    public int getDuration() {
        var subtasks = DEFAULT_MANAGER.getSubtasks(getId());
        super.setDuration(0);
        if (subtasks.size() > 0) {
            for (Subtask task : subtasks) {
                addDuration(task.getDuration());
            }
        }
        return super.getDuration();
    }

    @Override
    public void setDuration(int minutes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalDateTime getStartTime() {
        var subtasks = getSortedSubtasks();
        if (subtasks.size() != 0) {
            return subtasks.get(0).getStartTime();
        }
        return LocalDateTime.now();
    }

    @Override
    public void setStartTime(LocalDateTime time) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalDateTime getEndTime() {
        var subtasks = getSortedSubtasks();
        if (subtasks.size() != 0) {
            return subtasks.get(subtasks.size() - 1).getEndTime();
        } else {
            return super.getStartTime();
        }
    }

    @Override
    public String toString() {
        return "Комплексная задача " +
                " " +
                super.getStatus() +
                " " +
                super.getName() +
                " " +
                super.getDescription() +
                " (id = " + super.getId() + ")";
    }

    private ArrayList<Subtask> getSortedSubtasks() {
        var subtasks = DEFAULT_MANAGER.getSubtasks(getId());
        subtasks.sort((Task a, Task b) -> {
            var startTime1 = a.getStartTime();
            var startTime2 = b.getStartTime();
            if (startTime1.isAfter(startTime2)) {
                return 1;
            } else if (startTime2.equals(startTime1)) {
                return 0;
            } else {
                return -1;
            }
        });
        return subtasks;
    }
}
