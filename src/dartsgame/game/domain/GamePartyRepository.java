package dartsgame.game.domain;

import dartsgame.game.domain.GameParty;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GamePartyRepository extends CrudRepository<GameParty, Long> {
    @Query("SELECT g from GameParty g ORDER BY g.gameId DESC")
    public List<GameParty> findAllAvailableGames();

}
