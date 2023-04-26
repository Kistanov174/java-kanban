package tests;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import service.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
        historyManager = taskManager.getHistoryManager();
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