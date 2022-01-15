package bot;

import bot.Engine.Add;
import bot.Engine.Drafts.Draft;
import bot.Engine.Drafts.MapGenerator;
import bot.Engine.Drafts.Log;
import bot.Engine.Drafts.Undo;
import bot.Engine.Graduate;
import bot.Tools.ArrayHeapMinPQ;
import bot.Tools.FileHandler;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    February 17, 2021
 * Project: LaunchPoint Bot
 * Module:  Events.java
 * Purpose: Builds the bot by processing commands and analyzing user input.
 */
public class Events extends ListenerAdapter {

    /** The Discord server's current state. */
    public static Guild SERVER;

    /** The original interaction engaged by a user's command. */
    public static InteractionHook INTERACTION;

    /** Fields which determine the maximum number of drafts.  */
    private final static int MAX_LP_DRAFTS = 4;
    private final static int MAX_IO_DRAFTS = 2;

    /** Fields for storing drafts. */
    private List<Draft> lpDrafts;
    private List<Draft> ioDrafts;

    /** Fields for storing queued draft positions. */
    private ArrayHeapMinPQ<Integer> lpQueue;
    private ArrayHeapMinPQ<Integer> ioQueue;

    /**
     * Checks whether a part of an input string can be found
     * in a list of strings.
     * @param input the input string to compare.
     * @param lst the list of strings to search.
     * @return True if the input string was in the list.
     *         False otherwise.
     */
    private boolean isSimilar(String input, String[] lst) {
        for (String item : lst) {
            if (input.contains(item)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Defers the reply of a slash command, if necessary.
     * @param sc the slash command to analyze.
     */
    private void deferReplyIfNeeded(SlashCommandEvent sc) {
        String[] nonDeferCmds = {"startdraft"};
        if (!isSimilar(sc.getSubcommandName(), nonDeferCmds)) {
            sc.deferReply().queue();
        }
    }

    /**
     * Checks if the game set parameters make sense.
     * @param args the parameters to analyze.
     * @return True if there were less wins than total games.
     *         False otherwise.
     */
    private boolean gamesPlayedValid(List<OptionMapping> args) {
        int gamesPlayed = (int) args.get(0).getAsLong();
        int gamesWon = (int) args.get(1).getAsLong();
        if (gamesPlayed < gamesWon) {
            INTERACTION.sendMessage("Total games won cannot go beyond the set. "
                    + "Try again.").queue();
            return false;
        } else if (gamesPlayed < 0 || gamesWon < 0) {
            INTERACTION.sendMessage(
                    "The amount games played can't be negative?").queue();
            return false;
        } else if (gamesPlayed > 19) {
            INTERACTION.sendMessage(
                    "Are you sure that's how many games were played?").queue();
            return false;
        }

        return true;
    }

    /**
     * Checks whether the command user has permission to use the
     * command or not.
     * @param cmd the formal name of the command.
     * @param author the user of the command.
     * @return True if the command is not a staff command.
     *         False otherwise.
     */
    private boolean isStaffCommand(String cmd, Member author) {
        String[] staffCmds = {"cycle", "sub", "undo", "add", "grad"};
        Role staffRole = SERVER.getRolesByName("Staff", true).get(0);

        if (!author.getRoles().contains(staffRole)) {
            return isSimilar(cmd, staffCmds);
        }

        return false;
    }

    /**
     * Checks whether a command can be used in the interaction's channel
     * or not.
     * @param cmd the formal name of the command.
     * @return True if the command can be used in the channel.
     *         False otherwise.
     */
    private boolean wrongChannelUsed(String cmd) {
        String entryChannel = SERVER.getTextChannelsByName(
                "mit-entry-confirmation", false).get(0).getName();
        String lpDraftChannel = SERVER.getTextChannelsByName(
                "lp-looking-for-draft", false).get(0).getName();
        String lpReportsChannel = SERVER.getTextChannelsByName(
                "lp-staff-match-report", false).get(0).getName();
        String ioDraftChannel = SERVER.getTextChannelsByName(
                "lp-looking-for-draft", false).get(0).getName();
        String ioReportsChannel = SERVER.getTextChannelsByName(
                "io-staff-match-report", false).get(0).getName();
        String testChannel = SERVER.getTextChannelsByName(
                "bot-testing", false).get(0).getName();

        String channel = INTERACTION.getInteraction().getTextChannel().getName();
        boolean isEntryChannel = cmd.contains("add")
                && channel.equals(entryChannel);
        boolean isDraftChannel = cmd.contains("startdraft")
                && channel.equals(lpDraftChannel) || channel.equals(ioDraftChannel);
        boolean isReportsChannel = (cmd.contains("cycle") || cmd.contains("sub"))
                && channel.equals(lpReportsChannel) || channel.equals(ioReportsChannel);
        boolean isTestChannel = channel.equals(testChannel);

        return !(isEntryChannel || isDraftChannel || isReportsChannel
                || isTestChannel);
    }

    /**
     * Prints troubleshooting information.
     */
    private void printTroubleshootString() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Troubleshooting");
        eb.setColor(Color.BLUE);
        eb.addField("Manual Draft Reporting",
                "If a match report is giving you an error message, \n"
                        + "it is most likely due to a row in the spreadsheet \n"
                        + "missing information. For example, one common \n"
                        + "problem is presetting or overextending the \n"
                        + "formulas past the bottommost row.",
                false);
        eb.addField("Entering and Graduating Players",
                "If the role for a player isn't showing up or seemingly \n"
                        + "isn't being added, try *refreshing the roles* by opening \n"
                        + "`Server Settings > User Management > Members`. \n"
                        + "A second layer of refreshing can be done by \n"
                        + "searching for a player's name in `... > Members`. \n"
                        + "The roles should exist; this is currently a bug within \n"
                        + "Discord.",
                false);

        INTERACTION.sendMessageEmbeds(eb.build()).queue();
    }

    /**
     * Processes a draft, when possible.
     * @param prefix the prefix of the command.
     * @param author the user who ran the command.
     * @param ongoingDrafts list of ongoing drafts.
     * @param queue the
     */
    private void processDraft(String prefix, Member author,
                              List<Draft> ongoingDrafts,
                              ArrayHeapMinPQ<Integer> queue) {
        if (queue.size() == 0) {
            INTERACTION.getInteraction().reply(
                    "Wait until a draft has finished!").queue();
        } else {
            Draft newDraft =
                    new Draft(queue.removeSmallest(), prefix, author);

            ongoingDrafts.add(newDraft);
            newDraft.runCmd(null, null);
        }
    }

    /**
     * Processes drafts, when possible.
     * @param prefix the prefix of the command.
     * @param author the user who ran the command.
     */
    private void processDrafts(String prefix, Member author) {
        switch (prefix) {
            case "lp":
                if (lpDrafts == null) {
                    lpDrafts = new ArrayList<>();
                }
                if (lpQueue == null) {
                    lpQueue = new ArrayHeapMinPQ<>();
                    for (int i = 1; i <= MAX_LP_DRAFTS; i++) {
                        lpQueue.add(i, i);
                    }
                }

                processDraft(prefix, author, lpDrafts, lpQueue);
                break;
            case "io":
                if (ioDrafts == null) {
                    ioDrafts = new ArrayList<>();
                }
                if (ioQueue == null) {
                    ioQueue = new ArrayHeapMinPQ<>();
                    for (int i = 1; i <= MAX_IO_DRAFTS; i++) {
                        ioQueue.add(i, i);
                    }
                }

                processDraft(prefix, author, ioDrafts, ioQueue);
                break;
        }
    }

    /**
     * Find the undo file to load.
     * @param cmd the formal name of the command.
     * @return said file.
     */
    private FileHandler findSave(String cmd) {
        if (cmd.startsWith("lp")) {
            return new FileHandler("loadLP.txt");
        } else {
            return new FileHandler("loadIO.txt");
        }
    }

    /**
     * Structures a user into a mentionable ping, ignoring nicknames.
     * @param om an argument from a command.
     * @return the formatted ping.
     */
    private String mentionableFor(OptionMapping om) {
        String id = om.getAsMember().getId();
        return String.format("<@%s>", id);
    }

    /**
     * Saves a cycle command to an undo file.
     * @param cmd the formal name of the command.
     * @param args the arguments of the command, if they exist.
     */
    private void saveCycleCall(String cmd, List<OptionMapping> args) {
        List<OptionMapping> userArgs = args.subList(2, args.size());
        StringBuilder contents = new StringBuilder();
        int lastIndex = userArgs.size() - 1;

        contents.append(cmd).append(" ")
                .append(args.get(0).getAsString()).append(" ")
                .append(args.get(1).getAsString()).append(" ");
        for (int i = 0; i < lastIndex; i++) {
            contents.append(mentionableFor(userArgs.get(i))).append(" ");
        }
        contents.append(mentionableFor(userArgs.get(lastIndex)));

        FileHandler save = findSave(cmd);
        save.writeContents(contents.toString());
    }

    /**
     * Parses through which of the bot's commands to run.
     * @param author the user who ran the command.
     * @param prefix the prefix of the formal command name.
     * @param cmd the formal name of the command.
     * @param args the arguments of the command, if they exist.
     */
    private void parseCommands(Member author, String prefix, String cmd,
                               List<OptionMapping> args) {
        if (isStaffCommand(cmd, author) || wrongChannelUsed(cmd)) {
            if (!INTERACTION.getInteraction().isAcknowledged()) {
                INTERACTION.getInteraction().deferReply().queue();
            }
            INTERACTION.sendMessage(
                    "You do not have permission to use this command here.").queue();
        } else if (prefix.equals("mit")) {
            switch (cmd.substring(3)) {
                case "status":
                    INTERACTION.sendMessageFormat(
                            "The bot is online. Welcome, %s.",
                            author.getEffectiveName()).queue();
                    break;
                case "help":
                    printTroubleshootString();
                    break;
                case "profile":
                    INTERACTION.sendMessage(
                            "This command has not been implemented yet.").queue();
                    break;
                case "genmaps":
                    MapGenerator maps = new MapGenerator();
                    maps.runCmd(cmd, args);
                    break;
            }
        } else {
            switch (cmd.substring(2)) {
                case "add":
                    Add newcomer = new Add(prefix);
                    newcomer.runCmd(cmd, args);
                    break;
                case "grad":
                    Graduate grad = new Graduate(prefix);
                    grad.runCmd(cmd, args);
                    break;
                case "startdraft":
                    processDrafts(prefix, author);
                    break;
                case "cycle":
                case "sub":
                    if (gamesPlayedValid(args)) {
                        Log log = new Log(prefix);
                        log.runCmd(cmd, args);

                        saveCycleCall(cmd, args);
                    }
                    break;
                case "undo":
                    Undo undo = new Undo(prefix);
                    undo.runCmd(cmd, null);

                    FileHandler save = findSave(cmd);
                    save.writeContents("REDACTED");
                    break;
            }
        }
    }

    /**
     * Runs one of the bot's commands.
     * @param sc a slash command to analyze.
     */
    @Override
    public void onSlashCommand(SlashCommandEvent sc) {
        deferReplyIfNeeded(sc);

        Member author = sc.getMember();
        String cmd = sc.getName();
        String subGroup = sc.getSubcommandGroup();
        String subCmd = sc.getSubcommandName();
        List<OptionMapping> args = sc.getOptions();
        if (subGroup == null) {
            subGroup = "";
        }
        if (subCmd == null) {
            subCmd = "";
        }

        SERVER = sc.getGuild();
        INTERACTION = sc.getHook();

        // delete once the bot is ready
        if (sc.getMember().getRoles() == null || !sc.getMember().getRoles().contains(
                SERVER.getRolesByName("Staff", true).get(0))) {
            INTERACTION.sendMessage("The bot is not ready to use yet.").queue();
        }

        String formalCmd = cmd + subGroup + subCmd;
        parseCommands(author, cmd, formalCmd, args);
    }

    /**
     * Checks any button clicks.
     * @param bc a button click to analyze.
     */
    @Override
    public void onButtonClick(ButtonClickEvent bc) {
        String btnName = bc.getButton().getId();
        int indexOfNum = btnName.length() - 1;

        List<Draft> drafts;
        ArrayHeapMinPQ<Integer> queue;
        String suffix = btnName.substring(indexOfNum - 2, indexOfNum);
        int numButton = Integer.parseInt(btnName.substring(indexOfNum));
        switch (suffix) {
            case "LP":
                drafts = lpDrafts;
                queue = lpQueue;
                break;
            default:
                drafts = ioDrafts;
                queue = ioQueue;
                break;
        }

        switch (btnName.substring(0, indexOfNum - 2)) {
            case "join":
                drafts.get(numButton - 1).attemptDraft(bc);
                break;
            case "leave":
                drafts.get(numButton - 1).removePlayer(bc);
                break;
            case "requestSub":
                drafts.get(numButton - 1).requestSub(bc);
                break;
            case "sub":
                drafts.get(numButton - 1).addSub(bc);
                break;
            case "end":
                if (drafts.get(numButton - 1).endDraft(bc)) {
                    drafts.remove(drafts.get(numButton - 1));
                    queue.add(numButton, numButton);
                }
                break;

        }
    }
}
