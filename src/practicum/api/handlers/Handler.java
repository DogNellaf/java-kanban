package practicum.api.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import practicum.adapters.EpicAdapter;
import practicum.adapters.SubtaskAdapter;
import practicum.adapters.TaskAdapter;
import practicum.managers.Managers;
import practicum.managers.TaskManager;
import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;

import java.io.IOException;

public class Handler implements HttpHandler {

    protected Gson gson;
    protected TaskManager manager;
    protected HttpExchange exchange;

    public Handler() {
        manager = Managers.getDefault();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Task.class, new TaskAdapter());
        builder.registerTypeAdapter(Subtask.class, new SubtaskAdapter());
        builder.registerTypeAdapter(Epic.class, new EpicAdapter());
        builder.setPrettyPrinting();
        gson = builder.create();
    }

    @Override
    public void handle(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public int getId() {
        var query = exchange.getRequestURI().getQuery();
        if (query != null) {
            var params = query.split("&");
            for (String param : params) {
                var data = param.split("=");
                if (data[0].equals("id")) {
                    return Integer.parseInt(data[1]);
                }
            }
        }
        return 0;
    }

    public void sendMessageToClient(String message, int responseCode) {
        try {
            exchange.sendResponseHeaders(responseCode, 0);
            var os = exchange.getResponseBody();
            os.write(message.getBytes());
            os.close();
        } catch (IOException exception) {
            System.out.println("Возникло исключение при передаче данных клиенту: " + exception.getMessage());
        }

    }

    public void sendUnsupportedMethod() {
        sendMessageToClient("Данный метод не поддерживается", 400);
    }
}
