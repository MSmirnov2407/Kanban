package servise;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Manager {
    private Map<Integer, Task> tasks;   //список задач
    private Map<Integer, Subtask> subtasks; //список подзадач
    private Map<Integer, Epic> epics; //список эпиков
    private Integer taskId; //id задач
    private Integer subtaskId; //id подзадач
    private Integer epicId; //id эпиков

    /**
     * конструктор менеджера
     */
    public Manager() {
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
        taskId = 0;
        subtaskId = 0;
        epicId = 0;
    }

    /**
     * Возвращаем содержимое мапы tasks в виде списка всех задач
     *
     * @return ArrayList<Task>
     */
    public ArrayList<Task> getTasks() {
        var taskList = new ArrayList<Task>();
        for (Task task : tasks.values()) {
            taskList.add(task);
        }
        return taskList;
    }

    /**
     * Возвращаем содержимое мапы subtasks в виде списка всех подзадач
     *
     * @return ArrayList<Subtask>
     */
    public ArrayList<Subtask> getSubtasks() {
        var subtaskList = new ArrayList<Subtask>();
        for (Subtask subtask : subtasks.values()) {
            subtaskList.add(subtask);
        }
        return subtaskList;
    }

    /**
     * Возвращаем содержимое мапы epics в виде списка всех эпиков
     *
     * @return ArrayList<Epic>
     */
    public ArrayList<Epic> getEpics() {

        var epicList = new ArrayList<Epic>();
        for (Epic epic : epics.values()) {
            epicList.add(epic);
        }
        return epicList;
    }

    /**
     * Удаление всех задач
     */
    public void deleteAllTasks() {
        tasks.clear();
    }

    /**
     * удаление всех подзадач
     */
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) { //для каждого эпика в общем списке эпиков
            epic.getSubtaskIds().clear(); //очищаем список подзадач
            updateEpicStatus(epic.getId()); //обновляем статус эпика (он станет NEW)
        }
        subtasks.clear();
    }

    /**
     * удаление всех эпиков
     */
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear(); //сабтаски не существуют без эпиков
    }

    /**
     * получение таска по id
     *
     * @param id
     * @return Task
     */
    public Task getTaskById(Integer id) {
        return tasks.get(id);
    }

    /**
     * получение сабтаска по id
     *
     * @param id
     * @return Subtask
     */
    public Subtask getSubtaskById(Integer id) {
        return subtasks.get(id);
    }

    /**
     * получение эпика по id
     *
     * @param id
     * @return Epic
     */
    public Epic getEpicById(Integer id) {
        return epics.get(id);
    }

    /**
     * создание нового таска
     *
     * @param newTask
     * @return Integer id
     */
    public Integer createTask(Task newTask) {
        if (newTask != null) {
            taskId += 1; //инкрементируем id тасков
            newTask.setId(taskId); //присваиваем новый id новому таску
            tasks.put(taskId, newTask); //складываем в хешмап
            return taskId;
        } else {
            System.out.println("Ошибка создания задачи: получена ссылка со значением null");
            return null;
        }
    }

    /**
     * создание нового сабтаска
     *
     * @param newSubtask
     * @return Integer id
     */
    public Integer createSubtask(Subtask newSubtask) {
        if ((newSubtask != null) && (newSubtask.getEpicId() != null)) {
            subtaskId += 1; //инкрементируем id сабтасков
            newSubtask.setId(subtaskId); //присваиваем новый id новому сабтаску
            epics.get(newSubtask.getEpicId()).addSubtask(newSubtask); //сохранили в эпике инфо о его новой подзадаче
            subtasks.put(subtaskId, newSubtask); //складываем в хешмап
            return subtaskId;
        } else {
            System.out.println("Ошибка создания подзадачи: получена ссылка со значением null");
            return null;
        }
    }

    /**
     * создание нового эпика
     *
     * @param newEpic
     * @return Integer id
     */
    public Integer createEpic(Epic newEpic) {
        if (newEpic != null) {
            epicId += 1; //инкрементируем id эпиков
            newEpic.setId(epicId); //присваиваем новый id новому эпику
            epics.put(epicId, newEpic); //складываем в хешмап
            return epicId;
        } else {
            System.out.println("Ошибка создания эпика: получена ссылка со значением null");
            return null;
        }
    }

    /**
     * обновление тасков
     *
     * @param freshTask
     */
    public void updateTask(Task freshTask) {
        if ((freshTask != null) && (tasks.containsKey(freshTask.getId()))) {
            tasks.put(freshTask.getId(), freshTask); //добавляем обновленную задачу в список, заменяя прежнюю
        } else {
            System.out.println("Невозможно обновить задачу!");
        }
    }

    /**
     * обновление сабтасков
     *
     * @param freshSubtask
     */
    public void updateSubtask(Subtask freshSubtask) {
        if ((freshSubtask != null) && (subtasks.containsKey(freshSubtask.getId()))
                && (freshSubtask.getEpicId() != null)) {
            subtasks.put(freshSubtask.getId(), freshSubtask); //добавляем обновленную подзадачу, заменяя прежнюю
            updateEpicStatus(freshSubtask.getEpicId()); //вызываем метод пересчета статуса эпика
        } else {
            System.out.println("Невозможно обновить подзадачу!");
        }
    }

    /**
     * обновление эпиков
     *
     * @param freshEpic
     */
    public void updateEpic(Epic freshEpic) {
        if ((freshEpic != null) && (epics.containsKey(freshEpic.getId()))) {
            epics.put(freshEpic.getId(), freshEpic); //добавляем обновленную задачу в список, заменяя прежний
        } else {
            System.out.println("Невозможно обновить эпик'!");
        }
    }

    /**
     * удаление одного таска по id
     *
     * @param id
     */
    public void deleteTaskById(Integer id) {
        tasks.remove(id);
    }

    /**
     * Удаление одного сабтаска по id
     *
     * @param id
     */
    public void deleteSubtaskById(Integer id) {
        if (subtasks.containsKey(id)) {
            if (getSubtaskById(id).getEpicId() != null) {
                /*из общего списка эпиков берем тот,на кот.ссылается сабтаск*/
                Epic e = epics.get(getSubtaskById(id).getEpicId());
                e.deleteSubtask(id); // в найденном эпике удаляем подзадачу
                updateEpicStatus(e.getId()); //обновляем статус эпика после удаления подзадачи
            }
            subtasks.remove(id); //удаляем сабтакс из общего списка подзадач
        } else {
            System.out.println("Ошибка удаления подзадачи: несуществующий ID");
        }
    }

    /**
     * Удаление одного эпика по id
     *
     * @param id
     */
    public void deleteEpicById(Integer id) {
        if (tasks.containsKey(id)) {
            for (Integer i : epics.get(id).getSubtaskIds()) { //пробегаем по списку id подзадач эпика
                subtasks.remove(i); //и удаляем эти подзадачи из общего списка подзадач (подзадачи не существуют без эпика)
            }
            epics.remove(id); //после этого удаляем сам эпик.
        }
    }

    /**
     * обновление статуса эпика в зависимости от статусов подзадач
     *
     * @param epicId
     */
    private void updateEpicStatus(Integer epicId) {
        Epic epic = epics.get(epicId); //по переданному id достаем из хешмапы нужный эпик
        int newAmount = 0; //количество подзадач со статусом NEW
        int inProgressAmount = 0; // ...IN_PROGRESS
        int doneAmount = 0; // ... DONE

        if (!epics.containsKey(epicId)) { //если передан ошибочный ключ, выходим из метода
            System.out.println("Manager.updateEpicStatus: нет эпика с таким ключем");
            return;
        }
        if (epic.getSubtaskIds().isEmpty()) { //если подзадач нет, то статус эпика NEW
            epic.setStatus(Status.NEW);
        } else { //иначе
            int subtaskAmount = 0; //объявляем переменную для хранения общего кол-ва подзадач эпика
            for (Integer s : epic.getSubtaskIds()) { //цикл по всем id подзадач данного эпика. посчитаем кол-во подзадач
                Subtask subtask = subtasks.get(s); //берем из хешмапы подзадачу с нужным id
                subtaskAmount += 1; //подсчитываем количество подхадач
                switch (subtask.getStatus()) { //в зависимости от статуса подзадачи, увеличиваем нужный счетчик
                    case NEW:
                        newAmount += 1; //увеличиваем счетчик новых задач
                        break;
                    case IN_PROGRESS:
                        inProgressAmount += 1; //увеличиваем счетчик выполняемых задач
                        break;
                    case DONE:
                        doneAmount += 1; //увеличиваем счетчик выполненных задач
                        break;
                }//switch
            } //for
            if (doneAmount == subtaskAmount) { //если все подзадачи завершены
                epic.setStatus(Status.DONE);
            } else if (newAmount == subtaskAmount) { //если все подзадачи новые
                epic.setStatus(Status.NEW);
            } else { //если не все задачи завершены и не все задачи новые, то статус "в процессе"
                epic.setStatus(Status.IN_PROGRESS);
            }
        } //if (epic.getSubtasks().isEmpty())
    }
}
