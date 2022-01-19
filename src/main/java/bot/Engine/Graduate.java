package bot.Engine;

import bot.Tools.Command;
import bot.Tools.GoogleAPI;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;

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
     * Graduates a user from LaunchPoint.
     * @param sc the user's inputted command.
     * @param user the user to graduate.
     * @return a graduation congratulation message.
     */
    private String graduate(SlashCommandEvent sc, Member user) {
        String exitMessage = "";

        try {
            GoogleAPI link = new GoogleAPI(gradSheetID());
            removeRole(sc, user.getId(), getRole(sc, getSection()));
            addRole(sc, user.getId(), getRole(sc, getSection() + " Graduate"));

            if (getPrefix().equals("lp")) {
                addRole(sc, user.getId(), getRole(sc, "Ink Odyssey"));
                exitMessage = "Congratulations! We look forward to "
                        + "seeing you in Ink Odyssey and outside "
                        + "of MIT.";
            } else {
                exitMessage = "Congratulations! We look forward to "
                        + "seeing you beyond MIT.";
            }

            // tab name of the spreadsheet
            String tab = "'Graduates'";

            Values spreadsheet = link.getSheet().spreadsheets().values();
            TreeMap<Object, PlayerStats> data = link.readSection(
                    sc, tab, spreadsheet);
            if (data == null) {
                throw new IOException("The spreadsheet was empty.");
            } else if (!data.containsKey(user.getId())) {
                ValueRange newRow = link.buildRow(Arrays.asList(
                            user.getId(), user.getUser().getAsTag(),
                            user.getEffectiveName()));
                link.appendRow(tab, spreadsheet, newRow);
            }
        } catch (IOException | GeneralSecurityException e) {
            sendResponse(sc, "The spreadsheet could not load.", true);
            log("The " + getSection()
                    + " graduates spreadsheet could not load.", true);
        }

        return exitMessage;
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
            Member user = om.getAsMember();
            String exitMessage = graduate(sc, user);

            Member finalUser = args.get(args.size() - 1).getAsMember();
            if (user.equals(finalUser)) {
                listOfUsers.append(user.getAsMention())
                        .append("\n\n")
                        .append(exitMessage);
            } else {
                listOfUsers.append(user.getAsMention()).append(" ");
            }
        }

        sendResponse(sc, listOfUsers.toString(), false);
        log(args.size() + " " + getSection() + " graduate(s) processed.", false);
    }
}
