package bot.Engine.Games;

import java.util.TreeMap;

/**
 * @author  Wil Aquino
 * Date:    April 1, 2022
 * Project: Libra
 * Module:  Team.java
 * Purpose: Represents a team within a game.
 */
public class Team<P extends Player> {

    /** The player ceiling of this team. */
    private final int maxPlayers;

    /** The players of the team. */
    private final TreeMap<String, P> players;

    /** The amount of players a team needs. */
    private int playersNeeded;

    /**
     * Starts a team within a game.
     * @param playerCeiling the maximum number of players to set for the team.
     */
    public Team(int playerCeiling) {
        maxPlayers = playerCeiling;
        players = new TreeMap<>();
        clear();
    }

    /**
     * Adds a player to the team.
     * @param id the Discord ID of the player.
     * @param player the player to add.
     */
    public void add(String id, P player) {
        getPlayers().put(id, player);
        player.setTeamStatus(true);
        playersNeeded--;
    }

    /** Retrieves the players of the team. */
    public TreeMap<String, P> getPlayers() {
        return players;
    }

    /**
     * Checks whether a player is within the team already.
     * @param id the Discord ID of the player to check.
     * @return True if they are in the team.
     *         False otherwise.
     */
    public boolean contains(String id) {
        return getPlayers().containsKey(id);
    }

    /** Increments the amount of players this team needs. */
    public void requestSub() {
        playersNeeded++;
    }

    /**
     * Checks whether the team currently has no active players.
     * @return True if the team is empty or is full of
     *              inactive players.
     *         False otherwise.
     */
    public boolean isEmpty() {
    return playersNeeded == maxPlayers;
    }

    /**
     * Checks whether the team needs a sub or not.
     * @return True if a sub is needed.
     *         False otherwise.
     */
    public boolean needsPlayers() {
        return playersNeeded > 0;
    }

    /** Clears the team of its players. */
    public void clear() {
        for (P player : getPlayers().values()) {
            player.setTeamStatus(false);
        }
        getPlayers().clear();
        playersNeeded = maxPlayers;
    }
}
