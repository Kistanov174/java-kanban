import model.*;
import service.*;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        Task first = new Task("The first task", "This task is number one");
        Task second = new Task("The second task", "This task is number two");
        taskManager.addTask(first);
        taskManager.addTask(second);

        Epic threeItems = new Epic("The triple epic", "This epic has three subtasks");
        taskManager.addEpic(threeItems);
        Subtask numberOne = new Subtask("The first", "Number one", threeItems.getId());
        taskManager.addSubtask(numberOne);
        Subtask numberTwo = new Subtask("The second", "Number two", threeItems.getId());
        taskManager.addSubtask(numberTwo);
        Subtask numberThree = new Subtask("The third", "Number three", threeItems.getId());
        taskManager.addSubtask(numberThree);

        Epic zeroItem = new Epic("The single epic", "This epic has zero subtask");
        taskManager.addEpic(zeroItem);

        first.setStatus(Status.DONE);
        taskManager.updateTask(first);

        numberOne.setStatus(Status.DONE);
        taskManager.updateSubtask(numberOne);

        taskManager.getTaskById(1);
        printIdOfWatchedTasks(taskManager);
        taskManager.getTaskById(2);
        printIdOfWatchedTasks(taskManager);
        taskManager.getEpicById(3);
        printIdOfWatchedTasks(taskManager);
        taskManager.getSubTaskById(4);
        printIdOfWatchedTasks(taskManager);
        taskManager.getSubTaskById(4);
        printIdOfWatchedTasks(taskManager);
        taskManager.getSubTaskById(5);
        printIdOfWatchedTasks(taskManager);
        taskManager.getTaskById(1);
        printIdOfWatchedTasks(taskManager);
        taskManager.getTaskById(1);
        taskManager.getTaskById(1);
        taskManager.getTaskById(1);
        printIdOfWatchedTasks(taskManager);
        taskManager.getTaskById(2);
        printIdOfWatchedTasks(taskManager);
        taskManager.getTaskById(1);
        printIdOfWatchedTasks(taskManager);

        taskManager.deleteTaskById(2);
        printIdOfWatchedTasks(taskManager);

        taskManager.deleteEpicById(3);
        printIdOfWatchedTasks(taskManager);
    }
    private static void printIdOfWatchedTasks(TaskManager taskManager) {
        for (Task task : taskManager.getHistory()) {
            System.out.print(task.getId());
        }
        System.out.println();
    }
}