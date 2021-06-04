package bot.Engine;

import bot.Events;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

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
     * Retrieves a role given its name.
     * @param role the name of the role.
     */
    default Role getRole(String role) {
        return Events.SERVER.getRolesByName(
                role, true).get(0);
    }

    /**
     * Adds a role to a user.
     * @param user the given user.
     * @param role the role to add.
     */
    default void addRole(Member user, Role role) {
        Events.SERVER.addRoleToMember(user, role).queue();
    }

    /**
     * Removes a role from a user.
     * @param user the given user.
     * @param role the role to remove.
     */
    default void removeRole(Member user, Role role) {
        Events.SERVER.removeRoleFromMember(user, role).queue();
    }

    /**
     * Send a message to Discord in the channel the original
     * command was sent.
     * @param msg the message to send.
     */
    default void sendToDiscord(String msg) {
        Events.ORIGIN.sendMessage(msg).queue();
    }
}
