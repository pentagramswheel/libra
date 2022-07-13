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

    /**
     * Graduates a user within MIT.
     * @param sc the user's inputted command.
     * @param playerID the Discord ID of the player to graduate.
     * @return a graduation congratulation message.
     */
    private String graduate(SlashCommandEvent sc, String playerID,
                            GoogleSheetsAPI link,
                            TreeMap<Object, Object> data) {
        try {
            String rulesChannel;
            String exitMessage;

            switch (getPrefix()) {
                case "fs":
                    modifyRoles(sc, playerID,
                            Arrays.asList(
                                    getRole(sc, getSection() + " Graduate"),
                                    getRole(sc, "LaunchPoint")),
                            Collections.singletonList(
                                    getRole(sc, getSection())));

                    rulesChannel =
                            getChannel(sc, "lp-draft-rules").getAsMention();
                    exitMessage = "Congratulations! We look forward to "
                            + "seeing you in LaunchPoint. Make sure to "
                            + "read " + rulesChannel + " before playing "
                            + "in any drafts!";
                    break;
                case "lp":
                    modifyRoles(sc, playerID,
                            Arrays.asList(
                                    getRole(sc, getSection() + " Graduate"),
                                    getRole(sc, "Ink Odyssey")),
                            Collections.singletonList(
                                    getRole(sc, getSection())));

                    rulesChannel =
                            getChannel(sc, "io-draft-rules").getAsMention();
                    exitMessage = "Congratulations! We look forward to "
                            + "seeing you in Ink Odyssey. Make sure to "
                            + "read " + rulesChannel + " before playing "
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
        } catch (IOException e) {
            sendResponse(sc, "The spreadsheet could not load.", true);
            log("The " + getSection()
                    + " graduates spreadsheet could not load.", true);
            return null;
        }
    }

    /**
     * Runs the graduation command.
     * @param sc the user's inputted command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        sc.deferReply(false).queue();
        List<OptionMapping> args = sc.getOptions();

        GoogleSheetsAPI link;
        TreeMap<Object, Object> data;
        try {
            link = new GoogleSheetsAPI(gradSheetID());
            data = link.readSection(sc, TAB);
            if (data == null) {
                throw new IOException("The spreadsheet was empty.");
            }
        } catch (IOException | GeneralSecurityException e) {
            sendResponse(sc, "The spreadsheet could not load.", true);
            log("The " + getSection()
                    + " graduates spreadsheet could not load.", true);
            return;
        }

        StringBuilder listOfUsers = new StringBuilder();
        for (OptionMapping om : args) {
            Member player = om.getAsMember();
            String exitMessage = graduate(sc, player.getId(),
                    link, link.readSection(sc, TAB));
            if (exitMessage == null) {
                return;
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
    }
}
