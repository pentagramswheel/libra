package bot.Engine.Cycles;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    April 13, 2021
 * Project: Libra
 * Module:  PlayersStats.java
 * Purpose: Stores player stats.
 */
public class PlayerStats {

    /** Row within their associated draft spreadsheet. */
    private int numRow;

    /** The formal name of the player. */
    private String name;

    /** The nickname of the player within the server. */
    private String nickname;

    /** The amount of won sets the player has attained. */
    private int setWins;

    /** The amount of lost sets the player has attained. */
    private int setLosses;

    /** The amount of won games the player has attained. */
    private int gamesWon;

    /** The amount of lost games the player has attained. */
    private int gamesLost;

    /**
     * Construct the object by storing row data.
     * @param interaction the user interaction calling this method.
     * @param pos the row of the player within the cycle spreadsheet.
     * @param row the row data.
     */
    public PlayerStats(GenericInteractionCreateEvent interaction,
                       int pos, List<Object> row) {
        try {
            numRow = pos;

            name = row.get(0).toString();
            nickname = row.get(1).toString();
            if (row.size() > 2) {
                setWins = Integer.parseInt(row.get(2).toString());
                setLosses = Integer.parseInt(row.get(3).toString());
                gamesWon = Integer.parseInt(row.get(6).toString());
                gamesLost = Integer.parseInt(row.get(7).toString());
            }
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("Spreadsheet formatting problem detected.");
            interaction.getHook().sendMessage(
                    "***There seems to be a formatting problem within the "
                            + "spreadsheet.*** Please fix it!").queue();
        }
    }

    /**
     * Retrieve the row number of the player's stats
     * in their associated draft spreadsheet.
     */
    public int getSpreadsheetPosition() {
        return numRow;
    }

    /** Retrieve the name of the player on Discord. */
    public String getName() {
        return name;
    }

    /** Retrieve the nickname of the player on Discord. */
    public String getNickname() {
        return nickname;
    }

    /** Retrieve the player's amount of won sets. */
    public int getSetWins() {
        return setWins;
    }

    /** Retrieve the player's amount of lost sets. */
    public int getSetLosses() {
        return setLosses;
    }

    /** Retrieve the player's amount of won games. */
    public int getGamesWon() {
        return gamesWon;
    }

    /** Retrieve the player's amount of lost games. */
    public int getGamesLost() {
        return gamesLost;
    }
}
