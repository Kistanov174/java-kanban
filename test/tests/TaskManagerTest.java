package tests;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.HistoryManager;
import service.TaskManager;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected HistoryManager historyManager;
    protected Task task1;
    protected Task task2;
    protected Task task3;
    protected Epic epic1;
    protected Subtask subtask1;
    protected Subtask subtask2;
    protected Subtask subtask3;
    protected Subtask subtask4;

    @DisplayName("Проверка методов менеджера по работе с задачами")
    @Test
    void shouldMakeNewTask() {
        final int taskId = task1.getId();
        final Task savedTask = taskManager.getTaskById(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task1, savedTask, "Задачи не совпадают.");
        final List<Task> tasks = taskManager.getAllTasks();
        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(2, tasks.size(), "Неверное количество задач.");
        assertEquals(task1, tasks.get(0), "Задачи не совпадают.");
    }

    @DisplayName("Проверка исключения пересекающихся задач")
    @Test
    void shouldGetExceptionAboutWrongParameters() {
        LocalDateTime newDateTime = LocalDateTime.of(2023,4,7,13,0);

        Subtask newSubtask1 = new Subtask(subtask1.getId(), subtask1.getType(), subtask1.getName(), subtask1.getStatus(),
                subtask1.getDescription(), newDateTime, subtask1.getDuration(), subtask1.getEndTime(), subtask1.getEpicId());
        IllegalArgumentException exChangedSubtask = assertThrows(IllegalArgumentException.class,
               () -> taskManager.updateSubtask(newSubtask1));
        assertEquals("Некорректное значение параметров подзадачи", exChangedSubtask.getMessage());

        Task newTask2 = new Task(task2.getId(), task2.getType(), task2.getName(), task2.getStatus(),
                task2.getDescription(), newDateTime, task2.getDuration(), task2.getEndTime());
        IllegalArgumentException exChangedTask = assertThrows(IllegalArgumentException.class,
                () -> taskManager.updateTask(newTask2));
        assertEquals("Некорректное значение параметров задачи", exChangedTask.getMessage());

        IllegalArgumentException exNewTask = assertThrows(IllegalArgumentException.class,
                () -> taskManager.addTask(task3));
        assertEquals("Некорректное значение параметров задачи", exNewTask.getMessage());

        IllegalArgumentException exNewSubtask = assertThrows(IllegalArgumentException.class,
                () -> taskManager.addSubtask(subtask4));
        assertEquals("Некорректное значение параметров подзадачи", exNewSubtask.getMessage());
    }

    @DisplayName("Проверка создания приоритетного списка")
    @Test
    void shouldGetPrioritizedListOfTasks() {
        final Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertNotNull(prioritizedTasks, "Приоритетный список не возвращается");
        assertEquals(5, prioritizedTasks.size());
        assertEquals(subtask1, prioritizedTasks.toArray()[0], "Неверные приоритеты");
        LocalDateTime newDateTime = LocalDateTime.of(2023,1,1,1,45);
        Task newTask1 = new Task(task1.getId(), task1.getType(), task1.getName(), task1.getStatus(),
                task1.getDescription(), newDateTime, task1.getDuration(), null);
        taskManager.updateTask(newTask1);
        final Set<Task> newPrioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(task1, newPrioritizedTasks.toArray()[0], "Приоритеты не изменяются при обновлении задач");
        assertEquals(5, newPrioritizedTasks.size(), "Старая версия задачи не удаляется");
    }

    @DisplayName("Проверка работы менеджера с пустым списком задач")
    @Test
    void shouldGetExceptionToGetAndToDeleteNotExistTask() {
        Integer id = task1.getId();
        taskManager.deleteAllTasks();
        List<Task> tasks = taskManager.getAllTasks();
        assertTrue(tasks.isEmpty(), "Список задач не пустой");
        NoSuchElementException exGet = assertThrows(NoSuchElementException.class, () -> taskManager.getTaskById(id));
        assertEquals("Такой задачи нет", exGet.getMessage());
        NoSuchElementException exDel = assertThrows(NoSuchElementException.class, () -> taskManager.deleteTaskById(id));
        assertEquals("Такой задачи нет", exDel.getMessage());
    }

    @DisplayName("Проверка методов менеджера по работе с задачами")
    @Test
    void shouldMakeNewSubtask() {
        final int subtaskId = subtask1.getId();
        final Subtask savedSubtask = taskManager.getSubTaskById(subtaskId);
        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask1, savedSubtask, "Подзадача не совпадают.");
        final List<Subtask> subtasks = taskManager.getAllSubtasks();
        final Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertNotNull(subtasks, "Подзадачи на возвращаются.");
        assertNotNull(prioritizedTasks, "Приоритетный список не возвращается");
        assertEquals(3, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask1, subtasks.get(0), "Подзадачи не совпадают.");
    }

    @DisplayName("Проверка метода удаления задачи")
    @Test
    void shouldGetExceptionToGetTaskAfterDelete() {
        final int taskId = task1.getId();
        taskManager.deleteTaskById(taskId);
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> taskManager.getTaskById(taskId));
        assertEquals("Такой задачи нет", ex.getMessage());
    }

    @DisplayName("Проверка метода удаления всех задач")
    @Test
    void shouldGetEmptyListOfTasksAfterAllDelete() {
        final int taskId = task1.getId();
        taskManager.deleteAllTasks();
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> taskManager.getTaskById(taskId));
        assertEquals("Такой задачи нет", ex.getMessage());
        boolean isEmptyTasks = taskManager.getAllTasks().isEmpty();
        assertTrue(isEmptyTasks, "Список задач не пустой");
    }

    @DisplayName("Проверка метода удаления подзадачи")
    @Test
    void shouldGetExceptionToGetSubtaskAfterDelete() {
        final int subtaskId = subtask1.getId();
        taskManager.deleteSubtaskById(subtaskId);
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
               () -> taskManager.getSubTaskById(subtaskId));
        assertEquals("Такой подзадачи нет", ex.getMessage());
    }

    @DisplayName("Проверка метода удаления всех подзадач")
    @Test
    void shouldGetEmptyListOfSubtasksAfterAllDelete() {
        final int subtaskId = subtask2.getId();
        taskManager.deleteAllSubTasks();
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> taskManager.getSubTaskById(subtaskId));
        assertEquals("Такой подзадачи нет", ex.getMessage());
        boolean isEmptyTasks = taskManager.getAllSubtasks().isEmpty();
        assertTrue(isEmptyTasks, "Список подзадач не пустой");
    }

    @DisplayName("Проверка метода удаления эпика")
    @Test
    void shouldGetExceptionToGetEpicAfterDelete() {
        final int id = epic1.getId();
        taskManager.deleteEpicById(id);
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> taskManager.getEpicById(id));
        assertEquals("Такого эпика нет", ex.getMessage());
    }

    @DisplayName("Проверка метода удаления всех эпиков")
    @Test
    void shouldGetEmptyListOfEpicsAfterAllDelete() {
        final int id = epic1.getId();
        taskManager.deleteAllEpics();
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> taskManager.getEpicById(id));
        assertEquals("Такого эпика нет", ex.getMessage());
        boolean isEmptyTasks = taskManager.getAllEpics().isEmpty();
        assertTrue(isEmptyTasks, "Список эпиков не пустой");
    }

    @DisplayName("Проверка метода обновления задачи")
    @Test
    void shouldGetUpdatedTask() {
        task1.setStatus(Status.DONE);
        taskManager.updateTask(task1);
        assertEquals(Status.DONE, task1.getStatus(), "Статусы не совпадают");
    }

    @DisplayName("Проверка методов менеджера по работе с эпиками")
    @Test
    void shouldMakeNewEpic() {
        final int epicId = epic1.getId();
        final Task savedEpic = taskManager.getEpicById(epicId);
        final LocalDateTime startTimeEpic1 = epic1.getStartTime();
        final LocalDateTime endTimeEpic1 = epic1.getEndTime();
        final Duration durationEpic1 = epic1.getDuration();
        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic1, savedEpic, "Эпики не совпадают.");
        final List<Epic> epics = taskManager.getAllEpics();
        assertNotNull(epics, "Эпики на возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic1, epics.get(0), "Эпики не совпадают.");
        assertEquals(LocalDateTime.of(2023,1,4,12,45), startTimeEpic1);
        assertEquals(LocalDateTime.of(2023,3,7,17,30), endTimeEpic1,
                "Время окончания не совпадает");
        assertEquals(Duration.ofMinutes(310), durationEpic1);
    }

    @DisplayName("Проверка работы менеджера с пустым списком эпиков")
    @Test
    void shouldGetExceptionToGetAndToDeleteNotExistEpic() {
        Integer id = epic1.getId();
        List<Subtask> subtasksOfEpic = taskManager.getSubtasksOfEpic(id);
        taskManager.deleteAllEpics();
        List<Epic> epics = taskManager.getAllEpics();
        assertTrue(epics.isEmpty(), "Список задач не пустой");
        NoSuchElementException exGet = assertThrows(NoSuchElementException.class, () -> taskManager.getEpicById(id));
        assertEquals("Такого эпика нет", exGet.getMessage());
        NoSuchElementException exDel = assertThrows(NoSuchElementException.class, () -> taskManager.deleteEpicById(id));
        assertEquals("Такого эпика нет", exDel.getMessage());
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        for (Subtask subtask : subtasksOfEpic) {
            assertFalse(subtasks.contains(subtask), "");
        }
    }

    @DisplayName("Проверка работы менеджера с пустым списком подзадач")
    @Test
    void shouldGetExceptionToGetAndToDeleteNotExistSubtask() {
        Integer id = subtask1.getId();
        taskManager.deleteAllSubTasks();
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        assertTrue(subtasks.isEmpty(), "Список подзадач не пустой");
        NoSuchElementException exGet = assertThrows(NoSuchElementException.class, () -> taskManager.getSubTaskById(id));
        assertEquals("Такой подзадачи нет", exGet.getMessage());
        NoSuchElementException exDel = assertThrows(NoSuchElementException.class, () -> taskManager.deleteSubtaskById(id));
        assertEquals("Такой подзадачи нет", exDel.getMessage());
    }

    @DisplayName("Проверка назначения статуса NEW для пустого эпика")
    @Test
    void shouldGetStatusNewForEmptySubtaskList() {
        Epic epic = new Epic("The single epic", "This epic has zero subtask",
                null, null);
        taskManager.addEpic(epic);
        Status epicStatus = epic.getStatus();
        assertEquals(Status.NEW, epicStatus);
    }

    @DisplayName("Проверка статуса NEW для эпика если все подзадачи NEW")
    @Test
    void shouldGetStatusNewIfAllSubtasksHaveStatusNew() {
        Epic epic = new Epic("The triple epic", "This epic has three subtasks",
                null, null);
        taskManager.addEpic(epic);
        Subtask numberOne = new Subtask("The first", "Number one", epic.getId(),
                LocalDateTime.of(2023,1,4,12,45), Duration.ofMinutes(45));
        taskManager.addSubtask(numberOne);
        taskManager.addSubtask(numberOne);
        Subtask numberTwo = new Subtask("The second", "Number two", epic.getId(),
                LocalDateTime.of(2023,2,15,21,0), Duration.ofMinutes(150));
        taskManager.addSubtask(numberTwo);
        Subtask numberThree = new Subtask("The third", "Number three", epic.getId(),
                LocalDateTime.of(2023,3,7,15,35), Duration.ofMinutes(115));
        taskManager.addSubtask(numberThree);
        Status epicStatus = epic.getStatus();
        assertEquals(Status.NEW, epicStatus);
    }

    @DisplayName("Проверка назначения статуса DONE для эпика")
    @Test
    void shouldGetStatusDoneIfAllSubtasksHaveStatusDone() {
        Epic epic = new Epic("The double epic", "This epic has two subtasks",
                null, null);
        taskManager.addEpic(epic);
        Subtask numberOne = new Subtask("The first", "Number one", epic.getId(),
                LocalDateTime.of(2023,1,4,12,45), Duration.ofMinutes(45));
        taskManager.addSubtask(numberOne);
        taskManager.addSubtask(numberOne);
        Subtask numberTwo = new Subtask("The second", "Number two", epic.getId(),
                LocalDateTime.of(2023,2,15,21,0), Duration.ofMinutes(150));
        taskManager.addSubtask(numberTwo);
        numberOne.setStatus(Status.DONE);
        taskManager.updateSubtask(numberOne);
        numberTwo.setStatus(Status.DONE);
        taskManager.updateSubtask(numberTwo);
        Status epicStatus = epic.getStatus();
        assertEquals(Status.DONE, epicStatus);
    }

    @DisplayName("Проверка статуса IN_PROGRESS для эпика при разных статусах подзадач")
    @Test
    void shouldGetStatusInProgressIfSubtasksHaveStatusNewAndDone() {
        Epic epic = new Epic("The double epic", "This epic has two subtasks",
                null, null);
        taskManager.addEpic(epic);
        Subtask numberOne = new Subtask("The first", "Number one", epic.getId(),
                LocalDateTime.of(2023,1,4,12,45), Duration.ofMinutes(45));
        taskManager.addSubtask(numberOne);
        taskManager.addSubtask(numberOne);
        Subtask numberTwo = new Subtask("The second", "Number two", epic.getId(),
                LocalDateTime.of(2023,2,15,21,0), Duration.ofMinutes(150));
        taskManager.addSubtask(numberTwo);
        taskManager.addSubtask(numberTwo);
        numberOne.setStatus(Status.DONE);
        taskManager.updateSubtask(numberOne);
        numberTwo.setStatus(Status.NEW);
        taskManager.updateSubtask(numberTwo);
        Status epicStatus = epic.getStatus();
        assertEquals(Status.IN_PROGRESS, epicStatus);
    }

    @DisplayName("Проверка статуса IN_PROGRESS для эпика если все подзадачи IN_PROGRESS")
    @Test
    void shouldGetStatusInProgressIfSubtasksHaveStatusInProgress() {
        Epic epic = new Epic("The double epic", "This epic has two subtasks",
                null, null);
        taskManager.addEpic(epic);
        Subtask numberOne = new Subtask("The first", "Number one", epic.getId(),
                LocalDateTime.of(2023,1,4,12,45), Duration.ofMinutes(45));
        taskManager.addSubtask(numberOne);
        taskManager.addSubtask(numberOne);
        Subtask numberTwo = new Subtask("The second", "Number two", epic.getId(),
                LocalDateTime.of(2023,2,15,21,0), Duration.ofMinutes(150));
        taskManager.addSubtask(numberTwo);
        numberOne.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(numberOne);
        numberTwo.setStatus(Status.NEW);
        taskManager.updateSubtask(numberTwo);
        Status epicStatus = epic.getStatus();
        assertEquals(Status.IN_PROGRESS, epicStatus);
    }

    @DisplayName("Проверка методов по работе с историей просмотра")
    @Test
    void shouldGetCorrectBrowsingHistory() {

        List<Task> browsingHistory = taskManager.getHistory();
        assertNotNull(browsingHistory, "История не возвращается");

        assertTrue(browsingHistory.isEmpty(), "История изначально не пуста");

        taskManager.getTaskById(task1.getId());
        browsingHistory = taskManager.getHistory();
        assertEquals(task1, browsingHistory.get(0), "История не пишится");

        historyManager.remove(20);
        browsingHistory = taskManager.getHistory();
        assertEquals(1, browsingHistory.size(), "Удаляет не существующий элемент");

        historyManager.remove(task1.getId());
        browsingHistory = taskManager.getHistory();
        assertFalse(browsingHistory.contains(task1), "Элемент истории не удаляется");

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task1.getId());
        browsingHistory = taskManager.getHistory();
        assertEquals(1, browsingHistory.size(), "Происходит дублирование");

        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task1.getId());
        taskManager.getSubTaskById(subtask1.getId());
        taskManager.getSubTaskById(subtask3.getId());
        taskManager.getSubTaskById(subtask2.getId());
        historyManager.remove(task2.getId());
        browsingHistory = taskManager.getHistory();
        assertEquals(task1, browsingHistory.get(0), "Начало истории не удаляется");
        historyManager.remove(subtask3.getId());
        browsingHistory = taskManager.getHistory();
        assertEquals(subtask2, browsingHistory.get(2), "Середина истории не удаляется");
        historyManager.remove(subtask2.getId());
        browsingHistory = taskManager.getHistory();
        assertEquals(subtask1, browsingHistory.get(browsingHistory.size() - 1), "Конец истории не удаляется");
    }

    protected void initializeTasks() {
        task1 = new Task("Test addNewTask", "Test addNewTask description",
                LocalDateTime.of(2023,4,7,12,35),
                Duration.ofMinutes(120));
        taskManager.addTask(task1);

        task2 = new Task("Test addNewTask2", "Test addNewTask2 description",
                LocalDateTime.of(2023,1,23,19,10),
                Duration.ofMinutes(400));
        taskManager.addTask(task2);

        task3 = new Task("Test addNewTask3", "Test addNewTask3 description",
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
        subtask4 = new Subtask("The fourth", "Number fourth", epic1.getId(),
                LocalDateTime.of(2023,2,15,22,0), Duration.ofMinutes(150));
    }
}