package dartsgame.game;

import dartsgame.dto.*;
import dartsgame.game.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api")
public class GameController {
    @Autowired
    private GamePartyRepository gamePartyRepository;
    @Autowired
    private PlayerRepository playerRepository;

    @PostMapping("game/create")
    public ResponseEntity<Object> createRoom(@Valid @RequestBody NewGameRequest request, Principal principal) throws GameConflictException {
        String playerName = principal.getName();
        Optional<Player> playerContainer = playerRepository.findById(playerName);
        Player player;
        GameParty game;

        if (playerContainer.isPresent()) {
            player = playerContainer.get();
            if (player.isPlaying()) throw new GameConflictException("You have an unfinished game!");
            game = gamePartyRepository.save(new GameParty(playerName, request.getTargetScore()));
            player.setPlaying(true);
            playerRepository.save(player);
        } else {
            game = gamePartyRepository.save(new GameParty(playerName, request.getTargetScore()));
            playerRepository.save(new Player(playerName, game));
        }

        return new ResponseEntity<>(game, HttpStatus.OK);
    }

    @GetMapping("game/list")
    public ResponseEntity<Object> getGameList(Principal principal) {
        List<GameParty> gameList = gamePartyRepository.findAllAvailableGames();
        if (gameList.size() == 0) {
            return new ResponseEntity<>(gameList, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(gameList, HttpStatus.OK);
    }

    @GetMapping("game/join/{gameId}")
    public ResponseEntity<Object> requestJoin(@PathVariable Long gameId, Principal principal) throws GameConflictException, GameResourceNotFoundException {
        GameParty game = findGameParty(gameId);
        String playerName = principal.getName();
        verifySelfJoining(game, playerName);
        if (!game.availableToJoin()) throw new GameConflictException("You can't join the game!");

        Optional<Player> playerContainer = playerRepository.findById(playerName);
        Player player;
        if (playerContainer.isPresent()) {
            player = playerContainer.get();
            if (player.isPlaying()) throw new GameConflictException("You have an unfinished game!");
        }

        startGame(game, playerName);

        return new ResponseEntity<>(game, HttpStatus.OK);
    }

    @GetMapping("game/status")
    public ResponseEntity<Object> getGameStatus(Principal principal) {
        String playerName = principal.getName();
        Optional<Player> playerContainer = playerRepository.findById(playerName);
        Player player = null;
        if (playerContainer.isPresent()) {
            player = playerContainer.get();
        }

        if (player == null || player.getLastPlayedGame() == null) {
            return new ResponseEntity<>(Map.of(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(player.getLastPlayedGame(), HttpStatus.OK);
    }

    @PostMapping("game/throws")
    public ResponseEntity<Object> getPlayerThrows(@Valid @RequestBody DartTurn turnThrows, Principal principal) throws GameResourceNotFoundException, GameConflictException {
        String playerName = principal.getName();
        Player player = validatePlayerThrowPermission(playerName);
        GameParty game = player.getLastPlayedGame();

        if (game.processThrow(playerName, turnThrows)) {
            gameFinished(game);
        }
        gamePartyRepository.save(game);

        return new ResponseEntity<>(game, HttpStatus.OK);
    }

    @GetMapping("history/{gameId}")
    public ResponseEntity<List<GameHistory>> getGameHistories(@Min(value = 1, message = "Wrong Move!") @PathVariable String gameId) throws GameResourceNotFoundException, GameConflictException {
        GameParty game = findGameParty(gameId);
        if (game.availableToJoin()) return new ResponseEntity<>(List.of(), HttpStatus.OK);

        return new ResponseEntity<>(game.retrieveHistories(), HttpStatus.OK);
    }

    @PutMapping("game/cancel")
    public ResponseEntity<Object> cancelGame(@Valid @RequestBody CancelRequest request) throws GameResourceNotFoundException, GameConflictException {
        GameParty game = findGameParty(request.getGameId());
        if (!game.stillOngoing()) throw new GameConflictException("The game is already over!");
        request.validateStatus(game.getPlayerOne(), game.getPlayerTwo());

        game.setGameStatus(request.getStatus());
        gameFinished(game);
        gamePartyRepository.save(game);

        return new ResponseEntity<>(game, HttpStatus.OK);
    }

    @PutMapping("game/revert")
    public ResponseEntity<GameParty> revertGameStatus(@RequestBody RevertRequest request) throws GameResourceNotFoundException, GameConflictException {
        GameParty game = findGameParty(request.getGameId());
        List<DartMove> moves = game.getDartMoves();

        request.validateMoves(moves.size());
        game.revertMove(request.getMove());
        gamePartyRepository.save(game);
        return new ResponseEntity<>(game, HttpStatus.OK);
    }

    private void startGame(GameParty game, String playerTwo) {
        game.start(playerTwo);
        Player playerOne = playerRepository.findById(game.getPlayerOne()).get();
        playerOne.setLastPlayedGame(game);

        gamePartyRepository.save(game);
        playerRepository.save(playerOne);
        playerRepository.save(new Player(playerTwo, game));
    }

    private GameParty findGameParty(long id) throws GameResourceNotFoundException, GameConflictException {
        if (id <= 0) throw new GameConflictException("Wrong request!");
        Optional<GameParty> game = gamePartyRepository.findById(id);
        if (game.isEmpty()) throw new GameResourceNotFoundException("Game not found!");
        return game.get();
    }

    private GameParty findGameParty(String id) throws GameConflictException, GameResourceNotFoundException {
        long gameId;
        try {
            gameId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new GameConflictException("Wrong request!");
        }
        return findGameParty(gameId);
    }

    private void verifySelfJoining(GameParty game, String playerName) throws GameConflictException {
        if (game.getPlayerOne().equals(playerName)) {
            throw new GameConflictException("You can't play alone!");
        }
    }

    private GameParty findParticipateGame() {
        for (GameParty game : gamePartyRepository.findAll()) {
            if (game.stillOngoing()) return game;
        }
        return null;
    }

    private void gameFinished(GameParty game) {
        playerRepository.findById(game.getPlayerOne()).get().setPlaying(false);
        if (!"".equals(game.getPlayerTwo())) {
            playerRepository.findById(game.getPlayerTwo()).get().setPlaying(false);
        }
    }

    private Player validatePlayerThrowPermission(String playerName) throws GameResourceNotFoundException {
        Optional<Player> playerContainer = playerRepository.findById(playerName);
        if (playerContainer.isEmpty()) {
            throw new GameResourceNotFoundException("There are no games available!");
        }
        Player player = playerContainer.get();
        if (!playerContainer.get().isPlaying() || !player.getLastPlayedGame().stillOngoing()) {
            throw new GameResourceNotFoundException("There are no games available!");
        }
        return player;
    }
}
