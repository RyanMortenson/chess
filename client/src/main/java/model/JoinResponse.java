package model;

public record JoinResponse(
        String gameId,
        java.util.List<String> players,
        String message
) { }
