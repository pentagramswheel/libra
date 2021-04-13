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
 * Date:    February 17, 2021
 * Project: LaunchPoint Bot
 * Module:  Events.java
 * Purpose: Logs cycle information via command.
 */
public class CyclesLog implements Command {

    /**
     * Runs the draft logging command.
     * @param inChannel the channel the command was sent in.
     * @param outChannel the channel to output to, if it exists.
     * @param users the users to attach to the command output, if they exist.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(MessageChannel inChannel, MessageChannel outChannel,
                       List<Member> users, String[] args) {
        try {
            GoogleAPI link = new GoogleAPI(Discord.getCyclesSheetID());
            String range = "'Cycle 7'";
            Values tableVals = link.getSheet().spreadsheets().values();
            TreeMap<Object, PlayerStats> table = link.readSection(
                    inChannel, range, tableVals);

            for (Member user : users) {
                if (table == null) {
                    inChannel.sendMessage("The spreadsheet was empty.").queue();
                } else if (table.containsKey(user.getUser().getAsTag())) {

                    String userTag = user.getUser().getAsTag();
                    PlayerStats player = table.get(userTag);
                    inChannel.sendMessageFormat("Row %s - %s has won %s sets and lost %s sets.", player.getRow(), userTag, player.getStats().get(1), player.getStats().get(2)).queue();


                } else {
                    inChannel.sendMessageFormat(
                            "%s did not previously exist.",
                            user.getUser().getAsTag()).queue();
                }
            }

            inChannel.sendMessage("The match report was made.").queue();
        } catch (IOException | GeneralSecurityException e) {
            inChannel.sendMessage("The spreadsheet could not load.").queue();
        }
    }
}
