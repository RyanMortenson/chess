package service.requests;

public record JoinGameRequest(
        Integer gameID,
        String playerColor,
        String authToken
) {}
