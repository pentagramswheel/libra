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
            Member currPlayer = getPlayers().get(i).getAsMember();

            queue.append(currPlayer.getAsMention());
            logList.append(currPlayer.getAsMention()).append(" ");

            if (i == capt1 || i == capt2) {
                queue.append(" (captain)");
            }
            queue.append("\n");
        }
        queue.append(getPlayers().get(size - 1).getAsMember().getAsMention());

        eb.addField("Players", queue.toString(), false);
        String notice =
                "Go to " + getDraftChannel().getAsMention() + " to begin\n"
                + "the draft. Use the interface above\n"
                + "request subs as needed, as well as\n"
                + "end the draft to allow others to\n"
                + "queue more drafts.";
        eb.addField("Notice", notice, false);

        logList.deleteCharAt(logList.length() - 1);
        log("A draft was started with " + logList + ".");
        sendEmbed(eb);
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
        Member potentialPlayer = bc.getMember();
        // this block prevents repeated players in the initial draft start
        // comment/uncomment as needed
//        if (draftContains(potentialPlayer, getPlayers()) != -1) {
//            return;
//        }

        DraftPlayer newPlayer = new DraftPlayer(potentialPlayer);
        getPlayers().add(newPlayer);
        bc.editMessage(newPing()).queue();

        if (getPlayers().size() == 8) {
            toggleDraft();
            sendReport();

            ArrayList<Button> buttons = new ArrayList<>();
            String idSuffix = getPrefix().toUpperCase() + getNumDraft();
            buttons.add(Buttons.joinDraft(idSuffix)
                    .withLabel("Draft queue full.").asDisabled());
            buttons.add(Buttons.requestSub(idSuffix));
            buttons.add(Buttons.joinAsSub(idSuffix));
            buttons.add(Buttons.end(idSuffix));

            editButtons(bc, buttons);
        }
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
            bc.reply("You are not part of this draft!")
                    .setEphemeral(true).queue();
            return;
        } else if (convertedSub.getPings() > 0) {
            bc.reply("Stop spamming " + convertedSub.getAsMember().getAsMention()
                    + "!").setEphemeral(true).queue();
            return;
        }

        subsNeeded++;

        getPlayers().remove(convertedSub);
        getSubs().remove(convertedSub);
        subs.add(convertedSub);
        bc.editMessage(newPing() + "   [ " + subsNeeded + " sub(s) needed ]").queue();

        String update =
                getDraftRole().getAsMention() + " sub needed "
                        + "(search the drafts above).";
        sendMessage(update);
        convertedSub.incrementPings();
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
            bc.reply("Why would you sub for yourself?")
                    .setEphemeral(true).queue();
            return;
        } else if (subsNeeded == 0) {
            bc.reply("This draft hasn't requested any subs yet.")
                    .setEphemeral(true).queue();
            return;
        }

        subsNeeded--;

        DraftPlayer subPlayer = new DraftPlayer(player);
        subs.add(subPlayer);
        if (subsNeeded == 0) {
            bc.editMessage(newPing()).queue();
        } else {
            bc.editMessage(newPing() + "   [ " + subsNeeded + " sub(s) needed ]").queue();
        }

        String update =
                subPlayer.getAsMember().getAsMention() + " will be subbing for "
                        +"the draft in " + getDraftChannel().getAsMention() + ".";
        sendMessage(update);
    }

    /**
     * Removes a player from the draft, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void removePlayer(ButtonClickEvent bc) {
        Member player = bc.getMember();
        int playerIndex = draftContains(player, getPlayers());

        if (playerIndex == -1) {
            bc.reply("You're not in this draft!")
                    .setEphemeral(true).queue();
            return;
        }

        getPlayers().remove(playerIndex);
        bc.editMessage(newPing()).queue();
    }

    /**
     * Ends the draft is it is completed, via a button.
     * @param bc a button click to analyze.
     */
    public boolean endDraft(ButtonClickEvent bc) {
        if (!inProgress()) {
            editButton(bc, Buttons.end(getPrefix() + getNumDraft())
                    .withLabel("Draft Complete").asDisabled());
            return true;
        } else {
            bc.reply("This draft hasn't finished yet.").setEphemeral(true).queue();
            return false;
        }
    }

    /**
     * Runs the draft start command.
     * @param cmd the formal name of the command.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(String cmd, List<OptionMapping> args) {
        ArrayList<Button> buttons = new ArrayList<>();
        String idSuffix = getPrefix().toUpperCase() + getNumDraft();
        buttons.add(Buttons.joinDraft(idSuffix));
        buttons.add(Buttons.leave(idSuffix));

        String caption = getDraftRole().getAsMention() + " +7";
        sendButtons(caption, buttons);
        log("A " + getPrefix().toUpperCase() + " draft has been requested.");
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
