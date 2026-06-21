package practicum.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import practicum.model.Epic;
import practicum.model.Task;

import java.util.ArrayList;

public class EpicHandler extends TaskHandler {
    @Override
    public void clearTasks() {
        manager.clearEpics();
    }

    @Override
    public void handle(HttpExchange exchange) {
        this.exchange = exchange;
        var method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                sendEpicsToClient();
                break;
            case "POST":
                handlePostRequest(Epic.class);
                break;
            case "DELETE":
                deleteTask();
                break;
            default:
                sendUnsupportedMethod();
                break;
        }
    }

    private void sendEpicsToClient() {
        var tasks = new ArrayList<Task>(manager.getEpics());
        super.sendTasksToClient(tasks, "Epic");
    }
}

