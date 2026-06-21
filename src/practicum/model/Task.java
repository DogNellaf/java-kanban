package practicum.model;

import practicum.enums.Status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
    private static final int DEFAULT_DURATION = 0;
    private int id;
    private String name;
    private String description;
    private Status status;
    private int duration;
    private LocalDateTime startTime;

    public Task(int id, String name, String description, Status status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = LocalDateTime.now();
        this.duration = DEFAULT_DURATION;
    }

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
        this.startTime = LocalDateTime.now();
        this.duration = DEFAULT_DURATION;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void addDuration(int minutes) {
        duration += minutes;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setStartTime(LocalDateTime time) {
        startTime = time;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setDuration(int minutes) {
        duration = minutes;
    }

    public int getDuration() {
        return duration;
    }

    public LocalDateTime getEndTime() {
        return startTime.plusMinutes(duration);
    }

    @Override
    public String toString() {
        return this.status +
                " " +
                this.name +
                " " +
                this.description +
                " " +
                this.duration +
                " " +
                (this.startTime == null ? "—" : this.startTime.format(FORMATTER)) +
                " (id = " + this.id + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Task other = (Task) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), id);
    }
}
