package bot.Engine.Drafts;

import bot.Engine.Section;
import bot.Tools.Command;

import bot.Tools.Components;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @author  Wil Aquino, Turtle#1504
 * Date:    December 6, 2021
 * Project: Libra
 * Module:  Draft.java
 * Purpose: Formalizes and starts a draft.
 */
public class Draft extends Section implements Command {

    /** Flag for checking whether this draft has initialized or not. */
    private boolean initialized;

    /** The starting time of this draft's initial request. */
    private final long startTime;

    /** The formal number of this draft. */
    private final int numDraft;

    /** The formal process for executing this draft. */
    private DraftProcess draftProcess;

    /** The players of this draft. */
    private final TreeMap<String, DraftPlayer> players;
    private final TreeMap<String, DraftPlayer> subs;

    /** The indexes of the captains of this draft. */
    private int captain1, captain2;

    /** The number of subs needed for this draft. */
    private int subsNeeded;

    /** The pinged role of this draft. */
    private final Role draftRole;

    /** The draft chat channel this draft is occurring in. */
    private final TextChannel draftChat;

    /** The discord message ID for this draft's initial interface. */
    private String messageID;

    /**
     * Constructs a draft template and initializes the
     * draft start attributes.
     * @param sc the user's inputted command.
     * @param draft the numbered draft that this draft is.
     * @param abbreviation the abbreviation of the section.
     * @param initialPlayer the first player of the draft.
     */
    public Draft(SlashCommandEvent sc, int draft,
                 String abbreviation, Member initialPlayer) {
        super(abbreviation);

        initialized = false;
        startTime = System.currentTimeMillis();

        subsNeeded = 0;
        numDraft = draft;

//        Random r = new Random();
//        captain1 = r.nextInt(8);
//        captain2 = r.nextInt(8);
//        while (captain1 == captain2) {
//            captain2 = r.nextInt(8);
//        }
        captain1 = 0;
        captain2 = 5;

        players = new TreeMap<>();
        subs = new TreeMap<>();
        players.put(initialPlayer.getId(), new DraftPlayer());

        draftRole = getRole(sc, getSection());
        draftChat = getChannel(sc, getPrefix() + "-draft-chat-" + draft);
    }

    /**
     * Checks whether the draft request has been satisfied or not.
     * @return True if eight players have joined the draft.
     *         False otherwise.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Activates or deactivates the draft.
     */
    public void toggleDraft() {
        initialized = !initialized;
    }

    /**
     * Checks whether the draft request has timed out or not.
     * @param interaction the user interaction calling this method.
     * @return True if the request expired.
     *         False otherwise.
     */
    public boolean timedOut(GenericInteractionCreateEvent interaction) {
        // time limit of 30 minutes
        long timeLimit = 1800000;
        long currentTime = System.currentTimeMillis();

        if (!isInitialized() && currentTime - startTime > timeLimit) {
            ArrayList<Button> buttons = new ArrayList<>();
            String idSuffix = getPrefix().toUpperCase() + getNumDraft();
            buttons.add(Components.ForDraft.joinDraft(idSuffix)
                    .withStyle(ButtonStyle.SECONDARY).asDisabled());
            buttons.add(Components.ForDraft.leave(idSuffix)
                    .withStyle(ButtonStyle.SECONDARY).asDisabled());

            getMessage(interaction)
                    .editMessage("This draft request has expired.")
                    .setActionRow(buttons).queue();

            log("A draft request has timed out.", false);
            return true;
        }

        return false;
    }

    /**
     * Retrieves which draft number this is.
     * @return said number.
     */
    public int getNumDraft() {
        return numDraft;
    }

    /**
     * Retrieves the field for executing the draft.
     * @return said execution.
     */
    public DraftProcess getProcess() {
        return draftProcess;
    }

    /**
     * Retrieves the players of the draft.
     * @return said players.
     */
    public TreeMap<String, DraftPlayer> getPlayers() {
        return players;
    }

    /**
     * Retrieves the subs of the draft.
     * @return said subs.
     */
    public TreeMap<String, DraftPlayer> getSubs() {
        return subs;
    }

    /**
     * Retrieve the index within the list of players,
     * for captain 1.
     * @return said index.
     */
    public int getCaptIndex1() {
        return captain1;
    }

    /**
     * Retrieve the index within the list of players,
     * for captain 2.
     * @return said index.
     */
    public int getCaptIndex2() {
        return captain2;
    }

    /**
     * Retrieves the pinged role for this draft.
     * @return said role.
     */
    public Role getDraftRole() {
        return draftRole;
    }

    /**
     * Retrieves the draft chat channel which this draft
     * is occurring in.
     * @return said channel.
     */
    public TextChannel getDraftChannel() {
        return draftChat;
    }

