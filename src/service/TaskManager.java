package service;

import model.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface TaskManager {
    boolean checkUncrossingTasks(Task newTask);

    Set<Task> getPrioritizedTasks();
    public HistoryManager getHistoryManager();

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    void deleteAllTasks();

    void deleteAllEpics() throws IOException, InterruptedException;

    void deleteAllSubTasks() throws IOException, InterruptedException;

    Task getTaskById(Integer id) throws IOException, InterruptedException;

    Epic getEpicById(Integer id) throws IOException, InterruptedException;

    Subtask getSubTaskById(Integer id) throws IOException, InterruptedException;

    Integer addTask(Task task) throws IOException, InterruptedException;

    Integer addEpic(Epic epic) throws IOException, InterruptedException;

    Integer addSubtask(Subtask subtask) throws IOException, InterruptedException;

    void deleteTaskById(Integer id) throws IOException, InterruptedException;

    void deleteEpicById(Integer id) throws IOException, InterruptedException;

    void deleteSubtaskById(Integer id) throws IOException, InterruptedException;

    void updateTask(Task task) throws IOException, InterruptedException;

    void updateSubtask(Subtask subtask) throws IOException, InterruptedException;

    void updateEpic(Epic epic) throws IOException, InterruptedException;

    List<Subtask> getSubtasksOfEpic(Integer epicId);

    List<Task> getHistory();
}
