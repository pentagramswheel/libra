package bot.Tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.Button;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author  Wil Aquino, Turtle#1504
 * Date:    February 17, 2021
 * Project: Libra
 * Module:  Command.java
 * Purpose: Template for command classes.
 */
public interface Command {

    /**
      * Runs the slash command.
      * @param sc the command to analyze.
      */
    void runCmd(SlashCommandEvent sc);

    /**
     * Retrieves the users of a slash command.
     * @param sc the command to analyze.
     */
    default List<OptionMapping> extractUsers(SlashCommandEvent sc) {
        List<OptionMapping> users = new ArrayList<>();
        for (OptionMapping om : sc.getOptions()) {
            if (om.getType().equals(OptionType.USER)) {
                users.add(om);
            }
        }

        return users;
    }

    /**
     * Retrieves a user given their Discord ID.
     * @param interaction the user interaction calling this method.
     * @param id the Discord IO of the user.
     * @return the user.
     *         null otherwise.
     */
    default Member findMember(GenericInteractionCreateEvent interaction,
                              String id) {
        try {
            Guild server = interaction.getGuild();
            if (server == null) {
                throw new NullPointerException("Server link disconnected.");
            }

            Member foundMember = server.retrieveMemberById(id).complete();
            if (foundMember == null) {
                throw new NullPointerException("A member could not be found.");
            }

            return foundMember;
        } catch (NullPointerException e) {
            log("A user could not be found.", true);
            return null;
        }
    }

    /**
     * Retrieves a role given its name.
     * @param interaction the user interaction calling this method.
     * @param role the name of the role.
     * @return the role.
     *         null otherwise.
     */
    default Role getRole(GenericInteractionCreateEvent interaction,
                         String role) {
        try {
            Guild server = interaction.getGuild();
            if (server == null) {
                throw new NullPointerException("Server link disconnected.");
            }

            return server.getRolesByName(role, true).get(0);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            log("The role, " + role + ", could not be found.", true);
            return null;
        }
    }

    /**
     * Modifies the roles of a user.
     * @param interaction the user interaction calling this method.
     * @param id the Discord ID of the user.
     * @param toAdd the roles to add to the user.
     * @param toRemove the roles to remove from the user.
     */
    default void modifyRoles(GenericInteractionCreateEvent interaction,
                        String id, List<Role> toAdd, List<Role> toRemove) {
        try {
            Guild server = interaction.getGuild();
            if (server == null) {
                throw new NullPointerException("Server link disconnected.");
            }

            Member user = findMember(interaction, id);
            if (user == null) {
                throw new NullPointerException("Member could not be found.");
            }

            server.modifyMemberRoles(user, toAdd, toRemove).queue();
        } catch (NullPointerException e) {
            log("The server or member could not be found.", true);
        }
    }

    /**
     * Retrieves a channel given its name.
     * @param interaction the user interaction calling this method.
     * @param channel the name of the channel.
     * @return the channel.
     *         null otherwise.
     */
    default TextChannel getChannel(GenericInteractionCreateEvent interaction,
                                   String channel) {
        try {
            Guild server = interaction.getGuild();
            if (server == null) {
                throw new NullPointerException("Server link disconnected.");
            }

            return server.getTextChannelsByName(channel, true).get(0);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            log("The channel, " + channel + ", could not be found.", true);
            return null;
        }
    }

    /**
     * Sends a response to the user's interaction.
     * @param interaction the user interaction calling this method.
     * @param msg the message to send.
     * @param isEphemeral True if this should be an ephemeral message.
     *                    False otherwise.
     *
     * Note: The interaction must have been acknowledged before
     *       this method. If the interaction has deferred its reply,
     *       its ephemeral state follows the declared reply state.
     */
    default void sendResponse(GenericInteractionCreateEvent interaction,
                              String msg, boolean isEphemeral) {
        interaction.getHook().sendMessage(msg).setEphemeral(isEphemeral).queue();
    }

    /**
     * Sends a reply to the user's interaction.
     * @param interaction the user interaction calling this method.
     * @param msg the message to send.
     * @param isEphemeral True if this should be an ephemeral message.
     *                    False otherwise.
     */
    default void sendReply(GenericInteractionCreateEvent interaction,
                           String msg, boolean isEphemeral) {
        interaction.reply(msg).setEphemeral(isEphemeral).queue();
    }

    /**
     * Edits the message the user's interaction is linked with.
     * @param interaction the user interaction calling this method.
     * @param msg the message to send.
     *
     * Note: The interaction must have been acknowledged before
     *       this method.
     */
    default void editMessage(GenericInteractionCreateEvent interaction,
                             String msg) {
        interaction.getHook().editOriginal(msg).queue();
    }

    /**
     * Edits/sends a list of embedded messages linked with the
     * user's interaction.
     * @param interaction the user interaction calling this method.
     * @param ebs the embeds to send.
     *
     * Note: The interaction must have been acknowledged before
     *       this method.
     */
    default void sendEmbeds(GenericInteractionCreateEvent interaction,
                            List<EmbedBuilder> ebs) {
        ArrayList<MessageEmbed> builtEmbeds = new ArrayList<>(ebs.size());
        for (EmbedBuilder embed : ebs) {
            builtEmbeds.add(embed.build());
        }

        interaction.getHook().editOriginalEmbeds(builtEmbeds).queue();
    }

    /**
     * Edits/sends an embedded message linked with the
     * user's interaction.
     * @param interaction the user interaction calling this method.
     * @param eb the embed to send.
     *
     * Note: The interaction must have been acknowledged before
     *       this method.
     */
    default void sendEmbed(GenericInteractionCreateEvent interaction,
                           EmbedBuilder eb) {
        sendEmbeds(interaction, Collections.singletonList(eb));
    }

    /**
     * Replaces the parent link of buttons for a button,
     * by linking new buttons to the interaction.
     * @param interaction the user interaction calling this method.
     * @param caption the caption above the buttons
     * @param buttons the new buttons to link.
     *
     * Note: The interaction must have been acknowledged before
     *       this method.
     */
    default void sendButtons(GenericInteractionCreateEvent interaction,
                             String caption, List<Button> buttons) {
        interaction.getHook().editOriginal(caption)
                .setActionRow(buttons).queue();
    }

    /**
     * Replaces a parent button for a button, by linking
     * a new button to the interaction.
     * @param interaction the user interaction calling this method.
     * @param caption the caption above the button.
     * @param button the new button to link.
     *
     * Note: The interaction must have been acknowledged before
     *       this method.
     */
    default void sendButton(GenericInteractionCreateEvent interaction,
                            String caption, Button button) {
        sendButtons(interaction, caption, Collections.singletonList(button));
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
    default void log(String msg, boolean isProblem) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        if (isProblem) {
            logger.error(msg);
        } else {
            logger.info(msg);
        }
    }
}
