package bot.Engine;

import bot.Discord;

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
     * Retrives the row value at a certain index.
     * @param row the row to access.
     * @param i the index to access.
     * @return said indexed row value.
     */
    private int getValue(List<Object> row, int i) {
        return Integer.parseInt(row.get(i).toString());
    }

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
        String userTag = user.getUser().getAsTag();

        try {
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

            int setWins = getValue(row, 1);
            int setLosses = getValue(row, 2);
            int gamesWon = getValue(row, 5);
            int gamesLost = getValue(row, 6);

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

            sendToDiscord(String.format(
                    "%s's leaderboard stats were updated...",
                    userTag));
        } catch (IOException e) {
            sendToDiscord(String.format(
                    "User %s could not be updated...",
                    userTag));
        }
    }

    /**
     * Adds a user's stats within a spreadsheet.
     * @param link a connection to the spreadsheet.
     * @param user the user to update the stats of.
     * @param range the name of the spreadsheet section
     * @param tableVals the values of the spreadsheet section.
     * @param args the user input.
     *
     * Note: Users will be added at the next EMPTY row in the spreadsheet.
     */
    private void addUser(GoogleAPI link, Member user, String range,
                         Values tableVals, String[] args) {
        String userTag = user.getUser().getAsTag();

        try {
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

            sendToDiscord(String.format(
                    "%s was added to the leaderboard. Be sure to"
                            + " extend the column formulas accordingly"
                            + " (They're set to zero right now)...",
                    userTag));
        } catch (IOException e) {
            sendToDiscord(String.format(
                    "New user %s could not be added...",
                    userTag));
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

            // change based on the current cycle, the Sheet's current tab/cycle
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
                } else {
                    addUser(link, user, range, tableVals, args);
                }
            }

            sendToDiscord("The match report was processed.");
        } catch (IOException | GeneralSecurityException e) {
            sendToDiscord("The spreadsheet could not load.");
        }
    }
}
