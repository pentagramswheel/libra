package bot.Engine.Drafts;

import bot.Engine.Section;
import bot.Tools.BuiltButton;
import bot.Tools.Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author  Wil Aquino, Turtle#1504
 * Date:    December 6, 2021
 * Project: LaunchPoint Bot
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
     * @param abbreviation the abbreviation of the section.
     * @param initialPlayer the first player of the draft.
     */
    public Draft(int draft, String abbreviation, Member initialPlayer) {
        super(abbreviation);
        started = false;
        subsNeeded = 0;
        numDraft = draft;

        players = new ArrayList<>();
        subs = new ArrayList<>();
        DraftPlayer newPlayer = new DraftPlayer(initialPlayer);
        players.add(newPlayer);

        draftRole = getRole(getSection());
        draftChat = getChannel(getPrefix() + "-draft-chat-" + draft);
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
     */
    private void sendReport() {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Draft Confirmation");
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
            Member currPlayer = getPlayers().get(i).getInfo();

            queue.append(currPlayer.getAsMention());
            logList.append(currPlayer.getAsMention()).append(" ");

            if (i == capt1 || i == capt2) {
                queue.append(" (captain)");
            }
            queue.append("\n");
        }
        queue.append(getPlayers().get(size - 1).getInfo().getAsMention());

        eb.addField("Players", queue.toString(), false);
        eb.addField("Notice", "Go to " + getDraftChannel().getAsMention()
                + " to begin \nthe draft.", false);

        logList.deleteCharAt(logList.length() - 1);
        log("A draft was started with " + logList + ".");
        sendEmbed(eb);
    }

    /**
     * Formats a ping request for gathering players.
     * @return the request.
     */
    private String newRequest() {
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
     * @param player the player to check for.
     * @param lst a draft's list of players.
     * @return the index of the player within the draft's list of players.
     *         -1 if they are not in the draft yet.
     */
    private int draftContains(Member player, List<DraftPlayer> lst) {
        for (int i = 0; i < lst.size(); i++) {
            Member currPlayer = lst.get(i).getInfo();
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
        Member potentialPlayer = bc.getMember();
        // this block prevents repeated players in the initial draft start
        // comment/uncomment as needed
        if (draftContains(potentialPlayer, getPlayers()) != -1) {
            return;
        }

        DraftPlayer newPlayer = new DraftPlayer(potentialPlayer);
        getPlayers().add(newPlayer);
        bc.editMessage(newRequest()).queue();

        if (getPlayers().size() == 8) {
            disableButton(bc, "Draft queue full.");
            toggleDraft();
            sendReport();
        }
    }

    /**
     * Requests a sub for a draft, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void requestSub(ButtonClickEvent bc) {
        Member potentialPlayer = bc.getMember();
        int playerIndex = draftContains(potentialPlayer, getPlayers());
        int subIndex = draftContains(potentialPlayer, getSubs());
        boolean notInDraft = playerIndex == -1 && subIndex == -1;

        DraftPlayer convertedSub;
        if (!inProgress() || notInDraft) {
            return;
        } else if (playerIndex != -1) {
            convertedSub = getPlayers().get(playerIndex);
        } else {
            convertedSub = getSubs().get(subIndex);
        }

        if (convertedSub.getPings() > 0) {
            sendMessage("Stop spamming "
                    + convertedSub.getInfo().getAsMention() + "!");
            return;
        }

        subsNeeded++;

        getPlayers().remove(convertedSub);
        subs.add(convertedSub);
        bc.editMessage(newRequest() + " (subs needed)").queue();

        String update =
                getDraftRole().getAsMention() + " sub needed "
                        + "(search drafts above).";
        sendMessage(update);
        convertedSub.incrementPings();
    }

    /**
     * Adds a player to the draft's subs, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void addSub(ButtonClickEvent bc) {
        Member potentialPlayer = bc.getMember();
        boolean inDraft =
                !(draftContains(potentialPlayer, getPlayers()) == -1
                && draftContains(potentialPlayer, getSubs()) == -1);

        if (subsNeeded == 0 || inDraft) {
            return;
        }

        subsNeeded--;

        DraftPlayer subPlayer = new DraftPlayer(potentialPlayer);
        subs.add(subPlayer);
        if (subsNeeded == 0) {
            bc.editMessage(newRequest()).queue();
        }

        String update =
                subPlayer.getInfo().getAsMention() + " will be subbing for "
                        +"the draft in " + getDraftChannel().getAsMention() + ".";
        sendMessage(update);
    }

    /**
     * Removes a player from the draft, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void removePlayer(ButtonClickEvent bc) {
        Member player = bc.getMember();
        int foundIndex = draftContains(player, getPlayers());
        boolean notInDraft = draftContains(player, getPlayers()) == -1;

        if (inProgress() || notInDraft) {
            return;
        }

        getPlayers().remove(foundIndex);
        bc.editMessage(newRequest()).queue();
    }

    /**
     * Runs the draft start command.
     * @param cmd the formal name of the command.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(String cmd, List<OptionMapping> args) {
        String joinID = "join" + getPrefix().toUpperCase();
        String requestSubID = "requestSub" + getPrefix().toUpperCase();
        String subID = "sub" + getPrefix().toUpperCase();
        String leaveID = "leave" + getPrefix().toUpperCase();

        joinID += numDraft;
        requestSubID += numDraft;
        subID += numDraft;
        leaveID += numDraft;

        String caption = getDraftRole().getAsMention() + " +7";
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add(new BuiltButton(joinID, "Join Draft", 0).getButton());
        buttons.add(new BuiltButton(requestSubID, "Request Sub", 0).getButton());
        buttons.add(new BuiltButton(subID, "Join As Sub", 1).getButton());
        buttons.add(new BuiltButton(leaveID, "Leave", 3).getButton());

        sendButtons(caption, buttons);
        log("A " + getPrefix().toUpperCase() + " draft has been requested.");
    }
}
