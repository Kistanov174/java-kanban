package service;

import model.*;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int counter;
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;
    protected final HistoryManager historyManager;

    public InMemoryTaskManager() {
        counter = 0;
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
    }

    private int generateId() {
        return ++counter;
    }

    private void deleteFromBrowsingHistory(Map tasks) {
        for (Object id : tasks.keySet()) {
            historyManager.remove((int)id);
        }
    }

    @Override
    public List<Task> getAllTasks() {
        if (!tasks.isEmpty()) {
            return new ArrayList<>(tasks.values());
        }
        return Collections.emptyList();
    }

    @Override
    public List<Epic> getAllEpics() {
        if (!epics.isEmpty()) {
            return new ArrayList<>(epics.values());
        }
        return Collections.emptyList();
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        if (!subtasks.isEmpty()) {
            return new ArrayList<>(subtasks.values());
        }
        return Collections.emptyList();
    }

    @Override
    public void deleteAllTasks() {
        deleteFromBrowsingHistory(tasks);
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        deleteFromBrowsingHistory(subtasks);
        deleteFromBrowsingHistory(epics);
        deleteAllSubTasks();
        epics.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        deleteFromBrowsingHistory(subtasks);
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasksId().clear();
            updateEpic(epic);
        }
    }

    @Override
    public Task getTaskById(Integer id) {
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Epic getEpicById(Integer id) {
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public Subtask getSubTaskById(Integer id) {
        historyManager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public Integer addTask(Task task) {
        Integer id = generateId();
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public Integer addEpic(Epic epic) {
        Integer id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public Integer addSubtask(Subtask subtask) {
        if (epics.containsKey(subtask.getEpicId())) {
            Integer id = generateId();
            subtask.setId(id);
            subtasks.put(id, subtask);
            epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
            updateEpic(epics.get(subtask.getEpicId()));
            return id;
        }
        return 0;
    }

    @Override
    public void deleteTaskById(Integer id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpicById(Integer id) {
        int amountSubtaskInEpic = epics.get(id).getSubtasksId().size();
        for (int i = amountSubtaskInEpic - 1; i >= 0; i--) {
            deleteSubtaskById(epics.get(id).getSubtasksId().get(i));
        }
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        Integer epicId = subtasks.get(id).getEpicId();
        epics.get(epicId).deleteSubtaskId(id);
        subtasks.remove(id);
        historyManager.remove(id);
        updateEpic(epics.get(epicId));
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpic(epics.get(subtask.getEpicId()));
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        int numberOfDoneSubtask = 0;
        if (epics.containsKey(epic.getId())) {
            List<Subtask> subtaskOfEpic = getSubtasksOfEpic(epic.getId());
            for (Subtask subtask : subtaskOfEpic) {
                if (subtask.getStatus().equals(Status.DONE)) {
                    numberOfDoneSubtask++;
                }
            }
            if (numberOfDoneSubtask == subtaskOfEpic.size()) {
                epic.setStatus(Status.DONE);
            }
            if (numberOfDoneSubtask > 0 && numberOfDoneSubtask < subtaskOfEpic.size()) {
                epic.setStatus(Status.IN_PROGRESS);
            }
        }
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(Integer epicId) {
        List<Subtask> subtaskOfEpic = new ArrayList<>();
        if (epics.containsKey(epicId)) {
            for(Integer subtaskId : epics.get(epicId).getSubtasksId()) {
                subtaskOfEpic.add(subtasks.get(subtaskId));
            }
            return subtaskOfEpic;
        }
        return Collections.emptyList();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
