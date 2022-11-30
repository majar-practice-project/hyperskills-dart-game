package dartsgame.dto;

import dartsgame.game.GameConflictException;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

public class CancelRequest {
    @Min(value = 0, message = "Wrong move!")
    private Long gameId;

    private String status;

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void validateStatus(String playerOne, String playerTwo) throws GameConflictException {
        String regex = " wins!$";
        if(status.matches(".*"+regex)){
            String winner = status.replaceAll(regex, "");
            if(winner.equals(playerOne) || winner.equals(playerTwo) || winner.equals("Nobody")) return;
        }
        throw new GameConflictException("Wrong status!");
    }
}
