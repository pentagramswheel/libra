package bot;

import bot.Engine.Add;
import bot.Engine.CycleLog;
import bot.Engine.CycleUndo;
import bot.Engine.Graduate;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    February 17, 2021
 * Project: LaunchPoint Bot
 * Module:  Events.java
 * Purpose: Builds the bot by processing commands and analyzing user input.
 */
public class Events extends ListenerAdapter {

    /** The current state of the bot. */
    public static JDABuilder BOT;

    /** The Discord server's current state. */
    public static Guild SERVER;

    /** The original channel a command was sent in. */
    public static MessageChannel ORIGIN;

    /**
     * Prints the error message when a command has the incorrect arguments.
     */
    private void printArgsError() {
        ORIGIN.sendMessage("Invalid argument input. "
                + "See `lphelp` for more info.").queue();
    }

    /**
     * Checks if a command has the correct amount of arguments.
     * @param args the list of arguments to check.
     * @param n the number of arguments to have.
     */
    private boolean argsValid(String[] args, int n) {
        if (args.length > n) {
            printArgsError();
            return false;
        }

        return true;
    }

    /**
     * Checks if lpcycle command was typed correctly.
     * @param totalArgs the total amount of keywords typed for the command.
     * @param userArgs the amount of users typed with the command.
     * @return True if the format was correct.
     *         False otherwise.
     */
    private boolean cycleFormatInvalid(int totalArgs, int userArgs) {
        boolean outsideArgRange = totalArgs < 4 || totalArgs > 7;
        boolean notEnoughPlayers = userArgs != totalArgs - 3;

        return outsideArgRange || notEnoughPlayers;
    }

    /**
     * Checks if the game set parameters don't make sense.
     * @param args the parameters to analyze.
     * @return True if there were more wins than total games.
     *         False otherwise.
     */
    private boolean gamesPlayedInvalid(String[] args) {
        int totalArgs = args.length;
        return Integer.parseInt(args[totalArgs - 2])
                < Integer.parseInt(args[totalArgs - 1]);
    }

    /**
     * Checks if lpcycle has the correct arguments.
     * @param args the list of arguments to check.
     * @param users the mentioned users.
     * @return True if it does.
     *         False if not.
     */
    private boolean cycleArgsValid(String[] args, List<Member> users) {
        if (cycleFormatInvalid(args.length, users.size())) {
            printArgsError();
            return false;
        } else if (gamesPlayedInvalid(args)) {
            ORIGIN.sendMessage("Incorrect amount of games played detected. "
                    + "See `lphelp` for more info.").queue();
            return false;
        }

        return true;
    }

    /**
     * Checks if there are enough arguments for a, strictly, ping command.
     * @param args the list of arguments to check.
     * @param users the mentioned users.
     * @return True if it does.
     *         False if not.
     */
    private boolean pingArgsValid(String[] args, List<Member> users) {
        if (users.size() != args.length - 1) {
            printArgsError();
            return false;
        }

        return true;
    }

    /**
     * Prints the full list of commands.
     */
    private void printHelpString() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("LaunchPoint Simp Commands");
        eb.setColor(Color.GREEN);
        eb.addField("lphelp",
                "Displays the list of commands.",
                false);
        eb.addField("lphelp?",
                "Displays troubleshooting information for the \n"
                        + "commands.",
                false);
        eb.addField("lpstatus",
                "Checks if the bot is online.",
                false);
        eb.addField("lpcycle [players] [games played] [score]",
                "Reports scores for up to four players.",
                false);
        eb.addField("lpsub [players] [games played] [score]",
                "Reports scores for up to four players who subbed.",
                false);
        eb.addField("lpundo",
                "Reverts the previous cycle command, once and \n"
                        + "only once.",
                false);
        eb.addField("lpadd [players]",
                "Adds players into LaunchPoint.",
                false);
        eb.addField("lpcoach [players]",
                "Adds players to the LaunchPoint coaches.",
                false);
        eb.addField("lpgrad [players]",
                "Graduates players from LaunchPoint.",
                false);

