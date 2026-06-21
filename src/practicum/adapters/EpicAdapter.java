package practicum.adapters;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import practicum.enums.Status;
import practicum.model.Epic;

import java.io.IOException;

public class EpicAdapter extends Adapter<Epic> {

    public EpicAdapter() {
        super(() -> new Epic("", ""));
    }

    @Override
    public void write(JsonWriter writer, Epic epic) {
        try {
            writer.beginObject();
            super.write(writer, epic);
            writer.endObject();
        } catch (IOException exception) {
            System.out.println("Во время сериализации задачи возникло исключение: " + exception.getMessage());
        }
    }

    @Override
    public boolean setProperty(String name, Epic epic, JsonReader reader) throws IOException {
        switch (name) {
            case "name":
                epic.setName(reader.nextString());
                break;
            case "description":
                epic.setDescription(reader.nextString());
                break;
            case "status":
                var status = Status.valueOf(reader.nextString());
                epic.setStatus(status);
                break;
            default:
                return false;
        }
        return true;
    }
}
