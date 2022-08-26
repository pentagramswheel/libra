package bot.Engine.Games.Drafts;

import bot.Engine.Games.Player;

/**
 * @author  Wil Aquino
 * Date:    January 11, 2022
 * Project: Libra
 * Module:  DraftPlayer.java
 * Purpose: Represents a player playing within a draft.
 */
public class DraftPlayer extends Player {

    /** Flags for checking whether this player is a captain or not. */
    private boolean captainStatus1, captainStatus2;

    /** Point thresholds for this player. */
    public int minimumPoints;
    public int maximumPoints;

    /** The player's amount of won/lost matches during the draft. */
    private int matchWins;
    private int matchLosses;

    /**
     * Constructs the attributes of the draft player.
     * @param playerName the name of the player.
     * @param ceiling the maximum number of points to set for the player.
     * @param isSub a flag for knowing if the player is a sub or not.
     */
    public DraftPlayer(String playerName, int ceiling, boolean isSub) {
        super(playerName, isSub);

        captainStatus1 = captainStatus2 = false;

        minimumPoints = 0;
        maximumPoints = ceiling;

        matchWins = matchLosses = 0;
    }

    /**
     * Sets this player's captain status for the first team.
     * @param status the status to set it to.
     */
    public void setCaptainForTeam1(boolean status) {
        captainStatus1 = status;
    }

    /**
     * Checks if the player is the first team's captain within the draft.
     * @return True if they are a captain.
     *         False otherwise.
     */
    public boolean isCaptainForTeam1() {
        return captainStatus1;
    }

    /**
     * Sets this player's captain status for the second team.
     * @param status the status to set it to.
     */
    public void setCaptainForTeam2(boolean status) {
        captainStatus2 = status;
    }

    /**
     * Checks if the player is the second team's captain within the draft.
     * @return True if they are a captain.
     *         False otherwise.
     */
    public boolean isCaptainForTeam2() {
        return captainStatus2;
    }

    /**
     * Checks whether a player's points can be increased or not.
     * @param points the points to check.
     * @return True if they can be increased.
     *         False otherwise.
     */
    private boolean incrementable(int points) {
        return isActive() && points < maximumPoints;
    }

    /**
     * Checks whether a player's points can be decreased or not.
     * @param points the points to check.
     * @return True if they can be decreased.
     *         False otherwise.
     */
    private boolean decrementable(int points) {
        return isActive() && points > minimumPoints;
    }

    /** Increases the player's amount of wins by one. */
    public void incrementWins() {
        if (incrementable(getWins())) {
            matchWins++;
        }
    }

    /** Decreases the player's amount of wins by one. */
    public void decrementWins() {
        if (decrementable(getWins())) {
            matchWins--;
        }
    }

    /** Increases the player's amount of losses by one. */
    public void incrementLosses() {
        if (incrementable(getLosses())) {
            matchLosses++;
        }
    }

    /** Decreases the player's amount of losses by one. */
    public void decrementLosses() {
        if (decrementable(getLosses())) {
            matchLosses--;
        }
    }

    /** Retrieves the player's draft wins. */
    public int getWins() {
        return matchWins;
    }

    /** Retrieves the player's draft losses. */
    public int getLosses() {
        return matchLosses;
    }

    /** Overridden hash code for draft players. */
    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 89 * hash + (minimumPoints ^ (minimumPoints >>> 16));
        hash = 89 * hash + (maximumPoints ^ (maximumPoints >>> 16));
        hash = 89 * hash + (isCaptainForTeam1() ? 0 : 1);
        hash = 89 * hash + (isCaptainForTeam2() ? 0 : 1);
        hash = 89 * hash + (getWins() ^ (getWins() >>> 16));
        hash = 89 * hash + (getLosses() ^ (getLosses() >>> 16));

        return hash;
    }

    /** Overridden equals checking for players. */
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }

        DraftPlayer p = (DraftPlayer) o;
        return minimumPoints == p.minimumPoints
                && maximumPoints == p.maximumPoints
                && isCaptainForTeam1() == p.isCaptainForTeam1()
                && isCaptainForTeam2() == p.isCaptainForTeam2()
                && getWins() == p.getWins() && getLosses() == p.getLosses();
    }
}
