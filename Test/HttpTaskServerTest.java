import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.*;
import service.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskServerTest {
    public static Task task1;
    public static Task task2;
    public static Epic epic1;
    public static Epic epic2;
    public static Subtask subtask1;
    public static Subtask subtask2;
    private static HttpTaskServer httpTaskServer; //http-сервер обработки запросов к менеджеру

    public HttpClient client;
    public KVServer kvServer;
    Gson gson;

    @BeforeAll
    public static void beforeAll() {
        try {
            /*Создание подопытных тасков, эпиков, сабтасков. Эти объекты будут использоваться в тестах*/
            task1 = new Task("Task1", "Task description1", 1);
            sleep(150);
            task2 = new Task("Task2", "Task description2", 2);
            sleep(150);
            epic1 = new Epic("Epic1", "Epic description1", 3);
            sleep(150);
            epic2 = new Epic("Epic2", "Epic description2", 4);
            sleep(150);
            subtask1 = new Subtask("Subtask1", "Subtask description1", 3, 5);
            sleep(150);
            subtask2 = new Subtask("Subtask2", "Subtask description2", 3, 6);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @BeforeEach
    public void beforeEach() {
        gson = new Gson();
        try {
            client = HttpClient.newHttpClient();

            httpTaskServer = new HttpTaskServer();
            httpTaskServer.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @AfterEach
    public void afterEach() {
        httpTaskServer.stop();
    }

    @Test
    public void postTask() {
        URI url = URI.create("http://localhost:8080/tasks/task/");

        String json = gson.toJson(task1);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest requestPost = HttpRequest.newBuilder()
                .POST(body)
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);
            assertTrue("Таск успешно добавлен. Id = 1".equals(responsePost.body()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Test
    public void getTaskList() {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/");
        /*отрпавим запрос на сохранение таска*/
        String json = gson.toJson(task1);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest requestPost = HttpRequest.newBuilder()
                .POST(body)
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        /*отправляем запрос на вычитывание*/
        HttpRequest requestGet = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .build();
        try {
            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            Type taksksList = new TypeToken<ArrayList<Task>>() {
            }.getType(); //тип списка с тасками
            ArrayList<Task> tasks = gson.fromJson(responseGet.body(), taksksList);
            assertEquals(tasks.get(0), task1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getTaskById() {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/");
        /*отрпавим запрос на сохранение таска*/
        String json = gson.toJson(task1);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest requestPost = HttpRequest.newBuilder()
                .POST(body)
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        /*отправляем запрос на вычитывание*/
        URI urlWithId = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest requestGet = HttpRequest.newBuilder()
                .GET()
                .uri(urlWithId)
                .build();
        try {
            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());

            Task task = gson.fromJson(responseGet.body(), Task.class);
            assertEquals(task, task1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deleteAllTasks() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/tasks/task/");
            /*отрпавим запрос на сохранение таска*/
            String json = gson.toJson(task1);
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
            HttpRequest requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = client.send(requestPost, HttpResponse.BodyHandlers.ofString());

            /*сохраним еще один элемент*/
            json = gson.toJson(task2);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            response = client.send(requestPost, HttpResponse.BodyHandlers.ofString());

            /*отправляем запрос на удаление всех*/
            HttpRequest requestDelete = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(url)
                    .build();
            HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
            assertTrue(responseDelete.statusCode() == 200);

            /*отправляем запрос на вычитывание*/
            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(url)
                    .build();
            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            Type taksksList = new TypeToken<ArrayList<Task>>() {
            }.getType(); //тип списка с тасками
            ArrayList<Task> tasks = gson.fromJson(responseGet.body(), taksksList);
            assertEquals(responseGet.body(), "[]");
            assertTrue(tasks.isEmpty());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deleteTaskById() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/tasks/task/");
            /*отрпавим запрос на сохранение таска*/
            String json = gson.toJson(task1);
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
            HttpRequest requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());

            /*сохраним еще один элемент*/
            json = gson.toJson(task2);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());

            /*отправляем запрос на удаление одного*/
            URI urlWithId = URI.create("http://localhost:8080/tasks/task/?id=1");
            HttpRequest requestDelete = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(urlWithId)
                    .build();
            HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
            assertTrue(responseDelete.statusCode() == 200);

            /*отправляем запрос на вычитывание*/
            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(url)
                    .build();
            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            Type taksksList = new TypeToken<ArrayList<Task>>() {
            }.getType(); //тип списка с тасками
            ArrayList<Task> tasks = gson.fromJson(responseGet.body(), taksksList);
            assertEquals(tasks.get(0), task2);
            assertTrue(tasks.size() == 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void postEpic() {
        URI url = URI.create("http://localhost:8080/tasks/epic/");

        String json = gson.toJson(epic1);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest requestPost = HttpRequest.newBuilder()
                .POST(body)
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);
            assertTrue("Эпик успешно добавлен. Id = 3".equals(responsePost.body()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Test
    public void getEpicList() {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/epic/");

        /*отрпавим запрос на сохранение таска*/
        String json = gson.toJson(epic1);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest requestPost = HttpRequest.newBuilder()
                .POST(body)
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        /*отправляем запрос на вычитывание*/
        HttpRequest requestGet = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .build();
        try {
            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            Type epicsList = new TypeToken<ArrayList<Epic>>() {
            }.getType(); //тип списка с эпиками
            ArrayList<Epic> epics = gson.fromJson(responseGet.body(), epicsList);
            assertEquals(epics.get(0), epic1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getEPicById() {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/epic/");

        /*отрпавим запрос на сохранение эпика*/
        String json = gson.toJson(epic1);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest requestPost = HttpRequest.newBuilder()
                .POST(body)
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        /*отправляем запрос на вычитывание*/
        URI urlWithId = URI.create("http://localhost:8080/tasks/epic/?id=3");
        HttpRequest requestGet = HttpRequest.newBuilder()
                .GET()
                .uri(urlWithId)
                .build();
        try {
            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());

            Epic epic = gson.fromJson(responseGet.body(), Epic.class);
            assertEquals(epic, epic1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deleteAllEpics() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/tasks/epic/");

            /*отрпавим запрос на сохранение эпика*/
            String json = gson.toJson(epic1);
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
            HttpRequest requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = client.send(requestPost, HttpResponse.BodyHandlers.ofString());

            /*сохраним еще один элемент*/
            json = gson.toJson(epic2);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            response = client.send(requestPost, HttpResponse.BodyHandlers.ofString());

            /*отправляем запрос на удаление всех*/
            HttpRequest requestDelete = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(url)
                    .build();
            HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
            assertTrue(responseDelete.statusCode() == 200);

            /*отправляем запрос на вычитывание*/
            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(url)
                    .build();
            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            Type epicsList = new TypeToken<ArrayList<Epic>>() {
            }.getType(); //тип списка с тасками
            ArrayList<Epic> epics = gson.fromJson(responseGet.body(), epicsList);
            assertEquals(responseGet.body(), "[]");
            assertTrue(epics.isEmpty());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deleteEPicById() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/tasks/epic/");
            /*отрпавим запрос на сохранение эпика*/
            String json = gson.toJson(epic1);
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
            HttpRequest requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());

            /*сохраним еще один элемент*/
            json = gson.toJson(epic2);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());

            /*отправляем запрос на удаление одного*/
            URI urlWithId = URI.create("http://localhost:8080/tasks/epic/?id=3");
            HttpRequest requestDelete = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(urlWithId)
                    .build();
            HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
            assertTrue(responseDelete.statusCode() == 200);

            /*отправляем запрос на вычитывание*/
            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(url)
                    .build();
            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            Type epicsList = new TypeToken<ArrayList<Epic>>() {
            }.getType(); //тип списка с эпиками
            ArrayList<Epic> epics = gson.fromJson(responseGet.body(), epicsList);
            assertEquals(epics.get(0), epic2);
            assertTrue(epics.size() == 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void postSubtask() {
        try {
            /*создадим эпик и привсязанный к нему сабтаск*/
            URI url = URI.create("http://localhost:8080/tasks/epic/");

            String json = gson.toJson(epic1);
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
            HttpRequest requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            URI urlSubtask = URI.create("http://localhost:8080/tasks/subtask/");
            json = gson.toJson(subtask1);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlSubtask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);
            assertTrue("Сабтаск успешно добавлен. Id = 5".equals(responsePost.body()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Test
    public void getSubtaskList() {
        try {
            /*создадим эпик и привсязанный к нему сабтаск*/
            URI url = URI.create("http://localhost:8080/tasks/epic/");

            String json = gson.toJson(epic1);
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
            HttpRequest requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            URI urlSubtask = URI.create("http://localhost:8080/tasks/subtask/");
            json = gson.toJson(subtask1);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlSubtask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            /*отправляем запрос на вычитывание*/

            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(urlSubtask)
                    .build();
            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            Type subtaskList = new TypeToken<ArrayList<Subtask>>() {
            }.getType(); //тип списка с тасками
            ArrayList<Subtask> subtasks = gson.fromJson(responseGet.body(), subtaskList);
            assertEquals(subtasks.get(0), subtask1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getSubtaskById() {
        try {
            /*создадим эпик и привсязанный к нему сабтаск*/
            URI url = URI.create("http://localhost:8080/tasks/epic/");

            String json = gson.toJson(epic1);
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
            HttpRequest requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            URI urlSubtask = URI.create("http://localhost:8080/tasks/subtask/");
            json = gson.toJson(subtask1);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlSubtask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            /*отправляем запрос на вычитывание*/
            URI urlWithId = URI.create("http://localhost:8080/tasks/subtask/?id=5");
            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(urlWithId)
                    .build();

            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());

            Subtask subtask = gson.fromJson(responseGet.body(), Subtask.class);
            assertEquals(subtask, subtask1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deleteAllSubtasks() {
        try {
            /*создадим эпик и привсязанный к нему сабтаск*/
            URI url = URI.create("http://localhost:8080/tasks/epic/");

            String json = gson.toJson(epic1);
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
            HttpRequest requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            URI urlSubtask = URI.create("http://localhost:8080/tasks/subtask/");
            json = gson.toJson(subtask1);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlSubtask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            /*сохраним еще один элемент*/
            json = gson.toJson(subtask2);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlSubtask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            /*отправляем запрос на удаление всех*/
            HttpRequest requestDelete = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(urlSubtask)
                    .build();
            HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
            assertTrue(responseDelete.statusCode() == 200);

            /*отправляем запрос на вычитывание*/
            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(urlSubtask)
                    .build();
            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            Type subtaksksList = new TypeToken<ArrayList<Subtask>>() {
            }.getType(); //тип списка с тасками
            ArrayList<Subtask> subtasks = gson.fromJson(responseGet.body(), subtaksksList);
            assertEquals(responseGet.body(), "[]");
            assertTrue(subtasks.isEmpty());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deleteSubtaskById() {
        try {
            /*создадим эпик и привсязанный к нему сабтаск*/
            URI url = URI.create("http://localhost:8080/tasks/epic/");

            String json = gson.toJson(epic1);
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
            HttpRequest requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            URI urlSubtask = URI.create("http://localhost:8080/tasks/subtask/");
            json = gson.toJson(subtask1);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlSubtask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            /*сохраним еще один элемент*/
            json = gson.toJson(subtask2);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlSubtask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            /*отправляем запрос на удаление одного*/
            URI urlWithId = URI.create("http://localhost:8080/tasks/subtask/?id=5");
            HttpRequest requestDelete = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(urlWithId)
                    .build();
            HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
            assertTrue(responseDelete.statusCode() == 200);

            /*отправляем запрос на вычитывание*/
            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(urlSubtask)
                    .build();
            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            Type subtaksksList = new TypeToken<ArrayList<Subtask>>() {
            }.getType(); //тип списка с тасками
            ArrayList<Subtask> subtasks = gson.fromJson(responseGet.body(), subtaksksList);
            assertEquals(subtasks.get(0), subtask2);
            assertTrue(subtasks.size() == 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getEpicSubtasksId() {
        try {
            /*создадим эпик и привсязанный к нему сабтаск*/
            URI url = URI.create("http://localhost:8080/tasks/epic/");

            String json = gson.toJson(epic1);
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
            HttpRequest requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            URI urlSubtask = URI.create("http://localhost:8080/tasks/subtask/");
            json = gson.toJson(subtask1);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlSubtask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            /*сохраним еще один элемент*/
            json = gson.toJson(subtask2);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlSubtask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            /*выгрузим список сабтасков эпика*/
            URI urlEpicSubtasks = URI.create("http://localhost:8080/tasks/subtask/epic/?id=3");
            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(urlEpicSubtasks)
                    .build();
            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            Type subtaksksIdList = new TypeToken<List<Integer>>() {
            }.getType(); //тип списка с тасками
            List<Subtask> subtasks = gson.fromJson(responseGet.body(), subtaksksIdList);
            assertEquals(subtasks.get(0), subtask1.getId());
            assertEquals(subtasks.get(1), subtask2.getId());
            assertTrue(subtasks.size() == 2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void getPrioritizedTasks() {
        try {
            /*создадим эпик и привсязанный к нему сабтаск*/
            URI url = URI.create("http://localhost:8080/tasks/epic/");

            String json = gson.toJson(epic1);
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
            HttpRequest requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            URI urlSubtask = URI.create("http://localhost:8080/tasks/subtask/");
            json = gson.toJson(subtask1);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlSubtask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);
            /*сохраним еще один сабтаск*/
            json = gson.toJson(subtask2);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlSubtask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            /*создадим пару тасков*/
            URI urlTask = URI.create("http://localhost:8080/tasks/task/");
            /*отрпавим запрос на сохранение таска*/
            json = gson.toJson(task1);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlTask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            json = gson.toJson(task2);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlTask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            response = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            /*выгрузим список задач в порядке приоритета*/
            URI urlPrior = URI.create("http://localhost:8080/tasks/");
            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(urlPrior)
                    .build();
            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            Type prioritizedList = new TypeToken<List<Task>>() {
            }.getType(); //тип списка с тасками
            List<Task> prioritizedTasks = gson.fromJson(responseGet.body(), prioritizedList);
            assertEquals(prioritizedTasks.get(0), task1);
            assertEquals(prioritizedTasks.get(1), task2);
            assertEquals(prioritizedTasks.get(2), subtask1);
            assertEquals(prioritizedTasks.get(3), subtask2);
            assertTrue(prioritizedTasks.size() == 4);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void getHistory() {
        try {
            /*создадим эпик и привсязанный к нему сабтаск*/
            URI url = URI.create("http://localhost:8080/tasks/epic/");

            String json = gson.toJson(epic1);
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
            HttpRequest requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            URI urlSubtask = URI.create("http://localhost:8080/tasks/subtask/");
            json = gson.toJson(subtask1);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlSubtask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);
            /*сохраним еще один сабтаск*/
            json = gson.toJson(subtask2);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlSubtask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            responsePost = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            /*создадим пару тасков*/
            URI urlTask = URI.create("http://localhost:8080/tasks/task/");
            /*отрпавим запрос на сохранение таска*/
            json = gson.toJson(task1);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlTask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            json = gson.toJson(task2);
            body = HttpRequest.BodyPublishers.ofString(json);
            requestPost = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(urlTask)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            response = client.send(requestPost, HttpResponse.BodyHandlers.ofString());
            assertTrue(responsePost.statusCode() == 201);

            /*проверим, что история просмотров пуста*/
            URI urlHistory = URI.create("http://localhost:8080/tasks/history");
            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(urlHistory)
                    .build();
            HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            Type historyList = new TypeToken<List<Task>>() {
            }.getType(); //тип списка с тасками
            List<Task> historyTasks = gson.fromJson(responseGet.body(), historyList);
            assertTrue(historyTasks.size() == 0);

            /*повызываем задачи для сохранения их в истории просмотров*/
            URI urlSubtaskWithId = URI.create("http://localhost:8080/tasks/subtask/?id=5");
            requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(urlSubtaskWithId)
                    .build();
            responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            assertTrue(responseGet.statusCode() == 200);

            urlSubtaskWithId = URI.create("http://localhost:8080/tasks/subtask/?id=6");
            requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(urlSubtaskWithId)
                    .build();
            responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            assertTrue(responseGet.statusCode() == 200);

            URI urlTaskWithId = URI.create("http://localhost:8080/tasks/task/?id=2");
            requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(urlTaskWithId)
                    .build();
            responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            assertTrue(responseGet.statusCode() == 200);

            urlTaskWithId = URI.create("http://localhost:8080/tasks/task/?id=1");
            requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(urlTaskWithId)
                    .build();
            responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            assertTrue(responseGet.statusCode() == 200);

            /*проверим историю*/
            requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(urlHistory)
                    .build();
            responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
            historyTasks = gson.fromJson(responseGet.body(), historyList);
            assertTrue(historyTasks.size() == 4);
            assertEquals(historyTasks.get(0), subtask1);
            assertEquals(historyTasks.get(1), subtask2);
            assertEquals(historyTasks.get(2), task2);
            assertEquals(historyTasks.get(3), task1);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
