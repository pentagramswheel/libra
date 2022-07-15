package bot.Engine;

import bot.Tools.Command;
import bot.Tools.GoogleSheetsAPI;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import com.google.api.services.sheets.v4.model.ValueRange;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.TreeMap;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author  Wil Aquino
 * Date:    April 1, 2021
 * Project: Libra
 * Module:  Graduate.java
 * Purpose: Graduates users from LaunchPoint.
 */
public class Graduate extends Section implements Command {

    /** The tab name of the spreadsheet. */
    private static final String TAB = "Graduates";

    /**
     * Constructs the graduation attributes.
     * @param abbreviation the abbreviation of the section.
     */
    public Graduate(String abbreviation) {
        super(abbreviation);
    }

    /** Retrieves the name of the next MIT section. */
    private String getNextSection() {
        if (getSection().equals("Freshwater Shoals")) {
            return "LaunchPoint";
        } else {
            return "Ink Odyssey";
        }
    }

    /** Retrieves the prefix of the next MIT section. */
    private String getNextPrefix() {
        if (getPrefix().equals("fs")) {
            return "lp";
        } else {
            return "io";
        }
    }

    /**
     * Graduates a user within MIT.
     * @param sc the user's inputted command.
     * @param playerID the Discord ID of the player to graduate.
     * @return a graduation congratulation message.
     */
    private String graduate(SlashCommandEvent sc, String playerID,
                            GoogleSheetsAPI link,
                            TreeMap<Object, Object> data) throws IOException {
        String rulesChannel;
        String exitMessage;

        switch (getPrefix()) {
            case "fs":
            case "lp":
                modifyRoles(sc, playerID,
                        Arrays.asList(
                                getRole(sc, getSection() + " Graduate"),
                                getRole(sc, getNextSection())),
                        Collections.singletonList(
                                getRole(sc, getSection())));

                rulesChannel = getChannel(sc, getNextPrefix()
                        + "-draft-rules").getAsMention();
                exitMessage = "Congratulations! We look forward to seeing "
                        + "you in " + getNextSection() + ". Make sure to "
                        + "to read " + rulesChannel + " before playing "
                        + "in any drafts!";
                break;
            default:
                modifyRoles(sc, playerID,
                        Collections.singletonList(
                                getRole(sc, getSection() + " Graduate")),
                        Collections.singletonList(
                                getRole(sc, getSection())));

                exitMessage = "Congratulations! We look forward to "
                        + "seeing you beyond MIT.";
                break;
        }

        if (!data.containsKey(playerID)) {
            Member player = findMember(sc, playerID);
            ValueRange newRow = link.buildRow(Arrays.asList(
                        playerID, player.getUser().getAsTag(),
                        player.getEffectiveName()));
            link.appendRow(TAB, newRow);
        }

        return exitMessage;
    }

    /**
     * Runs the graduation command.
     * @param sc the user's inputted command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        sc.deferReply(false).queue();
        List<OptionMapping> args = sc.getOptions();

        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(gradSheetID());
            TreeMap<Object, Object> data = link.readSection(sc, TAB);

            StringBuilder listOfUsers = new StringBuilder();
            for (OptionMapping om : args) {
                Member player = om.getAsMember();
                String exitMessage = graduate(sc, player.getId(), link, data);
                if (exitMessage == null) {
                    throw new IOException();
                }

                Member finalUser = args.get(args.size() - 1).getAsMember();
                if (player.getId().equals(finalUser.getId())) {
                    listOfUsers.append(player.getAsMention())
                            .append("\n\n")
                            .append(exitMessage);
                } else {
                    listOfUsers.append(player.getAsMention()).append(" ");
                }
            }

            editMessage(sc, listOfUsers.toString());
            log(args.size() + " " + getSection() + " graduate(s) processed.", false);
        } catch (IOException | GeneralSecurityException e) {
            editMessage(sc, "The spreadsheet could not load.");
            log("The " + getSection()
                    + " graduates spreadsheet could not load.", true);
        }
    }
}
