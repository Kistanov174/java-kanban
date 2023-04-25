package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected  HistoryManager historyManager;
    protected Task task1;
    protected Task task2;
    protected Task task3;

    protected Epic epic1;
    protected Subtask subtask1;
    protected Subtask subtask2;
    protected Subtask subtask3;
    protected Subtask subtask4;

    @Test
    public void addNewTask() {
        final int taskId = task1.getId();
        final Task savedTask = taskManager.getTaskById(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task1, savedTask, "Задачи не совпадают.");
        final List<Task> tasks = taskManager.getAllTasks();
        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(2, tasks.size(), "Неверное количество задач.");
        assertEquals(task1, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    public void excludeCrossingTasks() {
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

    @Test
    public void workWithPrioritisedTasksList() {
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

    @Test
    public void workWithEmptyTasksList() {
        Integer id = task1.getId();
        taskManager.deleteAllTasks();
        List<Task> tasks = taskManager.getAllTasks();
        assertTrue(tasks.isEmpty(), "Список задач не пустой");
        NoSuchElementException exGet = assertThrows(NoSuchElementException.class, () -> taskManager.getTaskById(id));
        assertEquals("Такой задачи нет", exGet.getMessage());
        NoSuchElementException exDel = assertThrows(NoSuchElementException.class, () -> taskManager.deleteTaskById(id));
        assertEquals("Такой задачи нет", exDel.getMessage());
    }

    @Test
    public void addNewSubtask() {
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

    @Test
    public void deleteTask() {
        final int taskId = task1.getId();
        taskManager.deleteTaskById(taskId);
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> taskManager.getTaskById(taskId));
        assertEquals("Такой задачи нет", ex.getMessage());
    }

    @Test
    public void deleteAllTasks() {
        final int taskId = task1.getId();
        taskManager.deleteAllTasks();
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> taskManager.getTaskById(taskId));
        assertEquals("Такой задачи нет", ex.getMessage());
        boolean isEmptyTasks = taskManager.getAllTasks().isEmpty();
        assertTrue(isEmptyTasks, "Список задач не пустой");
    }

    @Test
    public void deleteSubtask() {
        final int subtaskId = subtask1.getId();
        taskManager.deleteSubtaskById(subtaskId);
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
               () -> taskManager.getSubTaskById(subtaskId));
        assertEquals("Такой подзадачи нет", ex.getMessage());
    }

    @Test
    public void deleteAllSubtasks() {
        final int subtaskId = subtask2.getId();
        taskManager.deleteAllSubTasks();
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> taskManager.getSubTaskById(subtaskId));
        assertEquals("Такой подзадачи нет", ex.getMessage());
        boolean isEmptyTasks = taskManager.getAllSubtasks().isEmpty();
        assertTrue(isEmptyTasks, "Список подзадач не пустой");
    }

    @Test
    public void deleteEpic() {
        final int id = epic1.getId();
        taskManager.deleteEpicById(id);
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> taskManager.getEpicById(id));
        assertEquals("Такого эпика нет", ex.getMessage());
    }

    @Test
    public void deleteAllEpics() {
        final int id = epic1.getId();
        taskManager.deleteAllEpics();
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> taskManager.getEpicById(id));
        assertEquals("Такого эпика нет", ex.getMessage());
        boolean isEmptyTasks = taskManager.getAllEpics().isEmpty();
        assertTrue(isEmptyTasks, "Список эпиков не пустой");
    }

    @Test
    public void updateTask() {
        task1.setStatus(Status.DONE);
        taskManager.updateTask(task1);
        assertEquals(Status.DONE, task1.getStatus(), "Статусы не совпадают");
    }

    @Test
    public void addNewEpic() {
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

    @Test
    public void workWithEmptyEpicsList() {
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

    @Test
    public void workWithEmptyListSubtasks() {
        Integer id = subtask1.getId();
        taskManager.deleteAllSubTasks();
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        assertTrue(subtasks.isEmpty(), "Список подзадач не пустой");
        NoSuchElementException exGet = assertThrows(NoSuchElementException.class, () -> taskManager.getSubTaskById(id));
        assertEquals("Такой подзадачи нет", exGet.getMessage());
        NoSuchElementException exDel = assertThrows(NoSuchElementException.class, () -> taskManager.deleteSubtaskById(id));
        assertEquals("Такой подзадачи нет", exDel.getMessage());
    }

    @Test
    public void shouldGetStatusNewForEmptySubtaskList() {
        Epic epic = new Epic("The single epic", "This epic has zero subtask",
                null, null);
        taskManager.addEpic(epic);
        Status epicStatus = epic.getStatus();
        assertEquals(Status.NEW, epicStatus);
    }

    @Test
    public void shouldGetStatusNewIfAllSubtasksHaveStatusNew() {
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

    @Test
    public void shouldGetStatusDoneIfAllSubtasksHaveStatusDone() {
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

    @Test
    public void shouldGetStatusInProgressIfSubtasksHaveStatusNewAndDone() {
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

    @Test
    public void shouldGetStatusInProgressIfSubtasksHaveStatusInProgress() {
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

    @Test
    public void workWithBrowsingHistory() {

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
        assertEquals(task1, browsingHistory.get(0), "Начадл истории не удаляется");
        historyManager.remove(subtask3.getId());
        browsingHistory = taskManager.getHistory();
        assertEquals(subtask2, browsingHistory.get(2), "Середина истории не удаляется");
        historyManager.remove(subtask2.getId());
        browsingHistory = taskManager.getHistory();
        assertEquals(subtask1, browsingHistory.get(browsingHistory.size() - 1), "Конец истории не удаляется");
    }
}