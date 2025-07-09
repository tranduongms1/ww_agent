package wisewires.agent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class Client {
    private Logger logger = LoggerFactory.getLogger(Client.class);

    private final String serverAddress;
    private final String endpoint;
    private final String token;
    private final HttpClient client;
    private Map<String, Object> user;

    public Client(String serverAddress, String token) {
        this.serverAddress = serverAddress;
        if (Util.isValidIPv4(serverAddress)) {
            this.endpoint = "http://" + serverAddress + "/api/v4";
        } else {
            this.endpoint = "https://" + serverAddress + "/api/v4";
        }
        this.token = token;
        this.client = HttpClient.newHttpClient();
    }

    public String getServerAddress() {
        return serverAddress;
    }

    private Builder requestBuilder(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(endpoint + path))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json");
    }

    public String doGet(String path) throws Exception {
        HttpRequest req = requestBuilder(path).GET().build();
        HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
        if (res.statusCode() >= 400) {
            System.err.println(res.body());
            throw new Exception("[GET] '%s' - %v".formatted(path, res.statusCode()), new Exception(res.body()));
        }
        return res.body();
    }

    public String doPut(String path, Object data) throws Exception {
        String body = new Gson().toJson(data);
        HttpRequest req = requestBuilder(path).PUT(BodyPublishers.ofString(body)).build();
        HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
        if (res.statusCode() >= 400) {
            System.err.println(res.body());
            throw new Exception("[PUT] '%s' - %v".formatted(path, res.statusCode()), new Exception(body));
        }
        return res.body();
    }

    public String doPost(String path, Object data) throws Exception {
        String body = new Gson().toJson(data);
        HttpRequest req = requestBuilder(path).POST(BodyPublishers.ofString(body)).build();
        HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
        if (res.statusCode() >= 400) {
            System.err.println(res.body());
            throw new Exception("[POST] '%s' - %v".formatted(path, res.statusCode()), new Exception(body));
        }
        return res.body();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getUser() throws Exception {
        if (this.user == null) {
            String res = doGet("/users/me");
            this.user = new Gson().fromJson(res, Map.class);
        }
        return this.user;
    }

    public String getUserId() throws Exception {
        return (String) getUser().get("id");
    }

    public String getUsername() throws Exception {
        return (String) getUser().get("username");
    }

    public void updateStatus(String status) {
        try {
            doPut("/users/me/status", Map.of("user_id", getUserId(), "status", status));
        } catch (Exception e) {
            logger.error("Unable to update status to " + status, e);
        }
    }
}
