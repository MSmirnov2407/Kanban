package service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.Endpoint;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.lang.Thread.sleep;

public class HttpTaskServer {
    private TaskManager manager; //таск-менеджер, используемый для реализации маппинга HttpServera
    private static final int PORT = 8080; //порт, прослушиваемый нашим Http-сервером
    private HttpServer httpServer; //HTTP-сервер, который настраивается при создании объекта HttpTaskServer
    private Gson gson; //для работы с JSON
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8; //переменная для хранения кодировки


    public static void main(String args) throws IOException, InterruptedException {
        HttpTaskServer httpTaskServer = new HttpTaskServer();
        httpTaskServer.manager.createTask(new Task("task1", "description1"));
        sleep(10);
        httpTaskServer.manager.createTask(new Task("task2", "description2"));
        sleep(10);
        int epicId = httpTaskServer.manager.createEpic(new Epic("Epic2", "epic description"));
        sleep(10);
        httpTaskServer.manager.createSubtask(new Subtask("subtask3", "descriptoin3", epicId));
        sleep(100);

        httpTaskServer.start();
    }

    /**
     * конструктор, в котором запускается Http-сервер с заданным портом и хэндлером запросов
     *
     * @throws IOException
     */
    public HttpTaskServer() throws IOException {
        /*В качестве менеджера задач получаем менеджер, сохраняющий данные в файле.*/
        manager = Managers.getFileBackedTaskManager(new File("src/file1.csv"));
        /*создаем объект gson Для далььнейшего использования*/
        gson = new Gson();
        /*создаем HttpServer*/
        httpServer = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        /*связываем созданный сервер и обработчик запросов по пути /tasks */
        httpServer.createContext("/tasks", new HttpTaskHandler());
    }

    /**
     * метод для Запуска HttpTaskServer
     */
    public void start() {
        System.out.println("Startes HttpTaskServer, PORT = " + PORT);
        System.out.println("http://localhost:" + PORT + "/tasks/");
        httpServer.start();
    }

    /**
     * метод для остановки HttpTaskServer
     */
    public void stop() {
        System.out.println("HttpTaskServer STOP");
        httpServer.stop(0);
    }

    /**
     * Вложенный класс хэндлера запросов. Имплементирует HttpHandler.
     */
    private class HttpTaskHandler implements HttpHandler {

