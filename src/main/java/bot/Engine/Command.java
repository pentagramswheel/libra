package bot.Engine;

import bot.Events;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    February 17, 2021
 * Project: LaunchPoint Bot
 * Module:  Command.java
 * Purpose: Template for command classes.
 */
public interface Command {

    /**
     * Runs the command.
     * @param outChannel the channel to output to, if it exists.
     * @param users the users to attach to the command output, if they exist.
     * @param args the arguments of the command, if they exist.
     */
    void runCmd(MessageChannel outChannel, List<Member> users, String[] args);

    /**
     * Send a message to Discord in the channel the original
     * command was sent.
     * @param msg the message to send.
     */
    default void sendToDiscord(String msg) {
        Events.ORIGIN.sendMessage(msg).queue();
    }
}
