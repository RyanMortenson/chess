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

        System.out.println("Welcome to Chess! Type "
                + Colors.FG_CYAN + "help"
                + Colors.RESET + " for commands.");

        while (true) {
            System.out.print(Colors.FG_CYAN + ">>> " + Colors.RESET);
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "help" -> {
                    System.out.println("Available commands:");
                    System.out.println("  " + Colors.FG_GREEN
                            + "register"
                            + Colors.RESET + " ~~ Create Account");
                    System.out.println("  " + Colors.FG_GREEN
                            + "login"
                            + Colors.RESET + " ~~ Play Chess");
                    System.out.println("  " + Colors.FG_GREEN
                            + "quit"
                            + Colors.RESET + " ~~ Quit Chess");
                    System.out.println("  " + Colors.FG_GREEN
                            + "help"
                            + Colors.RESET + " ~~ List Commands");
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
                        System.out.println("Registered as " + Colors.FG_YELLOW + resp.username() + Colors.RESET);
                        String token = resp.authToken();

                        // change to PostLoginClient
                        new PostLoginClient(facade, token).run();

                        // back to preLoginClient
                        System.out.println("You have been logged out.");
                    } catch (ResponseException e) {
                        System.err.println(Colors.FG_RED + "Registration failed: " + e.getMessage() + Colors.RESET);
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
                        System.out.println("Logged in as "
                                + Colors.FG_YELLOW + resp.username() + Colors.RESET);
                        String token = resp.authToken();

                        // change to PostLoginClient
                        new PostLoginClient(facade, token).run();

                        // back to PreLoginClient
                        System.out.println("You have been logged out.");
                    } catch (ResponseException ex) {
                        System.err.println(Colors.FG_RED
                                + "Login failed: " + ex.getMessage() + Colors.RESET);
                    }
                }

                default -> System.out.println(Colors.FG_YELLOW + "Unknown command."
                                    + Colors.RESET + " Type "
                                    + Colors.FG_CYAN + "help"
                                    + Colors.RESET + " to see options.");
            }
        }
    }
}
