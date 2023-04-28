package tests;

import org.junit.jupiter.api.BeforeEach;
import service.InMemoryTaskManager;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
        historyManager = taskManager.getHistoryManager();
        super.initializeTasks();
    }
}