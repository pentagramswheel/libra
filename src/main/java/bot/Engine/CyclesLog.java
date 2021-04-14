package bot.Engine;

import bot.Discord;
import bot.Events;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

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
 * Date:    February 17, 2021
 * Project: LaunchPoint Bot
 * Module:  Events.java
 * Purpose: Logs cycle information via command.
 */
public class CyclesLog implements Command {

    /**
     * Checks if a set was won.
     * @param won the amount of won games.
     * @param played the total amount of games played.
     * @return True if the set was won.
     *         False if the set was lost.
     */
    private boolean cycleSetWon(int won, int played) {
        double halfPlayed = (double) played / 2;
        return won >= halfPlayed;
    }

    /**
     * Updates a user's stats within a spreadsheet.
     * @param link a connection to the spreadsheet.
     * @param user the user to update the stats of.
     * @param range the name of the spreadsheet section
     * @param tableVals the values of the spreadsheet section.
     * @param table a map of all rows of the spreadsheet.
     * @param args the user input.
     */
    private void updateUser(GoogleAPI link, Member user, String range,
                            Values tableVals, TreeMap<Object, PlayerStats> table,
                            String[] args) {
        try {
            String userTag = user.getUser().getAsTag();
            PlayerStats player = table.get(userTag);
            List<Object> row = player.getStats();

            int cycleGamesPlayed, cycleGamesWon;
            if (args.length == 7) {
                cycleGamesPlayed = Integer.parseInt(args[5]);
                cycleGamesWon = Integer.parseInt(args[6]);
            } else {
                cycleGamesPlayed = Integer.parseInt(args[2]);
                cycleGamesWon = Integer.parseInt(args[3]);
            }

            int setWins =
                    Integer.parseInt(row.get(1).toString());
            int setLosses =
                    Integer.parseInt(row.get(2).toString());
            int gamesWon =
                    Integer.parseInt(row.get(5).toString());
            int gamesLost =
                    Integer.parseInt(row.get(6).toString());

            if (cycleSetWon(cycleGamesWon, cycleGamesPlayed)) {
                setWins++;
            } else {
                setLosses++;
            }
            gamesWon += cycleGamesWon;
            gamesLost += cycleGamesPlayed - cycleGamesWon;

            String updateRange = range + "!B" + player.getPosition()
                    + ":D" + player.getPosition();
            ValueRange newRow = new ValueRange().setValues(
                    Collections.singletonList(Arrays.asList(
                            user.getEffectiveName(), setWins, setLosses)));
            link.updateRow(updateRange, tableVals, newRow);

            updateRange = range + "!G" + player.getPosition()
                    + ":H" + player.getPosition();
            newRow = new ValueRange().setValues(
                    Collections.singletonList(Arrays.asList(
                            gamesWon, gamesLost)));
            link.updateRow(updateRange, tableVals, newRow);
        } catch (IOException e) {
            sendToDiscord("User could not be updated.");
        }
    }

    /**
     * Adds a user's stats within a spreadsheet.
     * @param link a connection to the spreadsheet.
     * @param user the user to update the stats of.
     * @param range the name of the spreadsheet section
     * @param tableVals the values of the spreadsheet section.
     * @param args the user input.
     */
    private void addUser(GoogleAPI link, Member user, String range,
                         Values tableVals, String[] args) {
        try {
            String userTag = user.getUser().getAsTag();

            int cycleGamesPlayed, cycleGamesWon;
            if (args.length == 7) {
                cycleGamesPlayed = Integer.parseInt(args[5]);
                cycleGamesWon = Integer.parseInt(args[6]);
            } else {
                cycleGamesPlayed = Integer.parseInt(args[2]);
                cycleGamesWon = Integer.parseInt(args[3]);
            }

            int setWins = 0;
            int setLosses = 0;
            if (cycleSetWon(cycleGamesWon, cycleGamesPlayed)) {
                setWins++;
            } else {
                setLosses++;
            }
            int gamesWon = cycleGamesWon;
            int gamesLost = cycleGamesPlayed - cycleGamesWon;

            ValueRange newRow = new ValueRange().setValues(
                    Collections.singletonList(Arrays.asList(
                            userTag, user.getEffectiveName(), setWins,
                            setLosses, 0, 0, gamesWon, gamesLost, 0, 0)));
            link.appendRow(range, tableVals, newRow);
        } catch (IOException e) {
            sendToDiscord("New user could not be added.");
        }
    }

    /**
     * Runs the cycle logging command.
     * @param outChannel the channel to output to, if it exists.
     * @param users the users to attach to the command output, if they exist.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(MessageChannel outChannel,
                       List<Member> users, String[] args) {
        try {
            GoogleAPI link = new GoogleAPI(Discord.getCyclesSheetID());

            // change based on the current cycle, the Sheet's current tab
            String range = "'Cycle 7'";

            Values tableVals = link.getSheet().spreadsheets().values();
            TreeMap<Object, PlayerStats> table = link.readSection(
                    range, tableVals);
            if (table == null) {
                sendToDiscord("The spreadsheet was empty.");
                return;
            }

            for (Member user : users) {
                if (table.containsKey(user.getUser().getAsTag())) {
                    updateUser(link, user, range, tableVals, table, args);
                    sendToDiscord(String.format(
                            "%s's leaderboard stats were updated...",
                            user.getUser().getAsTag()));
                } else {
                    addUser(link, user, range, tableVals, args);
                    sendToDiscord(String.format(
                            "%s was added to the leaderboard. Be sure to"
                                    + " extend the column formulas accordingly"
                                    + " (They're set to zero right now)...",
                            user.getUser().getAsTag()));
                }
            }

            sendToDiscord("The match report was processed.");
        } catch (IOException | GeneralSecurityException e) {
            sendToDiscord("The spreadsheet could not load.");
        }
    }
}
