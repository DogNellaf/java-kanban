package practicum.api.handlers;

import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import practicum.adapters.SubtaskAdapter;
import practicum.model.Subtask;

public class SubtaskEpicHandler extends Handler {

    public SubtaskEpicHandler() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Subtask.class, new SubtaskAdapter());
        builder.setPrettyPrinting();
        gson = builder.create();
    }

    @Override
    public void handle(HttpExchange exchange) {
        this.exchange = exchange;
        var method = exchange.getRequestMethod();
        if (method.equals("GET")) {
            sendSubtasksToClient();
        } else {
            sendUnsupportedMethod();
        }
    }

    private void sendSubtasksToClient() {
        int id = getId();
        if (id == 0) {
            sendMessageToClient("Укажите действительный id", 400);
            return;
        }

        var task = manager.find(id);
        if (task == null) {
            sendMessageToClient("Задача с id = " + id + " не найдена", 400);
            return;
        }

        var subtasks = manager.getSubtasks(id);
        var json = gson.toJson(subtasks);
        sendMessageToClient(json, 200);
    }
}
