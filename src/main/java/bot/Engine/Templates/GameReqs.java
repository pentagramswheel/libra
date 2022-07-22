package bot.Engine.Templates;

import bot.Engine.Games.GameProperties;
import bot.Engine.Games.Player;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.TreeMap;

/**
 * @author  Wil Aquino
 * Date:    July 21, 2022
 * Project: Libra
 * Module:  GameReqs.java
 * Purpose: Template for draft requests.
 */
public interface GameReqs {

    /**
     * Checks whether the request has been satisfied or not.
     * @return True if eight players have joined the queue.
     *         False otherwise.
     */
    boolean isInitialized();

    /** Retrieves the game's properties. */
    GameProperties getProperties();

    /** Retrieves the players of the game. */
    TreeMap<String, ? extends Player> getPlayers();

    /**
     * Retrieves the request interface of the game.
     * @param interaction the user interaction calling this method.
     */
    Message getMessage(GenericInteractionCreateEvent interaction);

    /** Retrieves the respective draft chat channel. */
    TextChannel getDraftChannel();

    /**
     * Checks whether the request has timed out or not.
     * @param interaction the user interaction calling this method.
     * @return True if the request expired.
     *         False otherwise.
     */
    boolean timedOut(GenericInteractionCreateEvent interaction);

    /** Retrieves the field for executing the draft. */
    ProcessReqs getProcess();

    /**
     * Sends a draft confirmation summary with all players of the draft.
     * @param interaction the user interaction calling this method.
     */
    void updateReport(GenericInteractionCreateEvent interaction);

    /**
     * Attempts to start a draft.
     * @param bc a button click to analyze.
     */
    void attemptDraft(ButtonClickEvent bc);

    /**
     * Refreshes the draft request's caption.
     * @param bc a button click to analyze.
     */
    void refresh(ButtonClickEvent bc);

    /**
     * Repings for remaining players.
     * @param bc a button click to analyze.
     */
    void reping(ButtonClickEvent bc);

    /**
     * Removes a player from the draft.
     * @param bc a button click to analyze.
     */
    void removeFromQueue(ButtonClickEvent bc);

    /**
     * Requests a sub for a draft.
     * @param bc a button click to analyze.
     */
    void requestSub(ButtonClickEvent bc);

    /**
     * Forcibly subs a person out of the draft.
     * @param sc a slash command to analyze.
     * @param playerID the Discord ID of the player to sub.
     */
    void forceSub(SlashCommandEvent sc, String playerID);

    /**
     * Adds a player to the draft's subs.
     * @param bc a button click to analyze.
     */
    void addSub(ButtonClickEvent bc);

    /**
     * Checks if the draft can be forcibly ended.
     * @param sc a slash command to analyze.
     * @return True if the draft was forcibly ended.
     *         False otherwise.
     */
    boolean canForceEnd(SlashCommandEvent sc);

}
