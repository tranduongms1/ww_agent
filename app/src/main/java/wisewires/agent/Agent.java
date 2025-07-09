package wisewires.agent;

import java.net.URI;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Agent extends WebSocketClient {
    private Logger logger = LoggerFactory.getLogger(Agent.class);

    private Context ctx;
    private Client client;
    private Queue<Post> posts;
    private Thread runThread;

    public Agent(String serverAddress, String token) {
        super(URI.create("wss://%s/api/v4/websocket".formatted(serverAddress)));
        if (Util.isValidIPv4(serverAddress)) {
            this.uri = URI.create("ws://%s/api/v4/websocket".formatted(serverAddress));
        }
        this.addHeader("Authorization", "Bearer " + token);
        this.ctx = new Context();
        this.client = new Client(serverAddress, token);
        this.ctx.client = this.client;
        this.posts = new ConcurrentLinkedQueue<Post>();
        this.runThread = new Thread(new AgentRunThread(this));
        this.runThread.start();
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
            case "posted": {
                Post post = Post.fromJson(msg.getString("post"));
                posts.add(post);
                break;
            }
            case "post_deleted": {
                Post post = Post.fromJson(msg.getString("post"));
                String postId = post.getId();
                posts.removeIf(p -> p.getId().equals(postId));
                break;
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("Connection closed: " + reason);
        Util.sleep(5000);
        new Thread(() -> reconnect());
    }

    @Override
    public void onError(Exception e) {
        logger.error("Connection error", e);
    }

    private class AgentRunThread implements Runnable {
        private final Agent agent;
        private final ExecutorService executor;

        AgentRunThread(Agent agent) {
            this.agent = agent;
            this.executor = Executors.newSingleThreadExecutor();
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                Post post = agent.posts.poll();
                if (post != null) {
                    runPost(post);
                }
            }
        }

        public void runPost(Post post) {
            List<String> lines = post.getMessage().lines().toList();
            for (String line : lines) {
                if (!line.isBlank()) {
                    executor.submit(() -> {
                        try {
                            SmokeTest.run(ctx, line);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }
}
