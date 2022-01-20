package bot.Engine.Drafts;

import bot.Engine.Section;
import bot.Tools.ButtonBuilder;
import bot.Tools.Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.util.List;
import java.util.ArrayList;

/**
 * @author  Wil Aquino, Turtle#1504
 * Date:    December 6, 2021
 * Project: Libra
 * Module:  Draft.java
 * Purpose: Formalizes and starts a draft.
 */
public class Draft extends Section implements Command {

    /** Flag for checking whether the draft has started or not. */
    private boolean started;

    /** The formal number of the draft. */
    private final int numDraft;

    /** The formal process for executing the draft. */
    private final DraftProcess draftProcess;

    /** The players of the draft. */
    private final List<DraftPlayer> players;
    private final List<DraftPlayer> subs;

    /** The indexes of the captains for this draft. */
    private int captain1, captain2;

    /** The number of subs needed for this draft. */
    private int subsNeeded;

    /** The pinged role for this draft. */
    private final Role draftRole;

    /** The draft chat channel this draft is occurring in. */
    private final TextChannel draftChat;

    private String messageURL;

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
        started = false;
        subsNeeded = 0;
        numDraft = draft;
        draftProcess = new DraftProcess(this);

//        Random r = new Random();
//        captain1 = r.nextInt(8);
//        captain2 = r.nextInt(8);
//        while (captain1 == captain2) {
//            captain2 = r.nextInt(8);
//        }
        captain1 = 0;
        captain2 = 1;

        players = new ArrayList<>();
        subs = new ArrayList<>();
        DraftPlayer newPlayer = new DraftPlayer(initialPlayer.getId());
        players.add(newPlayer);

