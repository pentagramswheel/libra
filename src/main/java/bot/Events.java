package bot;

import bot.Engine.Add;
import bot.Engine.Drafts.*;
import bot.Engine.Graduate;
import bot.Tools.ArrayHeapMinPQ;
import bot.Tools.FileHandler;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * @author  Wil Aquino
 * Date:    February 17, 2021
 * Project: Libra
 * Module:  Events.java
 * Purpose: Builds the bot by processing commands
 *          and analyzing user input.
 */
public class Events extends ListenerAdapter {

    /** Fields which determine the maximum number of drafts.  */
    private final static int MAX_LP_DRAFTS = 4;
    private final static int MAX_IO_DRAFTS = 2;

    /** Fields for storing drafts. */
    TreeMap<Integer, Draft> lpDrafts;
    TreeMap<Integer, Draft> ioDrafts;

    /**Stores How many times End Button has been clicked**/
    HashMap<Draft, Integer> numEndClicked = new HashMap<>();

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
     * Checks if the game set parameters make sense.
     * @param sc the user's inputted command.
     * @return True if there were less wins than total games.
     *         False otherwise.
     */
    private boolean gamesPlayedValid(SlashCommandEvent sc) {
        InteractionHook interaction = sc.getHook();
        List<OptionMapping> args = sc.getOptions();

        int gamesPlayed = (int) args.get(0).getAsLong();
        int gamesWon = (int) args.get(1).getAsLong();

        if (gamesPlayed < gamesWon) {
            interaction.sendMessage("Total games won cannot go beyond the set. "
                    + "Try again.").queue();
            return false;
        } else if (gamesPlayed < 0 || gamesWon < 0) {
            interaction.sendMessage(
                    "The amount games played can't be negative?").queue();
            return false;
        } else if (gamesPlayed > 19) {
            interaction.sendMessage(
                    "Are you sure that's how many games were played?").queue();
            return false;
        }

        return true;
    }

    /**
     * Checks whether the command user has permission to use the
     * command or not.
     * @param sc the user's inputted command.
     * @return True if the command is not a staff command.
     *         False otherwise.
     */
    private boolean isStaffCommand(SlashCommandEvent sc) {
        String[] staffCmds = {"cycle", "sub", "undo", "add", "grad"};

        try {
            Guild server = sc.getGuild();
            if (server == null) {
                throw new NullPointerException("Role not found.");
            }

            Member author = sc.getMember();
            String subCmd = sc.getSubcommandName();
            Role staffRole = server.getRolesByName("Staff", true).get(0);

            if (author != null && !author.getRoles().contains(staffRole)) {
                return isSimilar(subCmd, staffCmds);
            }
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("Roles could not be found.");
        }

        return false;
    }

    /**
     * Checks whether a command can be used in the interaction's channel
     * or not.
     * @param sc the user's inputted command.
     * @return True if the command can be used in the channel.
     *         False otherwise.
     */
    private boolean wrongChannelUsed(SlashCommandEvent sc) {
        Guild server = sc.getGuild();
        String subCmd = sc.getSubcommandName();

        String entryChannel = server.getTextChannelsByName(
                "mit-entry-confirmation", false).get(0).getName();
        String lpDraftChannel = server.getTextChannelsByName(
                "lp-looking-for-draft", false).get(0).getName();
        String lpReportsChannel = server.getTextChannelsByName(
                "lp-staff-match-report", false).get(0).getName();
        String ioDraftChannel = server.getTextChannelsByName(
                "lp-looking-for-draft", false).get(0).getName();
        String ioReportsChannel = server.getTextChannelsByName(
                "io-staff-match-report", false).get(0).getName();
        String testChannel = server.getTextChannelsByName(
                "bot-testing", false).get(0).getName();

        String channel = sc.getTextChannel().getName();
        boolean isEntryChannel = subCmd.equals("add")
                && channel.equals(entryChannel);
        boolean isDraftChannel = subCmd.equals("startdraft")
                && channel.equals(lpDraftChannel) || channel.equals(ioDraftChannel);
        boolean isReportsChannel = (subCmd.equals("cycle") || subCmd.equals("sub"))
                && channel.equals(lpReportsChannel) || channel.equals(ioReportsChannel);
        boolean isTestChannel = channel.equals(testChannel);

        return !(isEntryChannel || isDraftChannel || isReportsChannel
                || isTestChannel);
    }

