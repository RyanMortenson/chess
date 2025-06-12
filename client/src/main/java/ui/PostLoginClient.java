package ui;

import facade.ServerFacade;
import exception.ResponseException;
import model.GameData;
import model.ListGamesResponse;
import model.CreateGameResponse;
import model.JoinGameResponse;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class PostLoginClient {

    private final ServerFacade facade;
    private final String authToken;
    private final Scanner scanner = new Scanner(System.in);
    private static final String baseUrl = "localhost:8081";

    private static final String GAME_LIST_FORMAT = EscapeSequences.SET_TEXT_COLOR_GREEN + "%d  "
            + EscapeSequences.RESET_TEXT_COLOR + "Name: "
            + EscapeSequences.SET_TEXT_COLOR_BLUE + "%s   "
            + EscapeSequences.RESET_TEXT_COLOR + "White Player: "
            + EscapeSequences.SET_TEXT_COLOR_BLUE + "%s "
            + EscapeSequences.RESET_TEXT_COLOR + "Black Player: "
            + EscapeSequences.SET_TEXT_COLOR_BLUE + "%s"
            + EscapeSequences.RESET_TEXT_COLOR + "%n";

    public PostLoginClient(ServerFacade facade, String authToken) {
        this.facade = facade;
        this.authToken = authToken;
    }

    public void run() {
        printMenu();

        while (true) {
            System.out.print(EscapeSequences.SET_TEXT_COLOR_CYAN + "Chess home >>> " + EscapeSequences.RESET_TEXT_COLOR);
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "help" -> printMenu();

                case "logout" -> {
                    handleLogout();
                    return;
                }

                case "c" -> handleCreate();

                case "l" -> handleList();

                case "p" -> handleJoin();

                case "o" -> handleObserve();

                default -> System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                        + "Unknown command." + EscapeSequences.RESET_TEXT_COLOR
                        + " Type " + EscapeSequences.SET_TEXT_COLOR_GREEN + "help" + EscapeSequences.RESET_TEXT_COLOR
                        + " for options.");
            }
        }
    }

    private void handleLogout() {
        try {
            facade.logout(authToken);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Logged out.\n" + EscapeSequences.RESET_TEXT_COLOR);
        } catch (ResponseException e) {
            ClientUtils.printError(e);
        }
    }

    private void handleCreate() {
        System.out.print("Enter game name: ");
        String gameName = scanner.nextLine().trim();
        if (gameName.isEmpty()) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "Game name cannot be empty." + EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        try {
            CreateGameResponse resp = facade.createGame(gameName, authToken);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "Game created: " + gameName + EscapeSequences.RESET_TEXT_COLOR);
        } catch (ResponseException e) {
            ClientUtils.printError(e);
        }
    }

    private void handleList() {
        try {
            List<GameData> games = getGameList();
            if (games.isEmpty()) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                        + "No active games." + EscapeSequences.RESET_TEXT_COLOR);
            } else {
                printGames(games);
            }
        } catch (ResponseException e) {
            ClientUtils.printError(e);
        }
    }

    private void handleJoin() {
        try {
            List<GameData> games = getGameList();
            if (games.isEmpty()) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                        + "No games available to join." + EscapeSequences.RESET_TEXT_COLOR);
                return;
            }
            printGames(games);

            GameData selected = selectGame(games, "to play");
            if (selected == null) return;

            System.out.print("Choose color " + EscapeSequences.SET_TEXT_COLOR_GREEN + "WHITE"
                    + EscapeSequences.RESET_TEXT_COLOR + " or " + EscapeSequences.SET_TEXT_COLOR_GREEN + "BLACK"
                    + EscapeSequences.RESET_TEXT_COLOR + ": ");
            String color = scanner.nextLine().trim().toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                        + "Invalid color." + EscapeSequences.RESET_TEXT_COLOR);
                return;
            }

            facade.joinGame(selected.gameID(), color, authToken);
            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "Joined game " + selected.gameName() + " as " + color + EscapeSequences.RESET_TEXT_COLOR);

            // Start gameplay and stay in game until leave or resign
            new GameplayClient(baseUrl, authToken, selected.gameID(), color).run();

        } catch (ResponseException e) {
            ClientUtils.printError(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleObserve() {
        try {
            List<GameData> games = getGameList();
            if (games.isEmpty()) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                        + "No games available to observe." + EscapeSequences.RESET_TEXT_COLOR);
                return;
            }
            printGames(games);

            GameData selected = selectGame(games, "to observe");
            if (selected == null) return;

            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "Observing game: " + selected.gameName() + EscapeSequences.RESET_TEXT_COLOR);

            // Start gameplay in observer mode
            new GameplayClient(baseUrl, authToken, selected.gameID(), "OBSERVER").run();

        } catch (ResponseException e) {
            ClientUtils.printError(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printMenu() {
        System.out.println("Options:");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"c\"" + EscapeSequences.RESET_TEXT_COLOR + " ~~ Create a New Game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"l\"" + EscapeSequences.RESET_TEXT_COLOR + " ~~ List Existing Games");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"p\"" + EscapeSequences.RESET_TEXT_COLOR + " ~~ Join a Game to Play");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"o\"" + EscapeSequences.RESET_TEXT_COLOR + " ~~ Observe a Game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"logout\"" + EscapeSequences.RESET_TEXT_COLOR + " ~~ Logout");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "\"help\"" + EscapeSequences.RESET_TEXT_COLOR + " ~~ List these Commands");
    }

    private List<GameData> getGameList() throws ResponseException {
        return facade.listGames(authToken).games();
    }

    private void printGames(List<GameData> games) {
        for (int i = 0; i < games.size(); i++) {
            GameData g = games.get(i);
            System.out.printf(GAME_LIST_FORMAT,
                    i + 1,
                    g.gameName(),
                    g.whiteUsername() != null ? g.whiteUsername() : EscapeSequences.SET_TEXT_COLOR_RED + "(Empty Slot)" + EscapeSequences.RESET_TEXT_COLOR,
                    g.blackUsername() != null ? g.blackUsername() : EscapeSequences.SET_TEXT_COLOR_RED + "(Empty Slot)" + EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private GameData selectGame(List<GameData> games, String action) {
        System.out.print("Enter the number of the game " + action + ": ");
        String line = scanner.nextLine().trim();
        try {
            int choice = Integer.parseInt(line);
            if (choice < 1 || choice > games.size()) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Invalid selection." + EscapeSequences.RESET_TEXT_COLOR);
                return null;
            }
            return games.get(choice - 1);
        } catch (NumberFormatException ex) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Please enter a valid number." + EscapeSequences.RESET_TEXT_COLOR);
            return null;
        }
    }
}
