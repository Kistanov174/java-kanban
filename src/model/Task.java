package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task implements Comparable<Task> {
    protected Integer id;
    protected String Name;
    protected String description;
    protected Status status;
    protected Type type;
    protected Duration duration;
    protected LocalDateTime startTime;
    protected  LocalDateTime endTime;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        setEndTime();
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime() {
        if (this.startTime != null && this.duration!= null) {
            this.endTime = this.startTime.plusMinutes(this.duration.toMinutes());
        }
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Task(String name, String description, LocalDateTime startTime, Duration duration) {
        this.Name = name;
        this.description = description;
        status = Status.NEW;
        type = Type.TASK;
        this.startTime = startTime;
        this.duration = duration;
        if (startTime != null && duration != null) {
            endTime = startTime.plusMinutes(duration.toMinutes());
        }
    }

    public Task(Integer id, Type type, String name, Status status, String description,
                LocalDateTime startTime, Duration duration, LocalDateTime endTime) {
        this.id = id;
        this.type = type;
        this.Name = name;
        this.status = status;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
        this.endTime = endTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return this.id + "," + this.type + "," + this.Name + "," + this.getStatus() + "," + this.description + ","
                + this.startTime + "," + this.duration + "," + this.endTime;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Task anotherTask = (Task)object;
        return Objects.equals(id, anotherTask.id);
    }

    @Override
    public int compareTo(Task anotherTask) {
        if (anotherTask.startTime == null) {
            anotherTask.startTime = LocalDateTime.MAX;
        }
        if (this.startTime.isBefore(anotherTask.startTime)) {
            return -1;
        } else if (this.startTime.isAfter(anotherTask.startTime)) {
            return 1;
        } else {
            return 0;
        }
    }
}