        draftRole = getRole(sc, getSection());
        draftChat = getChannel(sc, getPrefix() + "-draft-chat-" + draft);
    }

    /**
     * Activates or deactivates the draft.
     */
    public void toggleDraft() {
        started = !started;
    }

    /**
     * Checks whether the draft has formally started yet.
     * @return True if the draft is in progress.
     *         False otherwise.
     */
    public boolean inProgress() {
        return started;
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
    public List<DraftPlayer> getPlayers() {
        return players;
    }

    /**
     * Retrieves the subs of the draft.
     * @return said subs.
     */
    public List<DraftPlayer> getSubs() {
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

    public String getURL() {
        return messageURL;
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

        int size = getPlayers().size();
        for (int i = 0; i < size; i++) {
            Member currPlayer = findMember(bc, getPlayers().get(i).getID());
            queue.append(currPlayer.getAsMention());
            logList.append(currPlayer.getAsMention()).append(" ");

            if (size == 8 && (i == getCaptIndex1() || i == getCaptIndex2())) {
                queue.append(" (captain)");
            }
            queue.append("\n");
        }
        eb.addField("Players", queue.toString(), false);

        if (size == 8) {
            String notice =
                    "Go to " + getDraftChannel().getAsMention() + " to begin\n"
                            + "the draft. Use the interface above to\n"
                            + "request subs as needed, as well as\n"
                            + "end the draft to allow others to\n"
                            + "queue more drafts.";
            eb.addField("Notice", notice, false);

            logList.deleteCharAt(logList.length() - 1);
            log("A " + getPrefix().toUpperCase() + " draft was started with "
                    + logList + ".", false);
        }

        sendEmbed(bc, eb);
    }

    /**
     * Formats a ping for gathering players.
     * @return the request.
     */
    private String newPing() {
        int pingsLeft;
        if (inProgress()) {
            pingsLeft = 0;
        } else {
            pingsLeft = 8 - getPlayers().size();
        }

        return getDraftRole().getAsMention() + " +" + pingsLeft;
    }

    /**
     * Checks whether a player is within a draft player list or not.
     * @param bc a button click to analyze.
     * @param player the player to check for.
     * @param lst a draft's list of players.
     * @return the index of the player within the draft's list of players.
     *         -1 if they are not in the draft yet.
     */
    private int draftContains(ButtonClickEvent bc, Member player,
                              List<DraftPlayer> lst) {
        for (int i = 0; i < lst.size(); i++) {
            Member currPlayer = findMember(bc, lst.get(i).getID());
            if (currPlayer.equals(player)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Attempts to start a draft.
     * @param bc a button click to analyze.
     */
    public void attemptDraft(ButtonClickEvent bc) {
        bc.deferEdit().queue();

        Member potentialPlayer = bc.getMember();
        // this block prevents repeated players in the initial draft start
        // comment/uncomment as needed
//        if (draftContains(potentialPlayer, getPlayers()) != -1) {
//            sendResponse(bc, "You are already in this draft!", true);
//            return;
//        }

        DraftPlayer newPlayer = new DraftPlayer(potentialPlayer.getId());
        getPlayers().add(newPlayer);

        if (getPlayers().size() == 8) {
            toggleDraft();

            List<Button> buttons = new ArrayList<>();
            String idSuffix = getPrefix().toUpperCase() + getNumDraft();
            String serverID = bc.getGuild().getId();
            String channelID = getDraftChannel().getId();

            buttons.add(Buttons.link(serverID, channelID));
            buttons.add(Buttons.requestSub(idSuffix));
            buttons.add(Buttons.joinAsSub(idSuffix));
            buttons.add(Buttons.end(idSuffix));

            sendButtons(bc, bc.getInteraction().getMessage().getContentRaw(),
                    buttons);
        }

        editMessage(bc, newPing());
        updateReport(bc);

        messageURL = bc.getMessage().getJumpUrl();
        getProcess().start(bc);
    }

    /**
     * Retrieves a player from the draft to sub, if they exist.
     * @param bc a button click to analyze.
     * @param player the player to convert.
     * @return the found player within the draft.
     *         null otherwise.
     */
    private DraftPlayer convertToSub(ButtonClickEvent bc, Member player) {
        int playerIndex = draftContains(bc, player, getPlayers());
        int subIndex = draftContains(bc, player, getSubs());

        if (playerIndex != -1) {
            return getPlayers().get(playerIndex);
        } else if (subIndex != -1) {
            return getSubs().get(subIndex);
        } else {
            return null;
        }
    }

    /**
     * Requests a sub for a draft, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void requestSub(ButtonClickEvent bc) {
        DraftPlayer convertedSub = convertToSub(bc, bc.getMember());

        if (convertedSub == null) {
            sendReply(bc, "You are not part of this draft!", true);
            return;
        } else if (convertedSub.getPings() > 0) {
            sendReply(bc, "Stop spamming " + bc.getMember().getAsMention()+ "!",
                    true);
            return;
        }
        bc.deferEdit().queue();

        getPlayers().remove(convertedSub);
        getSubs().remove(convertedSub);

        convertedSub.setInactive();
        getSubs().add(convertedSub);

        subsNeeded++;
        convertedSub.incrementPings();

        editMessage(bc, newPing() + "   // " + subsNeeded + " sub(s) needed");

        String update = getDraftRole().getAsMention() + " sub requested.";
        sendResponse(bc, update, false);
    }

    /**
     * Adds a player to the draft's subs, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void addSub(ButtonClickEvent bc) {
        Member player = bc.getMember();
        boolean inDraft =
                !(draftContains(bc, player, getPlayers()) == -1
                && draftContains(bc, player, getSubs()) == -1);

        /*if (inDraft) {
            sendReply(bc, "Why would you sub for yourself?", true);
            return;
        } else */if (subsNeeded == 0) {
            sendReply(bc, "This draft hasn't requested any subs yet.", true);
            return;
        }
        bc.deferEdit().queue();

        DraftPlayer subPlayer = new DraftPlayer(player.getId());
        getSubs().add(subPlayer);

        subsNeeded--;

        if (subsNeeded == 0) {
            editMessage(bc, newPing());
        } else {
            editMessage(bc, newPing() + "   // " + subsNeeded + " sub(s) needed");
        }

        String update =
                player.getAsMention() + " will be subbing for "
                        + "this draft in " + getDraftChannel().getAsMention() + ".";
        sendResponse(bc, update, false);
    }

    /**
     * Removes a player from the draft, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void removePlayer(ButtonClickEvent bc) {
        Member player = bc.getMember();
        int playerIndex = draftContains(bc, player, getPlayers());

        if (playerIndex == -1) {
            sendReply(bc, "You're not in this draft!", true);
            return;
        }
        bc.deferEdit().queue();

        getPlayers().remove(playerIndex);

        editMessage(bc, newPing());
        updateReport(bc);
    }

    /**
     * Ends the draft is it is completed, via a button.
     * @param bc a button click to analyze.
     */
    public boolean hasEnded(ButtonClickEvent bc) {
        if (!inProgress()) {
            bc.deferEdit().queue();
            sendButton(bc, "This draft has ended.",
                    Buttons.end(getPrefix() + getNumDraft())
                    .withStyle(ButtonStyle.SECONDARY).asDisabled());

            return true;
        } else {
            sendReply(bc, "This draft hasn't finished yet.", true);
            return false;
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
        buttons.add(Buttons.joinDraft(idSuffix));
        buttons.add(Buttons.leave(idSuffix));

        String caption = getDraftRole().getAsMention() + " +7";
        sendButtons(sc, caption, buttons);
        log("A " + getPrefix().toUpperCase()
                + " draft has been requested.", false);
    }

    /**
     * Buttons for formatting the draft.
     */
    private static class Buttons {

        /**
         * Builds the "Join Draft" button.
         * @param suffix the ID's suffix.
         * @return said button.
         */
        private static Button joinDraft(String suffix) {
            return new ButtonBuilder("join" + suffix,
                    "Join Draft", null, 0).getButton();
        }

        /**
         * Builds the "Leave" button.
         * @param suffix the ID's suffix.
         * @return the button.
         */
        private static Button leave(String suffix) {
            return new ButtonBuilder("leave" + suffix,
                    "Leave", null, 3).getButton();
        }

        /**
         * Builds the "Draft Channel" link button.
         * @param serverID the ID of the server with the channel in it.
         * @param channelID the ID of the channel to link to.
         * @return said button.
         */
        private static Button link(String serverID, String channelID) {
            String url =
                    String.format("https://discord.com/channels/%s/%s",
                            serverID, channelID);

            return new ButtonBuilder(null, "Draft Channel",
                    url, 4).getButton();
        }

        /**
         * Builds the "Request Sub" button.
         * @param suffix the ID's suffix.
         * @return said button.
         */
        private static Button requestSub(String suffix) {
            return new ButtonBuilder("requestSub" + suffix,
                    "Request Sub", null, 1).getButton();
        }

        /**
         * Builds the "Join As Sub" button.
         * @param suffix the ID's suffix.
         * @return said button.
         */
        private static Button joinAsSub(String suffix) {
            return new ButtonBuilder("sub" + suffix,
                    "Join as Sub", null, 0).getButton();
        }

        /**
         * Builds the "End Draft" button.
         * @param suffix the ID's suffix.
         * @return the button.
         */
        private static Button end(String suffix) {
            return new ButtonBuilder("end" + suffix,
                    "End Draft", null, 3).getButton();
        }
    }
}
