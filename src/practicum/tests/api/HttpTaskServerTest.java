package practicum.tests.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practicum.adapters.EpicAdapter;
import practicum.adapters.SubtaskAdapter;
import practicum.adapters.TaskAdapter;
import practicum.api.HttpTaskServer;
import practicum.api.KVServer;
import practicum.managers.HttpTaskManager;
import practicum.managers.Managers;
import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerTest {
    private static HttpClient client;
    private static URI serverURI;
    private static HttpTaskManager manager;
    private static Gson gson;
    private static KVServer kvServer;

    @BeforeAll
    public static void setUp() throws IOException, URISyntaxException, InterruptedException {

        Thread.sleep(1000);

        serverURI = new URI("http://localhost:" + HttpTaskServer.PORT + "/");

        kvServer = new KVServer();
        kvServer.start();

        HttpTaskServer taskServer = new HttpTaskServer();
        taskServer.start();

        manager = (HttpTaskManager) Managers.getDefault();
        manager.historyManager = Managers.getDefaultHistory();
        Epic.DEFAULT_MANAGER = manager;

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Task.class, new TaskAdapter());
        builder.registerTypeAdapter(Subtask.class, new SubtaskAdapter());
        builder.registerTypeAdapter(Epic.class, new EpicAdapter());
        builder.setPrettyPrinting();
        gson = builder.create();
    }

    @BeforeEach
    public void setUpClient() {
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    public static void stopServer() {
        kvServer.stop();
    }

    public String sendRequest(String address, String body, String method) throws IOException, InterruptedException {
        var requestBuilder = HttpRequest.newBuilder();

        var uri = serverURI.resolve(address);

        HttpRequest request;
        switch (method) {
            default:
                request = requestBuilder
                        .GET()
                        .uri(uri)
                        .build();
                break;
            case "POST":
                request = requestBuilder
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .uri(uri)
                        .build();
                break;
            case "DELETE":
                request = requestBuilder
                        .DELETE()
                        .uri(uri)
                        .build();
        }

        var handler = HttpResponse.BodyHandlers.ofString();
        return client.send(request, handler).body();
    }

    @Test
    public void shouldReturnEmptyListWhenHaveNotTasks() throws IOException, InterruptedException {
        manager.clear();
        var json = sendRequest("tasks/task", "", "GET");
        var original = "[]";
        assertEquals(json, original);
    }

    @Test
    public void shouldReturnTasks() throws IOException, InterruptedException {
        manager.clear();

        var task1 = new Task("Task1", "test");
        var task2 = new Task("Task2", "test");

        manager.add(task1, 0);
        manager.add(task2, 0);

        var json = sendRequest("tasks/task", "", "GET");
        var original = gson.toJson(manager.getPrioritizedTasks());
        assertEquals(json, original);
    }

    @Test
    public void shouldReturnTaskById() throws IOException, InterruptedException {
        manager.clear();

        var original = new Task("Task1", "test");
        var task2 = new Task("Task2", "test");

        manager.add(original, 0);
        manager.add(task2, 0);

        var json = sendRequest("tasks/task?id=" + 1, "", "GET");
        var task = gson.fromJson(json, Task.class);
        assertEquals(task.getName(), original.getName());
    }

    @Test
    public void shouldAddTaskByPostRequest() throws IOException, InterruptedException {
        manager.clear();

        var original = new Task("Task1", "test");
        original.setStartTime(LocalDateTime.now().plusMinutes(1000));
        var task2 = new Task("Task2", "test");

        manager.add(task2, 0);

        var body = gson.toJson(original);

        sendRequest("tasks/task?id=500", body, "POST");
        var json = sendRequest("tasks/task?id=500", "", "GET");
        var task = gson.fromJson(json, Task.class);
        assertEquals(task.getName(), original.getName());
    }

    @Test
    public void shouldDeleteTaskById() throws IOException, InterruptedException {
        manager.clear();

        var original = new Task("Task1", "test");
        original.setStartTime(LocalDateTime.now().plusMinutes(1000));
        var task2 = new Task("Task2", "test");

        manager.add(original, 0);
        manager.add(task2, 0);

        sendRequest("tasks/task?id=500", "", "DELETE");
        var json = sendRequest("tasks/task?id=500", "", "GET");
        assertEquals(json, "Не удалось найти задачу с id = 500");
    }

    @Test
    public void shouldDeleteTasks() throws IOException, InterruptedException {
        manager.clear();

        var original = new Task("Task1", "test");
        original.setStartTime(LocalDateTime.now().plusMinutes(1000));
        var task2 = new Task("Task2", "test");

        manager.add(original, 0);
        manager.add(task2, 0);

        sendRequest("tasks/task", "", "DELETE");
        var json = sendRequest("tasks/task", "", "GET");
        assertEquals(json, "[]");
    }

    @Test
    public void shouldReturnEpicById() throws IOException, InterruptedException {
        manager.clear();

        var original = new Epic("Task1", "test");
        var task2 = new Epic("Task2", "test");

        manager.add(original, 0);
        manager.add(task2, 0);

        var json = sendRequest("tasks/epic?id=" + 1, "", "GET");
        var task = gson.fromJson(json, Epic.class);
        assertEquals(task.getName(), original.getName());
    }

    @Test
    public void shouldReturnSubtaskById() throws IOException, InterruptedException {
        manager.clear();

        var original = new Epic("Task1", "test");
        original.setId(1);
        var task2 = new Subtask("Task2", "test", 1);

        manager.add(original, 1);
        manager.add(task2, 0);

        var json = sendRequest("tasks/subtask?id=" + 2, "", "GET");
        var task = gson.fromJson(json, Subtask.class);
        assertEquals(task.getName(), task2.getName());
    }

    @Test
    public void shouldDeleteEpics() throws IOException, InterruptedException {
        manager.clear();

        var original = new Epic("Task1", "test");
        var task2 = new Task("Task2", "test");

        manager.add(original, 0);
        manager.add(task2, 0);

        sendRequest("tasks/epic", "", "DELETE");
        var json = sendRequest("tasks/epic", "", "GET");
        assertEquals(json, "[]");
    }

    @Test
    public void shouldDeleteSubtasks() throws IOException, InterruptedException {
        manager.clear();

        var original = new Epic("Task1", "test");
        var task2 = new Subtask("Task2", "test", 1);

        manager.add(original, 1);
        manager.add(task2, 2);

        sendRequest("tasks/subtasks", "", "DELETE");
        var json = sendRequest("tasks/subtasks", "", "GET");
        assertEquals(json, "[]");
    }

    @Test
    public void shouldAddEpicByPostRequest() throws IOException, InterruptedException {
        manager.clear();

        var original = new Epic("Task1", "test");
        var task2 = new Task("Task2", "test");

        manager.add(task2, 0);

        var body = gson.toJson(original);

        sendRequest("tasks/epic?id=500", body, "POST");
        var json = sendRequest("tasks/epic?id=500", "", "GET");
        var task = gson.fromJson(json, Epic.class);
        assertEquals(task.getName(), original.getName());
    }

    @Test
    public void shouldAddSubtaskByPostRequest() throws IOException, InterruptedException {
        manager.clear();

        var original = new Epic("Task1", "test");
        var task2 = new Subtask("Task2", "test", 1);

        manager.add(original, 1);

        var body = gson.toJson(task2);

        sendRequest("tasks/subtask?id=500", body, "POST");
        var json = sendRequest("tasks/subtask?id=500", "", "GET");
        var task = gson.fromJson(json, Subtask.class);
        assertEquals(task.getName(), task2.getName());
    }

    @Test
    public void shouldReturnSubtasksByEpicId() throws IOException, InterruptedException {
        manager.clear();

        var original = new Epic("Task1", "test");
        var task2 = new Subtask("Task2", "test", 1);

        manager.add(original, 1);
        manager.add(task2, 33);

        var json = sendRequest("tasks/subtask/epic?id=1", "", "GET");
        var tasks = gson.fromJson(json, Subtask[].class);
        assertEquals(tasks[0].getName(), task2.getName());
    }

    @Test
    public void shouldReturnHistory() throws IOException, InterruptedException {
        manager.clear();

        var epic = new Epic("Task1", "test");
        var subtask = new Subtask("Task2", "test", 1);

        manager.add(epic, 1);
        manager.add(subtask, 33);

        manager.find(33);
        manager.find(1);

        manager.add(new Subtask("Task2", "test", 1), 0);
        manager.save();

        var json = sendRequest("tasks/history", "", "GET");
        var original = gson.toJson(manager.historyManager.getHistory());
        assertEquals(original, json);
    }

    @Test
    public void shouldReturnAllTasks() throws IOException, InterruptedException {
        manager.clear();

        var task1 = new Task("Task1", "test");
        var task2 = new Epic("Task2", "test");
        var task3 = new Subtask("Task3", "test", 1);

        manager.add(task2, 1);
        manager.add(task1, 0);
        manager.add(task3, 33);

        var json = sendRequest("tasks/", "", "GET");
        var original = gson.toJson(manager.getPrioritizedTasks());
        assertEquals(json, original);
    }
}
