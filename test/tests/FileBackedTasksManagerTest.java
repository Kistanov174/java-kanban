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
    File file = new File("src\\storage.csv");

    @DisplayName("Чтение несуществующего файла")
    @Test
    public void shouldGetExceptionReadingFile() {
        taskManager = new FileBackedTasksManager();
        File file  = new File("src\\NotExistedFile.csv");
        ManagerSaveException ex = assertThrows(ManagerSaveException.class,
                () -> FileBackedTasksManager.loadFromFile(file));
        assertEquals("Ошибка считывания в service.FileBackedTasksManager.loadFromFile()", ex.getMessage());
    }

    @DisplayName("Чтение пустого файла")
    @Test
    public void shouldGetEmptyTaskManager() {
        taskManager = new FileBackedTasksManager();
        assertNotNull(FileBackedTasksManager.loadFromFile(file), "Менеджер не создается");
        assertTrue(taskManager.getAllTasks().isEmpty(), "Список задач не пуст");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Список подзадач не пуст");
        assertTrue(taskManager.getAllEpics().isEmpty(), "Список эпиков не пуст");
        assertTrue(taskManager.getHistory().isEmpty(), "История просмотра не пуста");
    }

    @DisplayName("Чтение файла с пустой историей просмотра")
    @Test
    public void shouldGetTaskManagerWithEmptyBrowsingHistory() {
        taskManager = new FileBackedTasksManager();
        Task task = new Task("Test addNewTask", "Test addNewTask description",
                LocalDateTime.of(2023,4,7,12,35),
                Duration.ofMinutes(120));
        taskManager.addTask(task);
        assertNotNull(FileBackedTasksManager.loadFromFile(file), "Менеджер не создается");
        assertTrue(taskManager.getHistory().isEmpty(), "История просмотра не пуста");
        assertFalse(taskManager.getAllTasks().isEmpty(), "Список задач пуст");
        assertEquals("[]", taskManager.getHistory().toString());
    }

    @DisplayName("Запись и чтение эпика без подзадач")
    @Test
    public void shouldGetFileAndTaskManagerWithEmptyEpic() {
        Epic epic = new Epic("The single epic", "This epic has zero subtask",null, null);
        taskManager = new FileBackedTasksManager();
        taskManager.addEpic(epic);
        FileBackedTasksManager newTaskManager = FileBackedTasksManager.loadFromFile(file);
        Epic restoreEpic = newTaskManager.getEpicById(epic.getId());
        assertEquals(restoreEpic, epic, "Эпики не эдентичны");
    }

    @DisplayName("Запись и чтение задачи")
    @Test
    public void shouldGetFileAndTaskManagerWithTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description",
        LocalDateTime.of(2023,4,7,12,35), Duration.ofMinutes(120));
        taskManager = new FileBackedTasksManager();
        taskManager.addTask(task);
        FileBackedTasksManager newTaskManager = FileBackedTasksManager.loadFromFile(file);
        Task restoreTask = newTaskManager.getTaskById(task.getId());
        assertEquals(restoreTask, task, "Задачи не эдентичны");
    }

    @DisplayName("Запись и чтение эпика с подзадачами")
    @Test
    public void shouldGetFileAndTaskManagerWithNotEmptyEpic() {
        taskManager = new FileBackedTasksManager();
        Epic epic = new Epic("The single epic", "This epic has zero subtask",null, null);
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("The first", "Number one", epic.getId(),
                LocalDateTime.of(2023,1,4,12,45), Duration.ofMinutes(45));
        taskManager.addSubtask(subtask);
        FileBackedTasksManager newTaskManager = FileBackedTasksManager.loadFromFile(file);
        Subtask restoreSubtask = newTaskManager.getSubTaskById(subtask.getId());
        assertEquals(restoreSubtask, subtask, "Подзадачи не эдентичны");

        Epic restoreEpic = newTaskManager.getEpicById(epic.getId());
        assertEquals(restoreEpic, epic, "Эпики не эдентичны");
    }

    @DisplayName("Запись и чтение истории просмотра")
    @Test
    public void shouldGetFileAndTaskManagerWithNotEmptyBrowsingHistory() {
        taskManager = new FileBackedTasksManager();
        Epic epic = new Epic("The single epic", "This epic has zero subtask",null, null);
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("The first", "Number one", epic.getId(),
                LocalDateTime.of(2023,1,4,12,45), Duration.ofMinutes(45));
        taskManager.addSubtask(subtask);
        taskManager.getEpicById(epic.getId());
        taskManager.getSubTaskById(subtask.getId());
        FileBackedTasksManager newTaskManager = FileBackedTasksManager.loadFromFile(file);
        assertNotNull(newTaskManager.getHistory());
        List<Task> browsingHistory = newTaskManager.getHistory();
        assertAll("Сравнение всех полей пустого эпика",
                () -> assertEquals(epic, browsingHistory.get(0)),
                () -> assertEquals(subtask, browsingHistory.get(1))
        );
    }

    @BeforeEach
    public void beforeAll() {
        taskManager = new FileBackedTasksManager();
        historyManager = taskManager.getHistoryManager();
        File file = new File("src\\storage.csv");
        FileBackedTasksManager.loadFromFile(file);

        task1 = new Task("Test addNewTask", "Test addNewTask description",
                LocalDateTime.of(2023,4,7,12,35),
                Duration.ofMinutes(120));
        taskManager.addTask(task1);

        task2 = new Task("Test addNewTask2", "Test addNewTask2 description",
                LocalDateTime.of(2023,1,23,19,10),
                Duration.ofMinutes(400));
        taskManager.addTask(task2);

        task3 = new Task("Test addNewTask2", "Test addNewTask2 description",
                LocalDateTime.of(2023,4,7,13,35),
                Duration.ofMinutes(120));

        epic1 = new Epic("The triple epic", "This epic has three subtasks",
                null, null);
        taskManager.addEpic(epic1);
        subtask1 = new Subtask("The first", "Number one", epic1.getId(),
                LocalDateTime.of(2023,1,4,12,45), Duration.ofMinutes(45));
        taskManager.addSubtask(subtask1);
        subtask2 = new Subtask("The second", "Number two", epic1.getId(),
                LocalDateTime.of(2023,2,15,21,0), Duration.ofMinutes(150));
        taskManager.addSubtask(subtask2);
        subtask3 = new Subtask("The third", "Number three", epic1.getId(),
                LocalDateTime.of(2023,3,7,15,35), Duration.ofMinutes(115));
        taskManager.addSubtask(subtask3);
        subtask4 = new Subtask("The second", "Number two", epic1.getId(),
                LocalDateTime.of(2023,2,15,22,0), Duration.ofMinutes(150));
    }
}