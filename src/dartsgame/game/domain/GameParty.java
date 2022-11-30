package dartsgame.game.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dartsgame.dto.DartTurn;
import dartsgame.dto.GameHistory;
import dartsgame.game.GameConflictException;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

@Entity
public class GameParty {
    @Id
    @GeneratedValue
    private Long gameId;

    private String playerOne;

    private String playerTwo;

    private String gameStatus;

    private int playerOneScores;

    private int playerTwoScores;

    private String turn;

    @JsonIgnore
    @ElementCollection
    private List<DartMove> dartMoves;

    public GameParty() {
    }

    public GameParty(String creator, int targetScore) {
        this.playerOne = creator;
        this.turn = creator;
        this.playerTwo = "";
        this.playerOneScores = targetScore;
        this.playerTwoScores = targetScore;
        this.gameStatus = GameStatus.CREATED;
        this.dartMoves = new ArrayList<>();
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getPlayerOne() {
        return playerOne;
    }

    public void setPlayerOne(String playerOne) {
        this.playerOne = playerOne;
    }

    public String getPlayerTwo() {
        return playerTwo;
    }

    public void setPlayerTwo(String playerTwo) {
        this.playerTwo = playerTwo;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }

    public int getPlayerOneScores() {
        return playerOneScores;
    }

    public void setPlayerOneScores(int playerOneScores) {
        this.playerOneScores = playerOneScores;
    }

    public int getPlayerTwoScores() {
        return playerTwoScores;
    }

    public void setPlayerTwoScores(int playerTwoScores) {
        this.playerTwoScores = playerTwoScores;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public void start(String playerTwo) {
        this.gameStatus = GameStatus.STARTED;
        this.playerTwo = playerTwo;
        dartMoves.add(new DartMove(0, playerOneScores));
    }

    public boolean availableToJoin() {
        return GameStatus.CREATED.equals(gameStatus);
    }

    public boolean stillOngoing() {
        return GameStatus.CREATED.equals(gameStatus)
                || GameStatus.STARTED.equals(gameStatus)
                || GameStatus.PLAYING.equals(gameStatus);
    }

    public boolean processThrow(String playerName, DartTurn turnThrows) throws GameConflictException {
        if (!turn.equals(playerName)) throw new GameConflictException("Wrong turn!");

        if (playerOne.equals(playerName)) {
            int result = playerOneScores;
            for (int score : turnThrows.retrieveThrowsScores()) {
                if (result <= 1) throw new GameConflictException("Wrong throws!");
                result -= Math.abs(score);
                if (result == 0 && DartTurn.checkDoubleScore(score)) {
                    playerOneScores = 0;
                    gameStatus = GameStatus.finished(playerName);
                    dartMoves.add(new DartMove(dartMoves.size(), playerOneScores));
                    return true;
                }
            }
            turn = playerTwo;
            if (result > 1) {
                playerOneScores = result;
            }
            dartMoves.add(new DartMove(dartMoves.size(), playerOneScores));
        } else if (playerTwo.equals(playerName)) {
            int result = playerTwoScores;
            for (int score : turnThrows.retrieveThrowsScores()) {
                if (result <= 1) throw new GameConflictException("Wrong throws!");
                result -= Math.abs(score);
                if (result == 0 && DartTurn.checkDoubleScore(score)) {
                    playerTwoScores = 0;
                    gameStatus = GameStatus.finished(playerName);
                    dartMoves.add(new DartMove(dartMoves.size(), playerTwoScores));
                    return true;
                }
            }
            turn = playerOne;
            if (result > 1) {
                playerTwoScores = result;
            }
            dartMoves.add(new DartMove(dartMoves.size(), playerTwoScores));
        }
        gameStatus = GameStatus.PLAYING;
        return false;
    }

    public List<DartMove> getDartMoves() {
        return dartMoves;
    }

    public List<GameHistory> retrieveHistories() {
        GameHistory.Builder historyBuilder = new GameHistory.Builder(gameId, playerOne, playerTwo, gameStatus, playerOneScores, playerTwoScores, turn, dartMoves);
        return historyBuilder.buildAll();
    }

    public void revertMove(int move) throws GameConflictException {
        if (!stillOngoing()) throw new GameConflictException("The game is over!");
        dartMoves = dartMoves.subList(0, move + 1);
        if (move == 0) {
            gameStatus = GameStatus.STARTED;
            int targetScore = dartMoves.get(0).getRemainingScore();
            playerOneScores = targetScore;
            playerTwoScores = targetScore;
            turn = playerOne;
        } else {
            gameStatus = GameStatus.PLAYING;
            int currentMoveScore = dartMoves.get(move).getRemainingScore();
            int prevMoveScore = dartMoves.get(move - 1).getRemainingScore();
            playerOneScores = move % 2 == 1 ? currentMoveScore : prevMoveScore;
            playerTwoScores = move % 2 == 0 ? currentMoveScore : prevMoveScore;
            turn = move % 2 == 0 ? playerOne : playerTwo;
        }
    }

    private static class GameStatus {
        public static final String CREATED = "created";
        public static final String STARTED = "started";
        public static final String PLAYING = "playing";
        private static final String WINS = "%s wins!";

        public static String finished(String playerName) {
            return String.format(WINS, playerName);
        }
    }
}
