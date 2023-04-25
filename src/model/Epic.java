package model;

import service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task{
    private final List<Integer> subtasksId = new ArrayList<>();;

    public Epic(String name, String description, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        type = Type.EPIC;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Epic(Integer id, Type type, String name, Status status, String description,
                LocalDateTime startTime, Duration duration, LocalDateTime endTime) {
        super(id, type, name, status, description, startTime, duration, endTime);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void addSubtaskId(Integer id) {
        subtasksId.add(id);
    }

    public  void deleteSubtaskId(Integer id) {
        subtasksId.remove(id);
    }

    public List<Integer> getSubtasksId() {
        return subtasksId;
    }
}
