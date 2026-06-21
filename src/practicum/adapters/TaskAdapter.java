package practicum.adapters;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import practicum.model.Task;

import java.io.IOException;
import java.time.LocalDateTime;

public class TaskAdapter extends Adapter<Task> {

    public TaskAdapter() {
        super(() -> new Task("", ""));
    }

    @Override
    public void write(JsonWriter writer, Task task) {
        try {
            writer.beginObject();
            super.write(writer, task);
            writeProperty(writer, "duration", task.getDuration() + "");
            writeProperty(writer, "start_time", task.getStartTime().format(formatter));
            writer.endObject();
        } catch (IOException exception) {
            System.out.println("Во время сериализации задачи возникло исключение: " + exception.getMessage());
        }
    }

    @Override
    protected boolean setProperty(String name, Task task, JsonReader reader) throws IOException {
        var isStandard = super.setProperty(name, task, reader);
        if (!isStandard) {
            switch (name) {
                case "duration":
                    task.setDuration(reader.nextInt());
                    break;
                case "start_time":
                    var startTime = LocalDateTime.parse(reader.nextString(), formatter);
                    task.setStartTime(startTime);
                    break;
                default:
                    return false;
            }
        }
        return true;
    }
}