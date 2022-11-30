package dartsgame.game.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Player {
    @Id
    private String name;
    @ManyToOne
    @JoinColumn(name = "gameId")
    private GameParty lastPlayedGame;
    private boolean playing;

    public Player() {
    }

    public Player(String name) {
        this.name = name;
        this.playing = true;
    }

    public Player(String name, GameParty gameParty) {
        this.name = name;
        this.lastPlayedGame = gameParty;
        this.playing = true;
    }

    public GameParty getLastPlayedGame() {
        return lastPlayedGame;
    }

    public void setLastPlayedGame(GameParty lastPlayedGame) {
        this.lastPlayedGame = lastPlayedGame;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }
}
