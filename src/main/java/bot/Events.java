package bot;

import bot.Engine.DraftLog;
import bot.Engine.Graduate;
import net.dv8tion.jda.api.JDABuilder;
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
    public static JDABuilder bot;

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
     * Checks if all players are found within the command input.
     * @param ch the channel to send an error message in.
     * @param players the list of players to check.
     * @return True if six players were found.
     *         False otherwise.
     */
    private boolean allPlayersExist(MessageChannel ch, List<Member> players) {
        if (players.size() != 4) {
            ch.sendMessage("Not all players were listed. "
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
        return "`lphelp` - Displays the list of commands.\n"
                + "`lpwin [user] [user] [user] [user] [score]` - Adds win scores to given users.\n"
                + "`lplose [user] [user] [user] [user] [score]` - Adds lose scores to given users.\n"
                + "`lpgrad [user]` - Graduates a player from LaunchPoint.";
    }

    /**
     * Runs the "lpwin" command.
     * @param players the mentioned players.
     * @param ch the channel the command was ran in.
     * @param args the arguments of the command.
     */
    private void runDraftCmd(List<Member> players, MessageChannel ch,
                             String[] args) {
        DraftLog draft = new DraftLog();
        draft.runCmd(ch, null, players, args);
    }

    /**
     * Runs the "lpwin" command.
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
        String[] args = input.split(" ", 6);

        switch (args[0]) {
            case "lphelp":
                ch.sendMessage(getHelpString()).queue();
                break;
            case "lpwin":
            case "lplose":
                List<Member> users = e.getMessage().getMentionedMembers();
                if (checkArgs(ch, args, 6) && allPlayersExist(ch, users)) {
                    runDraftCmd(users, ch, args);
                }
                break;
            case "lpgrad":
                List<Member> user = e.getMessage().getMentionedMembers();
                if (checkArgs(ch, args, 6) && user.size() == 1) {
                    runGradCmd(user, ch);
                }
                break;
        }
    }
}
