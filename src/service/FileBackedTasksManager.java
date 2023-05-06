package service;

import exception.ManagerSaveException;
import model.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FileBackedTasksManager extends InMemoryTaskManager {
    public FileBackedTasksManager() {
        super();
    }
    private static final String FILE_NAME = "src\\storage.csv";
    private final static int FIRST_LINE_WITH_VALUE = 1;
    private final static int TASK_QUANTITY_ELEMENTS = 8;
    private final static int ID = 0;
    private final static int TYPE = 1;
    private final static int NAME = 2;
    private final static int STATUS = 3;
    private final static int DESCRIPTION = 4;
    private final static int START_TIME = 5;
    private final static int DURATION = 6;
    private final static int END_TIME = 7;
    private final static int EPIC_ID = 8;

    @Override
    public boolean checkUncrossingTasks(Task newTask) {
        return super.checkUncrossingTasks(newTask);
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return super.getPrioritizedTasks();
    }

    @Override
    public HistoryManager getHistoryManager() {
        return super.getHistoryManager();
    }

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

    protected void save() {
        try (FileWriter fileWriter = new FileWriter(FILE_NAME, StandardCharsets.UTF_8)) {
            fileWriter.write("id,type,name,status,description,startTime,duration,endTime,epic" + "\n");
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
            if (!historyManager.getHistory().isEmpty()) {
                fileWriter.write(historyToString(historyManager));
            } else {
                fileWriter.write(" ");
            }
        }
        catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в " + getClass().getName() + ".save()");
        }
    }

    public static FileBackedTasksManager loadFromFile(File file) {
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
        for (int i = FIRST_LINE_WITH_VALUE; i < lines.length; i++) {
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
        LocalDateTime startTime = null;
        Duration duration = null;
        LocalDateTime endTime = null;
        String[] taskElement = taskText.split(",");
        if (taskElement.length >= TASK_QUANTITY_ELEMENTS) {
            Integer id = Integer.parseInt(taskElement[ID]);
            String name = taskElement[NAME];
            String description = taskElement[DESCRIPTION];
            Type type = Type.valueOf(taskElement[TYPE]);
            Status status = Status.valueOf(taskElement[STATUS]);
            if (!taskElement[START_TIME].equals("null")) {
                startTime = LocalDateTime.parse(taskElement[START_TIME]);
            }
            if (!taskElement[DURATION].equals("null")) {
                duration = Duration.parse(taskElement[DURATION]);
            }
            if (!taskElement[END_TIME].equals("null")) {
                endTime = LocalDateTime.parse(taskElement[END_TIME]);
            }
            switch (type) {
                case TASK:
                    task = new Task(id, type, name, status, description, startTime, duration, endTime);
                    break;
                case EPIC:
                    task = new Epic(id, type, name, status, description, startTime, duration, endTime);
                    break;
                case SUBTASK:
                    Integer epicId = Integer.parseInt(taskElement[EPIC_ID]);
                    task = new Subtask(id, type, name, status, description, startTime, duration, endTime, epicId);
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
        if (!history.isBlank()) {
            List<Integer> historyId = new ArrayList<>();
            for(String id : history.split(",")) {
                historyId.add(Integer.parseInt(id));
            }
            return historyId;
        }
        return Collections.emptyList();
    }
}
