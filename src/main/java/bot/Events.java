package bot;

import bot.Engine.*;
import bot.Engine.Cycles.*;
import bot.Engine.Drafts.*;
import bot.Tools.ArrayHeapMinPQ;
import bot.Tools.FileHandler;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * @author  Wil Aquino, Turtle#1504
 * Date:    February 17, 2021
 * Project: Libra
 * Module:  Events.java
 * Purpose: Builds the bot by processing commands
 *          and analyzing user input.
 */
public class Events extends ListenerAdapter {

    /** A random number generator for the bot to use. */
    private final Random numGenerator;

    /** Fields which determine the maximum number of drafts. */
    private final static int MAX_LP_DRAFTS = 4;
    private final static int MAX_IO_DRAFTS = 2;

    /** Fields for storing numbers to drafts. */
    private TreeMap<Integer, Draft> lpDrafts;
    private TreeMap<Integer, Draft> ioDrafts;

    /** Fields for storing queued draft numbers. */
    private ArrayHeapMinPQ<Integer> lpQueue;
    private ArrayHeapMinPQ<Integer> ioQueue;

    /**
     * Loads the bot's event listener with a random number generator
     * @param generator the random number generator.
     */
    public Events(Random generator) {
        numGenerator = generator;
    }

    /**
     * Checks if the game set parameters make sense.
     * @param sc the user's inputted command.
     * @return True if there were less wins than total games.
     *         False otherwise.
     */
    private boolean gamesPlayedValid(SlashCommandEvent sc) {
        List<OptionMapping> args = sc.getOptions();

        int gamesPlayed = (int) args.get(0).getAsLong();
        int gamesWon = (int) args.get(1).getAsLong();

        if (gamesPlayed < gamesWon) {
            sc.reply("Total games won cannot go beyond the set. Try again.")
                    .setEphemeral(true).queue();
            return false;
        } else if (gamesPlayed < 0 || gamesWon < 0) {
            sc.reply("The amount games played cannot be negative.")
                    .setEphemeral(true).queue();
            return false;
        } else if (gamesPlayed > 19) {
            sc.reply("Are you sure that's how many games were played?")
                    .setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    /**
     * Checks if the number of maps needed for a map generation
     * makes sense.
     * @param sc the user's inputted command.
     * @return True if the number of maps was between 1 and 9.
     *         False otherwise.
     */
    private boolean mapsNeededValid(SlashCommandEvent sc) {
        List<OptionMapping> args = sc.getOptions();

        int numMaps = (int) args.get(0).getAsLong();
        if (numMaps > 7) {
            sc.reply("Too many maps requested. The set would be too long!")
                    .setEphemeral(true).queue();
            return false;
        } else if (numMaps < 1) {
            sc.reply("Why would you request zero or less maps?")
                    .setEphemeral(true).queue();
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
        String[] staffCmds = {"forceend", "cycle", "sub", "undo",
                "add", "grad", "award", "cyclescalc"};

        try {
            Guild server = sc.getGuild();
            if (server == null) {
                throw new NullPointerException("Role not found.");
            }

            Member author = sc.getMember();
            String subCmd = sc.getSubcommandName();
            Role staffRole = server.getRolesByName("Staff", true).get(0);

            if (author != null && !author.getRoles().contains(staffRole)) {
                for (String cmd : staffCmds) {
                    if (subCmd.equals(cmd)) {
                        return true;
                    }
                }
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
        String prefix = sc.getName();
        String subCmd = sc.getSubcommandName();
        if (prefix.equals("mit") || subCmd.equals("genmaps")) {
            return false;
        }

        String helpdesk = server.getTextChannelsByName(
                "helpdesk", false).get(0).getName();
        String entryChannel = server.getTextChannelsByName(
                "mit-entry-confirmation", false).get(0).getName();
        String lpDraftChannel = server.getTextChannelsByName(
                "lp-looking-for-draft", false).get(0).getName();
        String lpReportsChannel = server.getTextChannelsByName(
                "lp-match-report", false).get(0).getName();
        String ioDraftChannel = server.getTextChannelsByName(
                "io-looking-for-draft", false).get(0).getName();
        String ioReportsChannel = server.getTextChannelsByName(
                "io-match-report", false).get(0).getName();
        String testChannel = server.getTextChannelsByName(
                "bot-testing", false).get(0).getName();

        String channel = sc.getTextChannel().getName();
        boolean isDraftCommand =
                subCmd.equals("startdraft") || subCmd.equals("forcesub") || subCmd.equals("forceend");
        boolean isReportCommand =
                subCmd.equals("cycle") || subCmd.equals("sub") || subCmd.equals("undo");

        boolean isHelpdesk = channel.equals(helpdesk);
        boolean inEntryChannel = (subCmd.equals("add") || subCmd.equals("grad"))
                && channel.equals(entryChannel);
        boolean inLPChannel = prefix.equals("lp") && isDraftCommand
                && channel.equals(lpDraftChannel);
        boolean inIOChannel = prefix.equals("io") && isDraftCommand
                && channel.equals(ioDraftChannel);
        boolean inLPReportsChannel = prefix.equals("lp") && isReportCommand
                && channel.equals(lpReportsChannel);
        boolean inIOReportsChannel = prefix.equals("io") && isReportCommand
                && channel.equals(ioReportsChannel);
        boolean inTestChannel = channel.equals(testChannel);

        return !(isHelpdesk || inEntryChannel || inLPChannel || inIOChannel
                || inLPReportsChannel || inIOReportsChannel
                || inTestChannel);
    }

    /**
     * Prints troubleshooting information.
     * @param sc the user's inputted command.
     */
    private void printTroubleshootString(SlashCommandEvent sc) {
        sc.deferReply().queue();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Troubleshooting");
        eb.setColor(Color.BLUE);
        eb.addField("Manual Draft System [Staff]",
                "If a match report is giving you an error message, \n"
                        + "it is most likely due to a row in the spreadsheet \n"
                        + "missing information. For example, one common \n"
                        + "problem is presetting or overextending the \n"
                        + "formulas past the bottommost row.",
                false);
        eb.addField("Automatic Draft System",
                "(See the FAQ portion of `/mit draftdoc`)",
                false);
        eb.addField("Adding Roles to Players [Staff]",
                "If the role for a player isn't showing up or seemingly \n"
                        + "isn't being added, try *refreshing the roles* by opening \n"
                        + "`Server Settings > User Management > Members`. \n"
                        + "A second layer of refreshing can be done by \n"
                        + "searching for a player's name in `... > Members`. \n"
                        + "Note that it takes ~7 sec/player to add a role.",
                false);

        sc.getHook().editOriginalEmbeds(eb.build()).queue();
    }

    /**
     * Checks whether a player is in another draft or not.
     * @param interaction the user interaction calling this method.
     * @param drafts the list of drafts to check.
     * @return True if they are not found in another draft.
     *         False otherwise.
     */
    private boolean notInAnotherDraft(GenericInteractionCreateEvent interaction,
                                   TreeMap<Integer, Draft> drafts) {
        String playerID = interaction.getMember().getId();
        if (drafts == null) {
            return true;
        }

        for (Draft draft : drafts.values()) {
            if (draft.getPlayers().containsKey(playerID)
                    && draft.getPlayers().get(playerID).isActive()) {
                interaction.reply("You are already in a draft!")
                        .setEphemeral(true).queue();

                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether a draft request expired or not.
     * @param interaction the user interaction calling this method.
     * @param numDraft the number draft to analyze.
     * @param draft the actual draft to analyze.
     * @param drafts the source map of drafts.
     * @param queue the source queue of drafts.
     * @return True if the draft request ran out of time.
     *         False otherwise.
     */
    private boolean draftExpired(GenericInteractionCreateEvent interaction,
                                 int numDraft, Draft draft,
                                 TreeMap<Integer, Draft> drafts,
                                 ArrayHeapMinPQ<Integer> queue) {
        if (draft.timedOut(interaction) && !draft.isInitialized()) {
            drafts.remove(numDraft);
            queue.add(numDraft, numDraft);
            return true;
        }

        return false;
    }

    /**
     * Times out any drafts if possible.
     * @param interaction the user interaction calling this method.
     */
    private void timeoutDrafts(GenericInteractionCreateEvent interaction) {
        if (lpDrafts != null) {
            for (Map.Entry<Integer, Draft> draft : lpDrafts.entrySet()) {
                if (draftExpired(interaction, draft.getKey(), draft.getValue(),
                        lpDrafts, lpQueue)) {
                    break;
                }
            }
        }

        if (ioDrafts != null) {
            for (Map.Entry<Integer, Draft> draft : ioDrafts.entrySet()) {
                if (draftExpired(interaction, draft.getKey(), draft.getValue(),
                        ioDrafts, ioQueue)) {
                    break;
                }
            }
        }
    }

    /**
     * Processes a draft, when possible.
     * @param sc the user's inputted command.
     * @param prefix the prefix of the command.
     * @param author the user who ran the command.
     * @param ongoingDrafts the source map of drafts.
     * @param queue the source queue of drafts.
     */
    private void processDraft(SlashCommandEvent sc, String prefix, Member author,
                              TreeMap<Integer, Draft> ongoingDrafts,
                              ArrayHeapMinPQ<Integer> queue) {
        if (queue.size() == 0) {
            sc.reply("Wait until a draft has finished!").queue();
        } else {
            int draftButton = queue.removeSmallest();
            Draft newDraft =
                    new Draft(sc, draftButton, prefix, author, numGenerator);

            ongoingDrafts.put(draftButton, newDraft);
            newDraft.runCmd(sc);
        }
    }

    /**
     * Processes drafts, when possible.
     * @param sc the user's inputted command.
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
     * Attempts to forcibly sub a player out of
     * a draft.
     * @param sc the user's inputted command.
     * @param drafts the source map of drafts.
     * @param args the arguments of the command.
     */
    private void attemptForceSub(SlashCommandEvent sc,
                                 TreeMap<Integer, Draft> drafts,
                                 List<OptionMapping> args) {
        if (drafts == null) {
            sc.reply("No drafts have been started yet.")
                    .setEphemeral(true).queue();
            return;
        }

        int numDraft = (int) args.get(0).getAsLong();
        Member playerToSub = args.get(1).getAsMember();
        Draft draft = drafts.get(numDraft);

        if (draft == null) {
            sc.reply("That number draft does not exist.")
                    .setEphemeral(true).queue();
        } else {
            draft.forceSub(sc, playerToSub.getId());
        }
    }

    /**
     * Attempts to forcibly end a draft.
     * @param sc the user's inputted command.
     * @param drafts the source map of drafts.
     * @param args the arguments of the command.
     */
    private void attemptForceEnd(SlashCommandEvent sc,
                                 TreeMap<Integer, Draft> drafts,
                                 ArrayHeapMinPQ<Integer> queue,
                                 List<OptionMapping> args) {
        if (drafts == null) {
            sc.reply("No drafts have been started yet.")
                    .setEphemeral(true).queue();
            return;
        }

        int numDraft = (int) args.get(0).getAsLong();
        Draft draft = drafts.get(numDraft);
        if (draft == null) {
            sc.reply("That number draft does not exist.")
                    .setEphemeral(true).queue();
        } else if (draft.forceEnd(sc)) {
            drafts.remove(numDraft);
            queue.add(numDraft, numDraft);
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
                sc.reply("The bot is online. Welcome, "
                        + author.getEffectiveName()).queue();
                break;
            case "help":
                printTroubleshootString(sc);
                break;
            case "profile":
                sc.reply("This command has not been implemented yet.").queue();
                break;
            case "cyclescalc":
                PointsCalculator calculator = new PointsCalculator();
                calculator.runCmd(sc);
                break;
            case "draftdoc":
                String docLink =
                        "https://docs.google.com/document/d/1LoYjd2mqadu5g5D-BMNHfLk9zUouZZPzLWriu-vxCew/edit?usp=sharing";
                sc.reply(docLink).queue();
                break;
        }

        switch (subGroup) {
            case "profile":
                Profile profile = new Profile();
                profile.runCmd(sc);
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

        TreeMap<Integer, Draft> drafts;
        ArrayHeapMinPQ<Integer> queue;
        switch (prefix) {
            case "lp":
                drafts = lpDrafts;
                queue = lpQueue;
                break;
            default:
                drafts = ioDrafts;
                queue = ioQueue;
                break;
        }

        switch (subCmd) {
            case "add":
            case "deny":
                Add newcomer = new Add(prefix);
                newcomer.runCmd(sc);
                break;
            case "grad":
                Graduate grad = new Graduate(prefix);
                grad.runCmd(sc);
                break;
            case "award":
                Award award = new Award(prefix);
                award.runCmd(sc);
                break;
            case "genmaps":
                if (mapsNeededValid(sc)) {
                    MapGenerator maps = new MapGenerator(prefix, numGenerator);
                    maps.runCmd(sc);
                }
                break;
            case "startdraft":
                if (notInAnotherDraft(sc, drafts)) {
                    processDrafts(sc, prefix, author);
                }
                break;
            case "forcesub":
                attemptForceSub(sc, drafts, args);
                break;
            case "forceend":
                attemptForceEnd(sc, drafts, queue, args);
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
    public void onSlashCommand(@NotNull SlashCommandEvent sc) {
        timeoutDrafts(sc);
        if (isStaffCommand(sc) || wrongChannelUsed(sc)) {
            sc.reply("You do not have permission to use this command here.")
                    .setEphemeral(true).queue();
            return;
        }

        String cmdPrefix = sc.getName();
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
    public void onButtonClick(@NotNull ButtonClickEvent bc) {
        timeoutDrafts(bc);

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
        if (currDraft == null) {
            bc.reply("Sorry but that draft has expired. "
                            + "Feel free to start a new one!")
                    .setEphemeral(true).queue();
            return;
        }

        DraftProcess currProcess = currDraft.getProcess();
        switch (btnName.substring(0, indexOfNum - 2)) {
            case "join":
                if (notInAnotherDraft(bc, drafts)) {
                    currDraft.attemptDraft(bc);
                }
                break;
            case "requestRefresh":
                currDraft.refresh(bc);
                break;
            case "reping":
                currDraft.reping(bc);
                break;
            case "leave":
                currDraft.removeFromQueue(bc);
                break;
            case "requestSub":
                currDraft.requestSub(bc);
                break;
            case "reassign":
                currDraft.reassignCaptain(bc);
                break;
            case "sub":
                if (notInAnotherDraft(bc, drafts)) {
                    currDraft.addSub(bc);
                }
                break;
            case "resetTeams":
                currProcess.resetTeams(bc);
                break;
            case "beginDraft":
                currProcess.start(bc);
                break;
            case "plusOne":
                currProcess.changePointsForTeam(bc, bc.getMember().getId(), true);
                break;
            case "minusOne":
                currProcess.changePointsForTeam(bc, bc.getMember().getId(), false);
                break;
            case "processRefresh":
                bc.deferEdit().queue();
                currDraft.getProcess().refresh(bc);
                break;
            case "endDraftProcess":
                if (currProcess.hasEnded(bc)) {
                    drafts.remove(numButton);
                    queue.add(numButton, numButton);
                }
                break;
        }
    }

    /**
     * Checks for any menu selections.
     * @param sm a menu selection to analyze.
     */
    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent sm) {
        timeoutDrafts(sm);

        String menuName = sm.getComponent().getId();
        int indexOfNum = menuName.length() - 1;

        TreeMap<Integer, Draft> drafts;
        String suffix = menuName.substring(indexOfNum - 2, indexOfNum);
        int numMenu = Integer.parseInt(menuName.substring(indexOfNum));
        switch (suffix) {
            case "LP":
                drafts = lpDrafts;
                break;
            default:
                drafts = ioDrafts;
                break;
        }

        DraftProcess currProcess = drafts.get(numMenu).getProcess();
        if (menuName.substring(0, indexOfNum - 2).equals("teamSelection")) {
            currProcess.addPlayerToTeam(sm);
        }
    }
}
