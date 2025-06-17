package wisewires.agent;

import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Agent extends WebSocketClient {
    static Logger logger = LoggerFactory.getLogger(Agent.class);

    private Context ctx;
    private Client client;

    public Agent(String serverAddress, String token) {
        super(URI.create("ws://%s/api/v4/websocket".formatted(serverAddress)));
        this.addHeader("Authorization", "Bearer " + token);
        this.ctx = new Context();
        this.client = new Client(serverAddress, token);
        this.ctx.client = this.client;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.info("Connected to server");
        client.updateStatus("online");
    }

    @Override
    public void onMessage(String message) {
        WebSocketMessage msg = WebSocketMessage.fromJson(message);
        switch (msg.getEvent()) {
            case "posted":
                Post post = Post.fromJson(msg.getString("post"));
                System.out.println(post.getMessage());
                break;
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("Connection closed: " + reason);
        Util.delay(5);
        new Thread(() -> reconnect());
    }

    @Override
    public void onError(Exception e) {
        logger.error("Connection error", e);
    }
}
