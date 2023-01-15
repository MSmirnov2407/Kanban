import model.Task;
import org.junit.jupiter.api.*;
import service.KVServer;
import service.TaskManager;
import model.Epic;
import model.Subtask;
import service.Managers;
import model.Status;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    T manager; //ссылочная переменная для менеджера
    public Task task1;
    public Task task2;
    public Epic epic1;
    public Epic epic2;
    public Subtask subtask1;
    public Subtask subtask2;
    private static KVServer kvServer; //http-сервер-хранилище состояния менеджера
    @BeforeAll
    public static void beforeAll(){
        try{
            kvServer = new KVServer();
            kvServer.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @AfterAll
    public static void afterAll(){
        kvServer.stop();
    }

    @BeforeEach
    public void beforeEach() {
        manager = (T) Managers.getDefault(); //создаем объект менеджера
        /*Создание подопытных тасков, эпиков, сабтасков. Эти объекты будут использоваться в тестах*/
        task1 = new Task("Task1", "Task description1", 1);
        task2 = new Task("Task2", "Task description2", 2);
        epic1 = new Epic("Epic1", "Epic description1", 3);
        epic2 = new Epic("Epic2", "Epic description2", 4);
        subtask1 = new Subtask("Subtask1", "Subtask description1", 3, 5);
        subtask2 = new Subtask("Subtask2", "Subtask description2", 3, 6);
    }

    /**
     * проверка метода getTasks() с непустым списком задач.
     * Должен вернуть список задач
     */
    @Test
    public void getTasksArrayListIfGoodTasks() {
        manager.createTask(task1); //сложили в менеджер таски
        manager.createTask(task2);
        ArrayList<Task> tasks = manager.getTasks(); //вывели список тасков тестируемым методом getTask

        assertEquals(task1, tasks.get(0)); //проверили соответствие содержимого полученного списка тасков
        assertEquals(task2, tasks.get(1)); //сложенным в менеджер таскам
    }

    /**
     * проверка метода getTasks() с пустым списком задач.
     * Должен вернуть пустой список задач
     */
    @Test
    public void getTasksEmptyListIfNoTasks() {
        ArrayList<Task> tasks = manager.getTasks(); //вывели список тасков тестируемым методом getTask

        assertTrue(tasks.isEmpty()); //проверили отсутствие эл-ов в полученном списке
    }

    /**
     * проверка метода getSubtasks() с непустым списком подзадач.
     * Должен вернуть список подзадач
     */
    @Test
    public void getSubtasksArrayListIfGoodSubtasks() {
        manager.createEpic(epic1);  //добавим в менеджер эпик и связанные с ним сабтаски

        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        ArrayList<Subtask> subtasks = manager.getSubtasks(); //вывели список тасков тестируемым методом getTask

        assertEquals(subtask1, subtasks.get(0)); //проверили соответствие содержимого полученного списка сабтасков
        assertEquals(subtask2, subtasks.get(1)); //сложенным в менеджер сабтаски
    }

    /**
     * проверка метода getTasks() с пустым списком подзадач.
     * Должен вернуть пустой список подзадач
     */
    @Test
    public void getSubtasksEmptyListIfNoTasks() {
        ArrayList<Subtask> subtasks = manager.getSubtasks(); //вывели список тасков тестируемым методом getTask

        assertTrue(subtasks.isEmpty()); //проверили отсутствие эл-ов в полученном списке
    }

    /**
     * проверка метода getSubtasks() после добавления сабтаска с без предварительного создания эпиков.
     * Должен вернуть пустой список подзадач
     */
    @Test
    public void getSubtasksEmptyListIfNoEpic() {
        manager.createSubtask(subtask1); //добавим в менеджер сабтаски без предварительного создания эпиков
        manager.createSubtask(new Subtask("subtask", "description", null)); //сабтаск с эпиком null

        ArrayList<Subtask> subtasks = manager.getSubtasks(); //вывели список тасков тестируемым методом getTask
        assertTrue(subtasks.isEmpty()); //проверили отсутствие эл-ов в полученном списке

    }

    /**
     * проверка метода getEpics() с непустым списком эпиков.
     * Должен вернуть список эпиков
     */
    @Test
    public void getEpicsArrayListIfGoodEpics() {
        manager.createEpic(epic1); //сложили в менеджер эпики
        manager.createEpic(epic2);
        ArrayList<Epic> epics = manager.getEpics(); //вывели список тасков тестируемым методом getEpics

        assertEquals(epic1, epics.get(0)); //проверили соответствие содержимого полученного списка эпиков
        assertEquals(epic2, epics.get(1)); //сложенным в менеджер эпикам
    }

    /**
     * проверка метода getEpics() с пустым списком эпиков.
     * Должен вернуть пустой список эпиков
     */
    @Test
    public void getEpicsEmptyListIfNoEpics() {
        ArrayList<Epic> epics = manager.getEpics(); //вывели список эпиков тестируемым методом getEpics

        assertTrue(epics.isEmpty()); //проверили отсутствие эл-ов в полученном списке
    }

    /**
     * Проверка метода удаления всех тасков deleteAllTasks(), при условии, что таковые имеются.
     * Список тасков должен опустеть
     */
    @Test
    public void deleteAllTasksIfGoodTasks() {
        Integer taskId1 = manager.createTask(task1); //добавили пару тасков в менеджер
        Integer taskId2 = manager.createTask(task2);

        manager.getTaskById(taskId1); //вызвали их по id чтобы сохранить в историю просмотров
        manager.getTaskById(taskId2);

        manager.deleteAllTasks(); //удалили таски
        assertTrue(manager.getTasks().isEmpty()); //проверили отсутствие эл-ов в полученном списке
        assertTrue(manager.getHistory().isEmpty());//проверили отсутствие эл-ов в истоии вызовов

    }

    /**
     * Проверка метода удаления всех тасков deleteAllTasks(), при условии, что их итак нет.
     * Список тасков должен остаться пустым, без ошибок
     */
    @Test
    public void deleteAllTasksIfNoTasks() {
        manager.deleteAllTasks();
        assertTrue(manager.getTasks().isEmpty()); //проверили отсутствие эл-ов в полученном списке
    }

    /**
     * проверка метода удаления всех сабтасков deleteAllSubtasks()
     * При запросе списка подзадач должен вернуться пустой список
     */
    @Test
    public void deleteAllSubtasksIfGoodSubtasks() {
        manager.createEpic(epic1);  //добавим в менеджер эпик и связанные с ним сабтаски

        Integer subtaskId1 = manager.createSubtask(subtask1);
        Integer subtaskId2 = manager.createSubtask(subtask2);

        manager.getSubtaskById(subtaskId1); //вызвали их по id чтобы сохранить в историю просмотров
        manager.getSubtaskById(subtaskId2);

        manager.deleteAllSubtasks(); //удалили все подзадачи
        assertTrue(manager.getSubtasks().isEmpty()); //проверили отсутствие эл-в в полученном списке
        assertTrue(epic1.getSubtaskIds().isEmpty()); //проверили, что у эпика очистился список сабтасков
        assertTrue(manager.getHistory().isEmpty());//проверили отсутствие эл-ов в истоии вызовов
    }

    /**
     * проверка метода удаления всех сабтасков deleteAllSubtasks(), при условии, что их итак нет.
     * При запросе списка подзадач должен вернуться пустой список
     */
    @Test
    public void deleteAllSubtasksIfNoSubtasks() {
        manager.deleteAllSubtasks();
        assertTrue(manager.getSubtasks().isEmpty()); //проверили отсутствие эл-в в полученном списке
    }

    /**
     * проверка метода deleteAllEpics
     * При запросе списка эпиков, должен вернуться пустой спикок.
     * При запросе списка подзадач должен вернуться пустой список
     */
    @Test
    public void deleteAllEpicsIfGoodEpics() {
        Integer epicId = manager.createEpic(epic1);  //добавим в менеджер эпик и связанные с ним сабтаски
        Integer subtaskId1 = manager.createSubtask(subtask1);
        Integer subtaskId2 = manager.createSubtask(subtask2);

        manager.getSubtaskById(subtaskId1); //вызвали их по id чтобы сохранить в историю просмотров
        manager.getSubtaskById(subtaskId2);
        manager.getEpicById(epicId);

        manager.deleteAllEpics();
        assertTrue(manager.getEpics().isEmpty()); //проверили отсутствие эл-в в полученном списке
        assertTrue(manager.getSubtasks().isEmpty()); //проверили, что также очистился список сабтасков
        assertTrue(manager.getHistory().isEmpty());//проверили отсутствие эл-ов в истоии вызовов
    }

    /**
     * проверка метода удаления всех эпиков deleteAllEpics, при условии, что их итак нет
     * При запросе списка эпиков, должен вернуться пустой спикок.
     */
    @Test
    public void deleteAllEpicsIfNoEpics() {
        manager.deleteAllEpics();
        assertTrue(manager.getEpics().isEmpty()); //проверили отсутствие эл-в в полученном списке
    }

    /**
     * проверка метода getTaskById()
     * должен возвращаться нужный таск
     */
    @Test
    public void getTaskByIdIfGoodTask() {
        Integer taskId = manager.createTask(task1); //добавили таск в менеджер
        assertEquals(task1, manager.getTaskById(taskId)); //запросили такс по id
        assertEquals(task1, manager.getHistory().get(0)); // проверили его наличие в истории просмотров
    }

    /**
     * проверка метода getTaskById() при неправильном id
     * должен вернуться null
     */
    @Test
    public void getTaskByIdIfBadTaskId() {
        Integer taskId = manager.createTask(task1); //добавили таск в менеджер
        assertNull(manager.getTaskById(taskId + 99)); //запросили такс по неправильному id
    }

    /**
     * проверка метода getSubtaskById()
     * должен возвращаться нужный сабтаск
     */
    @Test
    public void getSubtaskByIdIfGoodSubtask() {
        manager.createEpic(epic1);  //добавим в менеджер эпик и связаный с ним сабтаск
        Integer subtaskId = manager.createSubtask(subtask1);

        assertEquals(subtask1, manager.getSubtaskById(subtaskId)); //запросили сабтакс по id
        assertEquals(subtask1, manager.getHistory().get(0)); // проверили его наличие в истории просмотров

    }

    /**
     * проверка метода getSubtaskById() при неправильном id
     * должен вернуться null
     */
    @Test
    public void getSubtaskByIdIfBadSubtaskId() {
        manager.createEpic(epic1);  //добавим в менеджер эпик и связаный с ним сабтаск
        Integer subtaskId = manager.createSubtask(subtask1);

        assertNull(manager.getSubtaskById(subtaskId + 99)); //запросили сабтакс по неправильному id
    }

    /**
     * проверка метода getEpicById()
     * должен возвращаться нужный эпик
     */
    @Test
    public void getEpicByIdIfGoodEpic() {
        Integer epicId = manager.createEpic(epic1); //добавили epic в менеджер
        assertEquals(epic1, manager.getEpicById(epicId)); //запросили epic по id
        assertEquals(epic1, manager.getHistory().get(0)); // проверили его наличие в истории просмотров
    }

    /**
     * проверка метода getEpicById() при неправильном id
     * должен вернуться null
     */
    @Test
    public void getEpicByIdIfBadEpicId() {
        Integer epicId = manager.createEpic(epic1); //добавили таск в менеджер
        assertNull(manager.getEpicById(epicId + 99)); //запросили такс по неправильному id
    }

    /**
     * Проверка метода createTask() при передаче тасков с id и без id
     * Должен вернуться id созданного таска
     */
    @Test
    public void createTaskIfGoodTask() {
        Integer taskId1 = manager.createTask(task1); //передали таск с заполненным id=1
        Integer taskId2 = manager.createTask(new Task("name2", "description2")); //таск без id
        Integer taskId3 = manager.createTask(new Task("name3", "description3", null)); //таск c id=null
        assertEquals(1, taskId1);
        assertEquals(2, taskId2);
        assertEquals(3, taskId3);
    }

    /**
     * Проверка метода createTask() при передаче в него null
     * Должен вернуться null
     */
    @Test
    public void createTaskIfBadTask() {
        Integer taskId = manager.createTask(null); //передали null в метод создания таска в менеджере
        assertNull(taskId);
    }

    /**
     * Проверка метода createSubtask() при передаче тасков с id и без id. Связанный эпик существует заранее.
     * Должен вернуться id созданного сабтаска
     */
    @Test
    public void createSubtaskIfGoodSubtask() {
        /*создали эпик, к которому привязываются сабтаски*/
        Integer epicId = manager.createEpic(new Epic("epic", "description epic", 3));
        Integer subtaskId1 = manager.createSubtask(subtask1); //передали сабтаск с заполненным id=5
        /*таск без id*/
        Integer subtaskId2 = manager.createSubtask(new Subtask("name2", "description2", 3));
        /*таск c id=null*/
        Integer subtaskId3 = manager.createSubtask(new Subtask("name3", "description3", 3, null));
        assertEquals(5, subtaskId1); //проверка id добавленных в менеджер эпиков
        assertEquals(6, subtaskId2);
        assertEquals(7, subtaskId3);

        /*проверим, что в Эпике сохранился список id его сабтасков*/
        Epic epic = manager.getEpicById(epicId); //берем эпик, к которому мы привязывали сабтаски
        Integer[] epicSubtaskIds = new Integer[epic.getSubtaskIds().size()]; // по длине сипска subtaskId создаем массив
        epicSubtaskIds = epic.getSubtaskIds().toArray(epicSubtaskIds); //превращаем список subtaskId в массив integer
        Integer subtaskIds[] = {subtaskId1, subtaskId2, subtaskId3}; //создаем массив,сохраняем в него id сабтасков
        Assertions.assertArrayEquals(epicSubtaskIds, subtaskIds); //сравниваем созданный массив и массив из эпика
    }

    /**
     * Проверка метода createSubtask() при передаче в него null и при создании без эпика
     * Должен вернуться null
     */
    @Test
    public void createSubtaskIfBadSubtask() {
        Integer subtaskId1 = manager.createSubtask(null); //передали null в метод создания сабтаска в менеджере
        Integer subtaskId2 = manager.createSubtask(new Subtask("n", "d", null));

        assertNull(subtaskId1);
        assertNull(subtaskId2);
    }

    /**
     * проверка метода createSubtask(), при передаче в него эпика с id или без id.
     * Должен вернуться id добавленного эпика.
     */
    @Test
    public void createEpicIfGoodEpic() {
        Integer epicId1 = manager.createEpic(epic1); //передали эпик с ненулевым id
        Integer epicId2 = manager.createEpic(new Epic("nnn", "ddd")); //передали эпик без id

        assertEquals(3, epicId1); //проверка id добавленных в менеджер эпиков
        assertEquals(4, epicId2);
        assertEquals(Status.NEW, manager.getEpicById(epicId2).getStatus()); //проверка статуса добавленного эпика

    }

    /**
     * проверка метода createSubtask(), при передаче в него null.
     * Должен вернуться null.
     */
    @Test
    public void createEpicIfBadEpic() {
        Integer epicId1 = manager.createEpic(null); //передали null

        assertNull(epicId1);
    }

    /**
     * проверка метода updateTask() при передаче в него задачи с существующим id.
     * Данные о задаче должны обновиться в списке задач менеджера.
     */
    @Test
    public void updateTaskIfGoodTask() {
        Integer taskId = manager.createTask(task1); //добавили в менеджер задачу
        manager.updateTask(new Task("new Task1", "new Description1", taskId)); //обновили задачу

        assertEquals("new Task1", manager.getTaskById(taskId).getName()); //проверим обновленное имя и описание
        assertEquals("new Description1", manager.getTaskById(taskId).getDescription());
    }

    /**
     * проверка метода updateTask() при передаче в него задачи с несуществующим id или null.
     * Данные о задаче не должны обновиться в списке задач менеджера.
     */
    @Test
    public void updateTaskIfBadTask() {
        Integer taskId = manager.createTask(task1); //добавили в менеджер задачу
        /*обновим задачу, передав неправильный id*/
        /*имя и описание существующей задачи не должны измениться*/
        manager.updateTask(new Task("new Task2", "new Description2", (taskId + 99)));
        assertNotEquals("new Task2", task1.getName());
        assertNotEquals("new Description2", task1.getDescription());

        /*обновим задачу, передав null*/
        /*задача в менеджере не должна измениться*/
        String name = task1.getName(); //имя задачи
        String description = task1.getDescription(); //описание задачи
        manager.updateTask(null);
        assertEquals(name, task1.getName());
        assertEquals(description, task1.getDescription());
    }

    /**
     * проверка метода updateSubtask() при передаче в него подзадачи с существующим id.
     * Данные о подзадаче должны обновиться в списке менеджера.
     */
    @Test
    public void updateSubtaskIfGoodSubtask() {
        /*добавим эпик, к которому будет относиться сабтаск*/
        Integer epicId = manager.createEpic(new Epic("Epic name", "Epic description"));
        /*добавим в менеджер сабтаск*/
        Integer subtaskId = manager.createSubtask(new Subtask("Subtask name",
                "Subtask description", epicId));

        manager.updateSubtask(new Subtask("new subtask1", "new Description1", epicId, subtaskId));//обновили
        Subtask subtask = manager.getSubtaskById(subtaskId);
        /*проверим обновленное имя и описание*/
        assertEquals("new subtask1", subtask.getName());
        assertEquals("new Description1", subtask.getDescription());
        /*изменим статус подзадачи. автоматически должен обновиться стстус эпика*/
        subtask.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask);
        assertEquals(Status.IN_PROGRESS, subtask.getStatus());
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epicId).getStatus());
    }

    /**
     * проверка метода updateSubtask() при передаче в него null или подзадачи с несуществующим id или epicId.
     * Существующие данные не должны измениться.
     */
    @Test
    public void updateSubtaskIfBadSubtask() {
        /*добавим эпик, к которому будет относиться сабтаск*/
        Integer epicId = manager.createEpic(new Epic("Epic name", "Epic description"));
        /*добавим в менеджер сабтаск*/
        Integer subtaskId = manager.createSubtask(new Subtask("Subtask name",
                "Subtask description", epicId));
        manager.updateSubtask(null);//обновили с передачей null
        manager.updateSubtask(new Subtask("Subtask name",
                "Subtask description", epicId * 1000, subtaskId));//обновили с передачей неправильного epicId
        manager.updateSubtask(new Subtask("Subtask name",
                "Subtask description", epicId, subtaskId * 1000));//обновили с передачей неправильного subtaskId

        Subtask subtask = manager.getSubtaskById(subtaskId); //берем из менеджера нашу подзадачу для проверки
        /*проверим, что данные с сабтаске остались прежними*/
        assertEquals("Subtask name", subtask.getName());
        assertEquals("Subtask description", subtask.getDescription());
    }

    /**
     * проверка метода updateEpic() при передаче в него эпика с существующим id.
     * Данные об эпике должны обновиться в списке менеджера.
     */
    @Test
    public void updateEpicIfGoodEpic() {
        Integer epicId = manager.createEpic(epic1); //добавили эпик в менджер
        manager.updateEpic(new Epic("updated name", "updated description", epicId)); //обновили
        Epic epic = manager.getEpicById(epicId); //взяли обновленный эпик для проверки

        assertEquals("updated name", epic.getName());
        assertEquals("updated description", epic.getDescription());
    }

    /**
     * проверка метода updateEpic() при передаче в него null или эпика с несуществующим id.
     * Существующие данные не должны измениться.
     */
    @Test
    public void updateEpicIfBadEpic() {
        Integer epicId = manager.createEpic(epic1); //добавили эпик в менджер
        String name = manager.getEpicById(epicId).getName(); //сохранили в переменных его имя и описание
        String description = manager.getEpicById(epicId).getDescription();

        /*обновим эпик с неправильным id и с передачей null*/
        manager.updateEpic(new Epic("BAD name", "BAD description", epicId * 1000));
        manager.updateEpic(null);

        Epic epic = manager.getEpicById(epicId); //взяли эпик для проверки
        /*проверим, что прежние имя и описание не изменились*/
        assertEquals(name, epic.getName());
        assertEquals(description, epic.getDescription());
    }

    /**
     * проверка метода deleteTaskById(), при существующем таске
     * Таск должен удалиться из списка в менеджере
     */
    @Test
    public void deleteTaskByIdIfGoodTask() {
        Integer taskId = manager.createTask(task1); //добавили таск в менеджер
        manager.getTaskById(taskId); //вызвали его по id чтобы сохранить в историю просмотров

        manager.deleteTaskById(taskId); //удалили таск из списка в менеджере
        assertTrue(manager.getTasks().isEmpty()); //проверили, что список пуст
        assertTrue(manager.getHistory().isEmpty());//проверили отсутствие эл-ов в истоии вызовов
    }

    /**
     * проверка метода deleteTaskById(), при несуществующем id таска или null
     * Таск не должен удалиться из списка в менеджере
     */
    @Test
    public void deleteTaskByIdIfBadTask() {
        Integer taskId = manager.createTask(task1); //добавили таск в менеджер

        manager.deleteTaskById(null); //передаем null в методу удаления
        manager.deleteTaskById(taskId * 1000); //передаем таск с неправильным id
        assertFalse(manager.getTasks().isEmpty()); //проверили, что список не пуст
    }

    /**
     * проверка метода deleteSubtaskById() при передаче существующего сабтаска.
     * Сабтаск должен удалиться из списка в менеджере и из списка в эпике.
     */
    @Test
    public void deleteSubtaskByIdIfGoodSubtask() {
        /*добавим эпик, к которому будет относиться сабтаск*/
        Integer epicId = manager.createEpic(new Epic("Epic name", "Epic description"));
        /*добавим в менеджер сабтаск*/
        Integer subtaskId = manager.createSubtask(new Subtask("Subtask name",
                "Subtask description", epicId));
        manager.getSubtaskById(subtaskId); //вызвали его по id чтобы сохранить в историю просмотров
        /*удалили сабтаски и проверили, что список сабтасков в менеджере и в эпике пусты*/
        manager.deleteSubtaskById(subtaskId);

        assertTrue(manager.getSubtasks().isEmpty());
        assertTrue(manager.getHistory().isEmpty());//проверили отсутствие эл-ов в истоии вызовов
        assertTrue(manager.getEpicById(epicId).getSubtaskIds().isEmpty());
    }

    /**
     * проверка метода deleteSubtaskById() при передаче несуществующего id или null.
     * Имеющаяся информация не должна измениться.
     */
    @Test
    public void deleteSubtaskByIdIfBadSubtask() {
        /*добавим эпик, к которому будет относиться сабтаск*/
        Integer epicId = manager.createEpic(new Epic("Epic name", "Epic description"));
        /*добавим в менеджер сабтаск*/
        Integer subtaskId = manager.createSubtask(new Subtask("Subtask name",
                "Subtask description", epicId));

        /*удалили сабтаск и проверили, что список сабтасков в менеджере и в эпике пусты*/
        manager.deleteSubtaskById(subtaskId * 1000);
        manager.deleteSubtaskById(null);
        assertFalse(manager.getSubtasks().isEmpty());
        assertFalse(manager.getEpicById(epicId).getSubtaskIds().isEmpty());
    }

    /**
     * проверка метода deleteEpicById() при передаче существующего эпика
     * Должен удалиться эпик и связанные с ним сабтаски
     */
    @Test
    public void deleteEpicByIdIfGoodEpic() {
        /*добавим эпик, к которому будет относиться сабтаск*/
        Integer epicId = manager.createEpic(new Epic("Epic name", "Epic description"));
        /*добавим в менеджер сабтаск*/
        Integer subtaskId = manager.createSubtask(new Subtask("Subtask name",
                "Subtask description", epicId));
        manager.getSubtaskById(subtaskId); //вызвали их по id чтобы сохранить в историю просмотров
        manager.getEpicById(epicId);

        /*удалили эпик и проверили, что список эпиков и сабтасков в  менеджере пусты*/
        manager.deleteEpicById(epicId);
        assertTrue(manager.getSubtasks().isEmpty());
        assertTrue(manager.getEpics().isEmpty());
        assertTrue(manager.getHistory().isEmpty());//проверили отсутствие эл-ов в истоии вызовов
    }

    /**
     * проверка метода deleteEpicById() при передаче несуществующего эпика или null
     * сохраненные данные не должны удаиться
     */
    @Test
    public void deleteEpicByIdIfBadEpic() {
        /*добавим эпик, к которому будет относиться сабтаск*/
        Integer epicId = manager.createEpic(new Epic("Epic name", "Epic description"));
        /*добавим в менеджер сабтаск*/
        Integer subtaskId = manager.createSubtask(new Subtask("Subtask name",
                "Subtask description", epicId));
        /*удалили эпик и проверили, что список эпиков и сабтасков в  менеджере пусты*/
        manager.deleteEpicById(epicId * 1000 + 1000);
        manager.deleteEpicById(null);

        assertFalse(manager.getSubtasks().isEmpty());
        assertFalse(manager.getEpics().isEmpty());
    }

    /**
     * проверка сортировки задач по времени начала задачи(подзадачи).
     */
    @Test
    public void getPrioritizedTasksIfGoodTasks() {
        /*добавляем таски с указанием времени начала*/
        Integer taskId1 = manager.createTask(new Task("Task1", "Description1",
                LocalDateTime.parse("2022-12-29T12:12:12.1111"), 8, 0));
        Integer taskId2 = manager.createTask(new Task("Task2", "Description2",
                LocalDateTime.parse("2022-12-31T13:10:11.2222"), 10, 1));
        Integer taskId3 = manager.createTask(new Task("Task1", "Description1",
                LocalDateTime.parse("2022-12-30T14:11:12.3333"), 9, 2));
        /*добавляем эпик и его сабтаск с указанием времени начала*/
        Integer epicId = manager.createEpic(new Epic("Epic1", "descr1",
                LocalDateTime.parse("2022-12-10T00:00:00.0000"), 22, 3 ));
        Integer subtaskId = manager.createSubtask(new Subtask("Subtask1", "descr1S", epicId,
                LocalDateTime.parse("2023-01-01T01:01:01.0001"), 1, 4 ));

        /*создаем список, в который вручную добавляем задачи в порядке приоритета (по времени начала)*/
        List<Task> manualPrioritizedList = new ArrayList<Task>();
        manualPrioritizedList.add(manager.getTaskById(taskId1));
        manualPrioritizedList.add(manager.getTaskById(taskId3));
        manualPrioritizedList.add(manager.getTaskById(taskId2));
        manualPrioritizedList.add(manager.getSubtaskById(subtaskId));
        /*сравниваем ручной список с автоматичекски созданным*/
        assertTrue(manualPrioritizedList.equals(manager.getPrioritizedTasks()));
    }

    /**
     * проверка сортировки задач с пустым спиком задач(подзадач).
     */
    @Test
    public void getPrioritizedTasksWithoutTasks() {
        assertTrue(manager.getPrioritizedTasks().isEmpty());
    }

    /**
     * проверка создания задач с одинаковым временем.
     * Последующие добавления задач с повторющимся временем не должно происходить
     */
    @Test
    public void createSameTimeTask() {
        /*добавляем таски с указанием времени начала*/
        Integer taskId1 = manager.createTask(new Task("Task1", "Description1",
                LocalDateTime.parse("2022-12-29T12:12:12.1111"), 8, 0));
        Integer taskId2 = manager.createTask(new Task("Task2", "Description2",
                LocalDateTime.parse("2022-12-29T12:12:12.1111"), 10, 1));

        /*создаем список, в который вручную добавляем задачу, которая должна сохраниться в getPrioritizedTasks
        * как первая добавленная*/
        List<Task> manualPrioritizedList = new ArrayList<Task>();
        manualPrioritizedList.add(manager.getTaskById(taskId1));
        /*сравниваем ручной список с автоматичекски созданным*/
        assertTrue(manualPrioritizedList.equals(manager.getPrioritizedTasks()));
    }

    /**
     * проверка создания задач с наложением по времени на другую задачу.
     * Создание такой задачи не должно быть выполнено
     */
    @Test
    public void createTaskWithTimeOverlay() {
        /*добавляем таски с указанием времени начала*/
        Integer taskId1 = manager.createTask(new Task("Task1", "Description1",
                LocalDateTime.parse("2022-12-29T12:12:12.1111"), 8, 0));
        Integer taskId2 = manager.createTask(new Task("Task2", "Description2",
                LocalDateTime.parse("2022-12-29T12:12:10.1111"), 10, 1));

        /*создаем список, в который вручную добавляем задачу, которая должна сохраниться в getPrioritizedTasks
         * как первая добавленная без обнаружения пересечения. Вторая задача не должна быть ждобавлена в менеджер*/
        List<Task> manualPrioritizedList = new ArrayList<Task>();
        manualPrioritizedList.add(manager.getTaskById(taskId1));
        /*сравниваем ручной список с автоматичекски созданным*/
        assertTrue(manualPrioritizedList.equals(manager.getPrioritizedTasks()));
        assertNull(taskId2);
    }
}