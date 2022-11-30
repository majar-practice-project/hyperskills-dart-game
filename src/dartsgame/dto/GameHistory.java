package dartsgame.dto;

import dartsgame.game.domain.DartMove;
import dartsgame.game.domain.GameParty;

import java.util.ArrayList;
import java.util.List;

public class GameHistory {
    private Long gameId;
    private int move;
    private String playerOne;
    private String playerTwo;
    private String gameStatus;
    private int playerOneScores;
    private int playerTwoScores;
    private String turn;

    public GameHistory() {
    }

    public GameHistory(Long gameId, int move, String playerOne, String playerTwo, String gameStatus, int playerOneScores, int playerTwoScores, String turn) {
        this.gameId = gameId;
        this.move = move;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.gameStatus = gameStatus;
        this.playerOneScores = playerOneScores;
        this.playerTwoScores = playerTwoScores;
        this.turn = turn;
    }

    public GameHistory(GameParty game, int move) {
        List<DartMove> moves = game.getDartMoves();
        DartMove dartMove = moves.get(move);
        this.gameId = game.getGameId();
        this.move = move;
        this.playerOne = game.getPlayerOne();
        this.playerTwo = game.getPlayerTwo();
        this.gameStatus = game.getGameStatus();
        if (move == moves.size() - 1) {
            this.playerOneScores = game.getPlayerOneScores();
            this.playerTwoScores = game.getPlayerTwoScores();
            this.turn = game.getTurn();
        } else {
            this.playerOneScores = move % 2 == 1 ? dartMove.getRemainingScore() : moves.get(Math.max(0, move - 1)).getRemainingScore();
            this.playerTwoScores = move % 2 == 0 ? dartMove.getRemainingScore() : moves.get(move - 1).getRemainingScore();
            this.turn = move % 2 == 1 ? playerOne : playerTwo;
            if (move == 0) this.turn = playerOne;
        }
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public int getMove() {
        return move;
    }

    public void setMove(int move) {
        this.move = move;
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

    public static class Builder {
        private Long gameId;
        private int move;
        private String playerOne;
        private String playerTwo;
        private String gameStatus;
        private int playerOneScores;
        private int playerTwoScores;
        private String turn;

        private List<DartMove> moves;

        public Builder(Long gameId, String playerOne, String playerTwo, String gameStatus, int playerOneScores, int playerTwoScores, String turn, List<DartMove> moves) {
            this.gameId = gameId;
            this.playerOne = playerOne;
            this.playerTwo = playerTwo;
            this.gameStatus = gameStatus;
            this.playerOneScores = playerOneScores;
            this.playerTwoScores = playerTwoScores;
            this.turn = turn;
            this.moves = moves;
        }

        public List<GameHistory> buildAll() {
            List<GameHistory> histories = new ArrayList<>();
            if(moves.isEmpty()) return histories;
            int prevScore = moves.get(0).getRemainingScore();
            histories.add(new GameHistory(gameId, 0, playerOne, playerTwo, "started", prevScore, prevScore, playerOne));
            int i;
            for (i = 1; i < moves.size() - 1; i++) {
                int playerOneTempScore = i % 2 == 1 ? moves.get(i).getRemainingScore() : prevScore;
                int playerTwoTempScore = i % 2 == 0 ? moves.get(i).getRemainingScore() : prevScore;
                String nextPlayer = i % 2 == 0 ? playerOne : playerTwo;
                histories.add(new GameHistory(gameId, i, playerOne, playerTwo, "playing", playerOneTempScore, playerTwoTempScore, nextPlayer));
                prevScore = moves.get(i).getRemainingScore();
            }
            if (moves.size() > 1) {
                String finalGameStatus = moves.get(i).getRemainingScore() == 0 ? i % 2 == 0 ? playerTwo : playerOne + " wins!" : gameStatus;
                histories.add(new GameHistory(gameId, i, playerOne, playerTwo, finalGameStatus, playerOneScores, playerTwoScores, turn));
            }
            return histories;
        }
    }
}
