
import model.*; //пакет с классами таск, сабтаск, эпик, enum Status
import servise.*;

public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager manager = new InMemoryTaskManager(); //создаем объект менеджера
        Integer id; //переменная для временного хранения id при промежуточных действиях

        /*создаем две задачи*/
        manager.createTask(new Task("Посмотреть фильм", "Персонаж (2006 г)"));
        manager.createTask(new Task("Постричь собаку", "а то уже глаз не видно"));
        /* создаем эпик с двумя подзадачами*/
        id = manager.createEpic(new Epic("Приготовить суп", "лёгенький суп из бульона и зажарки"));
        manager.createSubtask(new Subtask("сварить бульон", "куриный", id));
        manager.createSubtask(new Subtask("сделать зажарку", "лук+морковь", id));
        /* создаем эпик с одной подзадачей*/
        id = manager.createEpic(new Epic("Закрыть сессию", "первая сессия первого курса"));
        manager.createSubtask(new Subtask("Сдать физику", "12 апреля, каб.33", id));
        /*посмотрим на состояние объектов через печать*/
        System.out.println("tasks: " + manager.getTasks());
        System.out.println("epics: " + manager.getEpics());
        System.out.println("subtask: " + manager.getSubtasks());
        /*изменим статусы задач*/
        manager.getTaskById(1).setStatus(Status.IN_PROGRESS);
        manager.updateTask(manager.getTaskById(1));
        manager.getTaskById(2).setStatus(Status.DONE);
        manager.updateTask(manager.getTaskById(2));
        /*изменим статусы подзадач*/
        manager.getSubtaskById(2).setStatus(Status.DONE);
        manager.updateSubtask(manager.getSubtaskById(2));
        manager.getSubtaskById(3).setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(manager.getSubtaskById(3));
        /*посмотрим на состояние объектов через печать*/
        System.out.println();
        System.out.println("tasks: " + manager.getTasks());
        System.out.println("epics: " + manager.getEpics());
        System.out.println("subtask: " + manager.getSubtasks());
        /*удалим один таск и один эпик*/
        manager.deleteTaskById(2);
        manager.deleteEpicById(1);
        /*посмотрим на состояние объектов через печать*/
        System.out.println();
        System.out.println("tasks: " + manager.getTasks());
        System.out.println("epics: " + manager.getEpics());
        System.out.println("subtask: " + manager.getSubtasks());
    }
}
