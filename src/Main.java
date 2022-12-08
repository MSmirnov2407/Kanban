
import model.*; //пакет с классами таск, сабтаск, эпик, enum Status
import servise.*;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        String fileName = "src/file1.csv";
        TaskManager manager = Managers.getFileBackedTaskManager(fileName); //создаем объект менеджера
        Integer id; //переменная для временного хранения id при промежуточных действиях
        Task task; //вспомогательные переменные для хранения объектов
        Subtask subtask;
        Epic epic;

        /*создаем две задачи*/
        manager.createTask(new Task("Посмотреть фильм", "Персонаж (2006 г)"));
        manager.createTask(new Task("Постричь собаку", "а то уже глаз не видно"));
        /* создаем эпик с тремя подзадачами*/
        id = manager.createEpic(new Epic("Приготовить суп", "лёгенький суп из бульона и зажарки"));
        manager.createSubtask(new Subtask("сварить бульон", "куриный", id));
        manager.createSubtask(new Subtask("сделать зажарку", "лук+морковь", id));
        manager.createSubtask(new Subtask("добавить зажарку в бульон", "вроде конец", id));
        /* создаем эпик с без подзадач*/
        manager.createEpic(new Epic("Закрыть сессию", "первая сессия первого курса"));
        /*посмотрим на состояние объектов через печать*/
        System.out.println("tasks: " + manager.getTasks());
        System.out.println("epics: " + manager.getEpics());
        System.out.println("subtask: " + manager.getSubtasks());
        /*запрос созданных задач в разном порядке и проверка истории*/
        manager.getTaskById(0);
        manager.getTaskById(1);
        manager.getSubtaskById(3);
        manager.getSubtaskById(4);
        manager.getSubtaskById(5);
        manager.getEpicById(2);
        manager.getEpicById(6);
        System.out.println("ИСТОРИЯ 1: " + manager.getHistory());
        /*внесение изменений в истории просмотров*/
        manager.getTaskById(1);
        manager.getSubtaskById(3);
        manager.getSubtaskById(4);
        System.out.println("ИСТОРИЯ 2: " + manager.getHistory());
        /*создание нового менеджера по данным из файла*/
        FileBackedTasksManager fileBackedTasksManager = FileBackedTasksManager.loadFromFile(new File(fileName));
        System.out.println("file_tasks: " + fileBackedTasksManager.getTasks());
        System.out.println("file_epics: " + fileBackedTasksManager.getEpics());
        System.out.println("file_subtask: " + fileBackedTasksManager.getSubtasks());
        System.out.println("file_ИСТОРИЯ 2: " + fileBackedTasksManager.getHistory());
        /*удалим задачу, которая есть в истории и проверим историю*/
        manager.deleteTaskById(1);
        System.out.println("ИСТОРИЯ 3: " + manager.getHistory());
        /*удалим эпик с подзадачами. провеим историю*/
        manager.deleteEpicById(2);
        System.out.println("ИСТОРИЯ 4: " + manager.getHistory());
    }
}