        ORIGIN.sendMessage(eb.build()).queue();
    }

    /**
     * Print the command troubleshooting information.
     */
    private void printTroubleshootString() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Troubleshooting");
        eb.setColor(Color.GREEN);
        eb.addField("lpcycle, lpsub",
                "If the match report isn't giving you feedback, it \n"
                        + "is most likely due to the latest/bottommost row \n"
                        + "in the spreadsheet missing information. For \n"
                        + "example, one common problem is presetting or \n"
                        + "overextending the formulas past the latest row.",
                false);
        eb.addField("lpadd, lpcoach, lpgrad",
                "If the role for a player isn't showing up or seemingly \n"
                        + "isn't being added, try *refreshing the roles* by going \n"
                        + "to `Server Settings > User Management > Members`. \n"
                        + "A second layer of refreshing can be done by \n"
                        + "searching for a player's name in `... > Members`. \n"
                        + "The roles should exist; this is currently a Discord \n"
                        + "bug.",
                false);

        ORIGIN.sendMessage(eb.build()).queue();
    }

    /**
     * Saves a string to the undo file.
     * @param args the contents to save.
     */
    private void saveContents(String[] args) {
        File save = new File("load.txt");
        try {
            save.createNewFile();
            FileWriter fw = new FileWriter(save);

            if (args.length == 1) {
                fw.write(args[0]);
            } else {
                StringBuilder contents = new StringBuilder();
                contents.append(args[0].toUpperCase()).append(" ");
                for (int i = 1; i < args.length - 1; i++) {
                    contents.append(args[i]).append(" ");
                }
                contents.append(args[args.length - 1]);

                fw.write(contents.toString());
            }

            fw.close();
        } catch (IOException ioe) {
            ORIGIN.sendMessage(
                    "The undo file could not be loaded.").queue();
            ioe.printStackTrace();
        }
    }

    /**
     * Runs the "lpwin" or "lplose" command.
     * @param players the mentioned players.
     * @param args the arguments of the command.
     */
    private void runCyclesCmd(List<Member> players, String[] args) {
        CycleLog cycle = new CycleLog();
        cycle.runCmd(null, players, args);

        saveContents(args);
    }

    /**
     * Runs the `lpundo` command.
     */
    private void runUndoCmd() {
        CycleUndo undo = new CycleUndo();
        undo.runCmd(null, null, null);

        saveContents(new String[]{"REDACTED"});
    }

    /**
     * Runs the "lpadd" or "lpcoach" command.
     * @param players the mentioned players.
     * @param args the arguments of the command.
     */
    private void runAddCmd(List<Member> players, String[] args) {
        Add newcomer = new Add();
        newcomer.runCmd(null, players, args);
    }

    /**
     * Runs the "lpgrad" command.
     * @param players the mentioned players.
     */
    private void runGradCmd(List<Member> players) {
        Graduate grad = new Graduate();
        grad.runCmd(null, players, null);
    }

    /**
     * Runs one of the bot's commands.
     * @param e the command to analyze.
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        String input = e.getMessage().getContentRaw();
        String[] args = input.split("\\s+", 100);

        args[0] = args[0].toUpperCase();
        String cmd = args[0];

        List<Member> users;
        SERVER = e.getGuild();
        ORIGIN = e.getChannel();

        switch (cmd) {
            case "LPHELP":
                if (argsValid(args, 1)) {
                    printHelpString();
                }
                break;
            case "LPHELP?":
                if (argsValid(args, 1)) {
                    printTroubleshootString();
                }
                break;
            case "LPSTATUS":
                if (argsValid(args, 1)) {
                    String status = String.format(
                            "The bot is online. Welcome, %s.",
                            e.getMember().getEffectiveName());
                    ORIGIN.sendMessage(status).queue();
                }
                break;
            case "LPCYCLE":
            case "LPSUB":
                users = e.getMessage().getMentionedMembers();
                if (cycleArgsValid(args, users)) {
                    runCyclesCmd(users, args);
                }
                break;
            case "LPUNDO":
                if (argsValid(args, 1)) {
                    runUndoCmd();
                }
                break;
            case "LPADD":
            case "LPCOACH":
                users = e.getMessage().getMentionedMembers();
                if (pingArgsValid(args, users)) {
                    runAddCmd(users, args);
                }
                break;
            case "LPGRAD":
                users = e.getMessage().getMentionedMembers();
                if (pingArgsValid(args, users)) {
                    runGradCmd(users);
                }
                break;
        }
    }
}
