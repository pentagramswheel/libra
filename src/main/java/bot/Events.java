package bot;

import bot.Engine.CyclesLog;
import bot.Engine.Graduate;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
            ORIGIN.sendMessage("Invalid cycle argument input. "
                    + "See `lphelp` for more info.").queue();
            return false;
        } else if (gamesPlayedInvalid(args)) {
            ORIGIN.sendMessage("Incorrect amount of games played detected. "
                    + "See `lphelp` for more info.").queue();
            return false;
        }

        return true;
    }

    /**
     * Retrieves the full list of commands.
     * @return the help string.
     */
    private String getHelpString() {
        return "=== __**LaunchPoint Simp Commands**__ ===\n"
                + "`lphelp` - Displays the list of commands.\n===\n"
                + "`lpcycle [players] [games played] [score]` - Adds scores to up to four players.\n===\n"
                + "`lpsub [players] [games played] [score]` - Adds scores to up to four players who subbed.\n===\n"
                + "`lpgrad [players]` - Graduates players from LaunchPoint.\n===";
    }

    /**
     * Runs the "lpwin" or "lplose" command.
     * @param players the mentioned players.
     * @param args the arguments of the command.
     */
    private void runCyclesCmd(List<Member> players, String[] args) {
        CyclesLog cycle = new CyclesLog();
        cycle.runCmd(null, players, args);
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
        String[] args = input.split("\\s+", 7);
        List<Member> users;
        SERVER = e.getGuild();
        ORIGIN = e.getChannel();

        switch (args[0]) {
            case "lphelp":
                ORIGIN.sendMessage(getHelpString()).queue();
                break;
            case "lpcycle":
            case "lpsub":
                users = e.getMessage().getMentionedMembers();
                if (cycleArgsValid(args, users)) {
                    runCyclesCmd(users, args);
                }
                break;
            case "lpgrad":
                users = e.getMessage().getMentionedMembers();
                if (users.size() == args.length - 1) {
                    runGradCmd(users);
                }
                break;
        }
    }
}
