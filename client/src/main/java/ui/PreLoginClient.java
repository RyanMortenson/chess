package ui;

import exception.ResponseException;
import facade.ServerFacade;
import model.LoginRequest;
import model.LoginResponse;
import model.RegisterRequest;
import model.RegisterResponse;

import java.util.Scanner;

public class PreLoginClient {
    private final ServerFacade facade;

    public PreLoginClient(String baseUrl) {
        this.facade = new ServerFacade(baseUrl);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "♕ Welcome to Chess ♕ \n"
                + EscapeSequences.SET_TEXT_COLOR_YELLOW+ " Sign in to start. \n" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println("Options:");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN
                + "register"
                + EscapeSequences.RESET_TEXT_COLOR + " ~~ Create Account");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN
                + "login"
                + EscapeSequences.RESET_TEXT_COLOR + " ~~ Play Chess");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN
                + "quit"
                + EscapeSequences.RESET_TEXT_COLOR + " ~~ Quit Chess");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN
                + "help"
                + EscapeSequences.RESET_TEXT_COLOR + " ~~ List Commands");

        while (true) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_CYAN + "Chess login >>> " + EscapeSequences.RESET_TEXT_COLOR);
            String command = scanner.nextLine().trim().toLowerCase();

            if (command.isEmpty()) {
                continue;
            }


            switch (command) {
                case "help" -> {
                    System.out.println("Options:");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN
                            + "register"
                            + EscapeSequences.RESET_TEXT_COLOR + " ~~ Create Account");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN
                            + "login"
                            + EscapeSequences.RESET_TEXT_COLOR + " ~~ Play Chess");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN
                            + "quit"
                            + EscapeSequences.RESET_TEXT_COLOR + " ~~ Quit Chess");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN
                            + "help"
                            + EscapeSequences.RESET_TEXT_COLOR + " ~~ List Commands");
                }

                case "quit" -> {
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;
                }

                case "register" -> {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String password = scanner.nextLine().trim();
                    System.out.print("Email: ");
                    String email = scanner.nextLine().trim();

                    try {
                        RegisterRequest req = new RegisterRequest(username, password, email);
                        RegisterResponse resp = facade.register(req);
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Registered as "
                                + EscapeSequences.SET_TEXT_COLOR_YELLOW
                                + resp.username()
                                + EscapeSequences.RESET_TEXT_COLOR + "\n");
                        String token = resp.authToken();

                        // transition to PostLoginClient
                        new PostLoginClient(facade, token).run();

                        // back to PreLoginClient
                        System.out.println("You have been logged out.\n");
                    } catch (ResponseException e) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + extractErrorMessage(e)
                                + EscapeSequences.RESET_TEXT_COLOR);
                    }
                }

                case "login" -> {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String password = scanner.nextLine().trim();

                    try {
                        LoginRequest req = new LoginRequest(username, password);
                        LoginResponse resp = facade.login(req);
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Logged in as "
                                + EscapeSequences.SET_TEXT_COLOR_YELLOW
                                + resp.username()
                                + EscapeSequences.RESET_TEXT_COLOR);
                        String token = resp.authToken();

                        // transition to PostLoginClient
                        new PostLoginClient(facade, token).run();

                        // back to PreLoginClient
                        System.out.println("You have been logged out.\n");
                    } catch (ResponseException e) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + extractErrorMessage(e)
                                + EscapeSequences.RESET_TEXT_COLOR);
                    }
                }

                default -> System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                        + "Unknown command."
                        + EscapeSequences.RESET_TEXT_COLOR
                        + " Type "
                        + EscapeSequences.SET_TEXT_COLOR_CYAN
                        + "help"
                        + EscapeSequences.RESET_TEXT_COLOR
                        + " to see options.");
            }
        }
    }

    // GSON parser helper:
    private String extractErrorMessage(ResponseException e) {
        try {
            com.google.gson.JsonObject obj = new com.google.gson.Gson()
                    .fromJson(e.getMessage(), com.google.gson.JsonObject.class);
            if (obj.has("message")) {
                return obj.get("message").getAsString();
            }
        } catch (com.google.gson.JsonSyntaxException ex) {
            // If it isn't valid json
        }
        return e.getMessage();
    }
}
