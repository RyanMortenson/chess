package service.requests;

public record JoinGameRequest(
        int gameID,
        String playerColor
) {}
