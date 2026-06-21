package practicum.tests.managers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practicum.api.KVServer;
import practicum.api.KVTaskClient;
import practicum.managers.FileBackedTasksManager;
import practicum.managers.HttpTaskManager;
import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskManagerTest extends TaskManagerTest<FileBackedTasksManager> {
    private static KVServer server;

    @BeforeAll
    public static void startServer() throws IOException {
        server = new KVServer();
        server.start();
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void createManager() throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI("http://localhost:" + KVServer.PORT + "/");

        manager = new HttpTaskManager(uri);
        manager.clear();
        task = new Task("Task1", "test");
        task.setStartTime(LocalDateTime.of(2020, 1, 1, 1, 1, 1));
        manager.add(task, 0);

        Epic.DEFAULT_MANAGER = manager;

        KVTaskClient client = new KVTaskClient(uri);
        client.put("ids", "|");
    }

    @Test
    public void shouldSaveAndLoadByServer() {
        manager.clear();
        manager.historyManager.clear();

        manager.add(new Epic("Эпик 1", "Тестовое описание"), 1);
        manager.add(new Subtask("Подзадача 1", "Тестовое описание", 1), 2);
        manager.add(new Subtask("Подзадача 2", "Тестовое описание", 1), 3);
        manager.add(new Task("Задача 1", "Тестовое описание"), 4);
        manager.add(new Task("Задача 2", "Тестовое описание"), 5);
        manager.add(new Epic("Эпик 2", "Тестовое описание"), 6);
        manager.add(new Subtask("Подзадача 3", "Тестовое описание", 2), 7);
        manager.add(new Subtask("Подзадача 4", "Тестовое описание", 2), 8);
        manager.add(new Subtask("Подзадача 5", "Тестовое описание", 1), 9);

        var manager1 = loadManager();
        assertTrue(compareLoading(manager1));
    }

    @Test
    public void shouldSaveAndLoadWithoutTasks() {
        manager.clear();
        manager.historyManager.clear();
        var manager1 = loadManager();
        assertTrue(compareLoading(manager1));
    }

    @Test
    public void shouldSaveAndLoadWithEpicWithoutSubtasks() {
        manager.historyManager.clear();
        manager.clear();

        manager.add(new Epic("Epic Task 1", "Something2"), 0);
        manager.find(1);

        var manager1 = loadManager();

        assertTrue(compareLoading(manager1));
    }

    @Test
    public void shouldSaveAndLoadWithoutHistory() {
        manager.clear();
        manager.historyManager.clear();
        var manager1 = loadManager();

        assertTrue(compareLoading(manager1));
    }

    private HttpTaskManager loadManager() {
        return (HttpTaskManager) HttpTaskManager.load("http://localhost:" + KVServer.PORT + "/");
    }

    private boolean compareLoading(HttpTaskManager manager1) {
        StringBuilder saved = new StringBuilder(manager.getTasksInfo() + "\n");
        for (var task : manager.historyManager.getHistory()) {
            saved.append(task.getId());
            saved.append(" ");
        }

        StringBuilder loaded = new StringBuilder(manager1.getTasksInfo() + "\n");
        for (var task : manager1.historyManager.getHistory()) {
            loaded.append(task.getId());
            loaded.append(" ");
        }
        return saved.toString().equals(loaded.toString());
    }

}
