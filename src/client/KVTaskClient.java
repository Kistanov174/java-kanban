package client;

import exception.ManagerSaveException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private final String apiToken;
    public KVTaskClient(URI uri)  {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            HttpResponse<String> response = client.send(request, handler);
            apiToken = response.body();
        } catch(IOException | InterruptedException e) {
            throw new ManagerSaveException("Сбой регистрации в конструкторе " + KVTaskClient.class.getName());
        }
    }

    public void put(String key, String json) {
        URI uri = URI.create("http://localhost:8078/save/" + key + "/?API_TOKEN=" + apiToken);
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofString(json);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(publisher)
                    .uri(uri)
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Ошибка сохранения на сервер " + KVTaskClient.class.getName() + ".put()");
        }
    }

    public String load(String key) {
        URI uri = URI.create("http://localhost:8078/load/" + key + "/?API_TOKEN=" + apiToken);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            HttpResponse<String> response = client.send(request, handler);
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Ошибка загрузки с сервера " + KVTaskClient.class.getName() + ".load()");
        }
    }
}
