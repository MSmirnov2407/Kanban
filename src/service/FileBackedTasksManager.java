package service;

import model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private File file; // файл с историей

    /**
     * конструктор для менеджера сохранения информации в файл
     *
     * @param file - объект файла
     */
    public FileBackedTasksManager(File file) {
        super();
        this.file = file;
    }

    /**
     * Сохранение информации в файл
     */
    private void save() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) { //открыли буфер и поток чтения
            bufferedWriter.write("id,type,name,status,description,epic \n"); //записываем строку с называниями полей
            for (var task : getTasks()) { //пробегаем по всем таскам
                bufferedWriter.write(task.toString() + "\n"); // и выводим их в файл в виде строки
            }
            for (var epic : getEpics()) { //пробегаем по всем эпикам
                bufferedWriter.write(epic.toString() + "\n"); // и выводим их в файл в виде строки
            }
            for (var subtask : getSubtasks()) { //пробегаем по всем сабтаскам
                bufferedWriter.write(subtask.toString() + "\n"); // и выводим их в файл в виде строки
            }
            bufferedWriter.newLine(); //делаем отступ между задачами и историей
            bufferedWriter.write(historyToString(getHistoryManager())); //сохраняем историю в файл
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка процедуры сохранения истории");
        }
    }

    /**
     * создание задачи любого типа по данным из строки
     * и добавление ее в соответствующий список менеджера задач
     *
     * @param value - строка с информацией о задаче
     * @return - задача, созданная по строке
     */
    public Task fromString(String value) {
        String[] data = value.split(","); // делим строку на части. разделитель - запятая
        //0-id, 1 - тип, 2 - название, 3 - статус, 4 - описание [5]-epicId
        switch (TaskType.valueOf(data[1])) { // в зависимости от типа задачи создаем нужный объект
            case TASK:
                Task task = new Task(data[2], data[4], Integer.valueOf(data[0])); //создаем новый объект таска
                task.setStatus(Status.valueOf(data[3])); //меняем статус на статус из файла
                createTask(task);
                return task;
            case EPIC:
                Epic epic = new Epic(data[2], data[4], Integer.valueOf(data[0])); //создаем новый объект эпика
                epic.setStatus(Status.valueOf(data[3]));//меняем статус на статус из файла
                createEpic(epic);
                return epic;
            case SUBTASK:
                Subtask subtask = new Subtask(data[2], data[4], Integer.valueOf(data[5]), Integer.valueOf(data[0]));
                subtask.setStatus(Status.valueOf(data[3])); //меняем статус на статус из файла
                createSubtask(subtask);
                return subtask;
        }
        return null; //Если ни один тип не подошел
    }

    /**
     * Конвертация истории просмотров в строку
     *
     * @param manager - менеджер истории просмотров
     * @return строка с id просмотренных задач
     */
    public static String historyToString(HistoryManager manager) {
        StringBuilder history = new StringBuilder();
        List<Task> historyList = manager.getHistory();
        int size = historyList.size();
        for (int i = 0; i < size; i++) {
            history.append(historyList.get(i).getId());
            if (i < size - 1) {
                history.append(",");
            }
        }
        return history.toString();
    }

    /**
     * конвертация строки в историю просмотров
     *
     * @param value - строка с историей
     * @return история просмотров в виде списка id
     */
    static List<Integer> historyFromString(String value) {
        List<Integer> result = new ArrayList<>();
        if (value != null) { //если строка не пустая
            String[] historyString = value.split(",");
            for (var s : historyString) { //проходим в цикле по всей истории
                result.add(Integer.parseInt(s)); //добавляем элементы в виде Integer в список
            }
        }
        return result;
    }

    /**
     * Восстановление менеджера задач из файла
     * Построчно считывается информация о задачах и об истории просмотров
     * Всё сохраняется в менеджер задач
     *
     * @param file - файл .csv
     * @return восстановленный менеджер
     */
    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) { //создали поток чтения и буферизации
            /*считываем и сохраняем в менеджере задачи/эпики/подзадачи*/
            String line = br.readLine(); //считали первую строку (служебную)
            line = br.readLine(); //считали первую строку с полезной информацией
            while (line.length() > 1) { //считываем строки, пока не наткнемся на строку с отступом \n
                fileBackedTasksManager.fromString(line); //сохраняем в менеджер задачу из строки
                line = br.readLine(); //считываем очередную строку
            }
            /*считываем и добавляем в менеджер историю просмотров*/
            line = br.readLine(); //считываем историю просмотров (строку, следующую после отступа)
            Task task = null; //вспомогательная переменная
            for (var id : historyFromString(line)) { //проходим по истории из файла и сохраняем ее как связный список
                if (fileBackedTasksManager.tasks.containsKey(id)) { //если нужный id в списке тасков
                    task = fileBackedTasksManager.tasks.get(id); //берем из списка таксов нужный таск
                } else if (fileBackedTasksManager.epics.containsKey(id)) { //если нужный id в списке эпиков
                    task = fileBackedTasksManager.epics.get(id); //берем из списка эпиков нужный эпик
                } else if (fileBackedTasksManager.subtasks.containsKey(id)) { //если нужный id в списке сабтасков
                    task = fileBackedTasksManager.subtasks.get(id); //берем из списка сабтасков нужный сабтаск
                }
                fileBackedTasksManager.historyManager.add(task); //и складываем взятую задачу в иторию просмотров
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileBackedTasksManager;
    }

    /**
     * вспомогательный метод для тестирования класса
     *
     * @param args
     */
    public static void main(String args) {
        File file = new File("src/file1.csv");
        TaskManager manager = Managers.getFileBackedTaskManager(file); //создаем объект менеджера
        Integer id; //переменная для временного хранения id при промежуточных действиях

        /*создаем две задачи*/
        manager.createTask(new Task("Посмотреть фильм", "Персонаж (2006 г)"));
        manager.createTask(new Task("Постричь собаку", "а то уже глаз не видно"));
        /* создаем эпик с тремя подзадачами*/
        id = manager.createEpic(new Epic("Приготовить суп", "лёгенький суп из бульона и зажарки"));
        manager.createSubtask(new Subtask("сварить бульон", "куриный", id));
        manager.createSubtask(new Subtask("сделать зажарку", "лук+морковь", id));
        manager.createSubtask(new Subtask("добавить зажарку в бульон", "вроде конец", id));
        /* создаем эпик без подзадач*/
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
        /*внесение изменений в истории просмотров*/
        manager.getTaskById(1);
        manager.getSubtaskById(3);
        manager.getSubtaskById(4);
        System.out.println("ИСТОРИЯ 2: " + manager.getHistory());
        /*создание нового менеджера по данным из файла*/
        FileBackedTasksManager fileBackedTasksManager = FileBackedTasksManager.loadFromFile(file);
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

    /**
     * Удаление всех задач
     */
    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    /**
     * удаление всех подзадач
     */
    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    /**
     * удаление всех эпиков.
     * При удалении эпиков также удаляются сабтаски
     */
    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    /**
     * получение таска по id
     *
     * @param id запрашиваемого таска
     * @return запрашиваемый таск
     */
    @Override
    public Task getTaskById(Integer id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    /**
     * получение сабтаска по id
     *
     * @param id запрашиваемого сабтаска
     * @return запрашиваемый сабтаск
     */
    @Override
    public Subtask getSubtaskById(Integer id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }

    /**
     * получение эпика по id
     *
     * @param id запрашиваемого эпика
     * @return запрашиваемый эпик
     */
    @Override
    public Epic getEpicById(Integer id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    /**
     * создание нового таска
     *
     * @param newTask - новый объект класса task
     * @return id созданного таска
     */
    @Override
    public Integer createTask(Task newTask) {
        Integer id = super.createTask(newTask);
        save();
        return id;
    }

    /**
     * создание нового сабтаска
     * При создании проверяем, что переданный сабтаск соответсвует условиям:
     * ссылка на сабтаск не null И сабтаск привязан к эпику И такой эпик существует
     *
     * @param newSubtask новый объект Subtask
     * @return id созданного сабтаска
     */
    @Override
    public Integer createSubtask(Subtask newSubtask) {
        Integer id = super.createSubtask(newSubtask);
        save();
        return id;
    }

    /**
     * создание нового эпика
     *
     * @param newEpic новый объект Epic
     * @return id созданного эпика
     */
    @Override
    public Integer createEpic(Epic newEpic) {
        Integer id = super.createEpic(newEpic);
        save();
        return id;
    }

    /**
     * обновление тасков
     *
     * @param updatedTask - обновленный таск
     */
    @Override
    public void updateTask(Task updatedTask) {
        super.updateTask(updatedTask);
        save();
    }

    /**
     * обновление сабтасков
     * При обновлении проверяем, что переданный сабтаск соответсвует условиям:
     * ссылка на сабтаск не null И сабтаск существует И его эпик существует
     *
     * @param updatedSubtask обновленный сабтаск
     */
    @Override
    public void updateSubtask(Subtask updatedSubtask) {
        super.updateSubtask(updatedSubtask);
        save();
    }

    /**
     * обновление эпиков
     *
     * @param updatedEpic - обновляемый эпик
     */
    @Override
    public void updateEpic(Epic updatedEpic) {
        super.updateEpic(updatedEpic);
        save();
    }

    /**
     * удаление одного таска по id
     *
     * @param id удаляемой задачи
     */
    @Override
    public void deleteTaskById(Integer id) {
        super.deleteTaskById(id);
        save();
    }

    /**
     * Удаление одного сабтаска по id
     *
     * @param id удалаемой подзадачи
     */
    @Override
    public void deleteSubtaskById(Integer id) {
        super.deleteSubtaskById(id);
        save();
    }

    /**
     * Удаление одного эпика по id
     *
     * @param id удаляемого эпика
     */
    @Override
    public void deleteEpicById(Integer id) {
        super.deleteEpicById(id);
        save();
    }
}
