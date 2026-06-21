package practicum.model;

import practicum.enums.Status;

public class Subtask extends Task {
    private int epicId;

    public Subtask(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Подзадача " + super.toString() + ", относится к эпику " + epicId;
    }
}
