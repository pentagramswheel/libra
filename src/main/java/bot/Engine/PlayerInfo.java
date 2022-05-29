package bot.Engine;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Turtle#1504
 * Date: May 7, 2022
 * Project: Libra
 * Module: PlayerInfo.java
 * Purpose: Manages the storage of the profiles.
 */
public class PlayerInfo {
    /** Row position within their associated draft spreadsheet. */
    private int profilePosition;

    /** The id of the player. */
    private String id;

    /** The discord username and tag of the member. */
    private String tag;

    /** The preferred nickname of the member. */
    private String nickname;

    /** The preferred pronoun(s) of the member. */
    private String pronoun;

    /** The preferred playstyle of the member. */
    private String playstyle;

    /** The preferred weapon of the member. */
    private String weapon;

    /**
     * Construct the object by storing row data.
     * @param interaction the user interaction calling this method.
     * @param pos the row of the player within the profiles spreadsheet.
     * @param vals the row data.
     */
    public PlayerInfo(GenericInteractionCreateEvent interaction,
                       int pos, List<Object> vals) {
        try {
            profilePosition = pos;
            id = vals.get(0).toString();
            tag = vals.get(1).toString();
            nickname = vals.get(2).toString();
            pronoun = vals.get(3).toString();
            playstyle = vals.get(4).toString();
            weapon = vals.get(5).toString();


        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("Spreadsheet formatting problem detected.");
            interaction.getHook().sendMessage(
                    "***There seems to be a formatting problem within the "
                            + "spreadsheet.*** Please fix it!").queue();
        }
    }

    /**
     * Retrieve the row number of the member's stats
     * in their associated draft spreadsheet.
     * @return said row.
     */
    public int getDraftPosition() {
        return profilePosition;
    }

    /**
     * Retrieve the id of the member.
     * @return said id.
     */
    public String getId() { return id;}

    /**
     * Retrieve the username and tag of the member.
     * @return said username and tag.
     */
    public String getTag() { return tag;}

    /**
     * Retrieve the preferred nickname of the member.
     * @return said nickname.
     */
    public String getNickname() { return nickname;}

    /**
     * Retrieve the preferred pronoun(s) of the member.
     * @return said pronoun(s).
     */
    public String getPronoun() { return pronoun;}

    /**
     * Retrieve the preferred playstyle of the member.
     * @return said playstyle.
     */
    public String getPlaystyle() { return playstyle;}

    /**
     * Retrieve the preferred weapon of the member.
     * @return said weapon.
     */
    public String getWeapon() { return weapon;}
}