    /**
     * Retrieves the request interface of the draft.
     * @return said message.
     */
    public Message getMessage(GenericInteractionCreateEvent interaction) {
        MessageChannel channel =
//                getChannel(interaction, getPrefix() + "-looking-for-draft
                getChannel(interaction, "bot-testing");
        return channel.retrieveMessageById(messageID).complete();
    }

    /**
     * Sends a draft confirmation summary with all players
     * of the draft.
     * @param bc the button click to analyze.
     */
    private void updateReport(ButtonClickEvent bc) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Draft Queue");
        eb.setColor(getColor());

        StringBuilder queue = new StringBuilder();
        StringBuilder logList = new StringBuilder();

        int count = 0;
        for (String id : getPlayers().keySet()) {
            Member currPlayer = findMember(bc, id);
            queue.append(currPlayer.getAsMention());
            logList.append(currPlayer.getEffectiveName()).append(", ");

            if (getPlayers().size() == 8
                    && (count == getCaptIndex1() || count == getCaptIndex2())) {
                queue.append(" (captain)");
            }
            queue.append("\n");

            count++;
        }
        eb.addField("Players:", queue.toString(), false);

        queue = new StringBuilder();
        if (getSubs().size() > 0) {
            for (String id : getSubs().keySet()) {
                Member currPlayer = findMember(bc, id);
                queue.append(currPlayer.getAsMention()).append("\n");
                logList.append(currPlayer.getAsMention()).append(" ");
            }
            eb.addField("Subs:", queue.toString(), false);
        }

        if (getPlayers().size() + getSubs().size() >= 8) {
            String notice =
                    "Go to " + getDraftChannel().getAsMention() + " to begin\n"
                            + "the draft. Use the interface above to\n"
                            + "request subs as needed, as well as\n"
                            + "end the draft to allow others to\n"
                            + "queue more drafts.";
            eb.addField("Notice:", notice, false);

            logList.deleteCharAt(logList.length() - 1);
            if (!isInitialized()) {
                log("A " + getPrefix().toUpperCase() + " draft was started with "
                        + logList + ".", false);
            }
        }

