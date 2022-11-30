package dartsgame.game.domain;

import javax.persistence.Embeddable;

@Embeddable
public class DartMove {
    private int moves;
    private int remainingScore;

    public DartMove(){}

    public DartMove(int moves, int remainingScore) {
        this.moves = moves;
        this.remainingScore = remainingScore;
    }

    public int getMoves() {
        return moves;
    }

    public void setMoves(int moves) {
        this.moves = moves;
    }

    public int getRemainingScore() {
        return remainingScore;
    }

    public void setRemainingScore(int remainingScore) {
        this.remainingScore = remainingScore;
    }
}
