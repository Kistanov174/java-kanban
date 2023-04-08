package model;

public class Subtask extends Task{
    protected final int epicId;

    public Subtask(Integer id, Type type, String name, Status status, String description, Integer epicId) {
        super(id, type, name, status, description);
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    public Subtask(String name, String description, int epicId) {
        super(name, description);
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
