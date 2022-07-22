package bot.Engine.Drafts;

import bot.Engine.Games.Team;

/**
 * @author  Wil Aquino
 * Date:    April 1, 2022
 * Project: Libra
 * Module:  DraftTeam.java
 * Purpose: Represents a team within a draft.
 */
public class DraftTeam extends Team<DraftPlayer> {

    /** The opposing team. */
    private DraftTeam opponents;

    /** The minimum/maximum amount of won matches for a team. */
    private final int minimumScore;
    private final int maximumScore;

    /** The team's total score. */
    private int score;

    /**
     * Starts a team within a draft.
     * @param playerCeiling the maximum number of players to set for the team.
     * @param pointCeiling the maximum number of points to set for the team.
     */
    public DraftTeam(int playerCeiling, int pointCeiling) {
        super(playerCeiling);

        minimumScore = score = 0;
        maximumScore = pointCeiling;
    }

    /**
     * Sets the opponents of this team.
     * @param team the opposing team.
     */
    public void setOpponents(DraftTeam team) {
        opponents = team;
    }

    /** Retrieves the opposing team. */
    public DraftTeam getOpponents() {
        return opponents;
    }

    /**
     * Adds a win to the team's score.
     */
    public void incrementScore() {
        score++;

        for (DraftPlayer player : getPlayers().values()) {
            player.incrementWins();
        }
        for (DraftPlayer player : getOpponents().getPlayers().values()) {
            player.incrementLosses();
        }
    }

    /**
     * Subtracts a win from the team's score.
     */
    public void decrementScore() {
        score--;

        for (DraftPlayer player : getPlayers().values()) {
            player.decrementWins();
        }
        for (DraftPlayer player : getOpponents().getPlayers().values()) {
            player.decrementLosses();
        }
    }

    /** Retrieves the team's total score. */
    public int getScore() {
        return score;
    }

    /**
     * Checks whether the team has the minimum score or not.
     * @return True if they do.
     *         False otherwise.
     */
    public boolean hasMinimumScore() {
        return getScore() == minimumScore;
    }

    /**
     * Checks whether the team has the maximum score or not.
     * @return True if they do.
     *         False otherwise.
     */
    public boolean hasMaximumScore() {
        return getScore() == maximumScore;
    }
}
