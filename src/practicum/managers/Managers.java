package practicum.managers;

import practicum.api.KVServer;

import java.nio.file.Paths;

public class Managers {
    private final static String tasksPath = Paths.get("tasks.csv").toAbsolutePath().toString();
    private final static InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
    private final static FileBackedTasksManager fileTasksManager = new FileBackedTasksManager(tasksPath);

    /**
     * Lazily created so that simply loading this class (e.g. from a unit test that never touches
     * the network) does not trigger an HTTP call to the KV server. The server must be running
     * before the first call to {@link #getDefault()}.
     */
    public static HttpTaskManager httpTaskManager;

    private Managers() {
    }

    public static TaskManager getDefault() {
        if (httpTaskManager == null) {
            httpTaskManager = (HttpTaskManager) HttpTaskManager.load("http://localhost:" + KVServer.PORT + "/");
        }
        return httpTaskManager;
    }

    public static TaskManager getFileManager() {
        return fileTasksManager;
    }

    public static HistoryManager getDefaultHistory() {
        return historyManager;
    }

}
