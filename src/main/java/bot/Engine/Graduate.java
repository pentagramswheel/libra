package bot.Engine;

import bot.Discord;
import bot.Events;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;
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
public class Graduate implements Command {

    /**
     * Adds graduate role to a user.
     * @param user the given user.
     */
    private void addRole(Member user) {
        Role role = Events.SERVER.getRolesByName(
                "LaunchPoint Graduate", true).get(0);
        Events.SERVER.addRoleToMember(user, role).queue();
    }

    /**
     * Graduates a user from LaunchPoint.
     * @param user the user to graduate.
     */
    private void graduateUser(Member user) {
        try {
            GoogleAPI link = new GoogleAPI(Discord.getGradSheetID());
            String range = "'Graduates'";
            Values tableVals = link.getSheet().spreadsheets().values();
            TreeMap<Object, PlayerStats> table = link.readSection(
                    range, tableVals);

            if (table == null) {
                sendToDiscord("The retrieved data was empty.");
            } else if (table.containsKey(user.getId())) {
                sendToDiscord(String.format(
                        "%s has already graduated from LaunchPoint.",
                        user.getUser().getAsTag()));
            } else {
                ValueRange appendName = new ValueRange().setValues(
                    Collections.singletonList(Arrays.asList(
                            user.getId(), user.getUser().getAsTag(),
                            user.getEffectiveName())));
                link.appendRow(range, tableVals, appendName);

                sendToDiscord(String.format(
                        "Congratulations %s. We look forward to seeing you "
                                + "outside of LaunchPoint.",
                        user.getUser().getAsTag()));
            }
        } catch (IOException | GeneralSecurityException e) {
            sendToDiscord("The spreadsheet could not load.");
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
        for (Member user : users) {
            addRole(user);
            graduateUser(user);
        }
    }
}