    /**
     * Prints troubleshooting information.
     * @param sc the user's inputted command.
     */
    private void printTroubleshootString(SlashCommandEvent sc) {
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

        sc.getHook().editOriginalEmbeds(eb.build()).queue();
    }

    /**
     * Processes a draft, when possible.
     * @param sc the user's inputted command.
     * @param prefix the prefix of the command.
     * @param author the user who ran the command.
     * @param ongoingDrafts map of ongoing drafts.
     * @param queue the queue of drafts.
     */
    private void processDraft(SlashCommandEvent sc, String prefix, Member author,
                              TreeMap<Integer, Draft> ongoingDrafts,
                              ArrayHeapMinPQ<Integer> queue) {
        if (queue.size() == 0) {
            sc.reply("Wait until a draft has finished!").queue();
        } else {
            int draftButton = queue.removeSmallest();
            Draft newDraft =
                    new Draft(sc, draftButton, prefix, author);
            numEndClicked.put(newDraft, 0);
            ongoingDrafts.put(draftButton, newDraft);
            newDraft.runCmd(sc);
        }
    }

    /**
     * Processes drafts, when possible.
     * @param prefix the prefix of the command.
     * @param author the user who ran the command.
     */
    private void processDrafts(SlashCommandEvent sc, String prefix, Member author) {
        switch (prefix) {
            case "lp":
                if (lpDrafts == null) {
                    lpDrafts = new TreeMap<>();
                }
                if (lpQueue == null) {
                    lpQueue = new ArrayHeapMinPQ<>();
                    for (int i = 1; i <= MAX_LP_DRAFTS; i++) {
                        lpQueue.add(i, i);
                    }
                }

                processDraft(sc, prefix, author, lpDrafts, lpQueue);
                break;
            case "io":
                if (ioDrafts == null) {
                    ioDrafts = new TreeMap<>();
                }
                if (ioQueue == null) {
                    ioQueue = new ArrayHeapMinPQ<>();
                    for (int i = 1; i <= MAX_IO_DRAFTS; i++) {
                        ioQueue.add(i, i);
                    }
                }

                processDraft(sc, prefix, author, ioDrafts, ioQueue);
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
     * Runs a general command.
     * @param sc the slash command to analyze.
     */
    private void parseGeneralCommands(SlashCommandEvent sc) {
        sc.deferReply().queue();

        Member author = sc.getMember();
        String subGroup = sc.getSubcommandGroup();
        String subCmd = sc.getSubcommandName();
        if (subGroup == null) {
            subGroup = "";
        }
        if (subCmd == null) {
            subCmd = "";
        }

        switch (subCmd) {
            case "status":
                sc.getHook().sendMessageFormat(
                        "The bot is online. Welcome, %s.",
                        author.getEffectiveName()).queue();
                break;
            case "help":
                printTroubleshootString(sc);
                break;
            case "profile":
                sc.getHook().sendMessage(
                        "This command has not been implemented yet.").queue();
                break;
            case "genmaps":
                MapGenerator maps = new MapGenerator();
                maps.runCmd(sc);
                break;
        }
    }

    /**
     * Runs a section command.
     * @param sc the slash command to analyze.
     */
    private void parseSectionCommands(SlashCommandEvent sc) {
        Member author = sc.getMember();
        String prefix = sc.getName();
        String subGroup = sc.getSubcommandGroup();
        String subCmd = sc.getSubcommandName();
        List<OptionMapping> args = sc.getOptions();
        if (subGroup == null) {
            subGroup = "";
        }
        if (subCmd == null) {
            subCmd = "";
        }

        switch (subCmd) {
            case "add":
                Add newcomer = new Add(prefix);
                newcomer.runCmd(sc);
                break;
            case "grad":
                Graduate grad = new Graduate(prefix);
                grad.runCmd(sc);
                break;
            case "startdraft":
                processDrafts(sc, prefix, author);
                break;
            case "cycle":
            case "sub":
                if (gamesPlayedValid(sc)) {
                    ManualLog log = new ManualLog(prefix);
                    log.runCmd(sc);

                    saveCycleCall(prefix + subCmd, args);
                }
                break;
            case "undo":
                Undo undo = new Undo(prefix);
                undo.runCmd(sc);

                FileHandler save = findSave(prefix);
                save.writeContents("REDACTED");
                break;
        }
    }

    /**
     * Runs one of the bot's commands.
     * @param sc a slash command to analyze.
     */
    @Override
    public void onSlashCommand(SlashCommandEvent sc) {
        String cmdPrefix = sc.getName();
        if (isStaffCommand(sc)
                || wrongChannelUsed(sc)) {
            sc.reply("You do not have permission to use this command here.").queue();
        }

        switch (cmdPrefix) {
            case "mit":
                parseGeneralCommands(sc);
                break;
            case "lp":
            case "io":
                parseSectionCommands(sc);
                break;
        }
    }

    /**
     * Checks for any button clicks.
     * @param bc a button click to analyze.
     */

    @Override
    public void onButtonClick(ButtonClickEvent bc) {
        String btnName = bc.getButton().getId();
        int indexOfNum = btnName.length() - 1;

        TreeMap<Integer, Draft> drafts;
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

        Draft currDraft = drafts.get(numButton);
        DraftProcess currProcess = drafts.get(numButton).getProcess();
        switch (btnName.substring(0, indexOfNum - 2)) {
            case "join":
                numEndClicked.replace(currDraft,0);
                currDraft.attemptDraft(bc);
                break;
            case "leave":
                numEndClicked.replace(currDraft,0);
                currDraft.removePlayer(bc);
                break;
            case "requestSub":
                numEndClicked.replace(currDraft,0);
                currDraft.requestSub(bc);
                break;
            case "sub":
                numEndClicked.replace(currDraft,0);
                currDraft.addSub(bc);
                break;
            case "end":
                //if(differentperson){
                    int timesClicked = numEndClicked.get(currDraft);
                    numEndClicked.replace(currDraft, timesClicked+1);
                    if(numEndClicked.get(currDraft) >= 3){
                        //insert autolog here
                        currDraft.sendReply(bc, "Ended the draft.", false);
                        if (currDraft.hasEnded(bc)) {
                            drafts.remove(numButton);
                            queue.add(numButton, numButton);
                        }
                    }
                //}
                break;
            case "resetTeams":
                numEndClicked.replace(currDraft,0);
                currProcess.resetTeams(bc);
                System.out.println("Draft teams reset by " + bc.getMember().getId());
                break;
            case "add1":
                numEndClicked.replace(currDraft,0);
                currProcess.addPointToTeam(bc, bc.getMember());
                break;
            case "subtract1":
                numEndClicked.replace(currDraft,0);
                currProcess.subtractPointFromTeam(bc, bc.getMember());
                break;
        }
    }

    /**
     * Checks for any menu selections.
     * @param sm a menu selection to analyze.
     */
    @Override
    public void onSelectionMenu(SelectionMenuEvent sm) {
        String menuName = sm.getComponent().getId();
        int indexOfNum = menuName.length() - 1;

        TreeMap<Integer, Draft> drafts;
        String suffix = menuName.substring(indexOfNum - 2, indexOfNum);
        int numDraft = Integer.parseInt(menuName.substring(indexOfNum));
        switch (suffix) {
            case "LP":
                drafts = lpDrafts;
                break;
            default:
                drafts = ioDrafts;
                break;
        }

        DraftProcess currProcess = drafts.get(numDraft).getProcess();
        if (menuName.substring(0, indexOfNum - 2).equals("teamSelection")) {
            String playerID = sm.getInteraction().getSelectedOptions().get(0).getLabel();
            Member player = sm.getGuild().retrieveMemberById(playerID).complete();

            System.out.println("player " + playerID + " was added to " + sm.getMember().getId() + "'s Team");
            currProcess.addPlayerToTeam(sm, sm.getMember(), player);

            // replace with this block later
//            String playerID = sm.getInteraction().getSelectedOptions().get(0).getValue();
//            Member player = sm.getGuild().retrieveMemberById(playerID).complete();
//            currProcess.addPlayerToTeam(sm, sm.getMember(), player);
        }
    }
}
