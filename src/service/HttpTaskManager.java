package service;

import com.google.gson.*;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.ManagerSaveException;
import model.Subtask;
import model.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static service.FileBackedTasksManager.historyToString;

public class HttpTaskManager extends InMemoryTaskManager {

    KVTaskClient kvClient; //http-клиент для сервера-хранилища
    Gson gson; //для работы с json

    public HttpTaskManager(String storageServerURL) {
        super();
        try {
            kvClient = new KVTaskClient(storageServerURL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Сохранение всей информации из менеджера (таски,эпики, сабтаски, история) в виде json На сервер-хранилище
     */
    private void save() {
        try {
            JsonObject jsonManager = new JsonObject();
            jsonManager.addProperty("tasks", gson.toJson(tasks));
            jsonManager.addProperty("epics", gson.toJson(epics));
            jsonManager.addProperty("subtasks", gson.toJson(subtasks));
            jsonManager.addProperty("history", gson.toJson(historyManager.getHistory()));

            kvClient.put("HttpTaskManagerSerialized", jsonManager.toString());
        } catch (Exception e) {
            throw new ManagerSaveException("Ошибка процедуры сохранения истории");
        }
    }

    /**
     * Выгрузка состояния ТаскМенеджера с сервера-хранилища
     * выгружаются таски, сабтаски, эпики, история
     */
    public void loadFromServer() {
        try {
            Type takskMapType = new TypeToken<Map<Integer, Task>>() {}.getType(); //тип мапы с тасками
            Type epicsMapType = new TypeToken<Map<Integer, Epic>>() {}.getType(); //тип мапы с Эпиками
            Type subtasksMapType = new TypeToken<Map<Integer, Subtask>>() {}.getType(); //тип мапы с сабтасками
            Type tasksHistoryListType = new TypeToken<List<Task>>() {}.getType(); //тип списка с историей

            String loadedManagerState = kvClient.load("HttpTaskManagerSerialized"); //выгруженное состояниие менеджера
            JsonElement jsonElement = JsonParser.parseString(loadedManagerState); //преобр.строку в JsonElement

            /*jsonElement превращаем в JsonObject и берем одно из его полей по названию поля */
            JsonElement historyAsJsonElement = jsonElement.getAsJsonObject().get("history");
            /*преобразуем взятое поле из JsonElement в заявленный тип*/
            List<Task> historyDeserialized = new Gson().fromJson(historyAsJsonElement.getAsString(), tasksHistoryListType);
            historyDeserialized.stream()
                    .forEach(task -> historyManager.add(task)); //по считанному списку воссоздадим историю в менеджере

            /*jsonElement превращаем в JsonObject и берем одно из его полей по названию поля */
            JsonElement tasksAsJsonElement = jsonElement.getAsJsonObject().get("tasks");
            /*преобразуем взятое поле из JsonElement в заявленный тип*/
            Map<Integer, Task> tasksDeserialized = new Gson().fromJson(tasksAsJsonElement.getAsString(), takskMapType);
            tasksDeserialized.entrySet().stream()
                    .forEach(task -> createTask(task.getValue())); //по считанной мапе создаем таски в менеджере

            /*аналогично для эпиков*/
            JsonElement epicsAsJsonElement = jsonElement.getAsJsonObject().get("epics");
            Map<Integer, Epic> epicsDeserialized = new Gson().fromJson(epicsAsJsonElement.getAsString(), epicsMapType);
            epicsDeserialized.entrySet().stream()
                    .forEach(epic -> createEpic(epic.getValue())); //по считанной мапе создаем эпики в менеджере

            /*аналогично для сабтасков*/
            JsonElement subtasksAsJsonElement = jsonElement.getAsJsonObject().get("subtasks");
            Map<Integer, Subtask> subtasksDeserialized = new Gson().fromJson(subtasksAsJsonElement.getAsString(), subtasksMapType);
            subtasksDeserialized.entrySet().stream()
                    .forEach(subtask -> createSubtask(subtask.getValue())); //по считанной мапе создаем сабтаски в менеджере

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
