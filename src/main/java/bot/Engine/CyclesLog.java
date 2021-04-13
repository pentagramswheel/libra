package bot.Engine;

import bot.Discord;
import bot.Events;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;

import java.util.*;
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
     * Runs the cycle logging command.
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

            // change based on current cycle, the Sheet's current tab
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

                    if (args.length == 7) {
                        List<Object> row = player.getStats();
                        int cycleGamesPlayed = Integer.parseInt(args[5]);
                        int cycleGamesWon = Integer.parseInt(args[6]);
                        boolean cycleSetWon =
                                cycleGamesWon >= (cycleGamesPlayed / 2);

                        int setWins =
                                Integer.parseInt(row.get(1).toString());
                        int setLosses =
                                Integer.parseInt(row.get(2).toString());
                        int gamesWon =
                                Integer.parseInt(row.get(5).toString());
                        int gamesLost =
                                Integer.parseInt(row.get(6).toString());

                        if (cycleSetWon) {
                            setWins++;
                        } else {
                            setLosses++;
                        }
                        gamesWon += cycleGamesWon;
                        gamesLost += cycleGamesPlayed - cycleGamesWon;

                        String updateRange = range + "!B" + player.getPosition() + ":J" + player.getPosition();
                        ValueRange newRow = new ValueRange()
                                .setValues(Collections.singletonList(Arrays.asList(
                                        user.getEffectiveName(), setWins,
                                        setLosses, row.get(3), row.get(4),
                                        gamesWon, gamesLost, row.get(7),
                                        row.get(8))));

                        link.updateRow(updateRange, tableVals, newRow);
                    } else {




                    }

                } else {
                    inChannel.sendMessageFormat(
                            "%s was added to the leaderboard.",
                            user.getUser().getAsTag()).queue();
                }
            }

            inChannel.sendMessage("The match report was made.").queue();
        } catch (IOException | GeneralSecurityException e) {
            inChannel.sendMessage("The spreadsheet could not load.").queue();
        }
    }
}
