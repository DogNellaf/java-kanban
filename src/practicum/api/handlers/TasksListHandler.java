package practicum.api.handlers;

import com.sun.net.httpserver.HttpExchange;

public class TasksListHandler extends Handler {

    @Override
    public void handle(HttpExchange exchange) {
        this.exchange = exchange;
        var method = exchange.getRequestMethod();
        if (method.equals("GET")) {
            sendTasksListToClient();
        } else {
            sendUnsupportedMethod();
        }
    }

    private void sendTasksListToClient() {
        var tasksJson = gson.toJson(manager.getAllTasks());
        sendMessageToClient(tasksJson, 200);
    }
}
