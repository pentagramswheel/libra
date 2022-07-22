package bot.Engine.Templates;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;

/**
 * @author  Wil Aquino
 * Date:    July 20, 2022
 * Project: Libra
 * Module:  ProcessReqs.java
 * Purpose: Template for draft processes.
 */
public interface ProcessReqs {

    /** Retrieves the caption ping of the draft process. */
    String getPing();

    /**
     * Sends the details of the overall team selection with
     * the players of the draft.
     * @param interaction the user interaction calling this method.
     */
    void updateReport(GenericInteractionCreateEvent interaction);

    /**
     * Refreshes the process's interface.
     * @param interaction the user interaction calling this method.
     */
    void refresh(GenericInteractionCreateEvent interaction);

    /**
     * Determines if the draft meets the requirements to be ended.
     * @param bc the button click to analyze.
     * @return True if the draft has ended.
     *         False otherwise.
     */
    boolean hasEnded(ButtonClickEvent bc);

}
