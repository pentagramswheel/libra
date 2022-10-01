package bot;

import bot.Engine.*;
import bot.Engine.Cycles.*;
import bot.Engine.Games.Drafts.DraftGame;
import bot.Engine.Games.Drafts.DraftProcess;
import bot.Engine.Games.GameType;
import bot.Engine.Games.MapGenerator;
import bot.Engine.Games.Minigames.MiniGame;
import bot.Engine.Games.Minigames.MiniProcess;
import bot.Engine.Profiles.Profile;
import bot.Engine.Templates.*;
import bot.Tools.ArrayHeapMinPQ;
import bot.Tools.Components;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;

/**
 * @author  Wil Aquino
 * Date:    February 17, 2021
 * Project: Libra
 * Module:  Events.java
 * Purpose: Builds the bot by processing commands
 *          and analyzing user input.
 */
public class Events extends ListenerAdapter {

    /** A random number generator for the bot to use. */
    public static Random RANDOM_GENERATOR = new Random();

    /** Fields which determine the maximum number of drafts. */
    private final static int MAX_FS_DRAFTS = 3;
    private final static int MAX_LP_DRAFTS = 4;
    private final static int MAX_IO_DRAFTS = 2;

    /** Fields for storing numbers to drafts. */
    private TreeMap<Integer, GameReqs> fsDrafts;
    private TreeMap<Integer, GameReqs> lpDrafts;
    private TreeMap<Integer, GameReqs> ioDrafts;

    /** Fields for storing queued draft numbers. */
    private ArrayHeapMinPQ<Integer> fsQueue;
    private ArrayHeapMinPQ<Integer> lpQueue;
    private ArrayHeapMinPQ<Integer> ioQueue;

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
     * Checks whether the command user has permission to use the
     * command or not.
     * @param sc the user's inputted command.
     * @return True if the command is not a staff command.
     *         False otherwise.
     */
    private boolean isStaffCommand(SlashCommandEvent sc) {
        String[] staffCmds = {"forceend", "log", "sub", "undo",
                "add", "deny", "grad", "award", "cyclescalc"};

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
        if (prefix.equals("libra") || prefix.equals("mit") || prefix.equals("dc")
                || subCmd.equals("genmaps")) {
            return false;
        }

        String helpdesk = server.getTextChannelsByName(
                "helpdesk", false).get(0).getName();
        String entryChannel = server.getTextChannelsByName(
                "mit-entry-confirmation", false).get(0).getName();
        String fsDraftChannel = server.getTextChannelsByName(
                "\uD83D\uDCCDfs-looking-for-draft", false).get(0).getName();
        String lpDraftChannel = server.getTextChannelsByName(
                 "\uD83D\uDCCDlp-looking-for-draft", false).get(0).getName();
        String lpReportsChannel = server.getTextChannelsByName(
                "lp-match-report", false).get(0).getName();
        String ioDraftChannel = server.getTextChannelsByName(
                "\uD83D\uDCCDio-looking-for-draft", false).get(0).getName();
        String ioReportsChannel = server.getTextChannelsByName(
                "io-match-report", false).get(0).getName();
        String testChannel = server.getTextChannelsByName(
                "bot-testing", false).get(0).getName();

        String channel = sc.getTextChannel().getName();
        boolean isDraftCommand =
                subCmd.equals("startdraft") || subCmd.equals("forcesub") || subCmd.equals("forceend");
        boolean isReportCommand =
                subCmd.equals("log") || subCmd.equals("sub") || subCmd.equals("undo");

        boolean isHelpdesk = channel.equals(helpdesk);
        boolean inEntryChannel = (subCmd.equals("add") || subCmd.equals("deny") || subCmd.equals("grad"))
                && channel.equals(entryChannel);
        boolean inFSChannel = prefix.equals("fs") && isDraftCommand
                && channel.equals(fsDraftChannel);
        boolean inLPChannel = prefix.equals("lp") && isDraftCommand
                && channel.equals(lpDraftChannel);
        boolean inIOChannel = prefix.equals("io") && isDraftCommand
                && channel.equals(ioDraftChannel);

        boolean inLPReportsChannel = prefix.equals("lp") && isReportCommand
                && channel.equals(lpReportsChannel);
        boolean inIOReportsChannel = prefix.equals("io") && isReportCommand
                && channel.equals(ioReportsChannel);
        boolean inTestChannel = channel.equals(testChannel);

        return !(isHelpdesk || inEntryChannel
                || inFSChannel || inLPChannel || inIOChannel
                || inLPReportsChannel || inIOReportsChannel
                || inTestChannel);
    }

