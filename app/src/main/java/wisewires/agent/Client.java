package wisewires.agent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    private static byte[] joinBytes(byte[]... arrays) throws Exception {
        try (var out = new java.io.ByteArrayOutputStream()) {
            for (byte[] array : arrays) {
                out.write(array);
            }
            return out.toByteArray();
        }
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

    public void createPost(Post post) throws Exception {
        doPost("/posts", post);
    }

    public void updatePost(String postId, Map<String, Object> patch) throws Exception {
        doPut("/posts/" + postId + "/patch", patch);
    }

    public void updatePost(String postId, Attachment... attachments) throws Exception {
        updatePost(postId, Map.of(
                "message", "",
                "props", Map.of("attachments", attachments)));
    }

    @SuppressWarnings("unchecked")
    public String uploadFile(String channelId, String filePath) throws Exception {
        Path path = Path.of(filePath);
        String fileName = path.getFileName().toString();
        String mimeType = Files.probeContentType(path);
        String boundary = "Boundary" + Long.toHexString(System.currentTimeMillis());
        String channelPart = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"channel_id\"\r\n\r\n" +
                channelId + "\r\n";
        String filePartHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"files\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: " + mimeType + "\r\n\r\n";
        String ending = "\r\n--" + boundary + "--\r\n";
        byte[] fileContent = Files.readAllBytes(path);
        byte[] requestBody = joinBytes(
                channelPart.getBytes(),
                filePartHeader.getBytes(),
                fileContent,
                ending.getBytes());
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + "/files"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 201) {
            System.err.println(res.body());
            throw new Exception("Response code: " + res.statusCode());
        }
        Map<String, Object> resData = new Gson().fromJson(res.body(), Map.class);
        List<Map<String, Object>> infos = (List<Map<String, Object>>) resData.get("file_infos");
        return infos.get(0).get("id").toString();
    }
}
