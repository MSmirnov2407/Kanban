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
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskServerTest {
    public Task task1;
    public Task task2;
    public Epic epic1;
    public Epic epic2;
    public Subtask subtask1;
    public Subtask subtask2;
    private static HttpTaskServer httpTaskServer; //http-сервер обработки запросов к менеджеру

    public HttpClient client;
    public KVServer kvServer;
    Gson gson;

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

        /*Создание подопытных тасков, эпиков, сабтасков. Эти объекты будут использоваться в тестах*/
        task1 = new Task("Task1", "Task description1", 1);
        task2 = new Task("Task2", "Task description2", 2);
        epic1 = new Epic("Epic1", "Epic description1", 3);
        epic2 = new Epic("Epic2", "Epic description2", 4);
        subtask1 = new Subtask("Subtask1", "Subtask description1", 3, 5);
        subtask2 = new Subtask("Subtask2", "Subtask description2", 3, 6);
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
        HttpRequest request = HttpRequest.newBuilder()
                .POST(body)
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertTrue(response.statusCode() == 201);
            assertTrue("Таск успешно добавлен. Id = 1".equals(response.body()));
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
        HttpRequest request = HttpRequest.newBuilder()
                .POST(body)
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        /*отправляем запрос на вычитывание*/
        HttpRequest request2 = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .build();
        try {
            HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
            Type taksksList = new TypeToken<ArrayList<Task>>() {
            }.getType(); //тип списка с тасками
            ArrayList<Task> tasks = gson.fromJson(response2.body(), taksksList);
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
        HttpRequest request = HttpRequest.newBuilder()
                .POST(body)
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        /*отправляем запрос на вычитывание*/
        URI url2 = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request2 = HttpRequest.newBuilder()
                .GET()
                .uri(url2)
                .build();
        try {
            HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

            Task task = gson.fromJson(response2.body(), Task.class);
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
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            /*сохраним еще один элемент*/
            json = gson.toJson(task2);
            body = HttpRequest.BodyPublishers.ofString(json);
            request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            /*отправляем запрос на удаление всех*/
            URI url2 = URI.create("http://localhost:8080/tasks/task/");
            HttpRequest request2 = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(url2)
                    .build();
            HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
            assertTrue(response2.statusCode() == 200);

            /*отправляем запрос на вычитывание*/
            HttpRequest request3 = HttpRequest.newBuilder()
                    .GET()
                    .uri(url)
                    .build();
            HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
            Type taksksList = new TypeToken<ArrayList<Task>>() {
            }.getType(); //тип списка с тасками
            ArrayList<Task> tasks = gson.fromJson(response3.body(), taksksList);
            assertEquals(response3.body(), "[]");
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
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            /*сохраним еще один элемент*/
            json = gson.toJson(task2);
            body = HttpRequest.BodyPublishers.ofString(json);
            request = HttpRequest.newBuilder()
                    .POST(body)
                    .uri(url)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());

            /*отправляем запрос на удаление одного*/
            URI url2 = URI.create("http://localhost:8080/tasks/task/?id=1");
            HttpRequest request2 = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(url2)
                    .build();
            HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
            assertTrue(response2.statusCode() == 200);

            /*отправляем запрос на вычитывание*/
            HttpRequest request3 = HttpRequest.newBuilder()
                    .GET()
                    .uri(url2)
                    .build();
            HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
//            Type taksksList = new TypeToken<ArrayList<Task>>() {
//            }.getType(); //тип списка с тасками
           Task task = gson.fromJson(response3.body(), Task.class);
            assertEquals(task, task2);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
