package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.UserService;
import service.requests.RegisterRequest;
import service.results.RegisterResult;
import spark.Request;
import spark.Response;

import java.util.Map;

import static spark.Spark.post;
import static spark.Spark.halt;

public class UserHandler {
    private final UserService userService;
    private final Gson gson;

    public UserHandler(UserService userService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
    }

    public void registerRoutes() {
        post("/user", this::handleRegister);
    }

    private Object handleRegister(Request req, Response res) {
        try {
            RegisterRequest r = gson.fromJson(req.body(), RegisterRequest.class);
            // missing fields 400
            if (r.username() == null || r.password() == null || r.email() == null) {
                res.status(400);
                return gson.toJson(Map.of("message", "Error: all fields required"));
            }
            RegisterResult result = userService.register(r);
            res.type("application/json");
            res.status(200);
            return gson.toJson(result);
        } catch (DataAccessException e) {
            // duplicate username 403
            res.type("application/json");
            res.status(403);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

}