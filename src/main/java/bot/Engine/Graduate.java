package bot.Engine;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    March 9, 2021
 * Project: LaunchPoint Bot
 * Module:  Graduate.java
 * Purpose: Graduates a player from LaunchPoint.
 */
public class Graduate implements Command {

    /**
     * Runs the graduate command.
     * @param inChannel the channel the command was sent in.
     * @param outChannel the channel to output to, if it exists.
     * @param users the users to attach to the command output, if they exist.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(MessageChannel inChannel, MessageChannel outChannel,
                       List<Member> users, String[] args) {

    }
}
