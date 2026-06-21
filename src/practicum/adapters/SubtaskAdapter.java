package practicum.adapters;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import practicum.model.Subtask;

import java.io.IOException;
import java.time.LocalDateTime;

public class SubtaskAdapter extends Adapter<Subtask> {

    public SubtaskAdapter() {
        super(() -> new Subtask("", "", -1));
    }

    @Override
    public void write(JsonWriter writer, Subtask subtask) {
        try {
            writer.beginObject();
            super.write(writer, subtask);
            writeProperty(writer, "duration", subtask.getDuration() + "");
            writeProperty(writer, "start_time", subtask.getStartTime().format(formatter));
            writeProperty(writer, "epic_id", subtask.getEpicId() + "");
            writer.endObject();
        } catch (IOException exception) {
            System.out.println("Во время сериализации задачи возникло исключение: " + exception.getMessage());
        }
    }

    @Override
    protected boolean setProperty(String name, Subtask subtask, JsonReader reader) throws IOException {
        var isStandard = super.setProperty(name, subtask, reader);
        if (!isStandard) {
            switch (name) {
                case "duration":
                    subtask.setDuration(reader.nextInt());
                    break;
                case "start_time":
                    var startTime = LocalDateTime.parse(reader.nextString(), formatter);
                    subtask.setStartTime(startTime);
                    break;
                case "epic_id":
                    subtask.setEpicId(reader.nextInt());
                    break;
                default:
                    return false;
            }
        }
        return true;
    }
}