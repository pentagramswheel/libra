package bot.Tools;

import bot.Events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

import java.util.ArrayList;
import java.util.Collections;
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
    void runCmd(String cmd, List<OptionMapping> args);

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
     * Send a list of embedded messages to Discord in the place the
     * original interaction was made.
     * @param ebs the embeds to send.
     */
    default void sendEmbeds(List<EmbedBuilder> ebs) {
        ArrayList<MessageEmbed> builtEmbeds = new ArrayList<>(ebs.size());
        for (EmbedBuilder embed : ebs) {
            builtEmbeds.add(embed.build());
        }

        Events.INTERACTION.sendMessageEmbeds(builtEmbeds).queue();
    }

    /**
     * Send an embedded message to Discord in the place the
     * original command was sent.
     * @param eb the embed to send.
     */
    default void sendEmbed(EmbedBuilder eb) {
        sendEmbeds(Collections.singletonList(eb));
    }

    /**
     * Disables a button.
     * @param bc the button which was clicked.
     * @param newLabel the new label of the button.
     */
    default void disableButton(ButtonClickEvent bc, String newLabel) {
        String id = bc.getId();
        bc.getInteraction().editButton(
                Button.secondary(id, newLabel).asDisabled()).queue();
    }

    /**
     * Links a group of buttons to an interaction.
     * @param caption the caption of the button group.
     * @param ids the reference names of the buttons.
     * @param labels the labels for the buttons.
     * @param types the types of the buttons.
     */
    default void sendButtons(String caption, List<String> ids,
                             List<String> labels, List<Integer> types) {
        ReplyAction reply = Events.INTERACTION.getInteraction().reply(caption);
        for (int i = 0; i < ids.size(); i++) {
            String currID = ids.get(i);
            String currLabel = labels.get(i);
            int currType = types.get(i);
            Button button;

            switch (currType) {
                case 1:
                    button = Button.success(currID, currLabel);
                    break;
                case 2:
                    button = Button.secondary(currID, currLabel);
                    break;
                case 3:
                    button = Button.danger(currID, currLabel);
                    break;
                default:
                    button = Button.primary(currID, currLabel);
                    break;
            }
            reply.addActionRow(button);
        }

        reply.queue();
    }

    /**
     * Links a button to an interaction.
     * @param caption the caption of the button.
     * @param id the reference name of the button.
     * @param label the label for the button.
     * @param type the type of the button.
     */
    default void sendButton(String caption, String id, String label,
                            int type) {
        sendButtons(caption,
                Collections.singletonList(id),
                Collections.singletonList(label),
                Collections.singletonList(type));
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