        /**
         * Метод обработки запроса. Определяет запрашиваемый эндпоинт и вызывает соответствующий хэндлер для него
         *
         * @param exchange the exchange containing the request from the
         *                 client and used to send the response
         * @throws IOException
         */
        @Override
        public void handle(HttpExchange exchange) {
            try {
                /*получим эндпоинт из запроса. Т.к. в путь, возвращаемый методом getPath() не включаются параметры
                 * строки запроса, то в метод определения эндпоинта передаем не путь, а сам URI в виде строки*/
                Endpoint endpoint = getEndpoint(exchange.getRequestURI().toString(), exchange.getRequestMethod());
                /*в зависимости от полученного эндпоинта выбираем соответствующий обработчик*/
                switch (endpoint) {
                    case GET_TASK_LIST:
                        handleGetAllTasks(exchange);
                        break;
                    case GET_TASK_BY_ID:
                        handleGetTaskById(exchange);
                        break;
                    case ADD_OR_UPDATE_TASK:
                        handleAddOrUpdateTask(exchange);
                        break;
                    case DELETE_TASK_BY_ID:
                        handleDeleteTaskById(exchange);
                        break;
                    case DELETE_ALL_TASKS:
                        handleDeleteAllTasks(exchange);
                        break;
                    case GET_SUBTASK_LIST:
                        handleGetAllSubtasks(exchange);
                        break;
                    case GET_SUBTASK_BY_ID:
                        handleGetSubtaskById(exchange);
                        break;
                    case ADD_OR_UPDATE_SUBTASK:
                        handleAddOrUpdateSubtask(exchange);
                        break;
                    case DELETE_SUBTASK_BY_ID:
                        handleDeleteSubtaskById(exchange);
                        break;
                    case DELETE_ALL_SUBTASKS:
                        handleDeleteAllSubtasks(exchange);
                        break;
                    case GET_EPIC_LIST:
                        handleGetAllEpics(exchange);
                        break;
                    case GET_EPIC_BY_ID:
                        handleGetEpicById(exchange);
                        break;
                    case ADD_OR_UPDATE_EPIC:
                        handleAddOrUpdateEpic(exchange);
                        break;
                    case DELETE_EPIC_BY_ID:
                        handleDeleteEpicById(exchange);
                        break;
                    case DELETE_ALL_EPICS:
                        handleDeleteAllEpics(exchange);
                        break;
                    case GET_EPIC_SUBTASK_BY_ID:
                        handleGetEpicSubtaskById(exchange);
                        break;
                    case GET_HISTORY:
                        handleGetHistory(exchange);
                        break;
                    case GET_PRIORITIZED_TASKS:
                        handleGetPrioritizedTasks(exchange);
                        break;
                    default:
                        System.out.println("UNKNOWN");
                        writeResponse(exchange, "Некорректный запрос", 400);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                exchange.close(); //Закрываем соединение после завершения обработки запроса
            }
        }

        /**
         * обработчик эндпоинта GET /tasks/task/ - получение списка всех тасков
         *
         * @param exchange
         */
        private void handleGetAllTasks(HttpExchange exchange) throws IOException {
            System.out.println("GET_TASK_LIST");
            String jsonTaskList = gson.toJson(manager.getTasks());
            writeResponse(exchange, jsonTaskList, 200);
        }

        /**
         * обработчик эндпоинта GET /tasks/task/?id= - получение таска по id
         *
         * @param exchange
         */
        private void handleGetTaskById(HttpExchange exchange) throws IOException {
            System.out.println("GET_TASK_BY_ID");
            int taskId = 0;
            Optional<Integer> taskIdOpt = getTaskId(exchange); //получаем id задачи из запроса
            if (taskIdOpt.isEmpty()) { //если id не определен, то ошибка
                writeResponse(exchange, "Некорректный идентификатор таска", 400);
                return;
            } else {
                taskId = taskIdOpt.get(); //преобразуем id из optional в int
                Task task = manager.getTaskById(taskId); //берем из менеджера задачу по полученному id
                if (task == null) { //сравниваем с null задачу, взятую по требуемому id
                    writeResponse(exchange, "Задачи с запрошенным id не существует", 404);
                    return;
                }
                String jsonTask = gson.toJson(task); //запрашиваемый таск превращаем в json
                writeResponse(exchange, jsonTask, 200); //возвращаем в ответ на запрос json-таск
            }
        }

        /**
         * обработчик эндпоинта POST /tasks/task/ + json- body - добавление или обновление таска
         *
         * @param exchange
         */
        private void handleAddOrUpdateTask(HttpExchange exchange) throws IOException {
            System.out.println("ADD_OR_UPDATE_TASK");

            InputStream is = exchange.getRequestBody(); //считываем тело запроса
            String bodyString = new String(is.readAllBytes()); //превращаем в строку через readAllBytes

            try {
                Task taskFromBody = gson.fromJson(bodyString, Task.class); //конверт. из JSON через GSON в объект Таска
                if (taskFromBody == null || taskFromBody.getStartTime() == null || taskFromBody.getStatus() == null
                        || taskFromBody.getName() == null || taskFromBody.getName().isBlank()) {
                    writeResponse(exchange, "Поля задачи не могут быть пустыми", 400);
                    return;
                }
                //добавить или обновить таск
                Integer taskFromBodyId = taskFromBody.getId(); //id из таска, полученного в теле ответа
                if (taskFromBodyId != null) { //если у переданного таска нет id, то сразу переходим к созданию
                    for (Task task : manager.getTasks()) { //пробегаем по таскам в поисках совпадения id
                        if (task.getId() == taskFromBodyId) { //если запрашиваемый id найден, то обновняем таск
                            manager.updateTask(taskFromBody);
                            writeResponse(exchange, "Таск с id= " + taskFromBodyId + " обновлен", 200);
                            return;
                        }
                    }
                }
                manager.createTask(taskFromBody);
                if (manager.getTasks().contains(taskFromBody)) {
                    writeResponse(exchange, "Таск успешно добавлен. Id = " + taskFromBodyId, 201);
                    return;
                }
                /*если таск по какой-то причине не добавился:*/
                writeResponse(exchange, "Ошибка в добавлении таска ", 500);
            } catch (JsonSyntaxException ex) {
                writeResponse(exchange, "Получен некорректный JSON", 400);
            }
        }

        /**
         * обработчик эндпоинта DELETE /tasks/task/?id=1 - удаление таска по id
         *
         * @param exchange
         */
        private void handleDeleteTaskById(HttpExchange exchange) throws IOException {
            System.out.println("DELETE_TASK_BY_ID");
            int taskId;
            Optional<Integer> taskIdOpt = getTaskId(exchange); //получаем id задачи из запроса
            if (taskIdOpt.isEmpty()) { //если id не определен, то ошибка
                writeResponse(exchange, "Некорректный идентификатор таска", 400);
                return;
            } else {
                taskId = taskIdOpt.get(); //преобразуем id из optional в int
                /*чтобы удаляемая задача не попала в историю просмотров, надо избежать вызова метода getTaskById
                 * Поэтому проверку существования таска сделаем через просмотр всез тасков и проверку их id*/
                Optional<Task> taskOpt = manager.getTasks().stream()
                        .filter(task -> task.getId() == taskId)
                        .findFirst();
                if (taskOpt.isEmpty()) { //если задача не нашлась
                    writeResponse(exchange, "Задачи с запрошенным id не существует", 404);
                    return;
                }
            }
            manager.deleteTaskById(taskId);
            String responseMessage = "Таск с id= " + taskId + " успешно удалён";
            writeResponse(exchange, responseMessage, 200); //возвращаем ответ на запрос
        }

        /**
         * обработчик эндпоинта DELETE /tasks/task/ - удаление всех тасков
         *
         * @param exchange
         */
        private void handleDeleteAllTasks(HttpExchange exchange) throws IOException {
            System.out.println("DELETE_ALL_TASKS");
            manager.deleteAllTasks();
            writeResponse(exchange, "Все таски удалены", 200);
        }

        /**
         * обработчик эндпоинта GET /tasks/subtask/ - получение списка всех сабтасков
         *
         * @param exchange
         */
        private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
            System.out.println("GET_SUBTASK_LIST");
            String jsonSubtaskList = gson.toJson(manager.getSubtasks());
            writeResponse(exchange, jsonSubtaskList, 200);
        }

