package wisewires.agent;

public class App {
    public static void main(String[] args) {
        String serverAddress = args[0];
        String token = args[1];
        Agent agent = new Agent(serverAddress, token);
        agent.connect();
    }
}
