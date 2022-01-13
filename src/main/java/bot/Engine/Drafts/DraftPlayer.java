package bot.Engine.Drafts;

import net.dv8tion.jda.api.entities.Member;

/**
 * @author  Wil Aquino
 * Date:    January 11, 2022
 * Project: LaunchPoint Bot
 * Module:  DraftPlayer.java
 * Purpose: Represents a player playing within a draft.
 */
public class DraftPlayer {

    /** The draft player. */
    private final Member player;

    /** The player's amount of won matches during the draft. */
    private int matchWins;

    /** The player's amount of lost matches during the draft. */
    private int matchLosses;

    /** The player's amount of server pings during the draft. */
    private int pings;

    /**
     * Constructs the attributes of the draft player.
     * @param user the player to build the attributes for.
     */
    public DraftPlayer(Member user) {
        player = user;
        matchWins = matchLosses = 0;
        pings = 0;
    }

    /**
     * Retrieves the draft player.
     * @return said player.
     */
    public Member getAsMember() {
        return player;
    }

    /**
     * Retrieves the player's draft wins.
     * @return said wins.
     */
    public int getWins() {
        return matchWins;
    }

    /** Increases the player's draft wins by one. */
    public void incrementWins() {
        matchWins++;
    }

    /**
     * Retrieves the player's draft losses.
     * @return said losses.
     */
    public int getLosses() {
        return matchLosses;
    }

    /** Increases the player's draft losses by one. */
    public void incrementLosses() {
        matchLosses++;
    }

    /**
     * Retrieves the amount of times this player has pinged
     * during the draft.
     * @return said pings.
     */
    public int getPings() {
        return pings;
    }

    /** Increases the player's amount of pings by one. */
    public void incrementPings() {
        pings++;
    }
}
