package practicum.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import practicum.adapters.EpicAdapter;
import practicum.adapters.SubtaskAdapter;
import practicum.adapters.TaskAdapter;
import practicum.api.KVTaskClient;
import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;

import java.io.IOException;
import java.net.URI;
import java.util.TreeMap;

public class HttpTaskManager extends FileBackedTasksManager {
    private static KVTaskClient client;
    private static Gson gson;

    public HttpTaskManager(URI uri) throws IOException, InterruptedException {
        super("");
        client = new KVTaskClient(uri);

        var builder = new GsonBuilder();
        builder.registerTypeAdapter(Task.class, new TaskAdapter());
        builder.registerTypeAdapter(Subtask.class, new SubtaskAdapter());
        builder.registerTypeAdapter(Epic.class, new EpicAdapter());
        builder.setPrettyPrinting();
        gson = builder.create();
    }

    @Override
    public void clear() {
        super.clear();
        try {
            client.clear();
        } catch (Exception exception) {
            System.out.println("Не удалось очистить задачи на сервере");
        }
    }

    @Override
    public void save() {
        var tasks = super.getAllTasks();
        var idsData = new StringBuilder();

        try {
            for (var task : tasks) {
                var data = gson.toJson(task);
                int id = task.getId();
                client.put("" + id, data);
                idsData.append(id);
                idsData.append("_");
                idsData.append(task.getClass().getName());
                idsData.append(" ");
            }

            idsData.append("|");
            for (Task task : historyManager.getHistory()) {
                idsData.append(task.getId());
                idsData.append(" ");
            }
            client.put("ids", idsData.toString());
        } catch (Exception exception) {
            System.out.println("При попытке сохранить данные на сервер возникло исключение ошибка: " + exception.getMessage());
        }
    }

    public static TaskManager load(String path) {
        try {
            HttpTaskManager manager = new HttpTaskManager(new URI(path));

            manager.historyManager = new InMemoryHistoryManager();
            String rawData;
            try {
                rawData = client.load("ids");
            } catch (Exception exception) {
                System.out.println("На сервере нет сохраненных данных");
                return manager;
            }

            if (rawData.equals("|") || rawData.equals("")) {
                System.out.println("Нет данных для загрузки");
            } else {
                var splitedRawData = rawData.split("\\|");
                var rawIds = splitedRawData[0].split(" ");

                if (rawIds.length > 1 || !rawIds[0].equals("")) {

                    // TreeMap keeps ids ordered so that epics are recreated before their subtasks.
                    var data = new TreeMap<Integer, Task>();
                    for (String rawId : rawIds) {
                        var splitedRawId = rawId.split("_");

                        var type = Class.forName(splitedRawId[1]);
                        var id = Integer.parseInt(splitedRawId[0]);

                        var taskJson = client.load(id + "");
                        var task = (Task) gson.fromJson(taskJson, type);
                        task.setId(id);
                        data.put(id, task);
                    }

                    for (Integer id : data.keySet()) {
                        manager.add(data.get(id), id);
                    }

                    if (splitedRawData.length > 1) {
                        var history = splitedRawData[1].split(" ");
                        for (String historyId : history) {
                            var id = Integer.parseInt(historyId);
                            var task = manager.find(id);
                            manager.historyManager.add(task);
                        }
                    }
                }
            }
            return manager;
        } catch (Exception exception) {
            System.out.println("При попытке загрузить данные возникло исключение: " + exception.getMessage());
            return null;
        }

    }
}
