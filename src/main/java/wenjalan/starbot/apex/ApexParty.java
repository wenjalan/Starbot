package wenjalan.starbot.apex;

import net.dv8tion.jda.core.entities.Guild;

import java.util.LinkedList;
import java.util.List;

public class ApexParty {

    // the maximum number of players in a party
    public static final int MAX_PLAYERS = 3;

    // the list of players in this party
    protected List<ApexPlayer> players;

    // the owner of this party
    protected ApexPlayer owner;

    // the Guild this party is in
    protected Guild guild;

    // constructor
    public ApexParty(Guild guild, ApexPlayer owner) {
        this.guild = guild;
        this.owner = owner;
        this.players = new LinkedList<>();

        // add the owner to the party
        addPlayer(owner);
    }

    // adds a player to this party
    public void addPlayer(ApexPlayer p) {
        // check whether this party is full
        if (isFull()) {
            throw new IllegalStateException("party is full");
        }

        // set the player's parameters
        p.setParty(this);
        p.setIsLookingForParty(false);

        // add this player to the list
        players.add(p);
    }

    // removes a player from the party
    public void removePlayer(ApexPlayer p) {
        // if this player is the owner
        if (p.equals(owner)) {
            // if there's other players in the party, make one of them the owner
            if (getPlayerCount() > 1) {
                // find another player
                for (int i = 0; p.equals(owner) && i <= 2; i++) {
                    owner = players.get(i);
                }
            }
            // if there are no other players in the party, shut down the party and return
            else {
                // set parameters of all players
                for (ApexPlayer player : players) {
                    player.setParty(null);
                    player.setIsLookingForParty(true);
                }

                // destroy the party
                ApexPartyManager.removePartyFromGuild(this.guild, this);
                return;
            }
        }

        // set player parameters
        p.setParty(null);
        p.setIsLookingForParty(true);

        // remove the player from the list
        players.remove(p);
    }

    // returns the number of players in this party
    public int getPlayerCount() {
        return players.size();
    }

    // returns if this party is full
    public boolean isFull() {
        return players.size() >= MAX_PLAYERS;
    }

}
