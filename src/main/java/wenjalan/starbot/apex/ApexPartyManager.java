package wenjalan.starbot.apex;

import net.dv8tion.jda.core.entities.Guild;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

// matches up players to parties, on a guild-per-guild basis
public class ApexPartyManager {

    // the map of active Guilds to ApexPartyManagers
    protected static HashMap<Guild, Manager> managers = new HashMap<Guild, Manager>();

    // adds a new party to the guild
    public static void addPartyToGuild(Guild g, ApexParty p) {
        Manager manager = getManager(g);
        manager.getParties().add(p);
    }

    // removes a party from the guild
    public static void removePartyFromGuild(Guild g, ApexParty p) {
        Manager manager = getManager(g);
        manager.getParties().remove(p);
    }

    // adds a player to the guild
    public static void addPlayerToGuild(Guild g, ApexPlayer p) {
        Manager manager = getManager(g);
        manager.getPlayers().add(p);
    }

    // removes a player from the guild
    public static void removePlayerFromGuild(Guild g, ApexPlayer p) {
        Manager manager = getManager(g);
        manager.getPlayers().remove(p);
    }

    // an instance of a Manager class
    public class Manager {

        // the Guild this Manager is managing
        protected Guild guild;

        // the list of parties in this guild
        protected HashSet<ApexParty> parties;

        // the list of players in this guild
        protected HashSet<ApexPlayer> players;

        // constructor
        protected Manager(Guild g) {
            this.guild = g;
            this.parties = new HashSet<>();
            this.players = new HashSet<>();
        }

        // returns the Guild this Manager is managing
        public Guild getGuild() {
            return guild;
        }

        // returns the parties
        public HashSet<ApexParty> getParties() {
            return parties;
        }

        // returns the players
        public HashSet<ApexPlayer> getPlayers() {
            return players;
        }
    }

    // helpers //
    // returns the manager of a guild
    protected static Manager getManager(Guild g) {
        return managers.get(g);
    }

    // returns a list of parties that have open slots
    public static List<ApexParty> getAvailableParties(Guild g) {
        List<ApexParty> parties = new LinkedList<>();
        for (ApexParty p : getManager(g).getParties()) {
            if (!p.isFull()) parties.add(p);
        }
        return parties;
    }

}