    /**
     * Prints help information.
     * @param sm a menu selection to analyze. the user's inputted command.
     */
    private void printHelpOption(SelectionMenuEvent sm) {
        sm.deferEdit().queue();

        SelectOption chosenOption = sm.getInteraction().getSelectedOptions().get(0);
        String value = chosenOption.getValue();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Main.mitColor);

        switch (value) {
            case "0":
                eb.setTitle("Libra's Profile System");
                eb.setDescription("[Here's what you need to know!](https://docs.google.com/document/d/1LoYjd2mqadu5g5D-BMNHfLk9zUouZZPzLWriu-vxCew/edit?usp=sharing#heading=h.zi1r01w6ox4r)");
                break;
            case "1":
                eb.setTitle("Libra's Profile Troubleshooting");
                eb.addField("Pronouns Tips",
                        "Pronouns often come in the format: `she/her, he/they, ...`\n"
                                + "We encourage you to put all of your preferred pronouns,\n"
                                + "so other players know which ones you are most\n"
                                + "comfortable with!\n\n"
                                + "If you're fine with any pronouns, some give their\n"
                                + "preference as `Any`. If you would rather have others just\n"
                                + "ask, some give their preference as `Ask me`!", false);
                eb.addField("Weapons Tips",
                        "Weapons often come in the format:\n"
                                + "`Octobrush, Blasters, Scopes, '89, ...`\n"
                                + "While it's not a bad idea to include your entire weapon pool,\n"
                                + "it's generally fine for other players to know just the\n"
                                + "summarized version of your weapon pool.\n\n"
                                + "For example, if you play all splatlings, instead of listing\n"
                                + "every splatling, it is just as effective to just label them\n"
                                + "simply as `Splatlings`! Some weapons are even known to\n"
                                + "have an abbreviated name, such as Kensa (K)-branded\n"
                                + "weapons.\n\n"
                                + "`Kensa Splattershot Pro = Kensa Pro = KPro`\n"
                                + "`(Regular) Splattershot Jr. = Vanilla Jr = VJr`\n"
                                + "`Custom E-Liter 4K = Custom Liter = CLiter`", false);
                eb.addField("Remark",
                        "Although the formatting is somewhat strict, it is\n"
                                + "to create a consistency amongst all profiles\n"
                                + "and in case your profile is featured somewhere!", false);
                break;
            case "2":
                eb.setTitle("Libra's Automatic Minigame System");
                eb.setDescription("[Here's the quick overview!](https://docs.google.com/document/d/1LoYjd2mqadu5g5D-BMNHfLk9zUouZZPzLWriu-vxCew/edit?usp=sharing#heading=h.7akwpavnacs8)");
                break;
            case "3":
                eb.setTitle("Libra's Automatic Minigame System");
                eb.setDescription("[Here's the detailed overview!](https://docs.google.com/document/d/1LoYjd2mqadu5g5D-BMNHfLk9zUouZZPzLWriu-vxCew/edit?usp=sharing#heading=h.ysn2sl5wfhn4)");
                break;
            case "4":
                eb.setTitle("Libra's Automatic Draft System");
                eb.setDescription("[Here's the quick overview!](https://docs.google.com/document/d/1LoYjd2mqadu5g5D-BMNHfLk9zUouZZPzLWriu-vxCew/edit?usp=sharing#heading=h.rzj2bgwe2gos)");
                break;
            case "5":
                eb.setTitle("Libra's Automatic Draft System");
                eb.setDescription("[Here's the detailed overview!](https://docs.google.com/document/d/1LoYjd2mqadu5g5D-BMNHfLk9zUouZZPzLWriu-vxCew/edit?usp=sharing#heading=h.xt8dy64nsnj)");
                break;
            case "6":
                eb.setTitle("Libra's Documentation");
                eb.setDescription("[Here's the full document!](https://docs.google.com/document/d/1LoYjd2mqadu5g5D-BMNHfLk9zUouZZPzLWriu-vxCew/edit?usp=sharing)");
                break;
            case "7":
                eb.setTitle("Libra's FAQ");
                eb.setDescription("[Here are some answers!](https://docs.google.com/document/d/1LoYjd2mqadu5g5D-BMNHfLk9zUouZZPzLWriu-vxCew/edit?usp=sharing#heading=h.80874ddqf10r)");
                break;
            case "8":
                eb.setTitle("Match Report Help (Staff)");
                eb.setDescription("If a match report is giving you an error message,\n"
                        + "it is most likely due to a row in the spreadsheet\n"
                        + "missing information. For example, one common\n"
                        + "problem is presetting or overextending the\n"
                        + "formulas past the bottommost row.");
                break;
            default:
                eb.setTitle("Roles Help (Staff)");
                eb.setDescription("If the role for a player isn't showing up or seemingly\n"
                        + "isn't being added, try *refreshing the roles* by opening\n"
                        + "`Server Settings > User Management > Members`.\n"
                        + "Note that it takes ~0.2 sec/player to add a role.");
                break;
        }

