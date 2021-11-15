package bot.Engine;

import bot.Discord;
import bot.Tools.Command;
import bot.Tools.GoogleAPI;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;

import java.util.List;
import java.util.Collections;
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

    /** The Discord LaunchPoint role. */
    private final Role lpRole = getRole("LaunchPoint");

    /** The Discord LP Graduate role. */
    private final Role gradRole = getRole("LaunchPoint Graduate");

    /**
     * Graduates a user from LaunchPoint.
     * @param user the user to graduate.
     */
    private void graduate(Member user) {
        removeRole(user, lpRole);
        addRole(user, gradRole);

        try {
            GoogleAPI link = new GoogleAPI(Discord.getGradSheetID());
            String tab = "'Graduates'";
            Values spreadsheet = link.getSheet().spreadsheets().values();
            TreeMap<Object, PlayerStats> data = link.readSection(
                    tab, spreadsheet);

            if (data == null) {
                throw new IOException("The spreadsheet was empty.");
            } else if (data.containsKey(user.getId())) {
                sendToDiscord(String.format(
                        "%s has already graduated from LaunchPoint.",
                        user.getUser().getAsTag()));
            } else {
                ValueRange newRow = new ValueRange().setValues(
                    Collections.singletonList(Arrays.asList(
                            user.getId(), user.getUser().getAsTag(),
                            user.getEffectiveName())));
                link.appendRow(tab, spreadsheet, newRow);
            }
        } catch (IOException | GeneralSecurityException e) {
            sendToDiscord("The spreadsheet could not load.");
            log("The spreadsheet could not load.");
        }
    }

    /**
     * Runs the graduation command.
     * @param outChannel the channel to output to, if it exists.
     * @param users the users to attach to the command output, if they exist.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(MessageChannel outChannel, List<Member> users,
                       String[] args) {
        StringBuilder listOfUsers = new StringBuilder();
        listOfUsers.append("```\n");

        sendToDiscord("Processing users...");
        for (Member user : users) {
            graduate(user);

            Member finalUser = users.get(users.size() - 1);
            if (user.equals(finalUser)) {
                String welcomeMessage = "Congratulations. We look forward to "
                        + "seeing you in Maiden Voyage and outside of "
                        + "LaunchPoint.";
                listOfUsers.append(user.getUser().getAsTag())
                        .append("\n```")
                        .append(welcomeMessage);
            } else {
                listOfUsers.append(user.getUser().getAsTag()).append(", ");
            }
        }

        sendToDiscord(listOfUsers.toString());
        log(users.size() + " graduates were processed.");
    }
}
