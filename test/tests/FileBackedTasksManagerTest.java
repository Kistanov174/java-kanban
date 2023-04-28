package tests;

import exception.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.FileBackedTasksManager;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {
    private static final File FILE = new File("src\\storage.csv");

    @DisplayName("Чтение несуществующего файла")
    @Test
    void shouldGetExceptionReadingFile() {
        taskManager = new FileBackedTasksManager();
        File file  = new File("src\\NotExistedFile.csv");
        ManagerSaveException ex = assertThrows(ManagerSaveException.class,
                () -> FileBackedTasksManager.loadFromFile(file));
        assertEquals("Ошибка считывания в service.FileBackedTasksManager.loadFromFile()", ex.getMessage());
    }

    @DisplayName("Чтение пустого файла")
    @Test
    void shouldGetEmptyTaskManager() {
        taskManager = new FileBackedTasksManager();
        assertNotNull(FileBackedTasksManager.loadFromFile(FILE), "Менеджер не создается");
        assertTrue(taskManager.getAllTasks().isEmpty(), "Список задач не пуст");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Список подзадач не пуст");
        assertTrue(taskManager.getAllEpics().isEmpty(), "Список эпиков не пуст");
        assertTrue(taskManager.getHistory().isEmpty(), "История просмотра не пуста");
    }

    @DisplayName("Чтение файла с пустой историей просмотра")
    @Test
    void shouldGetTaskManagerWithEmptyBrowsingHistory() {
        taskManager = new FileBackedTasksManager();
        Task task = new Task("Test addNewTask", "Test addNewTask description",
                LocalDateTime.of(2023,4,7,12,35),
                Duration.ofMinutes(120));
        taskManager.addTask(task);
        assertNotNull(FileBackedTasksManager.loadFromFile(FILE), "Менеджер не создается");
        assertTrue(taskManager.getHistory().isEmpty(), "История просмотра не пуста");
        assertFalse(taskManager.getAllTasks().isEmpty(), "Список задач пуст");
        assertEquals("[]", taskManager.getHistory().toString());
    }

    @DisplayName("Запись и чтение эпика без подзадач")
    @Test
    void shouldGetFileAndTaskManagerWithEmptyEpic() {
        Epic epic = new Epic("The single epic", "This epic has zero subtask",null, null);
        taskManager = new FileBackedTasksManager();
        taskManager.addEpic(epic);
        FileBackedTasksManager newTaskManager = FileBackedTasksManager.loadFromFile(FILE);
        Epic restoreEpic = newTaskManager.getEpicById(epic.getId());
        assertEquals(restoreEpic, epic, "Эпики не эдентичны");
    }

    @DisplayName("Запись и чтение задачи")
    @Test
    void shouldGetFileAndTaskManagerWithTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description",
        LocalDateTime.of(2023,4,7,12,35), Duration.ofMinutes(120));
        taskManager = new FileBackedTasksManager();
        taskManager.addTask(task);
        FileBackedTasksManager newTaskManager = FileBackedTasksManager.loadFromFile(FILE);
        Task restoreTask = newTaskManager.getTaskById(task.getId());
        assertEquals(restoreTask, task, "Задачи не эдентичны");
    }

    @DisplayName("Запись и чтение эпика с подзадачами")
    @Test
    void shouldGetFileAndTaskManagerWithNotEmptyEpic() {
        taskManager = new FileBackedTasksManager();
        Epic epic = new Epic("The single epic", "This epic has zero subtask",null, null);
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("The first", "Number one", epic.getId(),
                LocalDateTime.of(2023,1,4,12,45), Duration.ofMinutes(45));
        taskManager.addSubtask(subtask);
        FileBackedTasksManager newTaskManager = FileBackedTasksManager.loadFromFile(FILE);
        Subtask restoreSubtask = newTaskManager.getSubTaskById(subtask.getId());
        assertEquals(restoreSubtask, subtask, "Подзадачи не эдентичны");
        Epic restoreEpic = newTaskManager.getEpicById(epic.getId());
        assertEquals(restoreEpic, epic, "Эпики не эдентичны");
    }

    @DisplayName("Запись и чтение истории просмотра")
    @Test
    void shouldGetFileAndTaskManagerWithNotEmptyBrowsingHistory() {
        taskManager = new FileBackedTasksManager();
        Epic epic = new Epic("The single epic", "This epic has zero subtask",null, null);
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("The first", "Number one", epic.getId(),
                LocalDateTime.of(2023,1,4,12,45), Duration.ofMinutes(45));
        taskManager.addSubtask(subtask);
        taskManager.getEpicById(epic.getId());
        taskManager.getSubTaskById(subtask.getId());
        FileBackedTasksManager newTaskManager = FileBackedTasksManager.loadFromFile(FILE);
        assertNotNull(newTaskManager.getHistory());
        List<Task> browsingHistory = newTaskManager.getHistory();
        assertAll("Сравнение всех полей истории просмотра",
                () -> assertEquals(epic, browsingHistory.get(0)),
                () -> assertEquals(subtask, browsingHistory.get(1))
        );
    }

    @BeforeEach
    void beforeAll() {
        taskManager = new FileBackedTasksManager();
        historyManager = taskManager.getHistoryManager();
        FileBackedTasksManager.loadFromFile(FILE);
        super.initializeTasks();
    }
}