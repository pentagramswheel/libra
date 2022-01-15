package bot.Tools;

import bot.Events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

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
    void runCmd(String cmd, List<OptionMapping> args);

    /**
     * Retrieves a role given its name.
     * @param role the name of the role.
     * @return the role.
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
     * Retrieves a channel given its name.
     * @param channel the name of the channel.
     * @return the channel.
     */
    default TextChannel getChannel(String channel) {
        return Events.SERVER.getTextChannelsByName(channel, true).get(0);
    }

    /**
     * Send a reply to the user's interaction made in Discord.
     * @param msg the message to send.
     */
    default void sendReply(String msg) {
        Events.INTERACTION.sendMessage(msg).queue();
    }

    /**
     * Send a message to Discord in the place the user's
     * interaction was made.
     * @param msg the message to send.
     */
    default void sendMessage(String msg) {
        Events.INTERACTION.getInteraction().getMessageChannel().sendMessage(
                msg).queue();
    }

    /**
     * Edits/sends a list of embedded messages linked with the
     * user's interaction.
     * @param ebs the embeds to send.
     */
    default void editEmbeds(List<EmbedBuilder> ebs) {
        ArrayList<MessageEmbed> builtEmbeds = new ArrayList<>(ebs.size());
        for (EmbedBuilder embed : ebs) {
            builtEmbeds.add(embed.build());
        }

        Events.INTERACTION.editOriginalEmbeds(builtEmbeds).queue();
    }

    /**
     * Edits/sends an embedded message linked with the
     * user's interaction.
     * @param eb the embed to send.
     */
    default void editEmbed(EmbedBuilder eb) {
        editEmbeds(Collections.singletonList(eb));
    }

    /**
     * Links a line of buttons to the user's interaction.
     * @param caption the caption of the button group.
     * @param buttons the group of buttons to link.
     */
    default void sendButtons(String caption, List<Button> buttons) {
        ReplyAction reply = Events.INTERACTION.getInteraction().reply(caption);
        reply.addActionRow(buttons).queue();
    }

    /**
     * Links a button to the user's interaction.
     * @param caption the caption of the button.
     * @param button the button to link.
     */
    default void sendButton(String caption, Button button) {
        sendButtons(caption, Collections.singletonList(button));
    }

    /**
     * Replaces the parent link of buttons for a button,
     * by linking new buttons to the interaction.
     * @param bc one of the original buttons.
     * @param buttons the new buttons to link.
     */
    default void editButtons(ButtonClickEvent bc, List<Button> buttons) {
        bc.getHook().editOriginalComponents().setActionRow(buttons).queue();
    }

    /**
     * Replaces a parent button for a button, by linking
     * a new button to the interaction.
     * @param bc the original button.
     * @param button the new button to link.
     */
    default void editButton(ButtonClickEvent bc, Button button) {
        editButtons(bc, Collections.singletonList(button));
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
        System.out.println(msg + " (" + new Date() + ")");
    }
}