        /**
         * обработчик эндпоинта GET /tasks/subtask/?id= - получение сабтаска по id
         *
         * @param exchange
         */
        private void handleGetSubtaskById(HttpExchange exchange) throws IOException {
            System.out.println("GET_SUBTASK_BY_ID");
            int subtaskId = 0;
            Optional<Integer> subtaskIdOpt = getTaskId(exchange); //получаем id подзадачи из запроса
            if (subtaskIdOpt.isEmpty()) { //если id не определен, то ошибка
                writeResponse(exchange, "Некорректный идентификатор сабтаска", 400);
                return;
            } else {
                subtaskId = subtaskIdOpt.get(); //преобразуем id из optional в int
                Subtask subtask = manager.getSubtaskById(subtaskId); //берем из менеджера сабтаск по полученному id
                if (subtask == null) { //сравниваем с null подзадачу, взятую по требуемому id
                    writeResponse(exchange, "Подзадачи с запрошенным id не существует", 404);
                    return;
                }
                String jsonSubtask = gson.toJson(subtask); //запрашиваемый сабтаск превращаем в json
                writeResponse(exchange, jsonSubtask, 200); //возвращаем в ответ на запрос json-сабтаск
            }
        }

        /**
         * обработчик эндпоинта POST /tasks/subtask/ + json- body - добавление или обновление сабтаска
         *
         * @param exchange
         */
        private void handleAddOrUpdateSubtask(HttpExchange exchange) throws IOException {
            System.out.println("ADD_OR_UPDATE_SUBTASK");

            InputStream is = exchange.getRequestBody(); //считываем тело запроса
            String bodyString = new String(is.readAllBytes()); //превращаем в строку через readAllBytes

            try {
                Subtask subtaskFromBody = gson.fromJson(bodyString, Subtask.class); //конверт. из JSON через GSON в объект Таска
                if (subtaskFromBody == null || subtaskFromBody.getStartTime() == null
                        || subtaskFromBody.getStatus() == null || subtaskFromBody.getName() == null
                        || subtaskFromBody.getName().isBlank() || subtaskFromBody.getEpicId() == null) {
                    writeResponse(exchange, "Поля подзадачи не могут быть пустыми", 400);
                    return;
                }
                //добавить или обновить сабтаск
                Integer subtaskFromBodyId = subtaskFromBody.getId();
                if (subtaskFromBodyId != null) { //если у переданного сабтаска нет id, то сразу переходим к созданию
                    for (Subtask subtask : manager.getSubtasks()) { //пробегаем по сабтаскам в поисках совпадения id
                        if (subtask.getId() == subtaskFromBodyId) { //если запрашиваемый id найден - обновняем сабтаск
                            manager.updateSubtask(subtaskFromBody);
                            writeResponse(exchange, "Сабтаск с id= " + subtaskFromBodyId + " обновлен", 200);
                            return;
                        }
                    }
                }
                manager.createSubtask(subtaskFromBody);
                if (manager.getSubtasks().contains(subtaskFromBody)) {
                    writeResponse(exchange, "Сабтаск успешно добавлен. Id = " + subtaskFromBodyId, 201);
                    return;
                }
                /*если сабтаск по какой-то причине не добавился:*/
                writeResponse(exchange, "Ошибка в добавлении сабтаска ", 500);
            } catch (JsonSyntaxException ex) {
                writeResponse(exchange, "Получен некорректный JSON", 400);
            }

        }

