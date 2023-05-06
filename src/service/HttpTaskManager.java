package service;

import client.KVTaskClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Epic;
import model.Subtask;
import model.Task;
import java.net.URI;

public class HttpTaskManager extends FileBackedTasksManager {
    private final KVTaskClient client;
    public HistoryManager historyManager;
    private static final String TASK_KEY = "tasks";
    private static final String EPIC_KEY = "epics";
    private static final String SUBTASK_KEY = "subtasks";
    private static final String HISTORY_KEY = "history";
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    public HttpTaskManager(URI uri) {
        historyManager = Managers.getDefaultHistory();
        client = new KVTaskClient(uri);
    }

    @Override
    protected void save() {
        client.put(TASK_KEY, toJsonTasks());
        client.put(EPIC_KEY, toJsonEpics());
        client.put(SUBTASK_KEY, toJsonSubtasks());
        client.put(HISTORY_KEY, toJsonHistory());
    }

    public void loadFromServer() {
        String response;
        Gson gson = new GsonBuilder().serializeNulls().create();
        response = client.load(TASK_KEY);
        Task[] tasks = gson.fromJson(response, Task[].class);
        for (Task task : tasks) {
            addTask(task);
        }
        response = client.load(EPIC_KEY);
        Epic[] epics = gson.fromJson(response, Epic[].class);
        for (Epic epic : epics) {
            addEpic(epic);
        }
        response = client.load(SUBTASK_KEY);
        Subtask[] subtasks = gson.fromJson(response, Subtask[].class);
        for (Subtask subtask : subtasks) {
            addSubtask(subtask);
        }
        response = client.load(HISTORY_KEY);
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
