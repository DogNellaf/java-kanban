package practicum.tests.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practicum.managers.FileBackedTasksManager;
import practicum.managers.InMemoryHistoryManager;
import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    private final String path = "test.csv";

    @BeforeEach
    public void createManager() {
        manager = new FileBackedTasksManager(path);
        manager.clear();
        task = new Task("Task1", "test");
        task.setStartTime(LocalDateTime.of(2020, 1, 1, 1, 1, 1));
        manager.add(task, 0);

        Epic.DEFAULT_MANAGER = manager;
    }

    @Test
    public void shouldReturnClassName() {
        assertEquals(getManager().getClassName(task), "Task");
    }

    @Test
    public void shouldSaveAndLoad() {
        var manager1 = getManager();

        manager1.historyManager = new InMemoryHistoryManager();
        manager1.add(new Task("Task 1", "Something1"), 0);
        manager1.add(new Epic("Epic Task 1", "Something2"), 0);
        manager1.add(new Subtask("Subtask 1", "Something3", 2), 0);
        manager1.find(2);
        manager1.find(1);
        manager1.add(new Task("Task 2", "Something4"), 0);

        assertTrue(compareLoading(manager1));
    }

    @Test
    public void shouldSaveAndLoadWithoutTasks() {
        var manager1 = getManager();

        manager1.historyManager = new InMemoryHistoryManager();

        assertTrue(compareLoading(manager1));
    }

    @Test
    public void shouldSaveAndLoadWithEpicWithoutSubtasks() {
        var manager1 = getManager();

        manager1.historyManager = new InMemoryHistoryManager();
        manager1.add(new Epic("Epic Task 1", "Something2"), 0);
        manager.find(1);

        assertTrue(compareLoading(manager1));
    }

    @Test
    public void shouldSaveAndLoadWithoutHistory() {
        var manager1 = getManager();

        manager1.historyManager = new InMemoryHistoryManager();
        manager1.add(new Task("Task 1", "Something"), 0);

        assertTrue(compareLoading(manager1));
    }

    private FileBackedTasksManager getManager() {
        return manager;
    }

    private boolean compareLoading(FileBackedTasksManager manager1) {
        StringBuilder saved = new StringBuilder(manager1.getTasksInfo() + "\n");
        for (var task : manager1.historyManager.getHistory()) {
            saved.append(task);
            saved.append("\n");
        }

        FileBackedTasksManager manager2 = (FileBackedTasksManager) FileBackedTasksManager.load(path);
        StringBuilder loaded = new StringBuilder(manager2.getTasksInfo() + "\n");
        for (var task : manager2.historyManager.getHistory()) {
            loaded.append(task);
            loaded.append("\n");
        }
        return saved.toString().equals(loaded.toString());
    }

}