        /**
         * обработчик эндпоинта DELETE /tasks/subtask/?id=1 - удаление сабтаска по id
         *
         * @param exchange
         */
        private void handleDeleteSubtaskById(HttpExchange exchange) throws IOException {
            System.out.println("DELETE_SUBTASK_BY_ID");
            int subtaskId;
            Optional<Integer> subtaskIdOpt = getTaskId(exchange); //получаем id подзадачи из запроса
            if (subtaskIdOpt.isEmpty()) { //если id не определен, то ошибка
                writeResponse(exchange, "Некорректный идентификатор сабтаска", 400);
                return;
            } else {
                subtaskId = subtaskIdOpt.get(); //преобразуем id из optional в int
                /*чтобы удаляемая подзадача не попала в историю просмотров, надо избежать вызова метода getTaskById
                 * Поэтому проверку существования таска сделаем через просмотр всех тасков и проверку их id*/
                Optional<Subtask> subtaskOpt = manager.getSubtasks().stream()
                        .filter(subtask -> subtask.getId() == subtaskId)
                        .findFirst();
                if (subtaskOpt.isEmpty()) { //если задача не нашлась
                    writeResponse(exchange, "Подзадачи с запрошенным id не существует", 404);
                    return;
                }
            }
            manager.deleteSubtaskById(subtaskId);
            String responseMessage = "Сабаск с id= " + subtaskId + " успешно удалён";
            writeResponse(exchange, responseMessage, 200); //возвращаем ответ на запрос

        }

        /**
         * обработчик эндпоинта DELETE /tasks/subtask/ - удаление всех сабтасков
         *
         * @param exchange
         */
        private void handleDeleteAllSubtasks(HttpExchange exchange) throws IOException {
            System.out.println("DELETE_ALL_SUBTASKS");
            manager.deleteAllSubtasks();
            writeResponse(exchange, "Все сабтаски удалены", 200);
        }

        /**
         * обработчик эндпоинта GET /tasks/epic/ - получение списка всех эпиков
         *
         * @param exchange
         */
        private void handleGetAllEpics(HttpExchange exchange) throws IOException {
            System.out.println("GET_EPIC_LIST");
            String jsonEpicList = gson.toJson(manager.getEpics());
            writeResponse(exchange, jsonEpicList, 200);
        }

