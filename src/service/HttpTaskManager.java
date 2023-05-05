package service;

import client.KVTaskClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Epic;
import model.Subtask;
import model.Task;
import java.io.IOException;
import java.net.URI;

public class HttpTaskManager extends FileBackedTasksManager {
    private final KVTaskClient client;
    public HistoryManager historyManager;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    public HttpTaskManager(URI uri) {
        historyManager = Managers.getDefaultHistory();
        client = new KVTaskClient(uri);
    }

    @Override
    protected void save() {
        client.put("tasks", toJsonTasks());
        client.put("epics", toJsonEpics());
        client.put("subtasks", toJsonSubtasks());
        client.put("history", toJsonHistory());
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
