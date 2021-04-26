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
 * Module:  Add.java
 * Purpose: Adds roles to users in LaunchPoint.
 */
public class Add implements Command {

    /**
     * Allows users entry into LaunchPoint.
     * @param users the users to add.
     */
    private void enterUsers(List<Member> users) {
        Role role = Events.SERVER.getRolesByName(
                "LaunchPoint", true).get(0);

        for (Member user : users) {
            addRole(user, role);
            sendToDiscord(String.format(
                    "Welcome to LaunchPoint, %s.",
                    user.getUser().getAsTag()));
        }
    }

    /**
     * Allows users to be coaches within LaunchPoint.
     * @param users the user to add.
     */
    private void coachUsers(List<Member> users) {
        Role role = Events.SERVER.getRolesByName(
                "Coaches", true).get(0);

        for (Member user : users) {
            addRole(user, role);
            sendToDiscord(String.format(
                    "Welcome to the LaunchPoint coaches, %s.",
                    user.getUser().getAsTag()));
        }
    }

    /**
     * Runs the add or coach command.
     * @param outChannel the channel to output to, if it exists.
     * @param users the users to attach to the command output, if they exist.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(MessageChannel outChannel, List<Member> users,
                       String[] args) {
        String cmd = args[0];
        if (cmd.equals("lpadd")) {
            enterUsers(users);
        } else {
            coachUsers(users);
        }
    }
}
