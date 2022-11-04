package servise;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private Map<Integer, Task> tasks;   //список задач
    private Map<Integer, Subtask> subtasks; //список подзадач
    private Map<Integer, Epic> epics; //список эпиков
    private int taskId; //id задач, инициализируется по умолчанию 0
    private int subtaskId; //id подзадач, инициализируется по умолчанию 0
    private int epicId; //id эпиков, инициализируется по умолчанию 0
    private final ArrayList<Task> viewedTaskHistory; //история просмотров
    private final int maxHistoryLength = 10; //максимальное количество записей в истории

    /**
     * конструктор менеджера
     */
    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
        viewedTaskHistory = new ArrayList<>();
    }

    /**
     * Возвращаем содержимое мапы tasks в виде списка всех задач
     *
     * @return список Тасков
     */
    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * Возвращаем содержимое мапы subtasks в виде списка всех подзадач
     *
     * @return Список Сабтасков
     */
    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    /**
     * Возвращаем содержимое мапы epics в виде списка всех эпиков
     *
     * @return Список эпиков
     */
    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    /**
     * Удаление всех задач
     */
    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    /**
     * удаление всех подзадач
     */
    @Override
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
    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear(); //сабтаски не существуют без эпиков
    }

    /**
     * получение таска по id
     *
     * @param id запрашиваемого таска
     * @return запрашиваемый таск
     */
    @Override
    public Task getTaskById(Integer id) {
        Task task = tasks.get(id); //взяли таск из общего списка
        viewedTaskHistory.add(task); //сохранили в истории просмотров
        if (viewedTaskHistory.size() > maxHistoryLength) { //если длина истории превысила 10 запросов
            viewedTaskHistory.remove(0); //удаляем самый старый
        }
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
        Subtask subtask = subtasks.get(id); //взяли сабтаск из общего списка
        viewedTaskHistory.add(subtask); //сохранили в истории просмотров
        if (viewedTaskHistory.size() > maxHistoryLength) { //если длина истории превысила 10 запросов
            viewedTaskHistory.remove(0); //удаляем самый старый
        }
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
        Epic epic = epics.get(id); //взяли сабтаск из общего списка
        viewedTaskHistory.add(epic); //сохранили в истории просмотров
        if (viewedTaskHistory.size() > maxHistoryLength) { //если длина истории превысила 10 запросов
            viewedTaskHistory.remove(0); //удаляем самый старый
        }
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
     * При создании проверяем, что переданный сабтаск соответсвует условиям:
     * ссылка на сабтаск не null И сабтаск привязан к эпику И такой эпик существует
     *
     * @param newSubtask новый объект Subtask
     * @return id созданного сабтаска
     */
    @Override
    public Integer createSubtask(Subtask newSubtask) {
        if (newSubtask != null) {
            Integer epicId = newSubtask.getEpicId(); //сохранили значение id Эпика,к кот. привязан новый сабтаск
            if (epicId != null && epics.containsKey(epicId)) {
                subtaskId += 1; //инкрементируем id сабтасков
                newSubtask.setId(subtaskId); //присваиваем новый id новому сабтаску
                epics.get(epicId).addSubtask(newSubtask); //сохранили в эпике инфо о его новой подзадаче
                subtasks.put(subtaskId, newSubtask); //складываем в хешмап
                return subtaskId;
            } else {
                System.out.println("Ошибка создания подзадачи: недостоверное значение Id эпика");
                return null;
            }
        } else {
            System.out.println("Ошибка создания подзадачи: получена ссылка со значением null");
            return null;
        }
    }

    /**
     * создание нового эпика
     *
     * @param newEpic новый объект Epic
     * @return id созданного эпика
     */
    @Override
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
     * @param updatedTask - обновленный таск
     */
    @Override
    public void updateTask(Task updatedTask) {
        if (updatedTask != null) {
            Integer updatedTaskId = updatedTask.getId(); //сохранили в переменную значение id переданного таска
            if (tasks.containsKey(updatedTaskId)) {
                tasks.put(updatedTaskId, updatedTask); //добавляем обновленную задачу в список, заменяя прежнюю
            } else {
                System.out.println("Ошибка обновления задачи: не найден Таск по id");
            }
        } else {
            System.out.println("Ошибка обновления задачи: получена ссылка со значением null");
        }
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
        if (updatedSubtask != null) {
            Integer epicId = updatedSubtask.getEpicId(); //значение id Эпика,к кот. привязан сабтаск
            Integer updatedSubtaskId = updatedSubtask.getId(); //id переданного сабтаска
            if (subtasks.containsKey(updatedSubtaskId) && epicId != null && epics.containsKey(epicId)) {
                subtasks.put(updatedSubtaskId, updatedSubtask); //добавляем обновленную подзадачу, заменяя прежнюю
                updateEpicStatus(epicId); //вызываем метод пересчета статуса эпика
            } else {
                System.out.println("Ошибка обновления подзадачи: на найден сабтаск или эпик");
            }
        } else {
            System.out.println("Ошибка обновления подзадачи: получена ссылка со знаением null");
        }
    }

    /**
     * обновление эпиков
     *
     * @param updatedEpic - обновляемый эпик
     */
    @Override
    public void updateEpic(Epic updatedEpic) {
        Integer updatedEpicId = updatedEpic.getId(); // сохранили в переменную id переданного эпика
        if (updatedEpic != null && epics.containsKey(updatedEpicId)) {
            epics.put(updatedEpicId, updatedEpic); //добавляем обновленную задачу в список, заменяя прежний
        } else {
            System.out.println("Невозможно обновить эпик'!");
        }
    }

    /**
     * удаление одного таска по id
     *
     * @param id удаляемой задачи
     */
    @Override
    public void deleteTaskById(Integer id) {
        tasks.remove(id);
    }

    /**
     * Удаление одного сабтаска по id
     *
     * @param id удалаемой подзадачи
     */
    @Override
    public void deleteSubtaskById(Integer id) {
        if (subtasks.containsKey(id)) {
            Integer epicId = getSubtaskById(id).getEpicId(); //id эпика, на кот.ссылается сабтаск
            if (epicId != null && epics.containsKey(epicId)) {
                /*из общего списка эпиков берем тот,на кот.ссылается сабтаск*/
                Epic e = epics.get(epicId);
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
     * @param id удаляемого эпика
     */
    @Override
    public void deleteEpicById(Integer id) {
        if (epics.containsKey(id)) {
            for (Integer i : epics.get(id).getSubtaskIds()) { //пробегаем по списку id подзадач эпика
                subtasks.remove(i); //и удаляем эти подзадачи из общего списка подзадач (подзадачи не существуют без эпика)
            }
            epics.remove(id); //после этого удаляем сам эпик.
        }
    }

    /**
     * Вернуть историю просмотров задач
     *
     * @return список Тасков/подтасков/эпиков
     */
    @Override
    public List<Task> getHistory() {
        return viewedTaskHistory;
    }

    /**
     * обновление статуса эпика в зависимости от статусов подзадач
     *
     * @param epicId - id обновляемого эпика
     */
    private void updateEpicStatus(Integer epicId) {
        Epic epic = epics.get(epicId); //по переданному id достаем из хешмапы нужный эпик
        boolean hasNewSubtasks = false; //наличие сабтасков со статусом NEW
        boolean hasInProgressSubtasks = false; // ...IN_PROGRESS
        boolean hasDoneSubtasks = false; // ... DONE

        if (!epics.containsKey(epicId)) { //если передан ошибочный ключ, выходим из метода
            System.out.println("Manager.updateEpicStatus: нет эпика с таким ключем");
            return;
        }
        if (epic.getSubtaskIds().isEmpty()) { //если подзадач нет, то статус эпика NEW
            epic.setStatus(Status.NEW);
        } else {
            for (Integer s : epic.getSubtaskIds()) { //цикл по всем id подзадач данного эпика. посчитаем кол-во подзадач
                Subtask subtask = subtasks.get(s); //берем из хешмапы подзадачу с нужным id
                switch (subtask.getStatus()) { //в зависимости от статуса подзадачи, увеличиваем нужный счетчик
                    case NEW:
                        hasNewSubtasks = true; // взводим флаг наличия подзадач в состоянии NEW
                        break;
                    case IN_PROGRESS:
                        hasInProgressSubtasks = true; // взводим флаг наличия подзадач в состоянии IN_PROGRESS
                        break;
                    case DONE:
                        hasDoneSubtasks = true; // взводим флаг наличия подзадач в состоянии DONE
                        break;
                }//switch
            } //for
            if (!hasNewSubtasks && !hasInProgressSubtasks && hasDoneSubtasks) { //если все подзадачи завершены
                epic.setStatus(Status.DONE);
            } else if (hasNewSubtasks && !hasInProgressSubtasks && !hasDoneSubtasks) { //если все подзадачи новые
                epic.setStatus(Status.NEW);
            } else { //если не все задачи завершены и не все задачи новые, то статус "в процессе"
                epic.setStatus(Status.IN_PROGRESS);
            }
        } //if (epic.getSubtasks().isEmpty())
    }
}
