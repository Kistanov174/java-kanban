package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task{
    protected final int epicId;

    public Subtask(Integer id, Type type, String name, Status status, String description,
                   LocalDateTime startTime, Duration duration, LocalDateTime endTime, Integer epicId) {
        super(id, type, name, status, description, startTime, duration, endTime);
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    public Subtask(String name, String description, int epicId, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        type = Type.SUBTASK;
        this.epicId = epicId;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return super.toString() + "," + this.epicId;
    }
}
