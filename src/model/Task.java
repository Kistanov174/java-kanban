package model;

import java.util.Objects;

public class Task {
    protected Integer id;
    protected String Name;
    protected String description;
    protected Status status;
    protected Type type;

    public Task(String name, String description) {
        this.Name = name;
        this.description = description;
        status = Status.NEW;
        type = Type.TASK;
    }

    public Task(Integer id, Type type, String name, Status status, String description) {
        this.id = id;
        this.type = type;
        this.Name = name;
        this.status = status;
        this.description = description;
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
        return this.id + "," + this.type + "," + this.Name + "," + this.getStatus() + "," + this.description;
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
}