        sendEmbed(bc, eb);
    }

    /**
     * Formats a ping for gathering players.
     * @return the request.
     */
    private String newPing() {
        int pingsLeft;
        if (isInitialized()) {
            pingsLeft = 0;
        } else {
            pingsLeft = 8 - getPlayers().size();
        }

        return getDraftRole().getAsMention() + " +" + pingsLeft;
    }

    /**
     * Updates the draft request.
     * @param bc a button click to analyze.
     */
    private void updateRequest(ButtonClickEvent bc) {
        editMessage(bc, newPing());
        updateReport(bc);
    }

    /**
     * Attempts to start a draft.
     * @param bc a button click to analyze.
     */
    public void attemptDraft(ButtonClickEvent bc) {
        bc.deferEdit().queue();

        String playerID = bc.getMember().getId();
//        if (getPlayers().containsKey(playerID)) {
//            sendResponse(bc, "You are already in this draft!", true);
//            return;
//        }
        getPlayers().put(playerID, new DraftPlayer());

        // for testing
        getPlayers().put("191016647543357440", new DraftPlayer());
        getPlayers().put("350386286256848896", new DraftPlayer());
        getPlayers().put("140942181023219713", new DraftPlayer());
        getPlayers().put("524592272411459584", new DraftPlayer());
        getPlayers().put("97288493029416960", new DraftPlayer());
        getPlayers().put("407939462325207044", new DraftPlayer());
        getPlayers().put("388507632480157696", new DraftPlayer());
        messageID = bc.getMessage().getId();

        if (getPlayers().size() == 8) {
            draftProcess = new DraftProcess(this);

            List<Button> buttons = new ArrayList<>();
            String idSuffix = getPrefix().toUpperCase() + getNumDraft();

            getProcess().initialize(bc);

            buttons.add(Components.ForDraft.draftLink(bc, idSuffix, getProcess()));
            buttons.add(Components.ForDraft.requestSub(idSuffix));
            buttons.add(Components.ForDraft.joinAsSub(idSuffix));
            buttons.add(Components.ForDraft.refresh(idSuffix));
            buttons.add(Components.ForDraft.endDraft(idSuffix));

            sendButtons(bc, bc.getInteraction().getMessage().getContentRaw(),
                    buttons);
            updateRequest(bc);

            toggleDraft();
        } else {
            updateRequest(bc);
            messageID = bc.getMessage().getId();
        }
    }

    /**
     * Refreshes the draft request's caption.
     * @param bc a button click to analyze.
     */
    public void refresh(ButtonClickEvent bc) {
        bc.deferEdit().queue();

        if (subsNeeded == 0) {
            editMessage(bc, newPing());
        } else {
            editMessage(bc,
                    newPing() + "   // " + subsNeeded + " sub(s) needed");
        }
        updateReport(bc);
    }

    /**
     * Removes a player from the draft.
     * @param id the Discord ID of the player.
     * @return the removed player.
     */
    private DraftPlayer removePlayer(String id) {
        if (getPlayers().containsKey(id)) {
            return getPlayers().remove(id);
        } else if (getSubs().containsKey(id)) {
            return getSubs().remove(id);
        }

        return null;
    }

    /**
     * Requests a sub for a draft, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void requestSub(ButtonClickEvent bc) {
        String playerID = bc.getMember().getId();
        DraftPlayer convertedSub = removePlayer(playerID);

        if (convertedSub == null) {
            sendReply(bc, "You are not part of this draft!", true);
        } else if (convertedSub.getPings() > 0) {
            sendReply(bc, "You have already been subbed out of the draft!",
                    true);
        } else {
            getSubs().put(playerID, convertedSub);

            subsNeeded++;
            convertedSub.setInactive();
            convertedSub.incrementPings();

            refresh(bc);

            String update = getDraftRole().getAsMention() + " sub requested.";
            sendResponse(bc, update, false);
        }
    }

    /**
     * Forcibly subs a person out of the draft.
     * @param sc a slash command to analyze.
     * @param playerID the Discord ID of the player to sub.
     */
    public void forceSub(SlashCommandEvent sc, String playerID) {
        String authorID = sc.getMember().getId();
        if (!getPlayers().containsKey(authorID)
            || !getSubs().containsKey(authorID)) {
            sendReply(sc, "You are not part of this draft!", true);
        }

        DraftPlayer convertedSub = removePlayer(playerID);

        if (convertedSub == null) {
            sendReply(sc, "That player is not part of this draft.", true);
        } else {
            getSubs().put(playerID, convertedSub);

            subsNeeded++;
            convertedSub.setInactive();
            convertedSub.incrementPings();

            String update = getDraftRole().getAsMention() + " sub requested "
                    + "(see above drafts after refreshing).";
            sendReply(sc, update, false);
        }
    }

    /**
     * Adds a player to the draft's subs, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void addSub(ButtonClickEvent bc) {
        String playerID = bc.getMember().getId();
        boolean inDraft = getPlayers().containsKey(playerID)
                || getSubs().containsKey(playerID);

        /*if (inDraft) {
            sendReply(bc, "You already left the draft!", true);
        } else */if (subsNeeded == 0) {
            sendReply(bc, "This draft hasn't requested any subs yet.", true);
        } else {
            subsNeeded--;
            getSubs().put(playerID, new DraftPlayer());

            refresh(bc);

            String update = bc.getMember().getAsMention()
                    + " will be subbing for this draft in "
                    + getDraftChannel().getAsMention() + ".";
            sendResponse(bc, update, false);
        }
    }

    /**
     * Removes a player from the draft, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void removeFromQueue(ButtonClickEvent bc) {
        String playerID = bc.getMember().getId();

        if (!getPlayers().containsKey(playerID)) {
            sendReply(bc, "You're not in this draft!", true);
        } else {
            bc.deferEdit().queue();

            getPlayers().remove(playerID);

            editMessage(bc, newPing());
            updateReport(bc);
        }
    }

    /**
     * Ends the draft is it is completed, via a button.
     * @param bc a button click to analyze.
     */
    public boolean hasEnded(ButtonClickEvent bc) {
        if (isInitialized()) {
            sendReply(bc, "This draft hasn't finished yet.", true);
            return false;
        } else {
            bc.deferEdit().queue();
            sendButton(bc, "This draft has ended.",
                    Components.ForDraft.endDraft(getPrefix() + getNumDraft())
                            .withStyle(ButtonStyle.SECONDARY).asDisabled());

            return true;
        }
    }

    /**
     * Runs the draft start command.
     * @param sc the user's inputted command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        sc.deferReply().queue();

        ArrayList<Button> buttons = new ArrayList<>();
        String idSuffix = getPrefix().toUpperCase() + getNumDraft();
        buttons.add(Components.ForDraft.joinDraft(idSuffix));
        buttons.add(Components.ForDraft.leave(idSuffix));

        String caption = getDraftRole().getAsMention() + " +7";

        sc.getHook().editOriginal(caption)
                .setActionRow(buttons).queue(
                        (message) -> messageID = message.getId());
        log("A " + getPrefix().toUpperCase()
                + " draft has been requested.", false);
    }
}
