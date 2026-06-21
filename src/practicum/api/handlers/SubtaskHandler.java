package practicum.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import practicum.model.Subtask;
import practicum.model.Task;

import java.util.ArrayList;

public class SubtaskHandler extends TaskHandler {
    @Override
    public void clearTasks() {
        manager.clearSubtasks();
    }

    @Override
    public void handle(HttpExchange exchange) {
        this.exchange = exchange;
        var method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                sendSubtasksToClient();
                break;
            case "POST":
                handlePostRequest(Subtask.class);
                break;
            case "DELETE":
                deleteTask();
                break;
            default:
                sendMessageToClient("Unsupported method", 400);
                break;
        }
    }

    private void sendSubtasksToClient() {
        var tasks = new ArrayList<Task>(manager.getSubtasks());
        sendTasksToClient(tasks, "Subtask");
    }
}
