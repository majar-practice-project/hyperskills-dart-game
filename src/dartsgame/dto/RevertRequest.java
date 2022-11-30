package dartsgame.dto;

import dartsgame.game.GameConflictException;
import dartsgame.game.GameResourceNotFoundException;

public class RevertRequest {
    private long gameId;
    private int move;

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public int getMove() {
        return move;
    }

    public void setMove(int move) {
        this.move = move;
    }

    public void validateMoves(int moveSize) throws GameConflictException {
        moveSize--;
        if (move < 0 || move > moveSize) {
            throw new GameConflictException("Move not found!");
        }
        if (move == moveSize) {
            throw new GameConflictException("There is nothing to revert!");
        }
    }
}
