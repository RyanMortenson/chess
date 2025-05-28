package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccessException;
import service.UserService;
import service.AuthService;
import service.exceptions.UnauthorizedException;
import service.requests.LoginRequest;
import service.results.LoginResult;
import spark.Request;
import spark.Response;
import java.util.Collections;

import static spark.Spark.delete;
import static spark.Spark.post;

public class AuthHandler {
    private final UserService userService;
    private final AuthService authService;
    private final Gson gson;

    public AuthHandler(UserService userService, AuthService authService, Gson gson) {
        this.userService = userService;
        this.authService = authService;
        this.gson = gson;
    }

    public void registerRoutes() {
        post("/session", this::handleLogin);
        delete("/session", this::handleLogout);
    }

    private Object handleLogin(Request req, Response res) {
        LoginRequest loginReq;
        try {
            loginReq = gson.fromJson(req.body(), LoginRequest.class);
        } catch (JsonSyntaxException e) {
            res.status(400);
            return errorBody("bad request");
        }

        if (loginReq == null || loginReq.username() == null || loginReq.password() == null) {
            res.status(400);
            return errorBody("bad request");
        }

        try {
            LoginResult result = userService.login(loginReq);
            res.status(200);
            return gson.toJson(result);

        } catch (UnauthorizedException e) {
            res.status(401);
            return errorBody("unauthorized");

        } catch (DataAccessException e) {
            return mapDataAccessError(res, e);

        } catch (Exception e) {
            res.status(500);
            return errorBody("internal error");
        }
    }

    private Object handleLogout(Request req, Response res) {
        String token = req.headers("authorization");
        if (token == null || token.isBlank()) {
            res.status(401);
            return errorBody("unauthorized");
        }

        try {
            authService.revokeToken(token);
            res.status(200);
            return "{}";

        } catch (DataAccessException e) {
            return mapDataAccessError(res, e);
        }
    }

    private Object mapDataAccessError(Response res, DataAccessException e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (msg.contains("unauthorized") || msg.contains("invalid")) {
            res.status(401);
            return errorBody("unauthorized");
        } else {
            res.status(500);
            return errorBody("internal server error");
        }
    }

    private String errorBody(String msg) {
        return gson.toJson(Collections.singletonMap("message", "Error: " + msg));
    }
}