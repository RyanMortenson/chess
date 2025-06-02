package model;

public record JoinRequest(
        String authToken,
        String gameId,
        String color
) { }
