package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected Map<Integer, Task> tasks;   //список задач
    protected Map<Integer, Subtask> subtasks; //список подзадач
    protected Map<Integer, Epic> epics; //список эпиков
    protected int id; // id для всех типов задач
    protected HistoryManager historyManager; //менеджер истории просмотров

    protected TreeSet<Task> prioritizedTasks; //treeSet для хранения тасков и сабтасков отсортированными по startTime

    /**
     * конструктор менеджера
     */
    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
        historyManager = Managers.getDefaultHistory(); //создаем менеджер историй по умолчанию
        prioritizedTasks = new TreeSet<>(new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                //если одна задача начинается позже доугой,то она по порядку выполнения стоит "правее", т.е.она "больше"
                if (o1.getStartTime().isAfter(o2.getStartTime())) {
                    return 1;
                } else if (o1.getStartTime().isBefore(o2.getStartTime())) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
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
        for (var taskId : tasks.keySet()) { //удаляем из истории просмотров все таски
            prioritizedTasks.remove(getTaskById(taskId)); //также удаляем из сортированного по приоритетам трисета.
            historyManager.remove(taskId);
        }
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
            updateEpicTime(epic.getId()); //пересчитываем временныве рамки эпика
        }
        removeSubtaskFromHistoryAndPriority();//удаляем из истории просмотров и из сортированного по приоритетам трисета
        subtasks.clear();
    }

    /**
     * удаление всех эпиков.
     * При удалении эпиков также удаляются сабтаски
     */
    @Override
    public void deleteAllEpics() {
        for (var epicId : epics.keySet()) { //удаляем из истории просмотров все эпики
            historyManager.remove(epicId);
        }
        removeSubtaskFromHistoryAndPriority();//удаляем из истории просмотров и из сортированного по приоритетам трисета
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
        historyManager.add(task); //сохранили в истории просмотров
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
        historyManager.add(subtask); //сохранили в истории просмотров
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
        historyManager.add(epic); //сохранили в истории просмотров
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
            if (!isTimeOverlay(newTask)) { //если нет наложений по времени
                if (newTask.getId() == null) { //если получили таск с незаполненным id, то генерируем ему новый номер
                    int taskId = generateId(); //формируем id для таска
                    newTask.setId(taskId); //присваиваем новый id новому таску
                } else if (newTask.getId() > this.id) { //иначе сместить текущий "счетчик" id, чтобы не случилось повтора
                    this.id = newTask.getId() + 1;
                }
                tasks.put(newTask.getId(), newTask); //складываем в хешмап
                prioritizedTasks.add(newTask); // добавили в сортированный по приоритетам (по времени начала) treeSet

                return newTask.getId();
            } else {
                System.out.println("Ошибка создания задачи: обнаружено наложение в расписании");
            }
        } else {
            System.out.println("Ошибка создания задачи: получена ссылка со значением null");
        }
        return null;
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
            if (!isTimeOverlay(newSubtask)) { //если нет наложений по времени
                Integer epicId = newSubtask.getEpicId(); //сохранили значение id Эпика,к кот. привязан новый сабтаск
                if (epicId != null && epics.containsKey(epicId)) {
                    if (newSubtask.getId() == null) { //если получили таск с незаполненным id, то генерируем ему новый номер
                        int subtaskId = generateId(); //формируем id для сабтаска
                        newSubtask.setId(subtaskId); //присваиваем новый id новому сабтаску
                    } else if (newSubtask.getId() > this.id) { //иначе сместить  "счетчик" id, чтобы не случилось повтора
                        this.id = newSubtask.getId() + 1;
                    }
                    epics.get(epicId).addSubtask(newSubtask); //сохранили в эпике инфо о его новой подзадаче
                    subtasks.put(newSubtask.getId(), newSubtask); //складываем в хешмап
                    prioritizedTasks.add(newSubtask); // добавили в сортированный по приоритетам (по времени начала) treeSet
                    updateEpicStatus(epicId); //обновляем статус эпика
                    updateEpicTime(epicId); //пересчитываем временные рамки эпика
                    return newSubtask.getId();
                } else {
                    System.out.println("Ошибка создания подзадачи: недостоверное значение Id эпика");
                }
            } else {
                System.out.println("Ошибка создания задачи: обнаружено наложение в расписании");
            }
        } else {
            System.out.println("Ошибка создания подзадачи: получена ссылка со значением null");
        }
        return null;
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
            if (newEpic.getId() == null) { //если получили таск с незаполненным id, то генерируем ему новый номер
                int epicId = generateId(); //формируем id для эпика
                newEpic.setId(epicId); //присваиваем новый id новому эпику
            } else if (newEpic.getId() > this.id) { //иначе сместить текущий "счетчик" id, чтобы не случилось повтора
                this.id = newEpic.getId() + 1;
            }
            epics.put(newEpic.getId(), newEpic); //складываем в хешмап
            return newEpic.getId();
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
                prioritizedTasks.remove(tasks.get(updatedTaskId)); //удалили старый таск из сортированного трисета
                tasks.put(updatedTaskId, updatedTask); //добавляем обновленную задачу в список, заменяя прежнюю
                prioritizedTasks.add(tasks.get(updatedTaskId)); //добавили обновленный таск в сортированный трисет
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
                prioritizedTasks.remove(subtasks.get(updatedSubtaskId)); //удалили старый сабтаск из трисета
                subtasks.put(updatedSubtaskId, updatedSubtask); //добавляем обновленную подзадачу, заменяя прежнюю
                prioritizedTasks.add(subtasks.get(updatedSubtaskId)); //добавили новый сабтаск в трисет
                updateEpicStatus(epicId); //вызываем метод пересчета статуса эпика
                updateEpicTime(epicId); //пересчитываем временные рамки эпика
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
        if (updatedEpic != null) {
            Integer updatedEpicId = updatedEpic.getId(); // сохранили в переменную id переданного эпика
            if (updatedEpic != null && epics.containsKey(updatedEpicId)) {
                epics.put(updatedEpicId, updatedEpic); //добавляем обновленную задачу в список, заменяя прежний
            } else {
                System.out.println("Невозможно обновить эпик'!");
            }
        } else {
            System.out.println("Невозможно обновить эпик. передан null!");

        }
    }

    /**
     * удаление одного таска по id
     *
     * @param id удаляемой задачи
     */
    @Override
    public void deleteTaskById(Integer id) {
        if (tasks.containsKey(id)) {
            prioritizedTasks.remove(tasks.get(id)); //удалили из сортированного по приоритетам трисета
            tasks.remove(id);
            historyManager.remove(id); // удаляем таск из истории просмотров
        }
    }

    /**
     * Удаление одного сабтаска по id
     *
     * @param id удалаемой подзадачи
     */
    @Override
    public void deleteSubtaskById(Integer id) {
        if (subtasks.containsKey(id)) {
            Integer epicId = subtasks.get(id).getEpicId(); //id эпика, на кот.ссылается сабтаск
            if (epicId != null && epics.containsKey(epicId)) {
                /*из общего списка эпиков берем тот,на кот.ссылается сабтаск*/
                Epic e = epics.get(epicId);
                e.deleteSubtask(id); // в найденном эпике удаляем подзадачу
                updateEpicStatus(e.getId()); //обновляем статус эпика после удаления подзадачи
                updateEpicTime(e.getId()); //пересчитываем временные рамки эпика
            }
            prioritizedTasks.remove(subtasks.get(id)); //удалили из сортированного по приоритетам трисета
            subtasks.remove(id); //удаляем сабтакс из общего списка подзадач
            historyManager.remove(id); // удаляем сабтаск из истории просмотров
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
                historyManager.remove(i); // также удаляем сабтаск из истории просмотров
            }
            epics.remove(id); //после этого удаляем сам эпик.
            historyManager.remove(id); // удаляем эпик из истории просмотров
        }
    }

    /**
     * Вернуть историю просмотров задач
     *
     * @return список Тасков/подтасков/эпиков
     */
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    /**
     * Вернуть таски и сабтаски в порядке очередности
     *
     * @return список тасков-сабтасков в порядке очередности
     */
    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<Task>(prioritizedTasks);
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
            System.out.println("Manager.updateEpicStatus: нет эпика с таким ключом");
            return;
        }
        if (epic.getSubtaskIds().isEmpty()) { //если подзадач нет, то статус эпика NEW
            epic.setStatus(Status.NEW);
        } else {
            for (Integer s : epic.getSubtaskIds()) { //цикл по всем id подзадач данного эпика. посчитаем кол-во подзадач
                Subtask subtask = subtasks.get(s); //берем из хешмапы подзадачу с нужным id
                if(subtask != null) { //если это не проверить, то при выгрузке данных
                                        // из внешнего хоранилища могут возникнуть проблемы
                                        //когда эпик со списком сабтасков уже создан, а сами сабтаски еще не все созданы
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
                }
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

    /**
     * метод пересчитывает временные рамки эпика в зависимости от рамок его сабтасков
     *
     * @param epicId - id эпика
     */
    private void updateEpicTime(Integer epicId) {
        Epic epic = epics.get(epicId); //берем эпик по переданному id
        if (!epic.getSubtaskIds().isEmpty()) {
            LocalDateTime epicStartTime = LocalDateTime.MAX; //переменная для хранения времени начала эпика
            LocalDateTime epicEndTime = LocalDateTime.MIN; //переменная для хранения времени окончания эпика
            for (var subtaskId : epic.getSubtaskIds()) { //проходим по всем сабтаскам данного эпика
                Subtask subtask = subtasks.get(subtaskId); //берем сабтаск по id
                if(subtask != null) { //если это не проверить, то при выгрузке данных
                    // из внешнего хоранилища могут возникнуть проблемы
                    //когда эпик со списком сабтасков уже создан, а сами сабтаски еще не все созданы
                    if (subtask.getStartTime().isBefore(epicStartTime)) { //если у сабтаска начало раньше, чем текущий старт
                        epicStartTime = subtask.getStartTime(); //запоминаем новый минимум (время начала)
                    }
                    if (subtask.getEndTime().isAfter(epicEndTime)) { //если у сабтаска время конца позже, чем текущий финиш
                        epicEndTime = subtask.getEndTime(); //запоминаем новый минимум (время начала)
                    }
                    /*увеличиваем продолжительность эпика на продолжительность текушего сабтаска*/
                    epic.setDuration(epic.getDuration() + subtask.getDuration());
                }
            }
            epic.setStartTime(epicStartTime); //перезаписываем время начала эпика
            epic.setEndTime(epicEndTime); //перезаписываем время конца эпика
        }
    }

    /**
     * Генерация id для таксков/сабтасков/эпиков
     *
     * @return сгенерированный id
     */
    private int generateId() {
        return id++;
    }

    /**
     * геттер для поля historyManager
     *
     * @return ссылку на historyManager
     */
    public HistoryManager getHistoryManager() {
        return this.historyManager;
    }

    /**
     * метод проверяет, есть ли пересечение веремени выполнения переданной задачи с другими задачами/подзадачами.
     *
     * @param newTask - проверяемый таск/сабтаск
     * @return результат проверки наличия пересечений времени.true - есть пересечение
     */
    private boolean isTimeOverlay(Task newTask) {
        for (var task : getPrioritizedTasks()) { //проходим по всем задачам/подзадачам в дереве
            if (newTask.getStartTime().isAfter(task.getStartTime())
                    && newTask.getStartTime().isBefore(task.getEndTime()) //если начало таска лежит внутри другого таска
                    || newTask.getEndTime().isAfter(task.getStartTime())
                    && newTask.getEndTime().isBefore(task.getEndTime()) //или конец таска лежит внутри другого таска
                    || newTask.getStartTime().isBefore(task.getStartTime())
                    && newTask.getEndTime().isAfter(task.getEndTime())) { //или таск полностью соджержит другой таск
                return true;
            }
        }
        return false;
    }

    /**
     * удаление сабтасков из истории просмотров задач
     * и из отсортированного по времени старта трисета
     */
    private void removeSubtaskFromHistoryAndPriority(){
        for (var subtaskId : subtasks.keySet()) { //удаляем из истории просмотров все сабтаски
            prioritizedTasks.remove(getSubtaskById(subtaskId)); //также удаляем из сортированного по приоритетам трисета
            historyManager.remove(subtaskId);
        }
    }
}
