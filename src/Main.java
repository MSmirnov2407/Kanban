
import model.*; //пакет с классами таск, сабтаск, эпик, enum Status
import servise.*;

public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager manager = new InMemoryTaskManager(); //создаем объект менеджера
        Integer id; //переменная для временного хранения id при промежуточных действиях
        Task task; //вспомогательные переменные для хранения объектов
        Subtask subtask;
        Epic epic;

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
        task = manager.getTaskById(1);
        task.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task);
        task = manager.getTaskById(2);
        task.setStatus(Status.DONE);
        manager.updateTask(task);
        /*выведем историю просмотра задач*/
        System.out.println("ИСТОРИЯ 1: " + manager.getHistory());
        /*изменим статусы подзадач*/
        subtask = manager.getSubtaskById(2);
        subtask.setStatus(Status.DONE);
        manager.updateSubtask(subtask);
        subtask = manager.getSubtaskById(3);
        subtask.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask);
        /*выведем историю просмотра задач*/
        System.out.println("ИСТОРИЯ 2: " + manager.getHistory());
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
        /*выведем историю просмотра задач*/
        System.out.println("ИСТОРИЯ 3: " + manager.getHistory());
        /* удлим один сабтаск*/
        manager.deleteSubtaskById(3);
        /*выведем историю просмотра задач*/
        System.out.println("ИСТОРИЯ 4: " + manager.getHistory());
    }
}
