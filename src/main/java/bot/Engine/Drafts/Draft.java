package bot.Engine.Drafts;

import bot.Engine.Section;
import bot.Tools.BuiltButton;
import bot.Tools.Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    /** The players of the draft. */
    private final List<DraftPlayer> players;
    private final List<DraftPlayer> subs;

    /** The number of subs needed for a draft. */
    private int subsNeeded;

    /** The pinged role for this draft. */
    private final Role draftRole;

    /** The draft chat channel this draft is occurring in. */
    private final TextChannel draftChat;

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

        players = new ArrayList<>();
        subs = new ArrayList<>();
        DraftPlayer newPlayer = new DraftPlayer(initialPlayer);
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
     * Retrieves the players of the draft.
     * @return said players.
     */
    public List<DraftPlayer> getPlayers() {
        return players;
    }

    /** Retrieves the subs of the draft. */
    public List<DraftPlayer> getSubs() {
        return subs;
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

        Random r = new Random();
        int capt1 = r.nextInt(8);
        int capt2 = r.nextInt(8);
        while (capt1 == capt2) {
            capt2 = r.nextInt(8);
        }

        int size = getPlayers().size();
        for (int i = 0; i < size; i++) {
            Member currPlayer = getPlayers().get(i).getAsMember();
            queue.append(currPlayer.getAsMention());
            logList.append(currPlayer.getAsMention()).append(" ");

            if (size == 8 && (i == capt1 || i == capt2)) {
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
     * Attempts to start a draft.
     * @param bc a button click to analyze.
     */
    public void attemptDraft(ButtonClickEvent bc) {
        bc.deferEdit().queue();

        Member potentialPlayer = bc.getMember();
        // this block prevents repeated players in the initial draft start
        // comment/uncomment as needed
        if (draftContains(potentialPlayer, getPlayers()) != -1) {
            sendResponse(bc, "You are already in this draft!", true);
            return;
        }

        DraftPlayer newPlayer = new DraftPlayer(potentialPlayer);
        getPlayers().add(newPlayer);

        if (getPlayers().size() == 8) {
            toggleDraft();

            ArrayList<Button> buttons = new ArrayList<>();
            String idSuffix = getPrefix().toUpperCase() + getNumDraft();
            buttons.add(Buttons.joinDraft(idSuffix)
                    .withLabel("Draft queue full.").asDisabled());
            buttons.add(Buttons.requestSub(idSuffix));
            buttons.add(Buttons.joinAsSub(idSuffix));
            buttons.add(Buttons.end(idSuffix));

            sendButtons(bc, bc.getInteraction().getMessage().getContentRaw(),
                    buttons);
        }

        editMessage(bc, newPing());
        updateReport(bc);
    }

    /**
     * Checks whether a player is within a draft player list or not.
     * @param player the player to check for.
     * @param lst a draft's list of players.
     * @return the index of the player within the draft's list of players.
     *         -1 if they are not in the draft yet.
     */
    private int draftContains(Member player, List<DraftPlayer> lst) {
        for (int i = 0; i < lst.size(); i++) {
            Member currPlayer = lst.get(i).getAsMember();
            if (currPlayer.equals(player)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Retrieves a player from the draft to sub, if they exist.
     * @param player the player to convert.
     * @return the found player within the draft.
     *         null otherwise.
     */
    private DraftPlayer convertToSub(Member player) {
        int playerIndex = draftContains(player, getPlayers());
        int subIndex = draftContains(player, getSubs());

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
        DraftPlayer convertedSub = convertToSub(bc.getMember());

        if (convertedSub == null) {
            sendReply(bc, "You are not part of this draft!", true);
            return;
        } else if (convertedSub.getPings() > 0) {
            sendReply(bc, "Stop spamming " + convertedSub.getAsMember().getAsMention()
                    + "!", true);
            return;
        }
        bc.deferEdit().queue();

        getPlayers().remove(convertedSub);
        getSubs().remove(convertedSub);
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
                !(draftContains(player, getPlayers()) == -1
                && draftContains(player, getSubs()) == -1);

        if (inDraft) {
            sendReply(bc, "Why would you sub for yourself?", true);
            return;
        } else if (subsNeeded == 0) {
            sendReply(bc, "This draft hasn't requested any subs yet.", true);
            return;
        }
        bc.deferEdit().queue();

        DraftPlayer subPlayer = new DraftPlayer(player);
        getSubs().add(subPlayer);

        subsNeeded--;

        if (subsNeeded == 0) {
            editMessage(bc, newPing());
        } else {
            editMessage(bc, newPing() + "   // " + subsNeeded + " sub(s) needed");
        }

        String update =
                subPlayer.getAsMember().getAsMention() + " will be subbing for "
                        +"this draft in " + getDraftChannel().getAsMention() + ".";
        sendResponse(bc, update, false);
    }

    /**
     * Removes a player from the draft, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void removePlayer(ButtonClickEvent bc) {
        Member player = bc.getMember();
        int playerIndex = draftContains(player, getPlayers());

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
     * Buttons for the draft.
     */
    private static class Buttons {

        /**
         * Builds the "Join Draft" button.
         * @param suffix the ID's suffix.
         * @return said button.
         */
        private static Button joinDraft(String suffix) {
            return new BuiltButton("join" + suffix,
                    "Join Draft", 0).getButton();
        }

        /**
         * Builds the "Leave" button.
         * @param suffix the ID's suffix.
         * @return the button.
         */
        private static Button leave(String suffix) {
            return new BuiltButton("leave" + suffix,
                    "Leave", 3).getButton();
        }

        /**
         * Builds the "Request Sub" button.
         * @param suffix the ID's suffix.
         * @return said button.
         */
        private static Button requestSub(String suffix) {
            return new BuiltButton("requestSub" + suffix,
                    "Request Sub", 1).getButton();
        }

        /**
         * Builds the "Join As Sub" button.
         * @param suffix the ID's suffix.
         * @return said button.
         */
        private static Button joinAsSub(String suffix) {
            return new BuiltButton("sub" + suffix,
                    "Join as Sub", 0).getButton();
        }

        /**
         * Builds the "End Draft" button.
         * @param suffix the ID's suffix.
         * @return the button.
         */
        private static Button end(String suffix) {
            return new BuiltButton("end" + suffix,
                    "End Draft", 3).getButton();
        }
    }
}
