import model.Epic;
import model.Subtask;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.KVServer;
import service.Managers;
import service.TaskManager;
import model.Status;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class EpicStatusTest {
    private static TaskManager manager; //ссылочная переменная для менеджера
    private static Integer epicId; //ссылочная переменная для хранения id эпика
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
        manager = Managers.getDefault(); //создаем объект менеджера
        epicId = manager.createEpic(new Epic("эпик-1 - имя", "эпик-1 - описание")); //создали эпик
    }

    /**
     * Проверка статуса эпика, у которого нет сабтасков.
     * Должен быть NEW как сразу после создания, так и после удаления всех имеющихся сабтасков
     */
    @Test
    public void epicStatusNewWithNoSubtasks() {
        Epic epic = manager.getEpicById(epicId); // присвоили переменной epic только созданный объект эпика
        assertEquals(Status.NEW, epic.getStatus(), "Неправильный статус эпика");//проверили его статус

        /*создадим сабтаск в нашем эпике. Поменяем его статус на Done. И удалим сабтаск.*/
        int subtaskId = manager.createSubtask(new Subtask("сабтаск", "сабтаск описание", epicId));
        Subtask subtask = manager.getSubtaskById(subtaskId);
        subtask.setStatus(Status.DONE);
        manager.updateSubtask(subtask);
        manager.deleteSubtaskById(subtaskId);
        /*снова проверим статус эпика, должен быть NEW*/
        assertEquals(Status.NEW, epic.getStatus(), "Неправильный статус эпика");
    }

    /**
     * Проверка статуса эпика, если статусы всех подзадач NEW
     * статус эпика должен быть NEW
     */
    @Test
    public void epicStatusNewWithAllSubtasksNew() {
        Epic epic = manager.getEpicById(epicId); // присвоили переменной epic только созданный объект эпика
        /*создадим сабтаски в нашем эпике*/
        manager.createSubtask(new Subtask("сабтаск1", "сабтаск1 описание", epicId));
        manager.createSubtask(new Subtask("сабтаск2", "сабтаск2 описание", epicId));
        manager.createSubtask(new Subtask("сабтаск3", "сабтаск3 описание", epicId));
        /*проверим статус эпика, должен быть NEW*/
        assertEquals(Status.NEW, epic.getStatus(), "Неправильный статус эпика");
    }

    /**
     * Проверка статуса эпика при всех сабтаскох со статусом DONE
     * статус эпика должен быть DONE
     */
    @Test
    public void epicStatusDoneWithAllSubtasksDone() {
        Epic epic = manager.getEpicById(epicId); // присвоили переменной epic только созданный объект эпика
        /*создадим сабтаски в нашем эпике*/
        int subtaskId;
        manager.createSubtask(new Subtask("сабтаск1", "сабтаск1 описание", epicId));
        manager.createSubtask(new Subtask("сабтаск2", "сабтаск2 описание", epicId));
        manager.createSubtask(new Subtask("сабтаск3", "сабтаск3 описание", epicId));

        Stream<Subtask> stream = manager.getSubtasks().stream(); //создадим стрим для прохода по сабтаскам
        stream.forEach((subtask) -> {       //для каждого эл-та стрима (т.е.для каждого сабтаска)
            subtask.setStatus(Status.DONE); // изменим статус на DONE
            manager.updateSubtask(subtask); // обновим сабтаск
        });
        /*проверим статус эпика, должен быть DONE*/
        assertEquals(Status.DONE, epic.getStatus(), "Неправильный статус эпика");
    }

    /**
     * Проверка статуса эпика при наличии сабтасков со статусом NEW и DONE
     * статус эпика должен быть IN_PROGRESS
     */
    @Test
    public void epicStatusInProgressWithSubtasksNewAndDone() {
        Epic epic = manager.getEpicById(epicId); // присвоили переменной epic только созданный объект эпика
        /*создадим сабтаски в нашем эпике*/
        int subtaskId;
        manager.createSubtask(new Subtask("сабтаск1", "сабтаск1 описание", epicId));
        subtaskId = manager.createSubtask(new Subtask("сабтаск2", "сабтаск2 описание", epicId));

        Subtask subtask = manager.getSubtaskById(subtaskId);
        subtask.setStatus(Status.DONE); // изменим статус сабтаска2 на DONE
        manager.updateSubtask(subtask); // обновим сабтаск2
        /*проверим статус эпика, должен быть IN_PROGRESS*/
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Неправильный статус эпика");
    }

    /**
     * Проверка статуса эпика при всех сабтаскох со статусом IN_PROGRESS
     * статус эпика должен быть IN_PROGRESS
     */
    @Test
    public void epicStatusInProgressWithAllSubtasksInProgress() {
        Epic epic = manager.getEpicById(epicId); // присвоили переменной epic только созданный объект эпика
        /*создадим сабтаски в нашем эпике*/
        int subtaskId;
        manager.createSubtask(new Subtask("сабтаск1", "сабтаск1 описание", epicId));
        manager.createSubtask(new Subtask("сабтаск2", "сабтаск2 описание", epicId));
        manager.createSubtask(new Subtask("сабтаск3", "сабтаск3 описание", epicId));

        Stream<Subtask> stream = manager.getSubtasks().stream(); //создадим стрим для прохода по сабтаскам
        stream.forEach((subtask) -> {       //для каждого эл-та стрима (т.е.для каждого сабтаска)
            subtask.setStatus(Status.IN_PROGRESS); // изменим статус на N_PROGRESS
            manager.updateSubtask(subtask); // обновим сабтаск
        });
        /*проверим статус эпика, должен быть N_PROGRESS*/
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Неправильный статус эпика");
    }
}