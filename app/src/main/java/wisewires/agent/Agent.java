package wisewires.agent;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
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

        AgentRunThread(Agent agent) {
            this.agent = agent;
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                Post post = agent.posts.poll();
                if (post == null || !post.getRootId().isEmpty())
                    continue;
                switch (post.getType()) {
                    case "": {
                        Attachment attachment = new Attachment();
                        attachment.setColor("default");
                        attachment.setText(post.getMessage());
                        try {
                            client.updatePost(post.getId(), attachment);
                            ctx.post = post;
                            runPost(post);
                            attachment.setColor("good");
                            client.updatePost(post.getId(), attachment);
                        } catch (Exception e) {
                            e.printStackTrace();
                            attachment.setColor("danger");
                            client.updatePost(post.getId(), attachment);
                        }
                        break;
                    }
                    case "custom_test_case": {
                        Attachment attachment = new Attachment();
                        attachment.setColor("default");
                        attachment.setText(post.getMessage());
                        try {
                            client.updatePost(post.getId(), attachment);
                            ctx.post = post;
                            runTestCase(post);
                            attachment.setColor("good");
                            client.updatePost(post.getId(), attachment);
                        } catch (Exception e) {
                            attachment.setColor("danger");
                            client.updatePost(post.getId(), attachment);
                            try {
                                attachment.setText(e.getMessage());
                                String path = Util.captureFullPage();
                                String fileId = client.uploadFile(post.getChannelId(), path);
                                Post p = new Post();
                                p.setChannelId(post.getChannelId());
                                p.setRootId(post.getId());
                                p.setFileIds(List.of(fileId));
                                p.setProps(new HashMap<>(Map.of("attachments", List.of(attachment))));
                                client.createPost(p);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                        break;
                    }
                }
            }
        }

        public void runPost(Post post) throws Exception {
            List<String> lines = post.getMessage().lines().toList();
            for (String line : lines) {
                if (!line.isBlank()) {
                    Browser.run(ctx, line);
                }
            }
            if (ctx.checkoutProcess != null) {
                Checkout.waitForNavigateTo();
                Checkout.process(ctx);
            }
        }

        public void runTestCase(Post post) throws Exception {
            List<String> lines = post.getStringProp("script").lines().toList();
            for (String line : lines) {
                if (!line.isBlank()) {
                    Browser.run(ctx, line);
                }
            }
            if (ctx.checkoutProcess != null) {
                Checkout.waitForNavigateTo();
                Checkout.process(ctx);
            }
        }
    }
}
