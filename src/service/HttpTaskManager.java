package service;

import client.KVTaskClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Epic;
import model.Subtask;
import model.Task;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

public class HttpTaskManager extends FileBackedTasksManager {
    private final KVTaskClient client;
    public HistoryManager historyManager;
    public Gson gson = new GsonBuilder().serializeNulls().create();

    public HttpTaskManager(URI uri) throws IOException, InterruptedException {
        historyManager = Managers.getDefaultHistory();
        client = new KVTaskClient(uri);
    }

    public void loadFromServer() throws IOException, InterruptedException {
        String response;
        Gson gson = new GsonBuilder().serializeNulls().create();
        response = client.load("tasks");
        Task[] tasks = gson.fromJson(response, Task[].class);
        for (Task task : tasks) {
            addTask(task);
        }
        response = client.load("epics");
        Epic[] epics = gson.fromJson(response, Epic[].class);
        for (Epic epic : epics) {
            addEpic(epic);
        }
        response = client.load("subtasks");
        Subtask[] subtasks = gson.fromJson(response, Subtask[].class);
        for (Subtask subtask : subtasks) {
            addSubtask(subtask);
        }
        response = client.load("history");
        Task[] history = gson.fromJson(response, Task[].class);
        for (Task task : history) {
            historyManager.add(task);
        }
    }

    @Override
    public boolean checkUncrossingTasks(Task newTask) {
        return super.checkUncrossingTasks(newTask);
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return super.getPrioritizedTasks();
    }

    @Override
    public HistoryManager getHistoryManager() {
        return super.getHistoryManager();
    }

    @Override
    public List<Task> getAllTasks() {
        return super.getAllTasks();
    }

    @Override
    public List<Epic> getAllEpics() {
        return super.getAllEpics();
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return super.getAllSubtasks();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
    }

    @Override
    public void deleteAllEpics() throws IOException, InterruptedException {
        super.deleteAllEpics();
    }

    @Override
    public void deleteAllSubTasks() throws IOException, InterruptedException {
        super.deleteAllSubTasks();
    }

    @Override
    public Task getTaskById(Integer id) throws IOException, InterruptedException {
        Task task = super.getTaskById(id);
        client.put("history", toJsonHistory());
        return task;
    }

    @Override
    public Epic getEpicById(Integer id) throws IOException, InterruptedException {
        Epic epic = super.getEpicById(id);
        client.put("history", toJsonHistory());
        return epic;
    }

    @Override
    public Subtask getSubTaskById(Integer id) throws IOException, InterruptedException {
        Subtask subtask = super.getSubTaskById(id);
        client.put("history", toJsonHistory());
        return subtask;
    }

    @Override
    public Integer addTask(Task task) throws IOException, InterruptedException {
        Integer id = super.addTask(task);
        String a = toJsonTasks();
        client.put("tasks", a);
        return id;
    }

    @Override
    public Integer addEpic(Epic epic) throws IOException, InterruptedException {
        Integer id = super.addEpic(epic);
        client.put("epics", toJsonEpics());
        return id;
    }

    @Override
    public Integer addSubtask(Subtask subtask) throws IOException, InterruptedException {
        Integer id = super.addSubtask(subtask);
        client.put("subtasks", toJsonSubtasks());
        return id;
    }

    @Override
    public void deleteTaskById(Integer id) throws IOException, InterruptedException {
        super.deleteTaskById(id);
        client.put("tasks", toJsonTasks());
    }

    @Override
    public void deleteEpicById(Integer id) throws IOException, InterruptedException {
        super.deleteEpicById(id);
        client.put("epics", toJsonEpics());
        client.put("subtasks", toJsonSubtasks());
    }

    @Override
    public void deleteSubtaskById(Integer id) throws IOException, InterruptedException {
        super.deleteSubtaskById(id);
        client.put("epics", toJsonEpics());
        client.put("subtasks", toJsonSubtasks());
    }

    @Override
    public void updateTask(Task task) throws IOException, InterruptedException {
        super.updateTask(task);
        client.put("tasks", toJsonTasks());
    }

    @Override
    public void updateSubtask(Subtask subtask) throws IOException, InterruptedException {
        super.updateSubtask(subtask);
        client.put("epics", toJsonEpics());
        client.put("subtasks", toJsonSubtasks());
    }

    @Override
    public void updateEpic(Epic epic) throws IOException, InterruptedException {
        super.updateEpic(epic);
        client.put("epics", toJsonEpics());
        client.put("subtasks", toJsonSubtasks());
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(Integer epicId) {
        return super.getSubtasksOfEpic(epicId);
    }

    @Override
    public List<Task> getHistory() {
        return super.getHistory();
    }

    private String toJsonTasks() {
        return gson.toJson(getAllTasks());
    }

    private String toJsonEpics() {
        return gson.toJson(getAllEpics());
    }

    private String toJsonSubtasks() {
        return gson.toJson(getAllSubtasks());
    }

    private String toJsonHistory() {
        return gson.toJson(getHistory());
    }
}
