package Servise;

import Model.Epic;
import Model.Subtask;
import Model.Task;
import java.util.HashMap;

public class Manager {
    private HashMap<Integer, Task> tasks;   //список задач
    private HashMap<Integer, Subtask> subtasks; //список подзадач
    private HashMap<Integer, Epic> epics; //список эпиков
    private Integer task_id; //id задач
    private Integer subtask_id; //id подзадач
    private Integer epic_id; //id эпиков

    public Manager() {
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
        task_id = 0;
        subtask_id = 0;
        epic_id = 0;
    } //конструктор менеджера

    public HashMap<Integer, Task> getTasks() {
        return tasks;
    } //возвращаем список всех задач

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    } //возвращаем список всех подзадач

    public HashMap<Integer, Epic> getEpics() {
        return epics;
    } //возвращаем список всех эпиков

    public void deleteAllTasks() {
        tasks.clear();
    } //удаление всех задач

    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) { //для каждого эпика в общем списке эпиков
            epic.getSubtasks().clear(); //очищаем список подзадач
            updateEpicStatus(epic.getId()); //обновляем статус эпика (он станет NEW)
        }
        subtasks.clear();
    } //удаление всех подзадач

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear(); //сабтаски не существуют без эпиков
    } //удаление всех эпиков

    public Task getOneTask(Integer id) {
        return tasks.get(id);
    } //получение таска по id

    public Subtask getOneSubask(Integer id) {
        return subtasks.get(id);
    } //получение сабтаска по id

    public Epic getOneEpic(Integer id) {
        return epics.get(id);
    } //получение эпика по id

    public Integer createTask(Task newTask) {
        if (newTask != null) {
            task_id += 1; //инкрементируем id тасков
            newTask.setId(task_id); //присваиваем новый id новому таску
            tasks.put(task_id, newTask); //складываем в хешмап
            return task_id;
        } else {
            return 0;
        }
    } //создаем новый таск

    public Integer createSubtask(Subtask newSubtask, Epic epic) {
        if ((newSubtask != null) && (epic != null)) {
            subtask_id += 1; //инкрементируем id сабтасков
            newSubtask.setId(subtask_id); //присваиваем новый id новому сабтаску
            newSubtask.setEpicId(epic.getId()); //присваиваем подзадаче номер ее эпика
            epic.addSubtask(newSubtask); //сохранили в эпике информацию о его новой подзадаче
            subtasks.put(subtask_id, newSubtask); //складываем в хешмап
            return subtask_id;
        } else {
            return 0;
        }
    } //создаем новый сабтаск

    public Integer createEpic(Epic newEpic) {
        if (newEpic != null) {
            epic_id += 1; //инкрементируем id эпиков
            newEpic.setId(epic_id); //присваиваем новый id новому эпику
            epics.put(epic_id, newEpic); //складываем в хешмап
            return epic_id;
        } else {
            return 0;
        }
    } //создаем новый эпик

    public void refreshTask(Task freshTask) {
        if ((freshTask != null) && (tasks.containsKey(freshTask.getId()))) {
            tasks.remove(freshTask.getId()); //удаляем из списка задач ту, которая совпадает по id с передаваемой
            tasks.put(freshTask.getId(), freshTask); //добавляем обновленную задачу в список
        } else {
            System.out.println("Невозможно обновить задачу!");
        }
    } //обновление тасков

    public void refreshSubtask(Subtask freshSubtask) {
        if ((freshSubtask != null) && (subtasks.containsKey(freshSubtask.getId()))) {
            subtasks.remove(freshSubtask.getId()); //удаляем из списка задач ту, которая совпадает по id с передаваемой
            subtasks.put(freshSubtask.getId(), freshSubtask); //добавляем обновленную задачу в список
            updateEpicStatus(freshSubtask.getEpicId()); //вызываем метод пересчета статуса эпика
        } else {
            System.out.println("Невозможно обновить подзадачу!");
        }
    } //обновление сабтасков

    public void refreshEpic(Epic freshEpic) {
        if ((freshEpic != null) && (epics.containsKey(freshEpic.getId()))) {
            epics.remove(freshEpic.getId()); //удаляем из списка задач ту, которая совпадает по id с передаваемой
            epics.put(freshEpic.getId(), freshEpic); //добавляем обновленную задачу в список
        } else {
            System.out.println("Невозможно обновить эпик'!");
        }
    } //обновление эпиков

    public void deleteOneTask(Integer id) {
        tasks.remove(id);
    } //удаление одного таска

    public void deleteOneSubtask(Integer id) {
        Epic e = epics.get(getOneSubask(id).getEpicId()); //из общего списка эпиков берем тот,на кот.ссылается сабтаск
        e.deleteOneSubtask(id); // в найденном эпике удаляем подзадачу
        updateEpicStatus(e.getId()); //обновляем статус эпика после удаления подзадачи
        subtasks.remove(id); //удаляем сабтакс из общего списка подзадач
    } //Удаление одного сабтаска

    public void deleteOneEpic(Integer id) {
        for (Integer i : epics.get(id).getSubtasks()) { //пробегаем по списку id подзадач эпика
            subtasks.remove(i); //и удаляем эти подзадачи из общего списка подзадач (подзадачи не существуют без эпика)
        }
        epics.remove(id); //после этого удаляем сам эпик.
    } //Удаление одного эпика

    private void updateEpicStatus(Integer epicId) {
        Epic epic = epics.get(epicId); //по переданному id достаем из хешмапы нужный эпик
        int newAmount = 0; //количество подзадач со статусом NEW
        int inProgressAmount = 0; // ...IN_PROGRESS
        int doneAmount = 0; // ... DONE

        if (!epics.containsKey(epicId)) { //если передан ошибочный ключ, выходим из метода
            System.out.println("Manager.updateEpicStatus: нет эпика с таким ключем");
            return;
        }
        if (epic.getSubtasks().isEmpty()) { //если подзадач нет, то статус эпика NEW
            epic.setStatus("NEW");
        } else { //иначе
            int subtaskAmount = 0; //объявляем переменную для хранения общего кол-ва подзадач эпика
            for (Integer s : epic.getSubtasks()) { //цикл по всем id подзадач данного эпика. посчитаем кол-во подзадач
                Subtask subtask = subtasks.get(s); //берем из хешмапы подзадачу с нужным id
                subtaskAmount += 1; //подсчитываем количество подхадач
                switch (subtask.getStatus()) { //в зависимости от статуса подзадачи, увеличиваем нужный счетчик
                    case "NEW":
                        newAmount += 1; //увеличиваем счетчик новых задач
                        break;
                    case "IN_PROGRESS":
                        inProgressAmount += 1; //увеличиваем счетчик выполняемых задач
                        break;
                    case "DONE":
                        doneAmount += 1; //увеличиваем счетчик выполненных задач
                        break;
                }//switch
            } //for
            if (doneAmount == subtaskAmount) { //если все подзадачи завершены
                epic.setStatus("DONE");
            } else if (newAmount == subtaskAmount) { //если все подзадачи новые
                epic.setStatus("NEW");
            } else { //если не все задачи завершены и не все задачи новые, то статус "в процессе"
                epic.setStatus("IN_PROGRESS");
            }
        } //if (epic.getSubtasks().isEmpty())
    } //обновляем статус эпика
}
