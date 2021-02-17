package bot;

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
 * Usages:
 */
public class Events extends ListenerAdapter {

    /** Field for storing the current state of the bot. */
    public static JDABuilder bot;

    /**
     * Checks if a command has the correct amount of arguments.
     * @param ch the channel to send an error message in.
     * @param args the list of arguments to check.
     * @param n the number of arguments to have.
     */
    private void checkArgs(MessageChannel ch, String[] args, int n) {
        if (args.length > n) {
            ch.sendMessage("Invalid argument input. "
                    + "See `--help` for more info.").queue();
        }
    }

    /**
     * Runs the "--help", "--introduce", or "--thankyou" command.
     * @param user the user who sent the command.
     * @param ch the channel the command was ran in.
     * @param args the arguments of the command.
     */
    private void runIntroCmd(Member user, MessageChannel ch, String[] args) {
//        Introduction intro = new Introduction();
//        intro.runCmd(ch, null, user, args);
    }
}
