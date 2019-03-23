package wenjalan.starbot.apex;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

public class ApexPlayer {

    // the Guild this user is in
    protected final Guild guild;

    // the User object of this player
    protected final User user;

    // whether this player is looking for a party
    protected boolean lookingForParty;

    // the party this player is in, null if none
    protected ApexParty party;

    // constructor
    public ApexPlayer(Guild g, User player) {
        this.guild = g;
        this.user = player;
        this.lookingForParty = true;
        this.party = null;
    }

    // returns the Guild this player is in
    public Guild getGuild() {
        return guild;
    }

    // returns the User object of this player
    public User getUser() {
        return user;
    }

    // returns whether this player is looking for a party
    public boolean isLookingForParty() {
        return lookingForParty;
    }

    // sets whether this player is looking for a party
    public void setIsLookingForParty(boolean b) {
        lookingForParty = b;
    }

    // returns the party this player is in, null if none
    public ApexParty getParty() {
        return party;
    }

    // sets the party this player is in
    public void setParty(ApexParty p) {
        party = p;
    }

    // overrides
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ApexPlayer) {
            ApexPlayer other = (ApexPlayer) obj;
            if (other.getUser().getIdLong() == this.getUser().getIdLong()) {
                return true;
            }
        }
        return false;
    }
}
