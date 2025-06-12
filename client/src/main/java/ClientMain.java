import ui.PreLoginClient;

public class ClientMain {
    public static void main(String[] args) {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 8081;
        String baseUrl = "http://localhost:" + port;

        new PreLoginClient(baseUrl).run();
    }
}