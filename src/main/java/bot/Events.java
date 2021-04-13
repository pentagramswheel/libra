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
 * Purpose: Builds the bot by adding commands and analyzing user input.
 */
public class Events extends ListenerAdapter {

    /** Field for storing the current state of the bot. */
    public static JDABuilder BOT;

    /** Field for storing the Discord server's current state. */
    public static Guild SERVER;

    /**
     * Checks if a command has the correct amount of arguments.
     * @param ch the channel to send an error message in.
     * @param args the list of arguments to check.
     * @param n the number of arguments to have.
     * @return True if it does.
     *         False if not.
     */
    private boolean checkArgs(MessageChannel ch, String[] args, int n) {
        if (args.length != n) {
            ch.sendMessage("Invalid argument input. "
                    + "See `lphelp` for more info.").queue();
            return false;
        }

        return true;
    }

    /**
     * Checks if lpcycle has the correct arguments.
     * @param ch the channel to send an error message in.
     * @param args the list of arguments to check.
     * @return True if it does.
     *         False if not.
     */
    private boolean cycleArgsValid(
            MessageChannel ch, String[] args, List<Member> users) {
        boolean hasEnoughArgs = args.length == 7 || args.length == 4;
        boolean allPlayersExist = users.size() == 1 || users.size() == 4;
        if (!hasEnoughArgs || !allPlayersExist) {
            ch.sendMessage("Invalid cycle argument input. "
                    + "See `lphelp` for more info.").queue();
            return false;
        }

        boolean badQuadGamesPlayed = args.length == 7
                && Integer.parseInt(args[5]) < Integer.parseInt(args[6]);
        boolean badSingleGamesPlayed = args.length == 4
                && Integer.parseInt(args[2]) < Integer.parseInt(args[3]);
        if (badQuadGamesPlayed || badSingleGamesPlayed) {
            ch.sendMessage("Incorrect amount of games played detected. "
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
     * @param ch the channel the command was ran in.
     * @param args the arguments of the command.
     */
    private void runCyclesCmd(List<Member> players, MessageChannel ch,
                             String[] args) {
        CyclesLog cycle = new CyclesLog();
        cycle.runCmd(ch, null, players, args);
    }

    /**
     * Runs the "lpgrad" command.
     * @param player the mentioned player.
     * @param ch the channel the command was ran in.
     */
    private void runGradCmd(List<Member> player, MessageChannel ch) {
        Graduate grad = new Graduate();
        grad.runCmd(ch, null, player, null);
    }

    /**
     * Runs one of the bot's commands.
     * @param e the command to analyze.
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        String input = e.getMessage().getContentRaw();
        MessageChannel ch = e.getChannel();
        String[] args = input.split(" ", 7);
        List<Member> users;
        SERVER = e.getGuild();

        switch (args[0]) {
            case "lphelp":
                ch.sendMessage(getHelpString()).queue();
                break;
            case "lpcycle":
                users = e.getMessage().getMentionedMembers();
                if (cycleArgsValid(ch, args, users)) {
                    runCyclesCmd(users, ch, args);
                }
                break;
            case "lpgrad":
                users = e.getMessage().getMentionedMembers();
                if (checkArgs(ch, args, 2) && users.size() == 1) {
                    runGradCmd(users, ch);
                }
                break;
        }
    }
}
