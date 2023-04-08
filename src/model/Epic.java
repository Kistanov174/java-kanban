package model;

import service.TaskManager;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task{
    private final List<Integer> subtasksId = new ArrayList<>();;

    public Epic(String name, String description) {
        super(name, description);
        type = Type.EPIC;
    }

    public Epic(Integer id, Type type, String name, Status status, String description) {
        super(id, type, name, status, description);
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
