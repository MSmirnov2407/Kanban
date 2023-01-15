import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.HttpTaskManager;
import service.Managers;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTasksManagerTest extends TaskManagerTest<HttpTaskManager> {


    @Override
    @BeforeEach
    public void beforeEach() {

        manager = (HttpTaskManager) Managers.getDefault(); //создаем объект менеджера
        task1 = new Task("Task1", "Task description1", 1);
        task2 = new Task("Task2", "Task description2", 2);
        epic1 = new Epic("Epic1", "Epic description1", 3);
        epic2 = new Epic("Epic2", "Epic description2", 4);
        subtask1 = new Subtask("Subtask1", "Subtask description1", 3, 5);
        subtask2 = new Subtask("Subtask2", "Subtask description2", 3, 6);
    }

    /**
     * проверка сохраниения на сервер состояния менеджера с хорошим тасками, эпиками, сабтасками и историей просмотров
     */
    @Test
    public void saveManagerWithGoodTasksWithHistory() {
        /*добавим в менеджер таск, эпик, сабтаски*/
        Integer taskId = manager.createTask(task1);
        Integer epicId = manager.createEpic(epic1);
        Integer subtaskId1 = manager.createSubtask(subtask1);
        Integer subtaskId2 = manager.createSubtask(subtask2);
        /*добавим все объекты в историю просмотров (и соответствеено на сервер-хранилище)*/
        manager.getTaskById(taskId);
        manager.getEpicById(epicId);
        manager.getSubtaskById(subtaskId1);
        manager.getSubtaskById(subtaskId2);
        /*считаем данные с сервера в другой экземпляр менеджера*/
        HttpTaskManager taskManagerFromServer = (HttpTaskManager) Managers.getDefault();
        taskManagerFromServer.loadFromServer();

        /*сравним истрии*/
        assertTrue(manager.getHistory().equals(taskManagerFromServer.getHistory()));
        /*сравним списки тасков*/
        assertTrue(manager.getTasks().equals(taskManagerFromServer.getTasks()));
        /*сравним списки эпиков*/
        assertTrue(manager.getEpics().equals(taskManagerFromServer.getEpics()));
        /*сравним списки сабтасков*/
        assertTrue(manager.getSubtasks().equals(taskManagerFromServer.getSubtasks()));

    }

    /**
     * проверка сохраниения на сервер состояния менеджера с хорошим тасками, эпиками, сабтасками но без истории
     */
    @Test
    public void saveManagerWithGoodTasksWithoutHistory() {
        /*добавим в менеджер таск, эпик, сабтаски. История просмотров при это остается пустой*/
        Integer taskId = manager.createTask(task1);
        Integer epicId = manager.createEpic(epic1);
        Integer subtaskId1 = manager.createSubtask(subtask1);
        Integer subtaskId2 = manager.createSubtask(subtask2);

        /*считаем данные с сервера в другой экземпляр менеджера*/
        HttpTaskManager taskManagerFromServer = (HttpTaskManager) Managers.getDefault();
        taskManagerFromServer.loadFromServer();

        /*сравним истрии. Истории д.б.пустыми*/
        assertTrue(manager.getHistory().equals(taskManagerFromServer.getHistory()));
        assertTrue(taskManagerFromServer.getHistory().isEmpty());
        /*сравним списки тасков*/
        assertTrue(manager.getTasks().equals(taskManagerFromServer.getTasks()));
        /*сравним списки эпиков*/
        assertTrue(manager.getEpics().equals(taskManagerFromServer.getEpics()));
        /*сравним списки сабтасков*/
        assertTrue(manager.getSubtasks().equals(taskManagerFromServer.getSubtasks()));
    }

    /**
     * проверка сохраниения на сервер состояния менеджера с эпиками, сабтасками и историей просмотров. Без тасков
     */
    @Test
    public void saveManagerWithoutTasksWithHistory() {
        /*добавим в менеджер эпик, сабтаски*/
        Integer epicId = manager.createEpic(epic1);
        Integer subtaskId1 = manager.createSubtask(subtask1);
        Integer subtaskId2 = manager.createSubtask(subtask2);
        /*добавим все объекты в историю просмотров (и соответствеено на сервер)*/
        manager.getEpicById(epicId);
        manager.getSubtaskById(subtaskId1);
        manager.getSubtaskById(subtaskId2);
        /*считаем данные с сервера в другой экземпляр менеджера*/
        HttpTaskManager taskManagerFromServer = (HttpTaskManager) Managers.getDefault();
        taskManagerFromServer.loadFromServer();

        /*сравним истрии*/
        assertTrue(manager.getHistory().equals(taskManagerFromServer.getHistory()));
        /*сравним списки тасков*/
        assertTrue(manager.getTasks().equals(taskManagerFromServer.getTasks()));
        /*сравним списки эпиков*/
        assertTrue(manager.getEpics().equals(taskManagerFromServer.getEpics()));
        /*сравним списки сабтасков*/
        assertTrue(manager.getSubtasks().equals(taskManagerFromServer.getSubtasks()));
    }

    /**
     * проверка сохраниения на сервер состояния менеджера с тасками, эпиками, но без сабтасков. С историей просмотров
     */
    @Test
    public void saveManagerWithoutSubtasksWithHistory() {
        /*добавим в менеджер таск, эпик, сабтаски*/
        Integer taskId = manager.createTask(task1);
        Integer epicId = manager.createEpic(epic1);
        /*добавим все объекты в историю просмотров (и соответствеено на сервер)*/
        manager.getTaskById(taskId);
        manager.getEpicById(epicId);
        /*считаем данные с сервера в другой экземпляр менеджера*/
        HttpTaskManager taskManagerFromServer = (HttpTaskManager) Managers.getDefault();
        taskManagerFromServer.loadFromServer();

        /*сравним истрии*/
        assertTrue(manager.getHistory().equals(taskManagerFromServer.getHistory()));
        /*сравним списки тасков*/
        assertTrue(manager.getTasks().equals(taskManagerFromServer.getTasks()));
        /*сравним списки эпиков*/
        assertTrue(manager.getEpics().equals(taskManagerFromServer.getEpics()));
        /*сравним списки сабтасков*/
        assertTrue(manager.getSubtasks().equals(taskManagerFromServer.getSubtasks()));
    }
}
