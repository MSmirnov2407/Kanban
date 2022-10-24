package Servise;

import Model.*; //пакет с классами таск, сабтаск, эпик

public class Main {
    public static void main(String[] args) {
        Manager manager = new Manager(); //создаем объект менеджера
        Integer id; //переменная для временного хранения id при промежуточных действиях

        /*создаем две задачи*/
        manager.createTask(new Task("Посмотреть фильм", "Персонаж (2006 г)"));
        manager.createTask(new Task("Постричь собаку", "а то уже глаз не видно"));
        /* создаем эпик с двумя подзадачами*/
        id = manager.createEpic(new Epic("Приготовить суп", "лёгенький суп из бульона и зажарки"));
        manager.createSubtask(new Subtask("сварить бульон", "куриный"), manager.getOneEpic(id));
        manager.createSubtask(new Subtask("сделать зажарку", "лук+морковь"), manager.getOneEpic(id));
        /* создаем эпик с одной подзадачей*/
        id = manager.createEpic(new Epic("Закрыть сессию", "первая сессия первого курса"));
        manager.createSubtask(new Subtask("Сдать физику", "12 апреля, каб.33"), manager.getOneEpic(id));
        /*посмотрим на состояние объектов через печать*/
        System.out.println("tasks: " + manager.getTasks());
        System.out.println("epics: " + manager.getEpics());
        System.out.println("subtaks: " + manager.getSubtasks());
        /*изменим статусы задач*/
        manager.getOneTask(1).setStatus("IN_PROGRESS");
        manager.refreshTask(manager.getOneTask(1));
        manager.getOneTask(2).setStatus("DONE");
        manager.refreshTask(manager.getOneTask(2));
        /*изменим статусы подзадач*/
        manager.getOneSubask(2).setStatus("DONE");
        manager.refreshSubtask(manager.getOneSubask(2));
        manager.getOneSubask(3).setStatus("IN_PROGRESS");
        manager.refreshSubtask(manager.getOneSubask(3));
        /*посмотрим на состояние объектов через печать*/
        System.out.println("");
        System.out.println("tasks: " + manager.getTasks());
        System.out.println("epics: " + manager.getEpics());
        System.out.println("subtaks: " + manager.getSubtasks());
        /*удалим один таск и один эпик*/
        manager.deleteOneTask(2);
        manager.deleteOneEpic(1);
        /*посмотрим на состояние объектов через печать*/
        System.out.println("");
        System.out.println("tasks: " + manager.getTasks());
        System.out.println("epics: " + manager.getEpics());
        System.out.println("subtaks: " + manager.getSubtasks());
    }
}
