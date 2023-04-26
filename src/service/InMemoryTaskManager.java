package service;

import model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int counter;
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;

    protected final HistoryManager historyManager;
    protected final Set<Task> prioritizedTasks;

    public InMemoryTaskManager() {
        counter = 0;
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
        prioritizedTasks = new TreeSet<>();
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public boolean checkUncrossingTasks(Task newTask) {
        LocalDateTime startNewTask = newTask.getStartTime();
        LocalDateTime endNewTask = newTask.getEndTime();
        Set<Task> prioritizedTasks = getPrioritizedTasks();
        for (Task task : prioritizedTasks) {
            LocalDateTime startTask = task.getStartTime();
            LocalDateTime endTask = task.getEndTime();
            boolean insideAnotherTaskPeriod = startNewTask.isAfter(startTask) && startNewTask.isBefore(endTask);
            boolean startInsideAnotherTask = startNewTask.isAfter(startTask) && endNewTask.isBefore(endTask);
            boolean endInsideAnotherTask = endNewTask.isAfter(startTask) && endNewTask.isBefore(endTask);
            boolean anotherTaskInside = startNewTask.isBefore(startTask) && endNewTask.isAfter(endTask);
            if (insideAnotherTaskPeriod || startInsideAnotherTask || endInsideAnotherTask || anotherTaskInside) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    private int generateId() {
        return ++counter;
    }

    private void deleteFromBrowsingHistory(Map tasks) {
        for (Object id : tasks.keySet()) {
            historyManager.remove((int)id);
        }
    }

    private void getStartEndDurationTimeEpic(Epic epic, List<Subtask> subtasksOfEpic) {
        if (!subtasksOfEpic.isEmpty()) {
            LocalDateTime earlyDateTime = LocalDateTime.MAX;
            LocalDateTime lateDateTime = LocalDateTime.MIN;
            Duration duration = Duration.ofMinutes(0);
            for(Subtask subtask : subtasksOfEpic) {
                duration = duration.plus(subtask.getDuration());
                LocalDateTime startSubtaskDateTime = subtask.getStartTime();
                int equalsStartResult = startSubtaskDateTime.compareTo(earlyDateTime);
                if (equalsStartResult < 0) {
                    earlyDateTime = startSubtaskDateTime;
                }
                LocalDateTime endSubtaskDateTime = subtask.getEndTime();
                int equalsEndResult = endSubtaskDateTime.compareTo(lateDateTime);
                if (equalsEndResult > 0) {
                    lateDateTime = endSubtaskDateTime;
                }
            }
            epic.setStartTime(earlyDateTime);
            epic.setDuration(duration);
            epic.setEndTime(lateDateTime);
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
        prioritizedTasks.removeAll(tasks.values());
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
        prioritizedTasks.removeAll(subtasks.values());
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasksId().clear();
            updateEpic(epic);
        }
    }

    @Override
    public Task getTaskById(Integer id) {
        if (tasks.containsKey(id)) {
            historyManager.add(tasks.get(id));
            return tasks.get(id);
        } else {
            throw new NoSuchElementException("Такой задачи нет");
        }
    }

    @Override
    public Epic getEpicById(Integer id) {
        if (epics.containsKey(id)) {
            historyManager.add(epics.get(id));
            return epics.get(id);
        } else {
            throw  new NoSuchElementException("Такого эпика нет");
        }
    }

    @Override
    public Subtask getSubTaskById(Integer id) {
        if (subtasks.containsKey(id)) {
            historyManager.add(subtasks.get(id));
            return subtasks.get(id);
        } else {
            throw new NoSuchElementException("Такой подзадачи нет");
        }
    }

    @Override
    public Integer addTask(Task task) {
        if (checkUncrossingTasks(task)) {
            Integer id = generateId();
            task.setId(id);
            task.setEndTime();
            tasks.put(id, task);
            prioritizedTasks.add(task);
            return id;
        } else {
            throw new IllegalArgumentException("Некорректное значение параметров задачи");
        }
    }

    @Override
    public Integer addEpic(Epic epic) {
        Integer id = generateId();
        epic.setId(id);
        updateEpic(epic);
        epics.put(id, epic);
        return id;
    }

    @Override
    public Integer addSubtask(Subtask subtask) {
        if (epics.containsKey(subtask.getEpicId()) && checkUncrossingTasks(subtask)) {
            Integer id = generateId();
            subtask.setId(id);
            //subtask.setEndTime();
            subtasks.put(id, subtask);
            prioritizedTasks.add(subtask);
            epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
            updateEpic(epics.get(subtask.getEpicId()));
            return id;
        } else {
            throw new IllegalArgumentException("Некорректное значение параметров подзадачи");
        }
    }

    @Override
    public void deleteTaskById(Integer id) {
        if (tasks.containsKey(id)) {
            prioritizedTasks.remove(tasks.get(id));
            tasks.remove(id);
            historyManager.remove(id);
        } else {
            throw  new NoSuchElementException("Такой задачи нет");
        }
    }

    @Override
    public void deleteEpicById(Integer id) {
        if (epics.containsKey(id)) {
            int amountSubtaskInEpic = epics.get(id).getSubtasksId().size();
            for (int i = amountSubtaskInEpic - 1; i >= 0; i--) {
                deleteSubtaskById(epics.get(id).getSubtasksId().get(i));
            }
            epics.remove(id);
            historyManager.remove(id);
        } else {
            throw new NoSuchElementException("Такого эпика нет");
        }
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        if (subtasks.containsKey(id)) {
            Integer epicId = subtasks.get(id).getEpicId();
            epics.get(epicId).deleteSubtaskId(id);
            prioritizedTasks.remove(subtasks.get(id));
            subtasks.remove(id);
            historyManager.remove(id);
            updateEpic(epics.get(epicId));
        } else {
            throw new NoSuchElementException("Такой подзадачи нет");
        }
    }

    @Override
    public void updateTask(Task task) {
        task.setEndTime();
        if (tasks.containsKey(task.getId()) && checkUncrossingTasks(task)) {
            prioritizedTasks.remove(getTaskById(task.getId()));
            tasks.put(task.getId(), task);
            prioritizedTasks.add(task);
        } else {
            throw new IllegalArgumentException("Некорректное значение параметров задачи");
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtask.setEndTime();
        if (subtasks.containsKey(subtask.getId()) && checkUncrossingTasks(subtask)) {
            prioritizedTasks.remove(getSubTaskById(subtask.getId()));
            subtasks.put(subtask.getId(), subtask);
            updateEpic(epics.get(subtask.getEpicId()));
            prioritizedTasks.add(subtask);
        } else {
            throw new IllegalArgumentException("Некорректное значение параметров подзадачи");
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        int numberOfDoneSubtask = 0;
        int numberOfSubtaskInProgress = 0;
        if (epics.containsKey(epic.getId())) {
            List<Subtask> subtaskOfEpic = getSubtasksOfEpic(epic.getId());
            for (Subtask subtask : subtaskOfEpic) {
                if (subtask.getStatus().equals(Status.DONE)) {
                    numberOfDoneSubtask++;
                }
                if (subtask.getStatus().equals(Status.IN_PROGRESS)) {
                    numberOfSubtaskInProgress++;
                }
            }
            if (numberOfDoneSubtask == subtaskOfEpic.size()) {
                epic.setStatus(Status.DONE);
            }
            if ((numberOfDoneSubtask > 0
                    && numberOfDoneSubtask < subtaskOfEpic.size())
                    || (numberOfSubtaskInProgress > 0)) {
                epic.setStatus(Status.IN_PROGRESS);
            }
            getStartEndDurationTimeEpic(epic, subtaskOfEpic);
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
