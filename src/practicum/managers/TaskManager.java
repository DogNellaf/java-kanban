package practicum.managers;

import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;

import java.util.ArrayList;

public interface TaskManager {
    boolean remove(int id);

    boolean change(Task task);

    boolean add(Task task, int id);

    Task find(int id);

    void clear();

    void clearTasks();

    void clearSubtasks();

    void clearEpics();

    ArrayList<Subtask> getSubtasks(int epicId);

    ArrayList<Subtask> getSubtasks();

    ArrayList<Epic> getEpics();

    ArrayList<Task> getTasks();

    ArrayList<Task> getAllTasks();

    String getTasksInfo();

    int size();

    ArrayList<Task> getPrioritizedTasks();
}
