package bot.Engine.Games;

import bot.Engine.Profiles.Profile;
import bot.Engine.Section;
import bot.Engine.Templates.Command;
import bot.Tools.Components;
import bot.Tools.DiscordWatch;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author  Wil Aquino
 * Date:    July 20, 2022
 * Project: Libra
 * Module:  Game.java
 * Purpose: Formalizes and starts a game, via a request interface.
 *
 * Note:    "Draft" and "game" are used interchangeably here.
 */
public class Game<G extends Game<?, S, T, P>, S extends Process<G, T, P>,
        T extends Team<P>, P extends Player>
        extends Section implements Command {

    /** Flag for checking whether this game has initialized or not. */
    private boolean initialized;

    /** Field for storing the properties of the game. */
    private final GameProperties properties;

    /** A watch for the game to use. */
    DiscordWatch watch;

    /** The formal number of this draft. */
    private final int numDraft;

    /** The players of this draft. */
    private final TreeMap<String, P> players;
    private final HashSet<String> playerHistory;

    /** The amount of inactive players within the draft. */
    private int numInactive;

    /** The draft chat channel this draft is occurring in. */
    private final TextChannel draftChat;

    /** The Discord message ID for this draft's initial interface. */
    private String messageID;

    /** The formal process for executing this draft. */
    private S process;

    /**
     * Constructs a draft/game and initializes the start attributes.
     * @param sc the user's inputted command.
     * @param type the type of draft/game this game is.
     * @param draft the numbered draft/game that this game is.
     * @param abbreviation the abbreviation of the section.
     */
    public Game(SlashCommandEvent sc, GameType type,
                int draft, String abbreviation)  {
        super(abbreviation);
        initialized = false;

        properties = new GameProperties(type);
        watch = new DiscordWatch();

        // set a timer for the draft's request
        watch.startTimerOne(50);
        // set a timer for when for allowing a reping
        watch.startTimerTwo(15);

        numDraft = draft;

        players = new TreeMap<>();
        playerHistory = new HashSet<>();
        numInactive = 0;

        draftChat = getChannel(sc, getPrefix() + "-draft-chat-" + draft);
    }

    /**
     * Checks whether the request has been satisfied or not.
     * @return True if eight players have joined the queue.
     *         False otherwise.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /** Retrieves the draft's properties. */
    public GameProperties getProperties() {
        return properties;
    }

    /** Retrieves the request's watch. */
    public DiscordWatch getWatch() {
        return watch;
    }

    /**
     * Activates or deactivates the request.
     * @param toStatus the status to set the request to.
     */
    public void toggle(boolean toStatus) {
        initialized = toStatus;
    }

    /** Retrieves which draft number this is. */
    public int getNumDraft() {
        return numDraft;
    }

    /** Retrieves the request's suffix, often to attach to component IDs. */
    public String suffix() {
        return getPrefix().toUpperCase() + getNumDraft();
    }

    /** Retrieves the players of the draft. */
    public TreeMap<String, P> getPlayers() {
        return players;
    }

    /** Retrieves the history of players which have queued into the draft. */
    public HashSet<String> getHistory() {
        return playerHistory;
    }

    /** Retrieves the number of inactive players within the draft. */
    public int getNumInactive() {
        return numInactive;
    }

    /** Retrieves the respective draft chat channel. */
    public TextChannel getDraftChannel() {
        return draftChat;
    }

    /**
     * Stores the message ID of this request interface.
     * @param id the ID to store.
     */
    public void setMessageID(String id) {
        messageID = id;
    }

    /** Retrieves the message ID of this request interface. */
    public String getMessageID() {
        return messageID;
    }

    /**
     * Retrieves the request interface of the draft.
     * @param interaction the user interaction calling this method.
     */
    public Message getMessage(GenericInteractionCreateEvent interaction) {
        MessageChannel channel =
                getChannel(interaction, "\uD83D\uDCCD" + getPrefix()
                        + "-looking-for-draft");
        return channel.retrieveMessageById(getMessageID()).complete();
    }

    /**
     * Stores the field for executing the draft.
     * @param proc the field to store.
     */
    public void setProcess(S proc) {
        process = proc;
    }

    /** Retrieves the field for executing the draft. */
    public S getProcess() {
        return process;
    }

    /**
     * Checks if the draft has formally started.
     * @return True if it has formally started.
     *         False otherwise.
     */
    public boolean draftStarted() {
        return isInitialized() && getProcess().hasStarted();
    }

    /**
     * Checks whether the draft request has timed out or not.
     * @param interaction the user interaction calling this method.
     * @return True if the request expired.
     *         False otherwise.
     */
    public boolean timedOut(GenericInteractionCreateEvent interaction) {
        if (!isInitialized() && messageID != null
                && getWatch().timerOneExpired()) {
            getMessage(interaction)
                    .editMessage("This draft has expired.")
                    .setActionRow(Components.ForDraft.refresh(
                                    getPrefix() + getNumDraft())
                            .asDisabled()).queue();

            String update = "A " + getPrefix().toUpperCase()
                    + " draft request has timed out.";
            log(update, false);
            return true;
        }

        return false;
    }

    /**
     * Builds the draft confirmation summary in the form of an embed.
     * @param eb the pre-built embed to build with.
     * @param players a vertical list of all players in the draft.
     * @param subs a list of all subs in the draft.
     * @return the pre-built summary.
     */
    public EmbedBuilder buildEmbed(EmbedBuilder eb, StringBuilder players,
                                   StringBuilder subs) {
        eb.setColor(getColor());

        eb.addField("Players:", players.toString(), false);
        if (subs.length() > 0) {
            eb.addField("Subs:", subs.toString(), false);
        }

        if (isInitialized()) {
            String notice =
                    "Go to (the pinged) "
                            + getDraftChannel().getAsMention() + "\n"
                            + "to begin the draft. Use this interface\n"
                            + "to sub out as needed. ";
            if (!getPrefix().equals("fs")) {
                notice += "__Before you\n"
                        + "sub out -- make sure you got your\n"
                        + "points in the draft chat__!";
            }

            eb.addField("Notice:", notice, false);
        } else {
            eb.addField("Expiration:",
                    DiscordWatch.discordTimeUntil(getWatch().getTimerOneEnd()),
                    false);
        }

        return eb;
    }

    /**
     * Sends a draft confirmation summary with all players of the draft.
     * @param interaction the user interaction calling this method.
     */
    public void updateReport(GenericInteractionCreateEvent interaction) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Queue " + getNumDraft()
                + " (" + getProperties().getName() + ")");

        StringBuilder players = new StringBuilder();
        StringBuilder subs = new StringBuilder();

        for (Map.Entry<String, P> mapping : getPlayers().entrySet()) {
            String id = mapping.getKey();
            P player = mapping.getValue();

            if (!player.isActive()) {
                subs.append(player.getAsMention(id))
                        .append(" (inactive)").append("\n");
            } else if (player.isSub()) {
                subs.append(player.getAsMention(id)).append("\n");
            } else {
                players.append(player.getAsMention(id)).append("\n");
            }
        }

        sendEmbed(interaction, buildEmbed(eb, players, subs));
    }

    /** Formats a request ping for gathering players. */
    private String newPing() {
        int activePlayers = players.size() - getNumInactive();
        int pingsLeft =
                getProperties().getMaximumPlayersToStart() - activePlayers;
        if (draftStarted()) {
            pingsLeft = 0;
        }

        return getSectionRole() + " +" + pingsLeft;
    }

    /**
     * Repings for remaining players.
     * @param bc a button click to analyze.
     */
    public void reping(ButtonClickEvent bc) {
        String authorID = bc.getMember().getId();
        setMessageID(bc.getMessageId());

        if (!getPlayers().containsKey(authorID)) {
            sendReply(bc, "You are not in this draft!", true);
        } else if (!getWatch().timerTwoExpired()) {
            sendReply(bc, String.format("Wait until %s to reping!",
                            DiscordWatch.discordTime(getWatch().getTimerTwoEnd())),
                    true);
        } else if (getProperties().getMaximumPlayersToStart() - getPlayers().size()
                > (getProperties().getMaximumPlayersToStart() / 2) + 1) {
            int numPlayersLeft =
                    (getProperties().getMaximumPlayersToStart() / 2) + 1;
            sendReply(bc,
                    String.format("Reping only when you need +%s or less!",
                            numPlayersLeft), true);
        } else {
            bc.deferEdit().queue();

            bc.editButton(Components.ForDraft.reping(suffix())
                            .asDisabled()).queue();
            sendResponse(bc, newPing() + " (reping)", false);
        }
    }

    /**
     * Removes a player from the queue.
     * @param bc a button click to analyze.
     */
    public void removeFromQueue(ButtonClickEvent bc) {
        String playerID = bc.getMember().getId();
        setMessageID(bc.getMessageId());

        if (!getPlayers().containsKey(playerID)) {
            sendReply(bc, "You are not in this draft!", true);
        } else {
            getPlayers().remove(playerID);
            refresh(bc);
        }
    }

    /**
     * Refreshes the draft request's caption.
     * @param bc a button click to analyze.
     *
     * Note: The interaction must not have been acknowledged
     *       before this method.
     */
    public void refresh(ButtonClickEvent bc) {
        bc.deferEdit().queue();
        setMessageID(bc.getMessageId());

        int activePlayers = getPlayers().size() - getNumInactive();

        if (activePlayers == getProperties().getMaximumPlayersToStart()
                || !draftStarted()) {
            editMessage(bc, newPing());
        } else {
            int subsNeeded =
                    getProperties().getMaximumPlayersToStart() - activePlayers;
            editMessage(bc,
                    newPing() + "   // " + subsNeeded + " sub(s) needed");
        }

        updateReport(bc);
    }

    /**
     * Checks whether a player is in Team 1 or not.
     * @param playerID the Discord ID of the player to check.
     * @return True if they are in Team 1.
     *         False otherwise.
     */
    private boolean teamOneContains(String playerID) {
        return isInitialized() && getProcess().getTeam1().contains(playerID);
    }

    /**
     * Checks whether a player is in Team 2 or not.
     * @param playerID the Discord ID of the player to check.
     * @return True if they are in Team 2.
     *         False otherwise.
     */
    private boolean teamTwoContains(String playerID) {
        return isInitialized() && getProcess().getTeam2().contains(playerID);
    }

    /**
     * Checks whether a player is in Team 3 or not.
     * @param playerID the Discord ID of the player to check.
     * @return True if they are in Team 3.
     *         False otherwise.
     */
    private boolean teamThreeContains(String playerID) {
        return isInitialized() && getProcess().getTeam3() != null
                && getProcess().getTeam3().contains(playerID);
    }

    /**
     * Checks whether a player can be subbed out of the draft or not.
     * @param interaction the user interaction calling this method.
     * @param playerID the Discord ID of the player.
     * @param player the player to sub out.
     * @param notFoundString a string to output if the player could not be found.
     * @param subbedTwiceString a string to output if the player has already
     *                          been subbed out from the draft.
     * @return True if the substitution was successful.
     *         False otherwise.
     */
    private boolean canSubOut(GenericInteractionCreateEvent interaction,
                              String playerID, P player,
                              String notFoundString, String subbedTwiceString) {
        if (player == null) {
            sendReply(interaction, notFoundString, true);
            return false;
        } else if (!player.isActive()) {
            sendReply(interaction, subbedTwiceString, true);
            return false;
        } else if (teamOneContains(playerID)) {
            getProcess().getTeam1().requestSub();
        } else if (teamTwoContains(playerID)) {
            getProcess().getTeam2().requestSub();
        } else if (teamThreeContains(playerID)) {
            getProcess().getTeam3().requestSub();
        }

        player.subOut();
        numInactive++;

        return true;
    }

    /** Prints a warning message if too many people sub out. */
    private void printSubWarningMessage() {
        getDraftChannel().sendMessage(
                "**Are multiple people leaving?** Remember at any moment, "
                        + "all of you should/can discuss whether to end the "
                        + "draft, either by clicking the `End Draft` button "
                        + "or by pinging a staff member to `force end` the "
                        + "draft. **Do not just sub out and ping "
                        + "everyone.**").queue();
    }

    /**
     * Checks whether a player can request a sub or not.
     * @param bc a button click to analyze.
     * @return True if they can.
     *         False otherwise.
     */
    public boolean canRequestSub(ButtonClickEvent bc) {
        String playerID = bc.getMember().getId();
        P foundPlayer = getPlayers().get(playerID);
        boolean subWasSuccessful =
                canSubOut(bc, playerID, foundPlayer,
                        "You are not in this draft!",
                        "You have already been subbed out of the draft!");

        if (subWasSuccessful) {
            String update = getSectionRole() + " +1 (sub)";
            sendResponse(bc, update, false);

            getDraftChannel().sendMessage(
                    "`" + foundPlayer.getName()
                            + "` has been subbed out.").queue();

            if (getNumInactive() % 2 == 0) {
                printSubWarningMessage();
            }
        }

        return subWasSuccessful;
    }

    /**
     * Checks whether a player can be forcibly subbed out or not.
     * @param sc a slash command to analyze.
     * @param playerID the Discord ID of the player to sub.
     * @return True if they can be.
     *         False otherwise.
     */
    public boolean canForceSub(SlashCommandEvent sc, String playerID) {
        String authorID = sc.getMember().getId();

        if (!getPlayers().containsKey(authorID)) {
            sendReply(sc, "You don't have access to that draft!", true);
        } else if (!isInitialized()) {
            sendReply(sc, "There's no point in subbing people out currently!", true);
        } else if (canSubOut(sc, playerID, getPlayers().get(playerID),
                "That player is not part of the draft.",
                "That player has already been subbed out of the draft!")) {
            String update = getSectionRole() + " +1 (sub, " + Emoji.fromEmote(
                            "refresh", 788354776999526410L, false)
                    .getAsMention() + " refresh the request)";
            sendReply(sc, update, false);

            getDraftChannel().sendMessage(
                    "`" + getPlayers().get(playerID).getName()
                            + "` has been subbed out.").queue();

            if (getNumInactive() % 2 == 0) {
                printSubWarningMessage();
            }

            return true;
        }

        return false;
    }

    /**
     * Checks whether a player can access a draft, according
     * to their draft section.
     * @param interaction the user interaction calling this method.
     * @return True if they are in the wrong section.
     *         False otherwise.
     */
    public boolean inWrongSection(GenericInteractionCreateEvent interaction) {
        return !interaction.getMember().getRoles().contains(
                getRole(interaction, getSection()));
    }

    /**
     * Subs a player into the draft.
     * @param playerID the Discord ID of the player.
     */
    public boolean canSubIn(ButtonClickEvent bc, String playerID) {
        String statement;
        boolean displayProfile = false;

        if (getPlayers().containsKey(playerID)) {
            P player = getPlayers().get(playerID);
            if (player.getSubAmount() == 2) {
                sendReply(bc,
                        "You have already been subbed out twice! You cannot "
                                + "sub anymore for this draft.", true);
                return false;
            } else if (teamOneContains(playerID)) {
                getProcess().getTeam1().add(playerID, player);
            } else if (teamTwoContains(playerID)) {
                getProcess().getTeam2().add(playerID, player);
            } else if (teamThreeContains(playerID)) {
                getProcess().getTeam3().add(playerID, player);
            }

            player.subIn(draftStarted());
            numInactive--;

            statement = "will be coming back to sub";
        } else {
            getHistory().add(playerID);

            statement = "will be subbing";
            displayProfile = true;
        }

        String update = String.format("<@%s> %s for this draft. ",
                playerID, statement);
        if (!getPrefix().equals("fs")) {
            update += "__Refresh the pinned interface to add them to "
                    + "a team!__";
        }

        getDraftChannel().sendMessage(update).queue();

        if (displayProfile) {
            MessageEmbed profile = new Profile().view(bc,
                    playerID, false, true, false);
            if (profile != null) {
                getDraftChannel().sendMessageEmbeds(profile).queue();
            }
        }

        return true;
    }

    /** Unpins anything in the respective draft chat channel. */
    public void unpinDraftChannelPins() {
        for (Message pin : getDraftChannel()
                .retrievePinnedMessages().complete()) {
            pin.unpin().complete();
        }
    }

    /**
     * Checks if the game can be forcibly ended.
     * @param sc a slash command to analyze.
     * @return True if the draft was forcibly ended.
     *         False otherwise.
     */
    public boolean canForceEnd(SlashCommandEvent sc) {
        if (getMessageID() == null) {
            sendReply(sc, Emoji.fromEmote(
                            "refresh", 788354776999526410L, false)
                    .getAsMention() + " Refresh the request once then try "
                    + "again.", true);
            return false;
        } else if (isInitialized() && getProcess().getMessageID() == null) {
            sendReply(sc, "Press the `End Draft` button in "
                    + getDraftChannel().getAsMention() + " "
                    + "once then try again.", true);
            return false;
        } else {
            getChannel(sc, "\uD83D\uDCCD" + getPrefix() + "-looking-for-draft")
                    .retrieveMessageById(getMessageID()).complete()
                    .editMessage("This draft has forcibly ended.")
                    .setActionRow(Components.ForDraft.refresh(
                                    getPrefix() + getNumDraft())
                            .asDisabled()).queue();

            if (isInitialized()) {
                unpinDraftChannelPins();
                getProcess().getMessage().delete().queue();

                getDraftChannel().sendMessage(
                        "The draft has been ended by staff. Sorry about the "
                                + "early stop! Feel free to request a new one!").queue();
                sendReply(sc, "Draft ended.", false);
            } else {
                sendReply(sc, "Draft ended.", true);
            }

            log("A draft was forcibly ended by "
                    + sc.getUser().getAsTag() + ".", false);

            return true;
        }
    }

    /**
     * Runs the draft start command.
     * @param sc the user's inputted command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        if (inWrongSection(sc)) {
            return;
        }

        List<Button> buttons = new ArrayList<>();
        buttons.add(Components.ForDraft.joinDraft(suffix()));
        buttons.add(Components.ForDraft.reping(suffix()));
        buttons.add(Components.ForDraft.leave(suffix()));
        buttons.add(Components.ForDraft.refresh(suffix()));

        String caption = getSectionRole() + " +"
                + (getProperties().getMaximumPlayersToStart() - 1);

        sc.reply(caption).addActionRow(buttons).queue();
        updateReport(sc);

        log("A " + getPrefix().toUpperCase()
                + " draft has been requested.", false);
    }
}
