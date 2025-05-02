package chess;

public class PiecesMovesCalculator {

    public static java.util.Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        ChessPiece piece = board.getPiece(position);
        if (piece == null) {
            return java.util.Collections.emptyList();
        }

        switch (piece.getPieceType()) {
            case BISHOP:
                return bishopMoves(board, position, piece);
            case ROOK:
                return rookMoves(board, position, piece);
            case QUEEN:
                return queenMoves(board, position, piece);
            case KING:
                return kingMoves(board, position, piece);
            case KNIGHT:
                return knightMoves(board, position, piece);
            case PAWN:
                return pawnMoves(board, position, piece);

            default:
                return java.util.Collections.emptyList();
        }
    }

    private static java.util.Collection<ChessMove> bishopMoves(ChessBoard board,
                                                              ChessPosition position,
                                                              ChessPiece bishop) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        int[][] directions = {
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] dir : directions) {
            int r = position.getRow() + dir[0];
            int c = position.getColumn() + dir[1];
            while (r >= 1 && r <= 8 && c >= 1 && c <=8 ) {
                ChessPosition target = new ChessPosition(r, c);
                ChessPiece occupant = board.getPiece(target);

                if (occupant == null) {
                    moves.add(new ChessMove(position, target, null));
                } else {
                    if (occupant.getTeamColor() != bishop.getTeamColor()) {
                        moves.add(new ChessMove(position, target, null));
                    }
                    break;
                }

                r += dir[0];
                c += dir[1];
            }
        }
        return moves;
    }

    private static java.util.Collection<ChessMove> rookMoves(ChessBoard board,
                                                            ChessPosition position,
                                                            ChessPiece rook) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        int[][] directions = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };
        for (int[] dir : directions) {
            int r = position.getRow() + dir[0];
            int c = position.getColumn() + dir[1];
            while (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                ChessPosition target = new ChessPosition(r,c);
                ChessPiece occupant = board.getPiece(target);

                if (occupant == null) {
                    moves.add(new ChessMove(position, target, null));
                } else {
                    if (occupant.getTeamColor() != rook.getTeamColor()) {
                        moves.add(new ChessMove(position, target, null));
                    }
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
        return moves;
    }

    private static java.util.Collection<ChessMove> queenMoves(ChessBoard board,
                                                             ChessPosition position,
                                                             ChessPiece queen) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        int[][] directions = {{1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1}, {0,-1}, {1,-1}};
        for (int[] dir : directions) {
            int r = position.getRow() + dir[0];
            int c = position.getColumn() + dir[1];
            while (r >= 1 && r <= 8 && c >= 1 && c <= 8) {
                ChessPosition target = new ChessPosition(r, c);
                ChessPiece occupant = board.getPiece(target);
                if (occupant == null) {
                    moves.add(new ChessMove(position, target, null));
                } else {
                    if (occupant.getTeamColor() != queen.getTeamColor()) {
                        moves.add(new ChessMove(position, target, null));
                    }
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
        return moves;
    }

    private static java.util.Collection<ChessMove> kingMoves(ChessBoard board,
                                                             ChessPosition position,
                                                             ChessPiece king) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        int[][] steps = {{1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1}, {0,-1}, {1,-1}};
        for (int[] step : steps) {
            int r = position.getRow() + step[0];
            int c = position.getColumn() + step[1];
            if (r>=1 && r<=8 && c>= 1 && c<= 8) {
                ChessPosition target = new ChessPosition(r, c);
                ChessPiece occupant = board.getPiece(target);
                if (occupant == null) {
                    moves.add(new ChessMove(position, target, null));
                } else {
                    if (occupant.getTeamColor() != king.getTeamColor()) {
                        moves.add(new ChessMove(position, target, null));
                    }
                }
            }
        }
        return moves;
    }

    private static java.util.Collection<ChessMove> knightMoves(ChessBoard board,
                                                               ChessPosition position,
                                                               ChessPiece knight) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        int[][] jumps = {{2, 1}, {1, 2},
                {-1, 2}, {-2, 1},
                {-1, -2}, {-2, -1},
                {1, -2}, {2, -1}};
        for (int[] jump : jumps) {
            int r = position.getRow() + jump[0];
            int c = position.getColumn() + jump[1];
            if (r>=1 && r<=8 && c>=1 && c<= 8) {
                ChessPosition target = new ChessPosition(r, c);
                ChessPiece occupant = board.getPiece(target);
                if (occupant == null) {
                    moves.add(new ChessMove(position, target, null));
                } else {
                    if (occupant.getTeamColor() != knight.getTeamColor()) {
                        moves.add(new ChessMove(position, target, null));
                    }
                }
            }
        }
        return moves;
    }

    private static java.util.Collection<ChessMove> pawnMoves(ChessBoard board,
                                                             ChessPosition position,
                                                             ChessPiece pawn) {
        java.util.List<ChessMove> moves = new java.util.ArrayList<>();
        int old_r = position.getRow(), r, r2=0, c = position.getColumn();
        boolean isWhite = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE);
        int[] attack_left = {1, -1};
        int[] attack_right = {1, 1};

        //std_move (white)
        if (isWhite) {
            if (old_r == 2) {
                r = old_r + 1;
                r2 = old_r + 2;
            } else {
                r = old_r + 1;
            }
        } else // std_move (black)
        {
            if (old_r == 7) {
                r = old_r - 1;
                r2 = old_r - 2;
            } else {
                r = old_r - 1;
            }
        }

        if (r>=1 && r<=8 && c>=1 && c<=8) {
            //Double move from start
            if (r2 != 0) {
                ChessPosition target2 = new ChessPosition(r2, c);
                ChessPiece occupant2 = board.getPiece(target2);
                if (occupant2 == null){
                    moves.add(new ChessMove(position, target2, null));
                }
            }
            //Single move
            ChessPosition target = new ChessPosition(r, c);
            ChessPiece occupant = board.getPiece(target);
            if(occupant == null) {
                if (r == 8) {
                    moves.add(new ChessMove(position, target, ChessPiece.PieceType.QUEEN));
                } else {
                    moves.add(new ChessMove(position, target, null));
                }
            }
        }
        return moves;
    }
}
