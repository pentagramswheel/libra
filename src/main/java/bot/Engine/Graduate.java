package bot.Engine;

import bot.Tools.Command;
import bot.Tools.GoogleSheetsAPI;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import com.google.api.services.sheets.v4.model.ValueRange;

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
    private String graduate(SlashCommandEvent sc, String playerID) {
        try {
            String exitMessage;
            GoogleSheetsAPI link = new GoogleSheetsAPI(gradSheetID());

            removeRole(sc, playerID, getRole(sc, getSection()));
            addRole(sc, playerID, getRole(sc, getSection() + " Graduate"));

            if (getPrefix().equals("lp")) {
                addRole(sc, playerID, getRole(sc, "Ink Odyssey"));
                exitMessage = "Congratulations! We look forward to "
                        + "seeing you in Ink Odyssey and outside "
                        + "of MIT.";
            } else {
                exitMessage = "Congratulations! We look forward to "
                        + "seeing you beyond MIT.";
            }

            // tab name of the spreadsheet
            String tab = "'Graduates'";

            TreeMap<Object, PlayerStats> data = link.readSection(sc, tab);
            if (data == null) {
                throw new IOException("The spreadsheet was empty.");
            } else if (!data.containsKey(playerID)) {
                Member player = findMember(sc, playerID);
                ValueRange newRow = link.buildRow(Arrays.asList(
                            playerID, player.getUser().getAsTag(),
                            player.getEffectiveName()));
                link.appendRow(tab, newRow);
            }

            return exitMessage;
        } catch (IOException | GeneralSecurityException e) {
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
        sc.deferReply().queue();
        List<OptionMapping> args = sc.getOptions();

        StringBuilder listOfUsers = new StringBuilder();
        for (OptionMapping om : args) {
            Member player = om.getAsMember();
            String exitMessage = graduate(sc, player.getId());
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

        sendResponse(sc, listOfUsers.toString(), false);
        log(args.size() + " " + getSection() + " graduate(s) processed.", false);
    }
}