        /**
         * обработчик эндпоинта GET /tasks/epic/?id= - получение эпика по id
         *
         * @param exchange
         */
        private void handleGetEpicById(HttpExchange exchange) throws IOException {
            System.out.println("GET_EPIC_BY_ID");
            int epicId = 0;
            Optional<Integer> epicIdOpt = getTaskId(exchange); //получаем id эпика из запроса
            if (epicIdOpt.isEmpty()) { //если id не определен, то ошибка
                writeResponse(exchange, "Некорректный идентификатор эпика", 400);
                return;
            } else {
                epicId = epicIdOpt.get(); //преобразуем id из optional в int
                Epic epic = manager.getEpicById(epicId); //берем из менеджера эпик по полученному id
                if (epic == null) { //сравниваем с null эпик, взятый по требуемому id
                    writeResponse(exchange, "Эпика с запрошенным id не существует", 404);
                    return;
                }
                String jsonEpic = gson.toJson(epic); //запрашиваемый эпик превращаем в json
                writeResponse(exchange, jsonEpic, 200); //возвращаем в ответ на запрос json-эпик
            }
        }

        /**
         * обработчик эндпоинта POST /tasks/epic/ + json- body - добавление или обновление эпика
         *
         * @param exchange
         */
        private void handleAddOrUpdateEpic(HttpExchange exchange) throws IOException {
            System.out.println("ADD_OR_UPDATE_EPIC");

            InputStream is = exchange.getRequestBody(); //считываем тело запроса
            String bodyString = new String(is.readAllBytes()); //превращаем в строку через readAllBytes

            try {
                Epic epicFromBody = gson.fromJson(bodyString, Epic.class); //конверт. из JSON через GSON в объект Эпика
                if (epicFromBody == null || epicFromBody.getStartTime() == null || epicFromBody.getStatus() == null
                        || epicFromBody.getName() == null || epicFromBody.getName().isBlank()
                        || epicFromBody.getEndTime() == null) {
                    writeResponse(exchange, "Поля эпика не могут быть пустыми", 400);
                    return;
                }
                //добавить или обновить эпик
                Integer epicFromBodyId = epicFromBody.getId();
                if (epicFromBodyId != null) { //если у переданного эпика нет id, то сразу переходим к созданию
                    for (Epic epic : manager.getEpics()) { //пробегаем по эпикам в поисках совпадения id
                        if (epic.getId() == epicFromBodyId) { //если запрашиваемый id найден, то обновняем эпик
                            manager.updateEpic(epicFromBody);
                            writeResponse(exchange, "Эпик с id= " + epicFromBodyId + " обновлен", 200);
                            return;
                        }
                    }
                }
                manager.createEpic(epicFromBody);
                if (manager.getEpics().contains(epicFromBody)) {
                    writeResponse(exchange, "Эпик успешно добавлен. Id = " + epicFromBodyId, 201);
                    return;
                }
                /*если эпик по какой-то причине не добавился:*/
                writeResponse(exchange, "Ошибка в добавлении эпика ", 500);
            } catch (JsonSyntaxException ex) {
                writeResponse(exchange, "Получен некорректный JSON", 400);
            }
        }

        /**
         * обработчик эндпоинта DELETE /tasks/epic/?id=1 - удаление эпика по id
         *
         * @param exchange
         */
        private void handleDeleteEpicById(HttpExchange exchange) throws IOException {
            System.out.println("DELETE_EPIC_BY_ID");
            int epicId;
            Optional<Integer> epicIdOpt = getTaskId(exchange); //получаем id Эпика из запроса
            if (epicIdOpt.isEmpty()) { //если id не определен, то ошибка
                writeResponse(exchange, "Некорректный идентификатор Эпика", 400);
                return;
            } else {
                epicId = epicIdOpt.get(); //преобразуем id из optional в int
                /*чтобы удаляемый Эпик не попал в историю просмотров, надо избежать вызова метода getEpicById
                 * Поэтому проверку существования Эпика сделаем через просмотр всех Эпиков и проверку их id*/
                Optional<Epic> epicOpt = manager.getEpics().stream()
                        .filter(epic -> epic.getId() == epicId)
                        .findFirst();
                if (epicOpt.isEmpty()) { //если эпика не нашлось
                    writeResponse(exchange, "Эпика с запрошенным id не существует", 404);
                    return;
                }
            }
            manager.deleteEpicById(epicId);
            String responseMessage = "Эпик с id= " + epicId + " успешно удалён";
            writeResponse(exchange, responseMessage, 200); //возвращаем ответ на запрос
        }

