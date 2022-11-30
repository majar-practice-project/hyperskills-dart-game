package dartsgame.dto;

import dartsgame.dto.validation.TargetScoreConstraint;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Pattern;

public class NewGameRequest {
    @TargetScoreConstraint
    private int targetScore;

    public void setTargetScore(int targetScore) {
        this.targetScore = targetScore;
    }

    public int getTargetScore() {
        return targetScore;
    }
}
