package service;
import exception.ManagerSaveException;
import model.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private static final String FILE_NAME = "src\\storage.csv";

    @Override
    public List<Task> getAllTasks() {
        return super.getAllTasks();
    }

    @Override
    public List<Epic> getAllEpics() {
        return super.getAllEpics();
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return super.getAllSubtasks();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubTaskById(Integer id) {
        Subtask subtask = super.getSubTaskById(id);
        save();
        return subtask;
    }

    @Override
    public Integer addTask(Task task) {
        Integer id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public Integer addEpic(Epic epic) {
        Integer id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer addSubtask(Subtask subtask) {
        Integer id = super.addSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void deleteTaskById(Integer id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(Integer id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(Integer epicId) {
        return super.getSubtasksOfEpic(epicId);
    }

    @Override
    public List<Task> getHistory() {
        return super.getHistory();
    }

    private void save() {
        try (FileWriter fileWriter = new FileWriter(FILE_NAME, StandardCharsets.UTF_8)) {
            fileWriter.write("id,type,name,status,description, epic" + "\n");
            for (Task task : getAllTasks()) {
                fileWriter.write(task.toString() + "\n");
            }
            for (Epic epic : getAllEpics()) {
                fileWriter.write(epic.toString() + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                fileWriter.write(subtask.toString() + "\n");
            }
            fileWriter.write("\n");
            fileWriter.write(historyToString(historyManager));
        }
        catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в " + getClass().getName() + ".save()");
        }
    }

    private static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager();
        try {
            Path path = Path.of(file.toString());
            String contentFile = Files.readString(path, StandardCharsets.UTF_8);
            String[] lines = contentFile.split("\n");
            restoreTasks(fileBackedTasksManager, lines);
            restoreHistory(fileBackedTasksManager, lines[lines.length - 1]);
        }
        catch (IOException e) {
            throw new ManagerSaveException("Ошибка считывания в " +
                    FileBackedTasksManager.class.getName() + ".loadFromFile()");
        }
        return fileBackedTasksManager;
    }

    private static void restoreTasks(FileBackedTasksManager fileBackedTasksManager, String[] lines) {
        for (int i = 1; i < lines.length; i++) {
            if (!lines[i].isEmpty()) {
                Task task = fileBackedTasksManager.fromString(lines[i]);
                if (task.getType().equals(Type.TASK)) {
                    fileBackedTasksManager.tasks.put(task.getId(), task);
                }
                if (task.getType().equals(Type.EPIC)) {
                    fileBackedTasksManager.epics.put(task.getId(), (Epic)task);
                }
                if (task.getType().equals(Type.SUBTASK)) {
                    fileBackedTasksManager.subtasks.put(task.getId(), (Subtask)task);
                }
            } else {
                break;
            }
        }
    }

    private static void restoreHistory(FileBackedTasksManager fileBackedTasksManager, String history) {
        for (int id : historyFromString(history)) {
            if (fileBackedTasksManager.tasks.containsKey(id)) {
                fileBackedTasksManager.historyManager.add(fileBackedTasksManager.tasks.get(id));
            }
            if (fileBackedTasksManager.epics.containsKey(id)) {
                fileBackedTasksManager.historyManager.add(fileBackedTasksManager.epics.get(id));
            }
            if (fileBackedTasksManager.subtasks.containsKey(id)) {
                fileBackedTasksManager.historyManager.add(fileBackedTasksManager.subtasks.get(id));
            }
        }
    }

    private Task fromString(String taskText) {
        Task task;
        String[] taskElement = taskText.split(",");
        if (taskElement.length >= 5) {
            Integer id = Integer.parseInt(taskElement[0]);
            String name = taskElement[2];
            String description = taskElement[4];
            Type type = Type.valueOf(taskElement[1]);
            Status status = Status.valueOf(taskElement[3]);
            switch (type) {
                case TASK:
                    task = new Task(id, type, name, status, description);
                    break;
                case EPIC:
                    task = new Epic(id, type, name, status, description);
                    break;
                case SUBTASK:
                    Integer epicId = Integer.parseInt(taskElement[5]);
                    task = new Subtask(id, type, name, status, description, epicId);
                    break;
                default:
                    throw new ManagerSaveException("Неопознаный объект в " + getClass().getName() + ".fromString");
            }
        } else {
            throw new ManagerSaveException("Не валидные данные в " + getClass().getName() + ".fromString()");
        }
        return task;
    }

    private static String historyToString(HistoryManager manager) {
        StringBuilder history = new StringBuilder();
        for (Task task : manager.getHistory()) {
            if(history.length() == 0) {
                history.append(task.getId());
            } else {
                history.append(",").append(task.getId());
            }
        }
        return history.toString();
    }

    private static List<Integer> historyFromString(String history) {
        List<Integer> historyId = new ArrayList<>();
        for(String id : history.split(",")) {
            historyId.add(Integer.parseInt(id));
        }
        return historyId;
    }

    public static void main(String[] args) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager();
        Task first = new Task("The first task", "This task is number one");
        Task second = new Task("The second task", "This task is number two");
        fileBackedTasksManager.addTask(first);
        fileBackedTasksManager.addTask(second);

        Epic threeItems = new Epic("The triple epic", "This epic has three subtasks");
        fileBackedTasksManager.addEpic(threeItems);
        Subtask numberOne = new Subtask("The first", "Number one", threeItems.getId());
        fileBackedTasksManager.addSubtask(numberOne);
        Subtask numberTwo = new Subtask("The second", "Number two", threeItems.getId());
        fileBackedTasksManager.addSubtask(numberTwo);
        Subtask numberThree = new Subtask("The third", "Number three", threeItems.getId());
        fileBackedTasksManager.addSubtask(numberThree);

        Epic zeroItem = new Epic("The single epic", "This epic has zero subtask");
        fileBackedTasksManager.addEpic(zeroItem);

        first.setStatus(Status.DONE);
        fileBackedTasksManager.updateTask(first);

        numberOne.setStatus(Status.DONE);
        fileBackedTasksManager.updateSubtask(numberOne);

        fileBackedTasksManager.getTaskById(1);
        fileBackedTasksManager.getSubTaskById(4);
        fileBackedTasksManager.getTaskById(2);
        fileBackedTasksManager.getEpicById(3);

        File file = new File(FILE_NAME);
        fileBackedTasksManager = loadFromFile(file);
        System.out.println(fileBackedTasksManager.getAllSubtasks());
        printIdOfWatchedTasks(fileBackedTasksManager);
    }

    private static void printIdOfWatchedTasks(FileBackedTasksManager fileBackedTasksManager) {
        for (Task task : fileBackedTasksManager.getHistory()) {
            System.out.print(task.getId());
        }
        System.out.println();
    }
}
