package practicum.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import practicum.enums.Status;
import practicum.model.Task;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

public class Adapter<T extends Task> extends TypeAdapter<T> {
    protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
    private final Supplier<T> factory;

    /**
     * @param factory creates a fresh task instance for every deserialization.
     *                A factory (instead of a single shared instance) is required so that
     *                concurrent or repeated {@code fromJson} calls do not return the same
     *                aliased object with overwritten fields.
     */
    public Adapter(Supplier<T> factory) {
        this.factory = factory;
    }

    @Override
    public void write(JsonWriter writer, T t) {
        try {
            writeProperty(writer, "id", t.getId() + "");
            writeProperty(writer, "name", t.getName());
            writeProperty(writer, "description", t.getDescription());
            writeProperty(writer, "status", t.getStatus() + "");
        } catch (IOException exception) {
            System.out.println("Во время сериализации задачи возникло исключение: " + exception.getMessage());
        }

    }

    @Override
    public T read(JsonReader reader) {
        try {
            T task = factory.get();
            reader.beginObject();
            while (reader.hasNext()) {
                setTaskProperty(task, reader);
            }
            reader.endObject();

            var hasName = !task.getName().equals("");
            var hasDescription = !task.getDescription().equals("");

            if (hasName && hasDescription) {
                return task;
            } else {
                throw new InterruptedIOException("На вход поступили некорректные данные");
            }
        } catch (IOException exception) {
            System.out.println("Во время десериализации возникло исключение: " + exception.getMessage());
            return null;
        }
    }

    protected void setTaskProperty(T task, JsonReader reader) throws IOException {
        var token = reader.nextName().toLowerCase();
        if (!token.equals("")) {
            reader.peek();
            var isCompleted = setProperty(token, task, reader);
            if (!isCompleted) {
                reader.skipValue();
            }
        }
    }

    protected boolean setProperty(String name, T task, JsonReader reader) throws IOException {
        switch (name) {
            case "name":
                task.setName(reader.nextString());
                break;
            case "description":
                task.setDescription(reader.nextString());
                break;
            case "status":
                var status = Status.valueOf(reader.nextString());
                task.setStatus(status);
                break;
            default:
                return false;
        }
        return true;
    }

    protected void writeProperty(JsonWriter writer, String name, String value) throws IOException {
        writer.name(name);
        writer.value(value);
    }
}
