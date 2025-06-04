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
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"c\"" + EscapeSequences.RESET_TEXT_COLOR
                + " ~~ Create a New Game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"l\"" + EscapeSequences.RESET_TEXT_COLOR
                + " ~~ List Existing Games");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"p\"" + EscapeSequences.RESET_TEXT_COLOR
                + " ~~ Join a Game to Play");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"o\"" + EscapeSequences.RESET_TEXT_COLOR
                + " ~~ Observe a Game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"logout\"" + EscapeSequences.RESET_TEXT_COLOR
                + " ~~ Logout");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"help\"" + EscapeSequences.RESET_TEXT_COLOR
                + " ~~ List these Commands");

        while (true) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_CYAN + "Chess home >>> " + EscapeSequences.RESET_TEXT_COLOR);
            String command = scanner.nextLine().trim().toLowerCase();

            final String format = EscapeSequences.SET_TEXT_COLOR_GREEN + "%d  "
                    + EscapeSequences.RESET_TEXT_COLOR + "Name: "
                    + EscapeSequences.SET_TEXT_COLOR_BLUE + "%s   "
                    + EscapeSequences.RESET_TEXT_COLOR + "White Player: "
                    + EscapeSequences.SET_TEXT_COLOR_BLUE + "%s "
                    + EscapeSequences.RESET_TEXT_COLOR + "Black Player: "
                    + EscapeSequences.SET_TEXT_COLOR_BLUE + "%s"
                    + EscapeSequences.RESET_TEXT_COLOR + "%n";

            switch (command) {
                case "help" -> {
                    System.out.println("Options:");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"c\"" + EscapeSequences.RESET_TEXT_COLOR
                            + " ~~ Create a New Game");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"l\"" + EscapeSequences.RESET_TEXT_COLOR
                            + " ~~ List Existing Games");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"p\"" + EscapeSequences.RESET_TEXT_COLOR
                            + " ~~ Join a Game to Play");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"o\"" + EscapeSequences.RESET_TEXT_COLOR
                            + " ~~ Observe a Game");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"logout\"" + EscapeSequences.RESET_TEXT_COLOR
                            + " ~~ Logout");
                    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"help\"" + EscapeSequences.RESET_TEXT_COLOR
                            + " ~~ List these Commands");
                }

                case "logout" -> {
                    try {
                        facade.logout(authToken);
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Logged out.\n"
                                + EscapeSequences.RESET_TEXT_COLOR);
                        return;  // back to PreLoginClient
                    } catch (ResponseException e) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + extractErrorMessage(e) + EscapeSequences.RESET_TEXT_COLOR);
                    }
                }

                case "c" -> {
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
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + extractErrorMessage(e) + EscapeSequences.RESET_TEXT_COLOR);
                    }
                }

                case "l" -> {
                    try {
                        ListGamesResponse resp = facade.listGames(authToken);
                        List<GameData> games = resp.games();
                        if (games.isEmpty()) {
                            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                                    + "No active games." + EscapeSequences.RESET_TEXT_COLOR);
                        } else {
                            for (int i = 0; i < games.size(); i++) {
                                GameData g = games.get(i);
                                System.out.printf(format,
                                        i + 1,
                                        g.gameName(),
                                        (g.whiteUsername()!=null) ? g.whiteUsername()
                                                : EscapeSequences.SET_TEXT_COLOR_RED + "(Empty Slot)"
                                                + EscapeSequences.RESET_TEXT_COLOR,
                                        (g.blackUsername()!=null) ? g.blackUsername()
                                                : EscapeSequences.SET_TEXT_COLOR_RED + "(Empty Slot)"
                                                + EscapeSequences.RESET_TEXT_COLOR);
                            }
                        }
                    } catch (ResponseException e) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + extractErrorMessage(e) + EscapeSequences.RESET_TEXT_COLOR);
                    }
                }

                case "p" -> {
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
                            System.out.printf(format,
                                    i + 1,
                                    g.gameName(),
                                    (g.whiteUsername()!=null) ? g.whiteUsername()
                                            : EscapeSequences.SET_TEXT_COLOR_RED + "(Empty Slot)"
                                            + EscapeSequences.RESET_TEXT_COLOR,
                                    (g.blackUsername()!=null) ? g.blackUsername()
                                            : EscapeSequences.SET_TEXT_COLOR_RED + "(Empty Slot)"
                                            + EscapeSequences.RESET_TEXT_COLOR);
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
                                + "Joined game " + selected.gameName() + " as " + color + EscapeSequences.RESET_TEXT_COLOR);

                        // Entering GameplayClient
                        // Draw the initial board from this perspective
                        new GameplayClient(selected.gameID(), color).drawInitialBoard();

                        // back to PostLoginClient
                        System.out.println("Returning to home menu.");
                    } catch (NumberFormatException ex) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + "Please enter a valid number." + EscapeSequences.RESET_TEXT_COLOR);
                    } catch (ResponseException e) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + extractErrorMessage(e) + EscapeSequences.RESET_TEXT_COLOR);
                    }
                }

                case "o" -> {
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
                            System.out.printf(format,
                                    i + 1,
                                    g.gameName(),
                                    (g.whiteUsername()!=null) ? g.whiteUsername()
                                            : EscapeSequences.SET_TEXT_COLOR_RED + "(Empty Slot)"
                                            + EscapeSequences.RESET_TEXT_COLOR,
                                    (g.blackUsername()!=null) ? g.blackUsername()
                                            : EscapeSequences.SET_TEXT_COLOR_RED + "(Empty Slot)"
                                            + EscapeSequences.RESET_TEXT_COLOR);
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
                                + "Observing game: " + selected.gameName() + EscapeSequences.RESET_TEXT_COLOR);

                        // Enter GameplayClient as observer
                        new GameplayClient(selected.gameID(), "OBSERVER").drawInitialBoard();

                        // back to PostLoginClient
                        System.out.println("Returning to home menu.");
                    } catch (NumberFormatException ex) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + "Please enter a valid number." + EscapeSequences.RESET_TEXT_COLOR);
                    } catch (ResponseException e) {
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + extractErrorMessage(e) + EscapeSequences.RESET_TEXT_COLOR);
                    }
                }

                default -> System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                        + "Unknown command."
                        + EscapeSequences.RESET_TEXT_COLOR
                        + " Type "
                        + EscapeSequences.SET_TEXT_COLOR_GREEN + "help" + EscapeSequences.RESET_TEXT_COLOR
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
