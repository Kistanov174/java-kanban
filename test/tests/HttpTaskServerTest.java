package tests;

import com.google.gson.Gson;
import model.*;
import org.junit.jupiter.api.*;
import server.HttpTaskServer;
import service.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.NoSuchElementException;

public class HttpTaskServerTest extends TaskManagerTest<HttpTaskManager> {
    private HttpTaskServer server;
    private HttpClient client;
    private final Gson gson = Managers.getGson();
    private final URI uri = URI.create("http://localhost:8078/register");

    @BeforeEach
    void beforeEach() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        server = new HttpTaskServer();
        historyManager = server.taskManager.getHistoryManager();
        taskManager = new HttpTaskManager(uri);
        super.initializeTasks();
    }

    @AfterEach
    void  afterEach() {
        server.stop();
    }

    @DisplayName("Проверка эндпоинта POST /tasks/task для добавления задачи")
    @Test
    void shouldGetAddedTask() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/task/");
        assertEquals(0, server.taskManager.getAllTasks().size(), "список задач изначально не пуст");
        String jsonTask = gson.toJson(task4);

        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString(jsonTask);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(publisher)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        assertEquals(1, server.taskManager.getAllTasks().size(), "список задач пуст");
        Integer id = Integer.parseInt(response.body());
        assertNotNull(server.taskManager.getTaskById(id));
    }

    @DisplayName("Проверка эндпоинта POST /tasks/epic для добавления эпика")
    @Test
    void shouldAddEpic() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/epic/");
        assertEquals(0, server.taskManager.getAllEpics().size(), "Список эпиков изначально не пуст");
        String jsonEpic = gson.toJson(epic2);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString(jsonEpic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(publisher)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        assertEquals(1, server.taskManager.getAllEpics().size(), "список эпиков пуст");
        Integer id = Integer.parseInt(response.body());
        assertNotNull(server.taskManager.getEpicById(id));
    }

    @DisplayName("Проверка эндпоинта POST /tasks/subtask для добавления подзадачи")
    @Test
    void shouldAddSubtask() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/subtask/");
        assertEquals(0, server.taskManager.getAllSubtasks().size(),
                "список подзадач изначально не пуст");
        server.taskManager.addEpic(epic2);
        String jsonSubtask = gson.toJson(subtask4);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString(jsonSubtask);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(publisher)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        assertEquals(1, server.taskManager.getAllSubtasks().size(), "список подзадач пуст");
        Integer id = Integer.parseInt(response.body());
        assertNotNull(server.taskManager.getSubTaskById(id));
    }

    @DisplayName("Проверка эндпоинта GET /tasks/task/?id=1")
    @Test
    void shouldGetTaskById() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/task/?id=1");
        server.taskManager.addTask(task4);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        Task receivedTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(receivedTask, "Задача не возвращается");
        Task expectedTask = server.taskManager.getTaskById(1);
        assertEquals(expectedTask, receivedTask, "Задачи не совпадают");
    }

    @DisplayName("Проверка эндпоинта GET /tasks/epic/?id=1")
    @Test
    void shouldGetEpicById() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/epic/?id=1");
        server.taskManager.addEpic(epic2);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        Task receivedEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(receivedEpic, "Эпик не возвращается");
        Task expectedEpic = server.taskManager.getEpicById(1);
        assertEquals(expectedEpic, receivedEpic, "Эпики не совпадают");
    }

    @DisplayName("Проверка эндпоинта GET /tasks/subtask/?id=2")
    @Test
    void shouldGetSubtaskById() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/subtask/?id=2");
        server.taskManager.addEpic(epic2);
        server.taskManager.addSubtask(subtask4);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        Subtask receivedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(receivedSubtask, "Эпик не возвращается");
        Subtask expectedSubtask = server.taskManager.getSubTaskById(2);
        assertEquals(expectedSubtask, receivedSubtask, "Подзадачи не совпадают");
    }

    @DisplayName("Проверка эндпоинта GET /tasks/history/")
    @Test
    void shouldGetBrowsingHistory() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/history/");
        server.taskManager.addEpic(epic2);
        server.taskManager.addSubtask(subtask4);
        server.taskManager.getSubTaskById(2);
        Epic expectedEpic = server.taskManager.getEpicById(1);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasks, "История просмотра не возращается");
        assertEquals(2, tasks.length, "Размер истории неверный");
        assertEquals(expectedEpic.getId(), tasks[1].getId(), "Содержание истории неверное");
    }

    @DisplayName("Проверка эндпоинта DELETE /tasks/task/?id=1")
    @Test
    void shouldDeleteTaskById() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/task/?id=1");
        server.taskManager.addTask(task4);
        assertEquals(1, server.taskManager.getAllTasks().size(), "Список пустой");
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> server.taskManager.getTaskById(1));
        assertEquals("Такой задачи нет", ex.getMessage(), "Задачи не удалилась");
        assertEquals(0, server.taskManager.getAllTasks().size(), "Список задач не изменился");
    }

    @DisplayName("Проверка эндпоинта DELETE /tasks/task/")
    @Test
    void shouldDeleteAllTasks() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/task/");
        server.taskManager.addTask(task4);
        server.taskManager.addTask(task3);
        assertEquals(2, server.taskManager.getAllTasks().size(), "Список пустой");
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> server.taskManager.getTaskById(1));
        assertEquals("Такой задачи нет", ex.getMessage(), "Задачи не удалились");
        assertEquals(0, server.taskManager.getAllTasks().size(), "Список задач не изменился");
    }

    @DisplayName("Проверка эндпоинта DELETE /tasks/epic/?id=1")
    @Test
    void shouldDeleteEpicById() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/epic/?id=1");
        server.taskManager.addEpic(epic2);
        assertEquals(1, server.taskManager.getAllEpics().size(), "Список пустой");
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> server.taskManager.getEpicById(1));
        assertEquals("Такого эпика нет", ex.getMessage(), "Эпик не удалился");
        assertEquals(0, server.taskManager.getAllEpics().size(), "Список эпиков не изменился");
    }

    @DisplayName("Проверка эндпоинта DELETE /tasks/epic/")
    @Test
    void shouldDeleteAllEpics() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/epic/");
        server.taskManager.addEpic(epic2);
        assertEquals(1, server.taskManager.getAllEpics().size(), "Список пустой");
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> server.taskManager.getEpicById(1));
        assertEquals("Такого эпика нет", ex.getMessage(), "Эпики не удалились");
        assertEquals(0, server.taskManager.getAllEpics().size(), "Список эпиков не изменился");
    }

    @DisplayName("Проверка эндпоинта DELETE /tasks/subtask/?id=2")
    @Test
    void shouldDeleteSubtaskById() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/subtask/?id=2");
        server.taskManager.addEpic(epic2);
        server.taskManager.addSubtask(subtask4);
        assertEquals(1, server.taskManager.getAllEpics().size(), "Список пустой");
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> server.taskManager.getSubTaskById(2));
        assertEquals("Такой подзадачи нет", ex.getMessage(), "Подзадача не удалилась");
        assertEquals(0, server.taskManager.getAllSubtasks().size(), "Список подзадач не изменился");
    }

    @DisplayName("Проверка эндпоинта DELETE /tasks/subtask/")
    @Test
    void shouldDeleteAllSubtask() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/subtask/");
        server.taskManager.addEpic(epic2);
        server.taskManager.addSubtask(subtask4);
        assertEquals(1, server.taskManager.getAllEpics().size(), "Список пустой");
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> server.taskManager.getSubTaskById(2));
        assertEquals("Такой подзадачи нет", ex.getMessage(), "Подзадачи не удалились");
        assertEquals(0, server.taskManager.getAllSubtasks().size(), "Список подзадач не изменился");
    }

    @DisplayName("Проверка эндпоинта POST /tasks/task/ для обновления задачи")
    @Test
    void shouldUpdateTask() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/task/?id=1");
        server.taskManager.addTask(task4);
        expectedTask4.setStatus(Status.DONE);
        String jsonUpdateTask = gson.toJson(expectedTask4);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString(jsonUpdateTask);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(publisher)
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        Status newStatus = server.taskManager.getTaskById(1).getStatus();
        assertEquals(Status.DONE, newStatus, "Статус не обновился");
    }

    @DisplayName("Проверка эндпоинта POST /tasks/epic/ для обновления эпика")
    @Test
    void shouldUpdateEpic() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/epic/");
        server.taskManager.addEpic(epic2);
        Epic updatedEpic = server.taskManager.getEpicById(epic2.getId());
        updatedEpic.setName("New name for epic");
        String jsonUpdatedEpic = gson.toJson(updatedEpic);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString(jsonUpdatedEpic);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(publisher)
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        String newName = server.taskManager.getEpicById(1).getName();
        assertEquals("New name for epic", newName, "Имя не обновилось");
    }

    @DisplayName("Проверка эндпоинта POST /tasks/subtask/ для обновления подзадачи")
    @Test
    void shouldUpdateSubtaskById() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:9180/tasks/subtask/");
        server.taskManager.addEpic(epic2);
        server.taskManager.addSubtask(subtask4);
        Subtask updatedSubtask = server.taskManager.getSubTaskById(subtask4.getId());
        updatedSubtask.setStatus(Status.IN_PROGRESS);
        String jsonUpdatedEpic = gson.toJson(updatedSubtask);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString(jsonUpdatedEpic);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(publisher)
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код сообщения не совпадает");
        Status newStatus = server.taskManager.getSubTaskById(2).getStatus();
        assertEquals(Status.IN_PROGRESS, newStatus, "Статус не обновился");
    }

    @DisplayName("Проверка сохранения и восстановления состояния HttpTaskManager")
    @Test
    void shouldSaveAndRestoreHttpTaskManager() throws IOException, InterruptedException {
        server.taskManager.addEpic(epic2);
        server.taskManager.addSubtask(subtask4);
        server.taskManager.getEpicById(1);
        server.taskManager.getSubTaskById(2);
        taskManager.loadFromServer();
        assertEquals(1, server.taskManager.getAllEpics().size(), "Список эпиков не восстановился");
        assertEquals(1, server.taskManager.getAllSubtasks().size(), "Список подзадач не восстановился");
        assertEquals(0, server.taskManager.getAllTasks().size(), "Список задач не восстановился");
        assertEquals(2, server.taskManager.getHistory().size(), "История просмотра не восстановилась");
    }
}


