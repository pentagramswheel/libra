package bot.Engine;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author
 * Date:
 * Project:
 * Module:
 * Purpose:
 */
public class PlayerInfo {

    /**
     * Construct the object by storing row data.
     * @param interaction the user interaction calling this method.
     * @param pos the row of the player within the profiles spreadsheet.
     * @param vals the row data.
     */
    public PlayerInfo(GenericInteractionCreateEvent interaction,
                       int pos, List<Object> vals) {
        try {
            int i = 0;
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("Spreadsheet formatting problem detected.");
            interaction.getHook().sendMessage(
                    "***There seems to be a formatting problem within the "
                            + "spreadsheet.*** Please fix it!").queue();
        }
    }
}
