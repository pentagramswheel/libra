package bot.Engine;

import bot.Tools.Command;
import bot.Tools.GoogleAPI;

import net.dv8tion.jda.api.entities.Member;
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
 * Project: LaunchPoint Bot
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
     * @param user the user to graduate.
     * @return a graduation congratulation message.
     */
    private String graduate(Member user) {
        String exitMessage = "";

        try {
            GoogleAPI link = new GoogleAPI(gradSheetID());
            if (getPrefix().equals("lp")) {
                removeRole(user, getRole("LaunchPoint"));
                addRole(user, getRole("LaunchPoint Graduate"));
                addRole(user, getRole("Ink Odyssey"));

                exitMessage = "Congratulations! We look forward to "
                        + "seeing you in Ink Odyssey and outside "
                        + "of MIT.";
            } else {
                removeRole(user, getRole("Ink Odyssey"));
                addRole(user, getRole("Ink Odyssey Graduate"));

                exitMessage = "Congratulations! We look forward to "
                        + "seeing you beyond MIT.";
            }

            // tab name of the spreadsheet
            String tab = "'Graduates'";

            Values spreadsheet = link.getSheet().spreadsheets().values();
            TreeMap<Object, PlayerStats> data = link.readSection(
                    tab, spreadsheet);
            if (data == null) {
                throw new IOException("The spreadsheet was empty.");
            } else if (!data.containsKey(user.getId())) {
                ValueRange newRow = link.buildRow(Arrays.asList(
                            user.getId(), user.getUser().getAsTag(),
                            user.getEffectiveName()));
                link.appendRow(tab, spreadsheet, newRow);
            }
        } catch (IOException | GeneralSecurityException e) {
            sendReply("The spreadsheet could not load.");
            log("The spreadsheet could not load.");
        }

        return exitMessage;
    }

    /**
     * Runs the graduation command.
     * @param cmd the formal name of the command.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(String cmd, List<OptionMapping> args) {
        StringBuilder listOfUsers = new StringBuilder();

        for (OptionMapping om : args) {
            Member user = om.getAsMember();
            String exitMessage = graduate(user);

            Member finalUser = args.get(args.size() - 1).getAsMember();
            if (user.equals(finalUser)) {
                listOfUsers.append(user.getAsMention())
                        .append("\n\n")
                        .append(exitMessage);
            } else {
                listOfUsers.append(user.getAsMention()).append(" ");
            }
        }

        sendReply(listOfUsers.toString());
        log(args.size() + " graduates were processed.");
    }
}
