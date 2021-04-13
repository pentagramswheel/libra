package bot.Engine;

import bot.Discord;
import bot.Events;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author  Wil Aquino
 * Date:    April 1, 2021
 * Project: LaunchPoint Bot
 * Module:  Graduate.java
 * Purpose: Graduates a user from LaunchPoint.
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
     * Runs the graduation command.
     * @param inChannel the channel the command was sent in.
     * @param outChannel the channel to output to, if it exists.
     * @param users the users to attach to the command output, if they exist.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(MessageChannel inChannel, MessageChannel outChannel,
                       List<Member> users, String[] args) {
        Member user = users.get(0);
        addRole(user);

        try {
            GoogleAPI link = new GoogleAPI(Discord.getSheetID());
            String range = "graduates";
            Values tableVals = link.getSheet().spreadsheets().values();
            TreeMap<Object, List<Object>> table = link.getSection(
                    inChannel, range, tableVals);

            if (table == null) {
                inChannel.sendMessage("The spreadsheet was empty.").queue();
            } else if (table.containsKey(user.getUser().getAsTag())) {
                inChannel.sendMessage("User has already graduated.").queue();
            } else {
                ArrayList<Object> lstWithName = new ArrayList<>();
                lstWithName.add(user.getUser().getAsTag());

                ValueRange appendName = new ValueRange().setValues(
                        Collections.singletonList(lstWithName));
                link.appendRow(range, tableVals, appendName);

                inChannel.sendMessage("Player graduated from LaunchPoint.").queue();
            }
        } catch (IOException | GeneralSecurityException e) {
            inChannel.sendMessage("The spreadsheet could not load.").queue();
        }
    }
}
