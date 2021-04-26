package bot.Engine;

import bot.Events;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    April 26, 2021
 * Project: LaunchPoint Bot
 * Module:  Graduate.java
 * Purpose: Enters users into LaunchPoint.
 */
public class Add implements Command {

    /**
     * Runs the add command.
     * @param outChannel the channel to output to, if it exists.
     * @param users the users to attach to the command output, if they exist.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(MessageChannel outChannel, List<Member> users,
                       String[] args) {
        Role role = Events.SERVER.getRolesByName(
                "LaunchPoint", true).get(0);

        for (Member user : users) {
            addRole(user, role);
            sendToDiscord(String.format(
                    "Welcome to LaunchPoint, %s.",
                    user.getUser().getAsTag()));
        }
    }
}
