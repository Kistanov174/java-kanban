package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import exception.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;
import service.Managers;
import service.TaskManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class HttpTaskServer {
    private static final int PORT = 9180;
    private final Gson gson;
    public TaskManager taskManager;
    private final HttpServer server;
    private final KVServer kvServer;
    private static final URI URI_REGISTER = URI.create("http://localhost:8078/register");
    private static final int LENGTH_ROOT_PATH = 2;
    private static final String TASK_ROOT = "task";
    private static final String EPIC_ROOT = "epic";
    private static final String SUBTASK_ROOT = "subtask";
    private static final String HISTORY_ROOT = "history";
    private static final String DELETE_METHOD = "DELETE";
    private static final String POST_METHOD = "POST";
    private static final String GET_METHOD = "GET";
    private static final int WRONG_ID = -1;


    public HttpTaskServer() throws IOException, InterruptedException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks/", this::getHandle);
        kvServer = new KVServer();
        kvServer.start();
        gson = Managers.getGson();
        taskManager = Managers.getDefault(URI_REGISTER);
        server.start();
        System.out.println("Запускается сервер");
    }

    public void stop() {
        kvServer.stop();
        server.stop(0);
    }

    private void getHandle(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        String[] splitPath = path.split("/");
        String paramPath = httpExchange.getRequestURI().getRawQuery();
        String method = httpExchange.getRequestMethod();
        String response;
        String body;

        try (InputStream os = httpExchange.getRequestBody()) {
            body = new String(os.readAllBytes());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка считывания запроса в " + HttpTaskServer.class + ".getHandle");
        }

        try (OutputStream os = httpExchange.getResponseBody()) {
            if (splitPath.length == LENGTH_ROOT_PATH && method.equals(GET_METHOD)) {
                response = gson.toJson(taskManager.getPrioritizedTasks());
                httpExchange.sendResponseHeaders(200, 0);
                os.write(response.getBytes());
            }
            if (splitPath.length > LENGTH_ROOT_PATH) {
                switch(splitPath[LENGTH_ROOT_PATH]) {
                    case TASK_ROOT:
                        response = getHandleTask(method, paramPath, body);
                        httpExchange.sendResponseHeaders(200, 0);
                        if (!response.isEmpty()) {
                            os.write(response.getBytes());
                        }
                        break;
                    case EPIC_ROOT:
                        response = getHandleEpic(method, paramPath, body);
                        httpExchange.sendResponseHeaders(200, 0);
                        if (!response.isEmpty()) {
                            os.write(response.getBytes());
                        }
                        break;
                    case SUBTASK_ROOT:
                        response = getHandleSubtask(method, paramPath, body);
                        httpExchange.sendResponseHeaders(200, 0);
                        if (!response.isEmpty()) {
                            os.write(response.getBytes());
                        }
                        break;
                    case HISTORY_ROOT:
                        response = gson.toJson(taskManager.getHistory());
                        httpExchange.sendResponseHeaders(200, 0);
                        if (!response.isEmpty()) {
                            os.write(response.getBytes());
                        }
                        break;
                }
            }
        } catch (IOException e) {
            httpExchange.sendResponseHeaders(405, 0);
            throw new ManagerSaveException("Ошибка отправки ответа на запрос в " + HttpTaskServer.class + ".getHandle");
        }
        finally {
            httpExchange.close();
        }
    }

    private String getHandleTask(String method, String path, String body) {
        String response = "";
        int id = WRONG_ID;
        if (path != null)
        {
            String idPath = path.replaceFirst("id=", "");
            if (!idPath.isEmpty()) {
                id = parseId(idPath);
            }
        }
        switch (method) {
            case GET_METHOD:
                if (id != WRONG_ID) {
                    response = gson.toJson(taskManager.getTaskById(id));
                } else {
                    response =  gson.toJson(taskManager.getAllTasks());
                }
                break;
            case POST_METHOD:
                Task task = gson.fromJson(body, Task.class);
                if (task.getId() == null) {
                    response = gson.toJson(taskManager.addTask(task));
                } else {
                    taskManager.updateTask(task);
                }
                break;
            case DELETE_METHOD:
                if (id != WRONG_ID) {
                    taskManager.deleteTaskById(id);
                } else {
                    taskManager.deleteAllTasks();
                }
                break;
        }
        return response;
    }

    private String getHandleEpic(String method, String path, String body) {
        String response = "";
        int id = WRONG_ID;
        if (path != null) {
            String idPath = path.replaceFirst("id=", "");
            if (!idPath.isEmpty()) {
                id = parseId(idPath);
            }
        }
        switch (method) {
            case GET_METHOD:
                if (id != WRONG_ID) {
                    response = gson.toJson(taskManager.getEpicById(id));
                } else {
                    response = gson.toJson(taskManager.getAllEpics());
                }
                break;
            case POST_METHOD:
                Epic epic = gson.fromJson(body, Epic.class);
                if (epic.getId() == null) {
                    response = gson.toJson(taskManager.addEpic(epic));
                } else {
                    taskManager.updateEpic(epic);
                }
                break;
            case DELETE_METHOD:
                if (id != WRONG_ID) {
                    taskManager.deleteEpicById(id);
                } else {
                    taskManager.deleteAllEpics();
                }
                break;
        }
        return response;
    }

    private String getHandleSubtask(String method, String path, String body) {
        String response = "";
        int id = WRONG_ID;
        if (path != null) {
            String idPath = path.replaceFirst("id=", "");
            if (!idPath.isEmpty()) {
                id = parseId(idPath);
            }
        }
        switch (method) {
            case GET_METHOD:
                if (id != WRONG_ID) {
                    response = gson.toJson(taskManager.getSubTaskById(id));
                } else {
                    response = gson.toJson(taskManager.getAllSubtasks());
                }
                break;
            case POST_METHOD:
                Subtask subtask = gson.fromJson(body, Subtask.class);
                if (subtask.getId() == null) {
                    response = gson.toJson(taskManager.addSubtask(subtask));
                } else {
                    taskManager.updateSubtask(subtask);
                }
                break;
            case DELETE_METHOD:
                if (id != WRONG_ID) {
                    taskManager.deleteSubtaskById(id);
                } else {
                    taskManager.deleteAllSubTasks();
                }
                break;
        }
        return response;
    }

    private int parseId(String pathId) {
        try {
            return Integer.parseInt(pathId);
        } catch (NumberFormatException e) {
            return WRONG_ID;
        }
    }
}