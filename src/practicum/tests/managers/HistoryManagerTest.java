package practicum.tests.managers;

import org.junit.jupiter.api.Test;
import practicum.managers.HistoryManager;
import practicum.model.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class HistoryManagerTest<T extends HistoryManager> {
    public T historyManager;

    public Task task;

    @Test
    public void shouldReturnTasksList() {
        var task2 = new Task("Task2", "test");
        task2.setId(2);
        historyManager.add(task2);

        var task3 = new Task("Task3", "test");
        task3.setId(3);
        historyManager.add(task3);

        assertEquals(historyManager.getHistory().getClass().getSimpleName(), "ArrayList");
        assertEquals(historyManager.getHistory().size(), 3);
    }

    @Test
    public void shouldAddTaskWhenHistoryIsEmpty() {
        var history = historyManager.getHistory();
        assertEquals(history.size(), 1);
        assertEquals(history.get(0).getId(), task.getId());
    }

    @Test
    public void shouldAddTaskWhenHasDuplicate() {
        var task2 = new Task("Task2", "test");
        task2.setId(task.getId());
        historyManager.add(task2);

        var history = historyManager.getHistory();
        assertEquals(history.size(), 1);
        assertEquals(history.get(0).getName(), task2.getName());
    }

    @Test
    public void shouldRemoveHistoryFromStart() {
        var task2 = new Task("Task2", "test");
        task2.setId(2);
        var task3 = new Task("Task3", "test");
        task3.setId(3);

        historyManager.add(task2);
        historyManager.add(task3);

        assertEquals(historyManager.getHistory().size(), 3);

        historyManager.remove(task2.getId());

        var history = historyManager.getHistory();

        assertEquals(history.size(), 2);

        assertEquals(history.get(0).getId(), task.getId());
        assertEquals(history.get(1).getId(), task3.getId());
    }

    @Test
    public void shouldRemoveHistoryFromMiddle() {
        var task2 = new Task("Task2", "test");
        task2.setId(2);
        var task3 = new Task("Task3", "test");
        task3.setId(3);

        historyManager.add(task2);
        historyManager.add(task3);

        assertEquals(historyManager.getHistory().size(), 3);

        historyManager.remove(task3.getId());

        var history = historyManager.getHistory();

        assertEquals(history.size(), 2);

        assertEquals(history.get(0).getId(), task.getId());
        assertEquals(history.get(1).getId(), task2.getId());
    }

    @Test
    public void shouldRemoveHistoryFromEnd() {
        var task2 = new Task("Task2", "test");
        task2.setId(2);
        var task3 = new Task("Task3", "test");
        task3.setId(3);

        historyManager.add(task2);
        historyManager.add(task3);

        assertEquals(historyManager.getHistory().size(), 3);

        historyManager.remove(task.getId());

        var history = historyManager.getHistory();

        assertEquals(history.size(), 2);

        assertEquals(history.get(0).getId(), task2.getId());
        assertEquals(history.get(1).getId(), task3.getId());
    }

    @Test
    public void shouldClear() {
        var task = new Task("Task1", "test");
        historyManager.add(task);

        assertEquals(historyManager.getHistory().size(), 1);
        historyManager.clear();
        assertEquals(historyManager.getHistory().size(), 0);
    }
}
