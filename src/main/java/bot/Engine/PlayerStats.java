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
    private final String row;

    /** The player's stats. */
    private final List<Object> stats;

    /** Construct the object by storing row stats. */
    public PlayerStats(String position, List<Object> vals) {
        row = position;
        stats = vals;
    }

    /**
     * Retrieve the row number of the player's stats.
     * @return said row.
     */
    public String getRow() {
        return row;
    }

    /**
     * Reterieve the stats of the player.
     * @return said stats.
     */
    public List<Object> getStats() {
        return stats;
    }
}