        /**
         * обработчик эндпоинта DELETE /tasks/epic/ - удаление всех эпиков
         *
         * @param exchange
         */
        private void handleDeleteAllEpics(HttpExchange exchange) throws IOException {
            System.out.println("DELETE_ALL_EPICS");
            manager.deleteAllEpics();
            writeResponse(exchange, "Все эпики удалены", 200);

        }

        /**
         * обработчик эндпоинта GET /tasks/subtask/epic/?id= - получение сабтасков конкретного эпика
         *
         * @param exchange
         */
        private void handleGetEpicSubtaskById(HttpExchange exchange) throws IOException {
            System.out.println("GET_EPIC_SUBTASK_BY_ID");
            int epicId;
            Optional<Integer> epicIdOpt = getTaskId(exchange); //получаем id Эпика из запроса

            if (epicIdOpt.isEmpty()) { //если id не определен, то ошибка
                writeResponse(exchange, "Некорректный идентификатор Эпика", 400);
                return;
            } else {
                epicId = epicIdOpt.get(); //преобразуем id из optional в int
                /*чтобы проверяемый Эпик не попал в историю просмотров, надо избежать вызова метода getEpicById
                 * Поэтому проверку существования Эпика сделаем через просмотр всех Эпиков и проверку их id*/
                Optional<Epic> epicOpt = manager.getEpics().stream()
                        .filter(epic -> epic.getId() == epicId)
                        .findFirst();
                if (epicOpt.isEmpty()) { //если эпика не нашлось
                    writeResponse(exchange, "Эпика с запрошенным id не существует", 404);
                    return;
                }
                //заворачиваем в json список id сабтасков запрашиваемого эпика
                String responseMessage = gson.toJson(epicOpt.get().getSubtaskIds());
                writeResponse(exchange, responseMessage, 200); //возвращаем ответ на запрос
            }
        }

        /**
         * обработчик эндпоинта GET /tasks/history - получение истории просмотров
         *
         * @param exchange
         */
        private void handleGetHistory(HttpExchange exchange) throws IOException {
            System.out.println("GET_HISTORY");
            String jsonHistory = gson.toJson(manager.getHistory());
            writeResponse(exchange, jsonHistory, 200);
        }

        /**
         * обработчик эндпоинта GET /tasks/ - получение списка задач, отсорттированный по приоритету
         *
         * @param exchange
         */
        private void handleGetPrioritizedTasks(HttpExchange exchange) throws IOException {
            System.out.println("GET_PRIORITIZED_TASKS");
            String jsonPrioritizedList = gson.toJson(manager.getPrioritizedTasks());
            writeResponse(exchange, jsonPrioritizedList, 200);
        }

