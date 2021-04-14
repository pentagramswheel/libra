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
     * Checks if a command has the correct amount of arguments.
     * @param args the list of arguments to check.
     * @param n the number of arguments to have.
     * @return True if it does.
     *         False if not.
     */
    private boolean checkArgs(String[] args, int n) {
        if (args.length != n) {
            ORIGIN.sendMessage("Invalid argument input. "
                    + "See `lphelp` for more info.").queue();
            return false;
        }

        return true;
    }

    /**
     * Checks if lpcycle has the correct arguments.
     * @param args the list of arguments to check.
     * @return True if it does.
     *         False if not.
     */
    private boolean cycleArgsValid(String[] args, List<Member> users) {
        boolean hasEnoughArgs = args.length == 7 || args.length == 4;
        boolean allPlayersExist = users.size() == 1 || users.size() == 4;
        if (!hasEnoughArgs || !allPlayersExist) {
            ORIGIN.sendMessage("Invalid cycle argument input. "
                    + "See `lphelp` for more info.").queue();
            return false;
        }

        boolean badQuadGamesPlayed = args.length == 7
                && Integer.parseInt(args[5]) < Integer.parseInt(args[6]);
        boolean badSingleGamesPlayed = args.length == 4
                && Integer.parseInt(args[2]) < Integer.parseInt(args[3]);
        if (badQuadGamesPlayed || badSingleGamesPlayed) {
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
                + "`lpcycle [user] [user] [user] [user] [games played] [score]` - Adds scores to the given users.\n===\n"
                + "`lpcycle [user] [games played] [score]` - Adds score to a single user.\n===\n"
                + "`lpgrad [user]` - Graduates a player from LaunchPoint.\n===";
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
     * @param player the mentioned player.
     */
    private void runGradCmd(List<Member> player) {
        Graduate grad = new Graduate();
        grad.runCmd(null, player, null);
    }

    /**
     * Runs one of the bot's commands.
     * @param e the command to analyze.
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        String input = e.getMessage().getContentRaw();
        String[] args = input.split(" ", 7);
        List<Member> users;
        SERVER = e.getGuild();
        ORIGIN = e.getChannel();

        switch (args[0]) {
            case "lphelp":
                ORIGIN.sendMessage(getHelpString()).queue();
                break;
            case "lpcycle":
                users = e.getMessage().getMentionedMembers();
                if (cycleArgsValid(args, users)) {
                    runCyclesCmd(users, args);
                }
                break;
            case "lpgrad":
                users = e.getMessage().getMentionedMembers();
                if (checkArgs(args, 2) && users.size() == 1) {
                    runGradCmd(users);
                }
                break;
        }
    }
}
