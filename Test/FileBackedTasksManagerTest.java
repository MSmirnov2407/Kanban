import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.FileBackedTasksManager;
import service.Managers;
import org.junit.jupiter.api.Assertions;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {
    @Override
    @BeforeEach
    public void beforeEach() {
        /*удалим файл истории, если он существует, чтобы писАть в чистый файл в тестах*/
        try {
            Files.deleteIfExists(Paths.get("src/file1.csv"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        file = new File("src/file1.csv");
        manager = Managers.getFileBackedTaskManager(file); //создаем объект менеджера

    }

    /**
     * проверка сохраниения в файл состояния менеджера с хорошим тасками, эпиками, сабтасками и историей просмотров
     */
    @Test
    public void saveManagerWithGoodTasksWithHistory() {
        /*добавим в менеджер таск, эпик, сабтаски*/
        Integer taskId = manager.createTask(task1);
        Integer epicId = manager.createEpic(epic1);
        Integer subtaskId1 = manager.createSubtask(subtask1);
        Integer subtaskId2 = manager.createSubtask(subtask2);
        /*добавим все объекты в историю просмотров (и соответствеено в файл)*/
        manager.getTaskById(taskId);
        manager.getEpicById(epicId);
        manager.getSubtaskById(subtaskId1);
        manager.getSubtaskById(subtaskId2);
        /*считаем данные из файла в другой экземпляр менеджера*/
        FileBackedTasksManager taskManagerFromFile = FileBackedTasksManager.loadFromFile(file);
        /*возьмем историю просмотра из нашего менеджера и из менеджера, созданного по файлу*/
        List<Task> managerHistory = manager.getHistory();
        List<Task> managerFromFile = taskManagerFromFile.getHistory();
        /*сравним истрии*/
        assertTrue(manager.getHistory().equals(taskManagerFromFile.getHistory()));
        /*сравним списки тасков*/
        assertTrue(manager.getTasks().equals(taskManagerFromFile.getTasks()));
        /*сравним списки эпиков*/
        assertTrue(manager.getEpics().equals(taskManagerFromFile.getEpics()));
        /*сравним списки сабтасков*/
        assertTrue(manager.getSubtasks().equals(taskManagerFromFile.getSubtasks()));

    }

    /**
     * проверка сохраниения в файл состояния менеджера с хорошим тасками, эпиками, сабтасками но без истории
     */
    @Test
    public void saveManagerWithGoodTasksWithoutHistory() {
        /*добавим в менеджер таск, эпик, сабтаски. История просмотров при это остается пустой*/
        Integer taskId = manager.createTask(task1);
        Integer epicId = manager.createEpic(epic1);
        Integer subtaskId1 = manager.createSubtask(subtask1);
        Integer subtaskId2 = manager.createSubtask(subtask2);

        /*считаем данные из файла в другой экземпляр менеджера*/
        FileBackedTasksManager taskManagerFromFile = FileBackedTasksManager.loadFromFile(file);
        /*возьмем историю просмотра из нашего менеджера и из менеджера, созданного по файлу*/
        List<Task> managerHistory = manager.getHistory();
        List<Task> managerFromFile = taskManagerFromFile.getHistory();
        /*сравним истрии. Истории д.б.пустыми*/
        assertTrue(manager.getHistory().equals(taskManagerFromFile.getHistory()));
        assertTrue(taskManagerFromFile.getHistory().isEmpty());
        /*сравним списки тасков*/
        assertTrue(manager.getTasks().equals(taskManagerFromFile.getTasks()));
        /*сравним списки эпиков*/
        assertTrue(manager.getEpics().equals(taskManagerFromFile.getEpics()));
        /*сравним списки сабтасков*/
        assertTrue(manager.getSubtasks().equals(taskManagerFromFile.getSubtasks()));
    }

    /**
     * проверка сохраниения в файл состояния менеджера с эпиками, сабтасками и историей просмотров. Без тасков
     */
    @Test
    public void saveManagerWithoutTasksWithHistory() {
        /*добавим в менеджер эпик, сабтаски*/
        Integer epicId = manager.createEpic(epic1);
        Integer subtaskId1 = manager.createSubtask(subtask1);
        Integer subtaskId2 = manager.createSubtask(subtask2);
        /*добавим все объекты в историю просмотров (и соответствеено в файл)*/
        manager.getEpicById(epicId);
        manager.getSubtaskById(subtaskId1);
        manager.getSubtaskById(subtaskId2);
        /*считаем данные из файла в другой экземпляр менеджера*/
        FileBackedTasksManager taskManagerFromFile = FileBackedTasksManager.loadFromFile(file);
        /*возьмем историю просмотра из нашего менеджера и из менеджера, созданного по файлу*/
        List<Task> managerHistory = manager.getHistory();
        List<Task> managerFromFile = taskManagerFromFile.getHistory();
        /*сравним истрии*/
        assertTrue(manager.getHistory().equals(taskManagerFromFile.getHistory()));
        /*сравним списки тасков*/
        assertTrue(manager.getTasks().equals(taskManagerFromFile.getTasks()));
        /*сравним списки эпиков*/
        assertTrue(manager.getEpics().equals(taskManagerFromFile.getEpics()));
        /*сравним списки сабтасков*/
        assertTrue(manager.getSubtasks().equals(taskManagerFromFile.getSubtasks()));
    }

    /**
     * проверка сохраниения в файл состояния менеджера с тасками, эпиками, но без сабтасков. С историей просмотров
     */
    @Test
    public void saveManagerWithoutSubtasksWithHistory() {
        /*добавим в менеджер таск, эпик, сабтаски*/
        Integer taskId = manager.createTask(task1);
        Integer epicId = manager.createEpic(epic1);
        /*добавим все объекты в историю просмотров (и соответствеено в файл)*/
        manager.getTaskById(taskId);
        manager.getEpicById(epicId);
        /*считаем данные из файла в другой экземпляр менеджера*/
        FileBackedTasksManager taskManagerFromFile = FileBackedTasksManager.loadFromFile(file);
        /*возьмем историю просмотра из нашего менеджера и из менеджера, созданного по файлу*/
        List<Task> managerHistory = manager.getHistory();
        List<Task> managerFromFile = taskManagerFromFile.getHistory();
        /*сравним истрии*/
        assertTrue(manager.getHistory().equals(taskManagerFromFile.getHistory()));
        /*сравним списки тасков*/
        assertTrue(manager.getTasks().equals(taskManagerFromFile.getTasks()));
        /*сравним списки эпиков*/
        assertTrue(manager.getEpics().equals(taskManagerFromFile.getEpics()));
        /*сравним списки сабтасков*/
        assertTrue(manager.getSubtasks().equals(taskManagerFromFile.getSubtasks()));
    }
}