        /**
         * Определение эндпоинта по запросу
         *
         * @param requestURI - URI запроса и requestMethod - метод запроса
         * @return enum - тип эндпоинта
         */
        private Endpoint getEndpoint(String requestURI, String requestMethod) {
            String[] uriParts = requestURI.split("/"); //делим запрос на части

            if (uriParts.length >= 2 && uriParts[1].equals("tasks")) { //путь всегда должен начинаться с tasks
                /*получение истории и сортитрованного по приоритетам списка задач*/
                if (uriParts.length == 2 && requestMethod.equals("GET")) {
                    return Endpoint.GET_PRIORITIZED_TASKS;
                }
                if (uriParts.length == 3 && uriParts[2].equals("history")
                        && requestMethod.equals("GET")) {
                    return Endpoint.GET_HISTORY;
                }

                /*эндпоинты для тасков*/
                if (uriParts.length == 3 && uriParts[2].equals("task") && requestMethod.equals("GET")) {
                    return Endpoint.GET_TASK_LIST;
                }
                if (uriParts.length == 3 && uriParts[2].equals("task") && requestMethod.equals("DELETE")) {
                    return Endpoint.DELETE_ALL_TASKS;
                }
                if (uriParts.length == 4 && uriParts[2].equals("task") && uriParts[3].startsWith("?id=")
                        && requestMethod.equals("GET")) {
                    return Endpoint.GET_TASK_BY_ID;
                }
                if (uriParts.length == 4 && uriParts[2].equals("task") && uriParts[3].startsWith("?id=")
                        && requestMethod.equals("DELETE")) {
                    return Endpoint.DELETE_TASK_BY_ID;
                }
                if (uriParts.length == 3 && uriParts[2].equals("task") && requestMethod.equals("POST")) {
                    return Endpoint.ADD_OR_UPDATE_TASK;
                }

                /*эндпоинты для сабтасков*/
                if (uriParts.length == 3 && uriParts[2].equals("subtask") && requestMethod.equals("GET")) {
                    return Endpoint.GET_SUBTASK_LIST;
                }
                if (uriParts.length == 3 && uriParts[2].equals("subtask") && requestMethod.equals("DELETE")) {
                    return Endpoint.DELETE_ALL_SUBTASKS;
                }
                if (uriParts.length == 4 && uriParts[2].equals("subtask") && uriParts[3].startsWith("?id=")
                        && requestMethod.equals("GET")) {
                    return Endpoint.GET_SUBTASK_BY_ID;
                }
                if (uriParts.length == 4 && uriParts[2].equals("subtask") && uriParts[3].startsWith("?id=")
                        && requestMethod.equals("DELETE")) {
                    return Endpoint.DELETE_SUBTASK_BY_ID;
                }
                if (uriParts.length == 3 && uriParts[2].equals("subtask") && requestMethod.equals("POST")) {
                    return Endpoint.ADD_OR_UPDATE_SUBTASK;
                }

                /*эндпоинты для эпиков*/
                if (uriParts.length == 3 && uriParts[2].equals("epic") && requestMethod.equals("GET")) {
                    return Endpoint.GET_EPIC_LIST;
                }
                if (uriParts.length == 3 && uriParts[2].equals("epic") && requestMethod.equals("DELETE")) {
                    return Endpoint.DELETE_ALL_EPICS;
                }
                if (uriParts.length == 4 && uriParts[2].equals("epic") && uriParts[3].startsWith("?id=")
                        && requestMethod.equals("GET")) {
                    return Endpoint.GET_EPIC_BY_ID;
                }
                if (uriParts.length == 4 && uriParts[2].equals("epic") && uriParts[3].startsWith("?id=")
                        && requestMethod.equals("DELETE")) {
                    return Endpoint.DELETE_EPIC_BY_ID;
                }
                if (uriParts.length == 3 && uriParts[2].equals("epic") && requestMethod.equals("POST")) {
                    return Endpoint.ADD_OR_UPDATE_EPIC;
                }
                /*получение сабтаска эпика по id эпика */
                if (uriParts.length == 5 && uriParts[2].equals("subtask") && uriParts[3].equals("epic")
                        && uriParts[4].startsWith("?id=") && requestMethod.equals("GET")) {
                    return Endpoint.GET_EPIC_SUBTASK_BY_ID;
                }
            }
            return Endpoint.UNKNOWN;
        }
    }

    /**
     * метод составления и отправки ответа на HTTp-запрос
     *
     * @param exchange
     * @param responseString - тело  ответа
     * @param responseCode   - код ответа
     * @throws IOException
     */
    private void writeResponse(HttpExchange exchange, String responseString, int responseCode) throws IOException {
        if (responseString.isBlank()) { //если строка ответа пустая
            exchange.sendResponseHeaders(responseCode, 0); //отправляем только заголовки
        } else {
            byte[] bytes = responseString.getBytes(DEFAULT_CHARSET); //преобразуем строку ответа в массив байт
            exchange.sendResponseHeaders(responseCode, bytes.length); //отправляем код ответа
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes); //заполняем тело ответа байтами строки ответа
            }
        }
    }

    /**
     * метод для получения id задачи из запроса
     *
     * @param exchange
     * @return optional<integer> id-таска из запроса
     */
    private Optional<Integer> getTaskId(HttpExchange exchange) {
        /*делим строку по разделителю ?id=. Строка запроса должна поделиться ровно на две части, вторая из
         * которых содержит только передаваемый id*/
        String[] uriParts = exchange.getRequestURI().toString().split("\\?id=");
        try {
            return Optional.of(Integer.parseInt(uriParts[1])); //возвращаем id в обертке Optional<Integer>
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }
}
