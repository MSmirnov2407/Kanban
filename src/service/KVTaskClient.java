package service;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    HttpClient httpClient; //для взаимодействияс Http-сервером
    String clientApiToken; //уникальный идентификатор клиента, получаемый от сервера при запросе /register
    String storageUrl; //URL сервера-хранилища

    public KVTaskClient(String storageUrl) throws IOException, InterruptedException {
        this.storageUrl = storageUrl; //сохраняем URL сервера-хранилища
        httpClient = HttpClient.newHttpClient(); //создаем стандартный http-клиент
        URI registerUri = URI.create(storageUrl + "/register"); //URI для обращения к ресурсу /register сервера
        /*формируем запрос на регистрацию через билдер запросов*/
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(); //билдер запроса
        HttpRequest request = requestBuilder //построение Http-запроса через билдер
                .GET()
                .uri(registerUri)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        /*получаем ответ на запрос с помощью метода отправки запроса*/
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        clientApiToken = response.body(); //сохраняем в API-token содержимое ответа на запрос регистрации
    }

    /**
     * Метод сохраняет состояние менеджера задач (списки задач и историю просмотроав)
     *
     * @param key  ключ, по которому сохраняется состояние менеджера на сервере-хранилище
     * @param json - само состояние менеджера задач
     */
    public void put(String key, String json) throws IOException, InterruptedException {
        /*формируем запрос на сохраниение состояния*/
        URI saveUri = URI.create(storageUrl + "/save/" + key + "?API_TOKEN=" + clientApiToken);
        /*конвертируем json-тело ответа в поток байтов, который можно отправить в качестве тела ответа*/
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest saveRequest = HttpRequest.newBuilder() //формируем запрос через билдер
                .POST(body)
                .uri(saveUri)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        /*отправляем запрос на сохранение*/
        HttpResponse<String> response = httpClient.send(saveRequest, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Возвращает состояние менеджера задач, хранящееся на сервере-хранилище под ключем key.
     *
     * @param key - строковый ключ, по которому хранится состояние менеджера на сервере-хранилище
     * @return состояние менеджера задач в виде строки
     */
    public String load(String key) throws IOException, InterruptedException {
        /*формируем запрос на выгрузку состояния*/
        URI loadUri = URI.create(storageUrl + "/load/" + key + "?API_TOKEN=" + clientApiToken);
        HttpRequest loadRequest = HttpRequest.newBuilder() //формируем запрос через билдер
                .GET()
                .uri(loadUri)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        /*отправляем запрос на сервер и возвращаем полученные данные*/
        HttpResponse<String> response = httpClient.send(loadRequest, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
