package bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
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
        int gamesPlayed = Integer.parseInt(args[totalArgs - 2]);
        int gamesWon = Integer.parseInt(args[totalArgs - 1]);

        return gamesPlayed < gamesWon;
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
        eb.addField("lpexit",
                "Remotely shuts down the bot.",
                false);

        ORIGIN.sendMessageEmbeds(eb.build()).queue();
    }

    /**
     * Print the command troubleshooting information.
     */
    private void printTroubleshootString() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Troubleshooting");
        eb.setColor(Color.GREEN);
        eb.addField("lpcycle, lpsub",
                "If a match report is giving you an error message, \n"
                        + "it is most likely due to a row in the spreadsheet \n"
                        + "missing information. For example, one common \n"
                        + "problem is presetting or overextending the \n"
                        + "formulas past the bottommost row.",
                false);
        eb.addField("lpadd, lpcoach, lpgrad",
                "If the role for a player isn't showing up or seemingly \n"
                        + "isn't being added, try *refreshing the roles* by opening \n"
                        + "`Server Settings > User Management > Members`. \n"
                        + "A second layer of refreshing can be done by \n"
                        + "searching for a player's name in `... > Members`. \n"
                        + "The roles should exist; this is currently a bug within \n"
                        + "Discord.",
                false);

        ORIGIN.sendMessageEmbeds(eb.build()).queue();
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

        SERVER = e.getGuild();
        ORIGIN = e.getChannel();
        List<Member> users = e.getMessage().getMentionedMembers();

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
            case "LPADD":
            case "LPCOACH":
                if (argsValid(args, users.size() + 1)) {
                    Commands.runAddCmd(users, args);
                }
                break;
            case "LPGRAD":
                if (argsValid(args, users.size() + 1)) {
                    Commands.runGradCmd(users);
                }
                break;
            case "LPCYCLE":
            case "LPSUB":
                if (cycleArgsValid(args, users)) {
                    Commands.runCyclesCmd(users, args);
                }
                break;
            case "LPUNDO":
                if (argsValid(args, 1)) {
                    Commands.runUndoCmd();
                }
                break;
            case "LPEXIT":
                Commands.runExitCmd();
                break;
        }
    }
}