        sm.getHook().editOriginal("Let's see...").setEmbeds(eb.build()).queue();
    }

    /**
     * Checks whether a player is in another draft or not.
     * @param interaction the user interaction calling this method.
     * @param generator a number generator for error messaging.
     * @param drafts the list of drafts to check.
     * @return their found draft.
     *         null otherwise.
     */
    private GameReqs notInAnotherDraft(GenericInteractionCreateEvent interaction,
                                        Random generator,
                                        TreeMap<Integer, GameReqs> drafts) {
        String playerID = interaction.getMember().getId();
        if (drafts == null) {
            return null;
        }

        for (GameReqs draft : drafts.values()) {
            if (draft.getPlayers().containsKey(playerID)
                    && draft.getPlayers().get(playerID).isActive()) {
                if (generator == null) {
                    interaction.reply("You are already in a draft!")
                            .setEphemeral(true).queue();
                }
                return draft;
            }
        }

        return null;
    }

    /**
     * Times out drafts in a section if possible.
     * @param interaction the user interaction calling this method.
     * @param drafts the source map of drafts.
     * @param queue the source queue of drafts.
     */
    private void timeoutDrafts(GenericInteractionCreateEvent interaction,
                              TreeMap<Integer, GameReqs> drafts,
                              ArrayHeapMinPQ<Integer> queue) {
        if (drafts != null) {
            for (Map.Entry<Integer, GameReqs> mapping : drafts.entrySet()) {
                int numDraft = mapping.getKey();
                GameReqs draft = mapping.getValue();

                if (!draft.isInitialized() && draft.timedOut(interaction)) {
                    drafts.remove(numDraft);
                    queue.add(numDraft, numDraft);
                    break;
                }
            }
        }
    }

    /**
     * Times out any drafts if possible.
     * @param interaction the user interaction calling this method.
     */
    private void timeoutAnyDrafts(GenericInteractionCreateEvent interaction) {
        timeoutDrafts(interaction, fsDrafts, fsQueue);
        timeoutDrafts(interaction, lpDrafts, lpQueue);
        timeoutDrafts(interaction, ioDrafts, ioQueue);
    }

    /**
     * Creates a new draft depending on command parameters given by FS/LP/IO.
     * @param sc the user's inputted command.
     * @param prefix the prefix of the command.
     * @param author the user who ran the command.
     * @param draftButton the numbered button associated with this draft.
     * @return the created draft.
     */
    private GameReqs newDraft(SlashCommandEvent sc,
                                    String prefix, Member author,
                                    int draftButton) {
        if (!sc.getName().equals("fs")) {
            return new DraftGame(sc, draftButton, prefix, author);
        }

        switch (sc.getOptions().get(0).getAsString()) {
            case "Ranked Modes":
                return new MiniGame(
                        sc, GameType.RANKED, draftButton, prefix, author);
            case "Turf War Only":
                return new MiniGame(
                        sc, GameType.TURF_WAR, draftButton, prefix, author);
            case "Hide & Seek":
                return new MiniGame(
                        sc, GameType.HIDE_AND_SEEK, draftButton, prefix, author);
            case "Juggernaut":
                return new MiniGame(
                        sc, GameType.JUGGERNAUT, draftButton, prefix, author);
            default:
                return new MiniGame(
                        sc, GameType.SPAWN_RUSH, draftButton, prefix, author);
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
    private void processDraft(SlashCommandEvent sc,
                              String prefix, Member author,
                              TreeMap<Integer, GameReqs> ongoingDrafts,
                              ArrayHeapMinPQ<Integer> queue) {
        if (queue.size() == 0) {
            sc.reply("Wait until a draft has finished!").queue();
        } else {
            int draftButton = queue.removeSmallest();
            GameReqs newDraft = newDraft(sc, prefix, author, draftButton);

            ongoingDrafts.put(draftButton, newDraft);
            newDraft.runCmd(sc);

            if (!sc.isAcknowledged()) {
                sc.reply("You don't have access to this section's drafts!")
                        .setEphemeral(true).queue();

                ongoingDrafts.remove(draftButton);
                queue.add(draftButton, draftButton);
            }
        }
    }

    /**
     * Processes drafts, when possible.
     * @param sc the user's inputted command.
     * @param prefix the prefix of the command.
     * @param author the user who ran the command.
     */
    private void processDrafts(SlashCommandEvent sc, String prefix,
                               Member author) {
        switch (prefix) {
            case "fs":
                if (fsDrafts == null) {
                    fsDrafts = new TreeMap<>();
                }
                if (fsQueue == null) {
                    fsQueue = new ArrayHeapMinPQ<>();
                    for (int i = 1; i <= MAX_FS_DRAFTS; i++) {
                        fsQueue.add(i, i);
                    }
                }

                processDraft(sc, prefix, author, fsDrafts, fsQueue);
                break;
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
            default:
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
     * Checks if a number draft exists among a map of drafts.
     * @param sc the user's inputted command.
     * @param drafts the source map of drafts.
     * @param args the arguments of the command.
     * @return the number if it does exist.
     *         -1 otherwise.
     */
    private int foundDraftNumber(SlashCommandEvent sc,
                                 TreeMap<Integer, GameReqs> drafts,
                                 List<OptionMapping> args) {
        if (drafts == null) {
            sc.reply("No drafts have been started yet.")
                    .setEphemeral(true).queue();
            return -1;
        }

        int numDraft = (int) args.get(0).getAsLong();
        if (drafts.get(numDraft) == null) {
            sc.reply("That number draft does not exist.")
                    .setEphemeral(true).queue();
        }

        return numDraft;
    }

    /**
     * Attempts to forcibly sub a player out of a draft.
     * @param sc the user's inputted command.
     * @param drafts the source map of drafts.
     * @param args the arguments of the command.
     */
    private void attemptForceSub(SlashCommandEvent sc,
                                 TreeMap<Integer, GameReqs> drafts,
                                 List<OptionMapping> args) {
        int numDraft = foundDraftNumber(sc, drafts, args);

        if (numDraft != -1) {
            GameReqs draft = drafts.get(numDraft);
            Member playerToSub = args.get(1).getAsMember();
            if (draft != null) {
                draft.forceSub(sc, playerToSub.getId());
            }
        }
    }

    /**
     * Attempts to forcibly end a draft.
     * @param sc the user's inputted command.
     * @param drafts the source map of drafts.
     * @param args the arguments of the command.
     */
    private void attemptForceEnd(SlashCommandEvent sc,
                                 TreeMap<Integer, GameReqs> drafts,
                                 ArrayHeapMinPQ<Integer> queue,
                                 List<OptionMapping> args) {
        int numDraft = foundDraftNumber(sc, drafts, args);

        if (numDraft != -1) {
            GameReqs draft = drafts.get(numDraft);
            if (draft != null && draft.canForceEnd(sc)) {
                drafts.remove(numDraft);
                queue.add(numDraft, numDraft);
            }
        }
    }

    /** Retrieves an embedded fun fact about Libra. */
    private EmbedBuilder getFunFact() {
        EmbedBuilder eb = new EmbedBuilder();

        String[] funFacts = {
                "MIT, originally known only as **LaunchPoint** at the time,\n"
                        + "was founded on July 15, 2020.",
                "MIT transitioned to an organization on December 5, 2021,\n"
                        + "with the announcement of Ink Odyssey.",
                "The MIT Wahoo Zones inside joke was founded on how generated\n"
                        + "draft maplists during earlier cycles of LaunchPoint would\n"
                        + "often include at least one game of Wahoo Zones.",
                "The MIT bread inside joke was founded on the fact that staff\n"
                        + "member PTW's college lunches on Tuesdays/Thursdays were him\n"
                        + "just stacking 6 slices of bread on a plate and eating it.",
                "Before the creation of Libra, MIT used to have a dedicated\n"
                        + "match report team to *manually* report draft scores (Often\n"
                        + "10-11 spreadsheet entries **per player**).",
                "Before the Patreon, Libra wasn't always running 24/7. The old\n"
                        + "match reports team would coordinate 'shifts' with PTW, so\n"
                        + "he could turn on the bot manually, often between college\n"
                        + "classes.",
                "The longest LPDC was LPDC2, which ran for around 8 hours, by\n"
                        + "5 staff members: TheMoo, Wug, Reef, Stan, Restia.",
                "Staff member Reef used to greet the staff chat every morning\n"
                        + "with the following picture:",
                "Minnow and Megalodon Cup started as their own organization\n"
                        + "named **Deep Sea Solutions**; founded on September 19, 2020;\n"
                        + "before they merged with MIT on March 5, 2022.",
                "The MIT organization idea and structure was planned at\n"
                        + "a Nandos.",
                "Libra's name was originated from a couple of ideas. One\n"
                        + "notably being that she is the impartial handler of drafts.",
                "Libra's current main is the slosher! She was the first one\n"
                        + "to create a `/mit profile`! (Go look it up)",
                "The largest amount of LPDC drops was during LPDC5, with a\n"
                        + "total of 32 drops."
        };

        int num = RANDOM_GENERATOR.nextInt(funFacts.length);
        eb.setTitle("Libra Fun Fact #" + (num + 1));
        if (num == 7) {
            eb.setImage("https://media.discordapp.net/attachments/845782222677213234/966973831896002591/7CC5DDF1-5967-4A8E-8CA4-9D2925808275.gif");
        }
        eb.setColor(Main.mitColor);
        eb.setDescription(funFacts[num]);

        return eb;
    }

    /**
     * Runs a Libra command.
     * @param sc the slash command to analyze.
     */
    private void parseLibraCommands(SlashCommandEvent sc) {
        Member author = sc.getMember();
        String subCmd = sc.getSubcommandName();
        if (subCmd == null) {
            subCmd = "";
        }

        switch (subCmd) {
            case "status":
                sc.reply("The bot is online. Welcome, "
                        + author.getEffectiveName()).queue();
                break;
            case "help":
                sc.reply("What can I help you with? (:")
                        .addActionRow(Components.ForGeneral.helpMenu("MT1"))
                        .setEphemeral(true).queue();
                break;
            case "fact":
                sc.replyEmbeds(getFunFact().build()).queue();
                break;
        }
    }

    /**
     * Runs a general command.
     * @param sc the slash command to analyze.
     */
    private void parseGeneralCommands(SlashCommandEvent sc) {
        String subGroup = sc.getSubcommandGroup();
        String subCmd = sc.getSubcommandName();
        if (subGroup == null) {
            subGroup = "";
        }
        if (subCmd == null) {
            subCmd = "";
        }

        if (subGroup.equals("profile") || subCmd.equals("qprofile")) {
            new Profile().runCmd(sc);
        } else {
            switch (subCmd) {
                case "ded":
                    sc.reply(Emoji.fromEmote(
                                    "Okayu_ded", 1016217763536187412L, false)
                            .getAsMention()).queue();
                    break;
            }
        }
    }

    /**
     * Runs a section command.
     * @param sc the slash command to analyze.
     */
    private void parseSectionCommands(SlashCommandEvent sc) {
        Member author = sc.getMember();
        String prefix = sc.getName();
        String subCmd = sc.getSubcommandName();
        List<OptionMapping> args = sc.getOptions();
        if (subCmd == null) {
            subCmd = "";
        }

        TreeMap<Integer, GameReqs> drafts;
        ArrayHeapMinPQ<Integer> queue;
        String leaderboardLink;
        switch (prefix) {
            case "fs":
                drafts = fsDrafts;
                queue = fsQueue;
                leaderboardLink = "";
                break;
            case "lp":
                drafts = lpDrafts;
                queue = lpQueue;
                leaderboardLink =
                        "https://docs.google.com/spreadsheets/d/1DMGWmvIz23fcx7ZtIVxDCyl1zjgdOF_5vccdlmifa7o/edit?usp=sharing";
                break;
            default:
                drafts = ioDrafts;
                queue = ioQueue;
                leaderboardLink =
                        "https://docs.google.com/spreadsheets/d/12T6J0jd6Z8opkmYaj6LDRpvQIiW6vqXAZUG0heAFNxc/edit?usp=sharing";
                break;
        }

        switch (subCmd) {
            case "add":
            case "deny":
                new Add(prefix).runCmd(sc);
                break;
            case "grad":
                new Graduate(prefix).runCmd(sc);
                break;
            case "award":
                new Award(prefix).runCmd(sc);
                break;
            case "cyclescalc":
                new PointsCalculator(prefix).runCmd(sc);
                break;
            case "genmaps":
                MapGenerator maps = new MapGenerator(prefix,
                        notInAnotherDraft(sc, RANDOM_GENERATOR, drafts));
                maps.runCmd(sc);
                break;
            case "leaderboard":
                sc.reply(leaderboardLink).queue();
                break;
            case "startdraft":
                if (notInAnotherDraft(sc, null, drafts) == null) {
                    processDrafts(sc, prefix, author);
                }
                break;
            case "forcesub":
                attemptForceSub(sc, drafts, args);
                break;
            case "forceend":
                attemptForceEnd(sc, drafts, queue, args);
                break;
            case "log":
            case "sub":
                if (gamesPlayedValid(sc)) {
                    new ManualLog(prefix).runCmd(sc);
                }
                break;
            case "undo":
                new Undo(prefix).runCmd(sc);
                break;
        }
    }

    /**
     * Runs one of the bot's commands.
     * @param sc a slash command to analyze.
     */
    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent sc) {
        timeoutAnyDrafts(sc);

        if (isStaffCommand(sc) || wrongChannelUsed(sc)) {
            sc.reply("You do not have permission to use this command here.")
                    .setEphemeral(true).queue();
        } else {
            String cmdPrefix = sc.getName();
            switch (cmdPrefix) {
                case "libra":
                    parseLibraCommands(sc);
                    break;
                case "mit":
                    parseGeneralCommands(sc);
                    break;
                case "fs":
                case "lp":
                case "io":
                    parseSectionCommands(sc);
                    break;
                case "dc":
                    new DraftCup().runCmd(sc);
            }
        }
    }

    /**
     * Processes a button click from game request interfaces.
     * @param bc a button click to analyze.
     * @param name the name of the button.
     * @param indexOfNum the index of the button's assigned number.
     * @param draft a draft request to analyze.
     * @param drafts the map that the request belongs to.
     * @return True if the parse matched with a button.
     *         False otherwise
     */
    private boolean parseRequestClicks(ButtonClickEvent bc, String name,
                                      int indexOfNum, GameReqs draft,
                                      TreeMap<Integer, GameReqs> drafts) {
        switch (name.substring(0, indexOfNum - 2)) {
            case "join":
                if (notInAnotherDraft(bc, null, drafts) == null) {
                    draft.attemptDraft(bc);
                }
                return true;
            case "setupEarly":
                ((MiniGame) draft).setup(bc);
                return true;
            case "reping":
                draft.reping(bc);
                return true;
            case "leave":
                draft.removeFromQueue(bc);
                return true;
            case "requestRefresh":
                draft.refresh(bc);
                return true;
            case "requestSub":
                draft.requestSub(bc);
                return true;
            case "reassign":
                ((DraftGame) draft).reassignCaptain(bc);
                return true;
            case "sub":
                if (notInAnotherDraft(bc, null, drafts) == null) {
                    draft.addSub(bc);
                }
                return true;
        }

        return false;
    }

    /**
     * Processes a button click from game request interfaces.
     * @param bc a button click to analyze.
     * @param name the name of the button.
     * @param indexOfNum the index of the button's assigned number.
     * @param numButton the button's assigned number.
     * @param process a draft process to analyze.
     * @param drafts the map that the respective request belongs to.
     * @param queue the queue that the draft belongs to.
     */
    private void parseProcessClicks(ButtonClickEvent bc, String name,
                                    int indexOfNum, int numButton,
                                    ProcessReqs process,
                                    TreeMap<Integer, GameReqs> drafts,
                                    ArrayHeapMinPQ<Integer> queue) {
        switch (name.substring(0, indexOfNum - 2)) {
            case "resetTeams":
                ((DraftProcess) process).resetTeams(bc);
                break;
            case "beginDraft":
                ((DraftProcess) process).start(bc);
                break;
            case "plusOne":
                process.changePointsForTeam(
                        bc, bc.getMember().getId(), true);
                break;
            case "minusOne":
                process.changePointsForTeam(
                        bc, bc.getMember().getId(), false);
                break;
            case "nextTurn":
                ((MiniProcess) process).rotateTurns(bc);
                break;
            case "endDraftProcess":
                if (process.hasEnded(bc)) {
                    drafts.remove(numButton);
                    queue.add(numButton, numButton);
                }
                break;
            case "processRefresh":
                bc.deferEdit().queue();
                process.refresh(bc);
                break;
        }
    }

    /**
     * Prints a message if a draft expired.
     * @param interaction the user interaction calling this method.
     */
    private void printExpirationMessage(GenericInteractionCreateEvent interaction) {
        interaction.reply("Sorry but that draft has expired. "
                        + "Feel free to start a new one!")
                .setEphemeral(true).queue();
    }

    /**
     * Processes button clicks.
     * @param bc a button click to analyze.
     */
    @Override
    public void onButtonClick(@NotNull ButtonClickEvent bc) {
        timeoutAnyDrafts(bc);

        String btnName = bc.getButton().getId();
        int indexOfNum = btnName.length() - 1;

        TreeMap<Integer, GameReqs> drafts;
        ArrayHeapMinPQ<Integer> queue;
        String suffix = btnName.substring(indexOfNum - 2, indexOfNum);
        int numButton = Integer.parseInt(btnName.substring(indexOfNum));
        switch (suffix) {
            case "FS":
                drafts = fsDrafts;
                queue = fsQueue;
                break;
            case "LP":
                drafts = lpDrafts;
                queue = lpQueue;
                break;
            default:
                drafts = ioDrafts;
                queue = ioQueue;
                break;
        }
        if (drafts == null) {
            bc.getMessage().delete().queue();
            printExpirationMessage(bc);
            return;
        }

        GameReqs currDraft = drafts.get(numButton);
        if (currDraft == null) {
            printExpirationMessage(bc);
        } else if (!parseRequestClicks(bc, btnName, indexOfNum,
                currDraft, drafts)) {
            ProcessReqs currProcess = currDraft.getProcess();

            if (currProcess == null) {
                bc.getMessage().delete().queue();
                printExpirationMessage(bc);
            } else {
                parseProcessClicks(bc, btnName, indexOfNum, numButton,
                        currProcess, drafts, queue);
            }
        }
    }

    /**
     * Processes menu selections.
     * @param sm a menu selection to analyze.
     */
    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent sm) {
        timeoutAnyDrafts(sm);

        String menuName = sm.getComponent().getId();
        int indexOfNum = menuName.length() - 1;

        TreeMap<Integer, GameReqs> drafts;
        String suffix = menuName.substring(indexOfNum - 2, indexOfNum);
        int numMenu = Integer.parseInt(menuName.substring(indexOfNum));
        switch (suffix) {
            case "MT":
                printHelpOption(sm);
                return;
            case "FS":
                drafts = fsDrafts;
                break;
            case "LP":
                drafts = lpDrafts;
                break;
            default:
                drafts = ioDrafts;
                break;
        }

        if (drafts == null) {
            sm.getMessage().delete().queue();
            printExpirationMessage(sm);
        }

        DraftProcess currProcess = ((DraftGame) drafts.get(numMenu)).getProcess();
        if (currProcess == null) {
            sm.getMessage().delete().queue();
            printExpirationMessage(sm);
        } else if (menuName.substring(0, indexOfNum - 2).equals("teamSelection")) {
            currProcess.addPlayerToTeam(sm);
        }
    }
}
