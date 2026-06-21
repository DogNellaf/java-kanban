package practicum.managers;

import practicum.enums.Status;
import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;

import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;

public class FileBackedTasksManager extends InMemoryTaskManager {
    public static final String CSV_HEADER = "id,type,name,status,description,duration,start_time,epic";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final String path;

    public FileBackedTasksManager(String path) {
        this.path = path;
    }

    @Override
    public boolean add(Task task, int id) {
        var isComplete = super.add(task, id);
        save();
        return isComplete;
    }

    @Override
    public Task find(int id) {
        var task = super.find(id);
        save();
        return task;
    }

    @Override
    public boolean change(Task task) {
        var isComplete = super.change(task);
        save();
        return isComplete;
    }

    @Override
    public boolean remove(int id) {
        var isComplete = super.remove(id);
        save();
        return isComplete;
    }

    @Override
    public void clear() {
        super.clear();
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    public void save() {
        try (FileWriter writer = new FileWriter(this.path)) {
            writer.write(CSV_HEADER + "\n");
            for (var task : super.getAllTasks()) {
                var data = convertTaskToCSV(task);
                writer.write(data);
            }
            writer.write("\n");
            var tasks = historyManager.getHistory();
            for (var task : tasks) {
                writer.write(task.getId() + ",");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TaskManager load(String path) {
        var file = new File(path);
        var manager = new FileBackedTasksManager(file.getPath());
        manager.historyManager = new InMemoryHistoryManager();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            String line = reader.readLine();
            var tasks = new ArrayList<Task>();
            while (line != null && !line.isEmpty()) {
                var data = line.split(",");
                var id = Integer.parseInt(data[0]);
                var className = data[1];
                var name = data[2];
                var status = Status.valueOf(data[3]);
                var description = data[4];
                var duration = Integer.parseInt(data[5]);
                var startTime = LocalDateTime.parse(data[6], formatter);
                Task task;
                switch (className) {
                    case "Epic" -> task = new Epic(id, name, description, status);
                    case "Subtask" -> {
                        var epicId = Integer.parseInt(data[7]);
                        task = new Subtask(id, name, description, status);
                        ((Subtask) task).setEpicId(epicId);
                        task.setDuration(duration);
                        task.setStartTime(startTime);
                    }
                    default -> {
                        task = new Task(id, name, description, status);
                        task.setDuration(duration);
                        task.setStartTime(startTime);
                    }
                }
                tasks.add(task);
                line = reader.readLine();
            }
            tasks.sort(Comparator.comparingInt(Task::getId));
            for (var task : tasks) {
                manager.add(task, task.getId());
            }
            var historyRawData = reader.readLine();
            if (historyRawData != null) {
                var historyData = historyRawData.split(",");
                for (var data : historyData) {
                    int id = Integer.parseInt(data);
                    manager.find(id);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return manager;
    }

    private static String convertTaskToCSV(Task task) {
        var builder = new StringBuilder();
        var className = task.getClass().getSimpleName();
        addDataInString(builder, task.getId(), false);
        addDataInString(builder, className, false);
        addDataInString(builder, task.getName(), false);
        addDataInString(builder, task.getStatus(), false);
        addDataInString(builder, task.getDescription(), false);
        if (className.equals("Epic")) {
            addDataInString(builder, "0", false);

            var midnight = LocalDateTime.of(LocalDateTime.now().toLocalDate(), LocalTime.MIDNIGHT);

            addDataInString(builder, midnight.format(formatter), false);
        } else {
            addDataInString(builder, task.getDuration(), false);
            addDataInString(builder, task.getStartTime().format(formatter), false);
        }

        if (className.equals("Subtask")) {
            addDataInString(builder, ((Subtask) task).getEpicId(), true);
        } else {
            addDataInString(builder, "null", true);
        }
        return builder + "\n";
    }

    private static void addDataInString(StringBuilder builder, Object data, boolean isEnd) {
        builder.append(data);
        if (!isEnd) {
            builder.append(",");
        }
    }
}
