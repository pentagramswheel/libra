package bot.Tools;

import bot.Events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

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
     * @param cmd the formal name of the command.
     * @param args the arguments of the command, if they exist.
     */
    void runCmd(MessageChannel outChannel, String cmd,
                List<OptionMapping> args);

    /**
     * Retrieves a role given its name.
     * @param role the name of the role.
     */
    default Role getRole(String role) {
        return Events.SERVER.getRolesByName(role, true).get(0);
    }

    /**
     * Adds a role to a user.
     * @param user the given user.
     * @param role the role to add.
     */
    default void addRole(Member user, Role role) {
        List<Role> roleList = user.getRoles();
        while (!roleList.contains(role)) {
            Events.SERVER.addRoleToMember(user.getId(), role).queue();

            // prevent Discord rate limiting
            wait(2000);
            user = Events.SERVER.retrieveMemberById(user.getId()).complete();
            roleList = user.getRoles();
        }
    }

    /**
     * Removes a role from a user.
     * @param user the given user.
     * @param role the role to remove.
     */
    default void removeRole(Member user, Role role) {
        while (user.getRoles().contains(role)) {
            Events.SERVER.removeRoleFromMember(user.getId(), role).queue();

            // prevent Discord rate limiting
            wait(2000);
            user = Events.SERVER.retrieveMemberById(user.getId()).complete();
        }
    }

    /**
     * Send a message to Discord in the place the original
     * interaction was made.
     * @param msg the message to send.
     */
    default void sendReply(String msg) {
        Events.INTERACTION.sendMessage(msg).queue();
    }

    /**
     * Send a formatted message to Discord in the place the
     * original interaction was made.
     * @param msg the message to send.
     */
    default void sendFormat(String msg, Object... args) {
        Events.INTERACTION.sendMessageFormat(msg, args).queue();
    }

    /**
     * Send an embedded message to Discord in the place the
     * original interaction was made.
     * @param eb the embed to send.
     * @param spam a flag to check whether multiple embeds are
     *             being sent or not.
     */
    default void sendEmbed(EmbedBuilder eb, boolean spam) {
        if (spam) {
            Events.ORIGIN.sendMessageEmbeds(eb.build()).queue();
        } else {
            Events.INTERACTION.sendMessageEmbeds(eb.build()).queue();
        }
    }

    /**
     * Send an embedded message to Discord in the place the
     * original command was sent.
     * @param eb the embed to send.
     */
    default void sendEmbed(EmbedBuilder eb) {
        sendEmbed(eb, false);
    }

    /**
     * Pause the program for a certain amount of time.
     * @param ms the time to pause in milliseconds.
     */
    default void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Log the processed command's actions to the console.
     * @param msg the message to to attach to the log.
     */
    default void log(String msg) {
        System.out.println(msg + " (" + Time.currentTime() + ")");
    }
}
