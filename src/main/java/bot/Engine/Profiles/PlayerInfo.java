package bot.Engine.Profiles;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    May 7, 2022
 * Project: Libra
 * Module:  PlayerInfo.java
 * Purpose: Manages the storage of the profiles.
 */
public class PlayerInfo {

    /** Row within the profiles spreadsheet. */
    private int numRow;

    /** The discord tag of the player. */
    private String tag;

    /** The nickname of the player. */
    private String nickname;

    /** The friend code of the player. */
    private String friendcode;

    /** The preferred pronouns of the player. */
    private String pronouns;

    /** The preferred playstyle of the player. */
    private String playstyle;

    /** The preferred main weapons of the player. */
    private String weapons;

    /** The average rank of the player. */
    private String rank;

    /** The competitive team the player is on. */
    private String team;

    /**
     * Construct the object by storing row data.
     * @param interaction the user interaction calling this method.
     * @param pos the row of the player within the profiles spreadsheet.
     * @param row the row data.
     */
    public PlayerInfo(GenericInteractionCreateEvent interaction,
                       int pos, List<Object> row) {
        try {
            numRow = pos;

            tag = row.get(0).toString();
            nickname = row.get(1).toString();
            friendcode = row.get(2).toString();
            pronouns = row.get(3).toString();
            playstyle = row.get(4).toString();
            weapons = row.get(5).toString();
            rank = row.get(6).toString();
            team = row.get(7).toString();
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("Spreadsheet formatting problem detected.");
            interaction.getHook().sendMessage(
                    "***There seems to be a formatting problem within the "
                            + "spreadsheet.*** Please fix it!").queue();
        }
    }

    /**
     * Retrieve the row number of the player's profile
     * within the profiles spreadsheet.
     */
    public int getSpreadsheetPosition() {
        return numRow;
    }

    /**
     * Retrieves the Discord tag of the player.
     */
    public String getAsTag() {
        return tag;
    }

    /**
     * Retrieves the nickname of the player.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Retrieves the friend code of the player.
     */
    public String getFC() {
        return friendcode;
    }

    /** Retrieves the preferred pronouns of the player. */
    public String getPronouns() {
        return pronouns;
    }

    /**
     * Retrieves the playstyle of the player.
     */
    public String getPlaystyle() {
        return playstyle;
    }

    /**
     * Retrieves the weapon pool of the player.
     */
    public String getWeaponPool() {
        return weapons;
    }

    /**
     * Retrieves the rank of the player.
     */
    public String getRank() {
        return rank;
    }

    /** Retrieves the team the player is on. */
    public String getTeam() {
        return team;
    }
}
