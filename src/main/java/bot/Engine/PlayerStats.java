package bot.Engine;

import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    April 13, 2021
 * Project: LaunchPoint Bot
 * Module:  Events.java
 * Purpose: Stores player stats.
 */
public class PlayerStats {

    /** Row position within the player's affiliated spreadsheet. */
    private final String position;

    /** The player's stats. */
    private final List<Object> stats;

    /** Construct the object by storing row stats. */
    public PlayerStats(String pos, List<Object> vals) {
        position = pos;
        stats = vals;
    }

    /**
     * Retrieve the row number of the player's stats.
     * @return said row.
     */
    public String getPosition() {
        return position;
    }

    /**
     * Reterieve the stats of the player.
     * @return said stats.
     */
    public List<Object> getStats() {
        return stats;
    }
}
