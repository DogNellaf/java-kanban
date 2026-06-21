package practicum.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import practicum.model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends Handler {

    @Override
    public void handle(HttpExchange exchange) {
        this.exchange = exchange;
        var method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                sendTasksToClient(manager.getTasks(), "Task");
                break;
            case "POST":
                handlePostRequest(Task.class);
                break;
            case "DELETE":
                deleteTask();
                break;
            default:
                sendMessageToClient("Unsupported method", 400);
                break;
        }
    }

    public void clearTasks() {
        manager.clearTasks();
    }

    public void sendTasksToClient(List<Task> tasks, String type) {
        int id = getId();

        String message;
        int code = 200;

        if (id == 0) {
            message = gson.toJson(tasks);
        } else {
            var task = manager.find(id);
            if (task == null) {
                message = "Не удалось найти задачу с id = " + id;
                code = 404;
            } else {
                if (task.getClass().getSimpleName().equals(type)) {
                    message = gson.toJson(task);
                } else {
                    message = "Задача " + id + " не имеет тип " + type;
                    code = 404;
                }
            }
        }
        sendMessageToClient(message, code);
    }

    public void handlePostRequest(Class type) {
        String json = "";
        try {
            json = new String(exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);
        } catch (IOException exception) {
            System.out.println("Не удалось получить json из запроса");
        }


        int id = getId();

        Task task;
        try {
            task = (Task) gson.fromJson(json, type);
        } catch (Exception exception) {
            sendMessageToClient("Структура задачи некорректна", 400);
            return;
        }
        task.setId(id);

        var sameTask = manager.find(id);

        String message;
        int code;

        if (sameTask != null) {
            if (manager.change(task)) {
                message = "Задача была успешно изменена";
                code = 200;
            } else {
                message = "Не удалось изменить задачу";
                code = 500;
            }

        } else {
            if (manager.add(task, id)) {
                message = "Задача была успешно добавлена";
                code = 200;
            } else {
                message = "Не удалось добавить задачу";
                code = 500;
            }
        }
        sendMessageToClient(message, code);
    }

    public void deleteTask() {
        int id = getId();

        String message;
        int code;

        if (id != 0) {
            if (manager.remove(id)) {
                message = "Задача была успешно удалена";
                code = 200;
            } else {
                message = "Не удалось удалить задачу";
                code = 500;
            }
        } else {
            clearTasks();
            message = "Все задачи удалены";
            code = 200;
        }

        sendMessageToClient(message, code);
    }
}
