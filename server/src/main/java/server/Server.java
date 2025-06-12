package server;

import com.google.gson.Gson;
import dataaccess.*;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import service.AuthService;
import service.UserService;
import service.GameService;
import service.DBService;
import service.exceptions.UnauthorizedException;
import spark.Spark;


import java.util.Map;

import static spark.Spark.*;

public class Server {
    private final Gson gson;
    private final AuthService authService;
    private final UserService userService;
    private final GameService gameService;
    private final DBService databaseService;

    public Server() {
        this.gson            = new Gson();
        UserDAO userDao      = new MySqlUserDAO();
        AuthDAO authDao      = new MySqlAuthDAO();
        GameDAO gameDao      = new MySqlGameDAO();
        this.authService     = new AuthService(authDao);
        this.userService     = new UserService(userDao, authDao);
        this.gameService     = new GameService(gameDao, authDao);
        this.databaseService = new DBService(userDao, authDao, gameDao);
    }

    public int run(int desiredPort) {
        try {
            dataaccess.DatabaseInitializer.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        port(desiredPort);

        webSocket("/ws", server.websocket.WebSocketHandler.class);
        staticFiles.location("web");

        System.out.println("Registering WebSocket");


        post("/clear", (req, res) -> {
            // Wipe out all users, tokens, games, etc.
            databaseService.clear();
            res.status(200);
            return "{}";
        });


        exception(DataAccessException.class, (e, req, res) -> {
            res.type("application/json");
            res.status(500);
            res.body(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        });


        before((req, res) -> {
            if ("websocket".equalsIgnoreCase(req.headers("Upgrade"))) {
                return;
            }

            String path   = req.pathInfo();
            String method = req.requestMethod();

            if ("POST".equals(method)   && "/clear".equals(path)) {return;}
            if ("DELETE".equals(method) && "/db".equals(path)) {return;}
            if ("POST".equals(method)   && "/user".equals(path)) {return;}
            if ("POST".equals(method)   && "/session".equals(path)) {return;}
            if ("DELETE".equals(method) && "/session".equals(path)) {return;}


            String token = req.headers("AuthToken");
            if (token == null || token.isBlank()) {
                token = req.headers("Authorization");
            }
            if (token == null || token.isBlank()) {
                halt(401, gson.toJson(Map.of("message", "Error: unauthorized")));
            }

            try {
                authService.validateToken(token);
            } catch (UnauthorizedException ue) {
                halt(401, gson.toJson(Map.of("message", "Error: unauthorized")));
            }
        });



        new UserHandler(userService, gson).registerRoutes();
        new AuthHandler(userService, authService, gson).registerRoutes();
        new GameHandler(gameService, gson).registerRoutes();


        delete("/db", (req, res) -> {
            databaseService.clear();
            res.status(200);
            return "{}";
        });

        awaitInitialization();
        return port();
    }

    public void stop() {
        Spark.stop();
    }
}
