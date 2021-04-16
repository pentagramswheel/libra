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

    /** The formal name of the player. */
    private final String name;

    /** The nickname of the player within the server. */
    private final String nickname;

    /** The amount of won sets the player has attained. */
    private int setWins;

    /** The amount of lost sets the player has attained. */
    private int setLosses;

    /** The amount of won games the player has attained. */
    private int gamesWon;

    /** The amount of lost games the player has attained. */
    private int gamesLost;

    /** Construct the object by storing row stats. */
    public PlayerStats(String pos, List<Object> vals) {
        position = pos;
        name = vals.get(0).toString();
        nickname = vals.get(1).toString();
        if (vals.size() > 2) {
            setWins = Integer.parseInt(vals.get(2).toString());
            setLosses = Integer.parseInt(vals.get(3).toString());
            gamesWon = Integer.parseInt(vals.get(6).toString());
            gamesLost = Integer.parseInt(vals.get(7).toString());
        }
    }

    /**
     * Retrieve the row number of the player's stats.
     * @return said row.
     */
    public String getPosition() {
        return position;
    }

    /**
     * Retrieve the name of the player on Discord.
     * @return said name.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieve the nickname of the player on Discord.
     * @return said nickname.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Retrieve the player's amount of won sets.
     * @return said wins.
     */
    public int getSetWins() {
        return setWins;
    }

    /**
     * Retrieve the player's amount of lost sets.
     * @return said losses.
     */
    public int getSetLosses() {
        return setLosses;
    }

    /**
     * Retrieve the player's amount of won games.
     * @return said wins.
     */
    public int getGamesWon() {
        return gamesWon;
    }

    /**
     * Retrieve the player's amount of lost games.
     * @return said losses.
     */
    public int getGamesLost() {
        return gamesLost;
    }
}
