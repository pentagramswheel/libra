package bot.Engine;

import bot.Discord;
import bot.Tools.Command;
import bot.Tools.GoogleAPI;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
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
public class Graduate extends bot.Events implements Command {

    /**
     * Graduates a user from LaunchPoint.
     * @param cmd the formal name of the command.
     * @param user the user to graduate.
     * @return a graduation congratulation message.
     */
    private String graduate(String cmd, Member user) {
        Role lpRole = getRole("LaunchPoint");
        Role lpGradRole = getRole("LaunchPoint Graduate");
        Role ioRole = getRole("Ink Odyssey");
        Role ioGradRole = getRole("Ink Odyssey Graduate");
        String exitMessage = "";

        try {
            GoogleAPI link;
            if (cmd.equals("lpgrad")) {
                removeRole(user, lpRole);
                addRole(user, lpGradRole);
                addRole(user, ioRole);

                link = new GoogleAPI(Discord.getLPGradSheetID());
                exitMessage = "Congratulations! We look forward to "
                        + "seeing you in Ink Odyssey and outside "
                        + "of MIT.";
            } else {
                removeRole(user, ioRole);
                addRole(user, ioGradRole);

                link = new GoogleAPI(Discord.getIOGradSheetID());
                exitMessage = "Congratulations! We look forward to "
                        + "seeing you beyond MIT.";
            }

            String tab = "'Graduates'";
            Values spreadsheet = link.getSheet().spreadsheets().values();
            TreeMap<Object, PlayerStats> data = link.readSection(
                    tab, spreadsheet);

            if (data == null) {
                throw new IOException("The spreadsheet was empty.");
            } else if (data.containsKey(user.getId())) {
                sendReply(String.format(
                        "%s has already graduated from LaunchPoint.",
                        user.getUser().getAsTag()));
            } else {
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
        listOfUsers.append("```\n");

        for (OptionMapping om : args) {
            Member user = om.getAsMember();
            String exitMessage = graduate(cmd, user);

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
