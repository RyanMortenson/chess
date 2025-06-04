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
        helpHelper();

        while (true) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_CYAN + "Chess login >>> "
                    + EscapeSequences.RESET_TEXT_COLOR);
            String command = scanner.nextLine().trim().toLowerCase();

            if (command.isEmpty()) {
                continue;
            }

            switch (command) {
                case "help" -> {
                    helpHelper();
                }

                case "q" -> {
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;
                }

                case "r" -> {
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

                        new PostLoginClient(facade, token).run();

                        System.out.println("You have been logged out.\n");
                    } catch (ResponseException e) {
                        ClientUtils.printError(e);
                    }
                }

                case "l" -> {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String password = scanner.nextLine().trim();

                    try {
                        LoginRequest req = new LoginRequest(username, password);
                        LoginResponse resp = facade.login(req);
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Logged in as "
                                + EscapeSequences.SET_TEXT_COLOR_YELLOW
                                + resp.username() + "\n"
                                + EscapeSequences.RESET_TEXT_COLOR);
                        String token = resp.authToken();

                        new PostLoginClient(facade, token).run();
                    } catch (ResponseException e) {
                        ClientUtils.printError(e);
                    }
                }

                default -> System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                        + "Unknown command."
                        + EscapeSequences.RESET_TEXT_COLOR
                        + " Type "
                        + EscapeSequences.SET_TEXT_COLOR_GREEN
                        + "help"
                        + EscapeSequences.RESET_TEXT_COLOR
                        + " to see options.");
            }
        }
    }

    private void helpHelper() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "♕ Welcome to Chess ♕ \n"
                + EscapeSequences.SET_TEXT_COLOR_YELLOW+ " Sign in to start. \n" + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println("Options:");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN
                + "\"r\""
                + EscapeSequences.RESET_TEXT_COLOR + " ~~ Register New Account");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN
                + "\"l\""
                + EscapeSequences.RESET_TEXT_COLOR + " ~~ Login to Existing Account");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN
                + "\"q\""
                + EscapeSequences.RESET_TEXT_COLOR + " ~~ Quit Program");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN
                + "\"help\""
                + EscapeSequences.RESET_TEXT_COLOR + " ~~ List these Commands");
    }
}
