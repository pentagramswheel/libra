package bot.Engine;

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

    /** Row position within their associated draft spreadsheet. */
    private String draftPosition;

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
     * @param vals the row data.
     */
    public PlayerStats(GenericInteractionCreateEvent interaction,
                       String pos, List<Object> vals) {
        try {
            draftPosition = pos;
            name = vals.get(0).toString();
            nickname = vals.get(1).toString();
            if (vals.size() > 2) {
                setWins = Integer.parseInt(vals.get(2).toString());
                setLosses = Integer.parseInt(vals.get(3).toString());
                gamesWon = Integer.parseInt(vals.get(6).toString());
                gamesLost = Integer.parseInt(vals.get(7).toString());
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
     * @return said row.
     */
    public String getDraftPosition() {
        return draftPosition;
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
