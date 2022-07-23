package bot.Engine;

import bot.Engine.Templates.Command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Collections;
import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    April 26, 2021
 * Project: Libra
 * Module:  Add.java
 * Purpose: Adds users into sections within MIT.
 */
public class Add extends Section implements Command {

    /**
     * Constructs the add attributes.
     * @param abbreviation the abbreviation of the section.
     */
    public Add(String abbreviation) {
        super(abbreviation);
    }

    /**
     * Allows a user entry into a MIT section, if possible.
     * @param sc the user's inputted command.
     * @param playerID the Discord ID of the player to graduate.
     * @return the entrance welcome message.
     */
    private String enter(SlashCommandEvent sc, String playerID) {
        if (sc.getSubcommandName().equals("add")) {
            modifyRoles(sc, playerID,
                    Collections.singletonList(
                            getRole(sc, getSection())),
                    null);

            if (sc.getName().equals("fs")) { // for temp JAR
                return "Welcome to " + getSection() + "! We hope that you "
                        + "are as excited as we are for the upcoming, "
                        + "new section.";
            }

            String rulesChannel =
                    getChannel(sc, getPrefix() + "-draft-rules").getAsMention();
            return "Welcome to " + getSection() + "! Make sure to read "
                    + rulesChannel + " before playing in any drafts!";
        } else {
            return "Unfortunately, you have been denied entry into "
                    + getSection() + " at this time.";
        }
    }

    /**
     * Runs the enter command.
     * @param sc the inputted slash command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        sc.deferReply(false).queue();
        List<OptionMapping> args = sc.getOptions();

        StringBuilder listOfUsers = new StringBuilder();
        for (OptionMapping om : args) {
            Member user = om.getAsMember();
            String welcomeMessage = enter(sc, user.getId());

            Member finalUser = args.get(args.size() - 1).getAsMember();
            if (user.equals(finalUser)) {
                listOfUsers.append(user.getAsMention())
                        .append("\n\n")
                        .append(welcomeMessage);
            } else {
                listOfUsers.append(user.getAsMention()).append(" ");
            }
        }

        editMessage(sc, listOfUsers.toString());
        log(args.size() + " " + getSection()
                + " player(s) processed/denied.", false);
    }
}
