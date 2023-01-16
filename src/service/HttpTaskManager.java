package service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.ManagerSaveException;
import model.Subtask;
import model.Task;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class HttpTaskManager extends FileBackedTasksManager {

    private KVTaskClient kvClient; //http-клиент для сервера-хранилища
    private final Gson gson; //для работы с json

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
    @Override
    protected void save() {
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
            Type takskMapType = new TypeToken<Map<Integer, Task>>() {
            }.getType(); //тип мапы с тасками
            Type epicsMapType = new TypeToken<Map<Integer, Epic>>() {
            }.getType(); //тип мапы с Эпиками
            Type subtasksMapType = new TypeToken<Map<Integer, Subtask>>() {
            }.getType(); //тип мапы с сабтасками
            Type tasksHistoryListType = new TypeToken<List<Task>>() {
            }.getType(); //тип списка с историей

            String loadedManagerState = kvClient.load("HttpTaskManagerSerialized"); //выгруженное состояниие менеджера
            JsonElement jsonElement = JsonParser.parseString(loadedManagerState); //преобр.строку в JsonElement

            /*jsonElement превращаем в JsonObject и берем одно из его полей по названию поля */
            JsonElement historyAsJsonElement = jsonElement.getAsJsonObject().get("history");
            /*преобразуем взятое поле из JsonElement в заявленный тип*/
            List<Task> historyDeserialized = new Gson().fromJson(historyAsJsonElement.getAsString(), tasksHistoryListType);
            historyDeserialized.forEach(task -> historyManager.add(task)); //по считанному списку воссоздадим историю в менеджере

            /*jsonElement превращаем в JsonObject и берем одно из его полей по названию поля */
            JsonElement tasksAsJsonElement = jsonElement.getAsJsonObject().get("tasks");
            /*преобразуем взятое поле из JsonElement в заявленный тип*/
            Map<Integer, Task> tasksDeserialized = new Gson().fromJson(tasksAsJsonElement.getAsString(), takskMapType);
            tasksDeserialized.forEach((key, value) -> createTask(value)); //по считанной мапе создаем таски в менеджере

            /*аналогично для эпиков*/
            JsonElement epicsAsJsonElement = jsonElement.getAsJsonObject().get("epics");
            Map<Integer, Epic> epicsDeserialized = new Gson().fromJson(epicsAsJsonElement.getAsString(), epicsMapType);
            epicsDeserialized.forEach((key, value) -> createEpic(value)); //по считанной мапе создаем эпики в менеджере

            /*аналогично для сабтасков*/
            JsonElement subtasksAsJsonElement = jsonElement.getAsJsonObject().get("subtasks");
            Map<Integer, Subtask> subtasksDeserialized = new Gson().fromJson(subtasksAsJsonElement.getAsString(), subtasksMapType);
            subtasksDeserialized.forEach((key, value) -> createSubtask(value)); //по считанной мапе создаем сабтаски в менеджере

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
