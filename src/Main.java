
import model.Epic;
import model.Subtask;
import model.Task;
import service.*;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        new KVServer().start(); //создаем и запускаем сервер-хранилище

        TaskManager manager = new HttpTaskManager("http://localhost:8078");
        /*создаем две задачи*/
        manager.createTask(new Task("Посмотреть фильм", "Персонаж (2006 г)"));
        manager.createTask(new Task("Постричь собаку", "а то уже глаз не видно"));
        /* создаем эпик с тремя подзадачами*/
        Integer id = manager.createEpic(new Epic("Приготовить суп", "лёгенький суп из бульона и зажарки"));
        manager.createSubtask(new Subtask("сварить бульон", "куриный", id));
        manager.createSubtask(new Subtask("сделать зажарку", "лук+морковь", id));
        manager.createSubtask(new Subtask("добавить зажарку в бульон", "вроде конец", id));
        /* создаем эпик без подзадач*/
        manager.createEpic(new Epic("Закрыть сессию", "первая сессия первого курса"));

        manager.getTaskById(1);
        manager.getTaskById(0);
        System.out.println("origin history"+manager.getHistory());

        HttpTaskManager manager2 = new HttpTaskManager("http://localhost:8078");
        manager2.loadFromServer();
    }
}
