package ui;

import facade.ServerFacade;
import exception.ResponseException;
import model.CreateGameResponse;
import model.GameData;
import model.ListGamesResponse;
import model.JoinGameResponse;

import java.util.List;
import java.util.Scanner;

public class PostLoginClient {

    private final ServerFacade facade;
    private final String authToken;
    private final Scanner scanner = new Scanner(System.in);

    public PostLoginClient(ServerFacade facade, String authToken) {
        this.facade = facade;
        this.authToken = authToken;
    }

    public void run() {
        System.out.println("Options:");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "create game" + EscapeSequences.RESET_TEXT_COLOR
                + " ~~ create a new game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "list games" + EscapeSequences.RESET_TEXT_COLOR
                + " ~~ list existing games");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "play game" + EscapeSequences.RESET_TEXT_COLOR
                + " ~~ join a game to play");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "observe game" + EscapeSequences.RESET_TEXT_COLOR
                + " ~~ observe a game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "logout" + EscapeSequences.RESET_TEXT_COLOR
                + " ~~ log out");

        while (true) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_CYAN + "Chess home >>> " + EscapeSequences.RESET_TEXT_COLOR);
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "help" -> {
                    System.out.println("Options:");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "create game" + EscapeSequences.RESET_TEXT_COLOR
                            + " ~~ create a new game");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "list games" + EscapeSequences.RESET_TEXT_COLOR
                            + " ~~ list existing games");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "play game" + EscapeSequences.RESET_TEXT_COLOR
                            + " ~~ join a game to play");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "observe game" + EscapeSequences.RESET_TEXT_COLOR
                            + " ~~ observe a game");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "logout" + EscapeSequences.RESET_TEXT_COLOR
                            + " ~~ log out");
                }

                case "logout" -> {
                    try {
                        facade.logout(authToken);
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Logged out." + EscapeSequences.RESET_TEXT_COLOR);
                        return;  // back to PreLoginClient
                    } catch (ResponseException e) {
                        System.err.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + extractErrorMessage(e) + EscapeSequences.RESET_TEXT_COLOR);
                    }
                }

                case "create game" -> {
                    System.out.print("Enter game name: ");
                    String gameName = scanner.nextLine().trim();
                    if (gameName.isEmpty()) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                                + "Game name cannot be empty." + EscapeSequences.RESET_TEXT_COLOR);
                        break;
                    }
                    try {
                        CreateGameResponse resp = facade.createGame(gameName, authToken);
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                                + "Game created: " + gameName + EscapeSequences.RESET_TEXT_COLOR);
                    } catch (ResponseException e) {
                        System.err.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + extractErrorMessage(e) + EscapeSequences.RESET_TEXT_COLOR);
                    }
                }

                case "list games" -> {
                    try {
                        ListGamesResponse resp = facade.listGames(authToken);
                        List<GameData> games = resp.games();
                        if (games.isEmpty()) {
                            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                                    + "No active games." + EscapeSequences.RESET_TEXT_COLOR);
                        } else {
                            for (int i = 0; i < games.size(); i++) {
                                GameData gameData = games.get(i);
                                System.out.printf("%d) Name: \"%s\", White: %s, Black: %s%n",
                                        i + 1,
                                        gameData.gameName(),
                                        gameData.whiteUsername(),
                                        gameData.blackUsername());
                            }
                        }
                    } catch (ResponseException e) {
                        System.err.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + extractErrorMessage(e) + EscapeSequences.RESET_TEXT_COLOR);
                    }
                }

                case "play game" -> {
                    try {
                        ListGamesResponse resp = facade.listGames(authToken);
                        List<GameData> games = resp.games();
                        if (games.isEmpty()) {
                            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                                    + "No games available to join." + EscapeSequences.RESET_TEXT_COLOR);
                            break;
                        }
                        for (int i = 0; i < games.size(); i++) {
                            GameData g = games.get(i);
                            System.out.printf("%d) \"%s\", White=%s, Black=%s%n",
                                    i + 1,
                                    g.gameName(),
                                    (g.whiteUsername() == null ? "(empty)" : g.whiteUsername()),
                                    (g.blackUsername() == null ? "(empty)" : g.blackUsername()));
                        }
                        System.out.print("Enter the number of the game to play: ");
                        String line = scanner.nextLine().trim();
                        int choice = Integer.parseInt(line);
                        if (choice < 1 || choice > games.size()) {
                            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                                    + "Invalid selection." + EscapeSequences.RESET_TEXT_COLOR);
                            break;
                        }
                        GameData selected = games.get(choice - 1);

                        System.out.print("Choose color " + EscapeSequences.SET_TEXT_COLOR_GREEN
                                + "WHITE" + EscapeSequences.RESET_TEXT_COLOR + " or "
                                + EscapeSequences.SET_TEXT_COLOR_GREEN + "BLACK"
                                + EscapeSequences.RESET_TEXT_COLOR +  ": ");

                        String color = scanner.nextLine().trim().toUpperCase();
                        if (!color.equals("WHITE") && !color.equals("BLACK")) {
                            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                                    + "Invalid color." + EscapeSequences.RESET_TEXT_COLOR);
                            break;
                        }
                        JoinGameResponse joinResp = facade.joinGame(
                                selected.gameID(), color, authToken);
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                                + "Joined game " + selected.gameID() + " as " + color + EscapeSequences.RESET_TEXT_COLOR);

                        // Entering GameplayClient
                        // Draw the initial board from this perspective
                        new GameplayClient(selected.gameID(), color).drawInitialBoard();

                        // back to PostLoginClient
                        System.out.println("Returning to home menu.");
                    } catch (NumberFormatException ex) {
                        System.err.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + "Please enter a valid number." + EscapeSequences.RESET_TEXT_COLOR);
                    } catch (ResponseException e) {
                        System.err.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + extractErrorMessage(e) + EscapeSequences.RESET_TEXT_COLOR);
                    }
                }

                case "observe game" -> {
                    try {
                        ListGamesResponse resp = facade.listGames(authToken);
                        List<GameData> games = resp.games();
                        if (games.isEmpty()) {
                            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                                    + "No games available to observe." + EscapeSequences.RESET_TEXT_COLOR);
                            break;
                        }
                        for (int i = 0; i < games.size(); i++) {
                            GameData g = games.get(i);
                            System.out.printf("%d) ID=%d, Name=\"%s\", White=%s, Black=%s%n",
                                    i + 1,
                                    g.gameID(),
                                    g.gameName(),
                                    g.whiteUsername(),
                                    g.blackUsername());
                        }
                        System.out.print("Enter the number of the game to observe: ");
                        String line = scanner.nextLine().trim();
                        int choice = Integer.parseInt(line);
                        if (choice < 1 || choice > games.size()) {
                            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                                    + "Invalid selection." + EscapeSequences.RESET_TEXT_COLOR);
                            break;
                        }
                        GameData selected = games.get(choice - 1);
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                                + "Observing game " + selected.gameID() + EscapeSequences.RESET_TEXT_COLOR);

                        // Enter GameplayClient as observer
                        new GameplayClient(selected.gameID(), "OBSERVER").drawInitialBoard();

                        // back to PostLoginClient
                        System.out.println("Returning to home menu.");
                    } catch (NumberFormatException ex) {
                        System.err.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + "Please enter a valid number." + EscapeSequences.RESET_TEXT_COLOR);
                    } catch (ResponseException e) {
                        System.err.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + extractErrorMessage(e) + EscapeSequences.RESET_TEXT_COLOR);
                    }
                }

                default -> System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                        + "Unknown command."
                        + EscapeSequences.RESET_TEXT_COLOR
                        + " Type "
                        + EscapeSequences.SET_TEXT_COLOR_CYAN + "help" + EscapeSequences.RESET_TEXT_COLOR
                        + " for options.");
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
