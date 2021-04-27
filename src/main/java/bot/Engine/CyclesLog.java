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
     * Checks if this it the lpcycle or lpsub command.
     * @param args the user input.
     * @return True if the lpsub command was called.
     *         False if the lpcycle command was called.
     */
    private boolean checkForSub(String[] args) {
        return args[0].equals("lpcycle");
    }

    /**
     * Retrieve the amount of set games were played.
     * @param args the user input.
     * @return said amount.
     */
    private int getGamesPlayed(String[] args) {
        return Integer.parseInt(args[args.length - 2]);
    }

    /**
     * Retrieve the amount of won set games.
     * @param args the user input
     * @return said amount.
     */
    private int getGamesWon(String[] args) {
        return Integer.parseInt(args[args.length - 1]);
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
     * @param notSub a flag to check if the user is a sub or not.
     */
    private void updateUser(GoogleAPI link, Member user, String range,
                            Values tableVals, TreeMap<Object, PlayerStats> table,
                            String[] args, boolean notSub) {
        try {
            PlayerStats player = table.get(user.getId());

            int cycleGamesPlayed = getGamesPlayed(args);
            int cycleGamesWon = getGamesWon(args);

            int setWins = player.getSetWins();
            int setLosses = player.getSetLosses();
            int gamesWon = player.getGamesWon();
            int gamesLost = player.getGamesLost();

            if (notSub) {
                if (cycleSetWon(cycleGamesWon, cycleGamesPlayed)) {
                    setWins++;
                } else {
                    setLosses++;
                }
            }
            gamesWon += cycleGamesWon;
            gamesLost += cycleGamesPlayed - cycleGamesWon;

            String updateRange = range + "!B" + player.getPosition()
                    + ":E" + player.getPosition();
            ValueRange newRow = new ValueRange().setValues(
                    Collections.singletonList(Arrays.asList(
                            player.getName(), player.getNickname(),
                            setWins, setLosses)));
            link.updateRow(updateRange, tableVals, newRow);

            updateRange = range + "!H" + player.getPosition()
                    + ":I" + player.getPosition();
            newRow = new ValueRange().setValues(
                    Collections.singletonList(Arrays.asList(
                            gamesWon, gamesLost)));
            link.updateRow(updateRange, tableVals, newRow);

            sendToDiscord(String.format(
                    "%s's leaderboard stats were updated...",
                    player.getName()));
        } catch (IOException e) {
            sendToDiscord(String.format(
                    "User %s could not be updated...",
                    user.getUser().getAsTag()));
        }
    }

    /**
     * Checks if the user is a sub, then updates a user's stats
     * within a spreadsheet.
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
        updateUser(link, user, range, tableVals, table, args,
                checkForSub(args));
    }

    /**
     * Adds a user's stats within a spreadsheet.
     * @param link a connection to the spreadsheet.
     * @param user the user to update the stats of.
     * @param range the name of the spreadsheet section
     * @param tableVals the values of the spreadsheet section.
     * @param args the user input.
     * @param notSub a flag to check if the user is a sub or not.
     *
     * Note: Users will be added at the next EMPTY row in the spreadsheet.
     */
    private void addUser(GoogleAPI link, Member user, String range,
                         Values tableVals, String[] args, boolean notSub) {
        String userTag = user.getUser().getAsTag();

        try {
            int cycleGamesPlayed = getGamesPlayed(args);
            int cycleGamesWon = getGamesWon(args);

            int setWins = 0;
            int setLosses = 0;

            if (notSub) {
                if (cycleSetWon(cycleGamesWon, cycleGamesPlayed)) {
                    setWins++;
                } else {
                    setLosses++;
                }
            }
            int cycleGamesLost = cycleGamesPlayed - cycleGamesWon;

            ValueRange newRow = new ValueRange().setValues(
                    Collections.singletonList(Arrays.asList(
                            user.getId(), userTag, user.getEffectiveName(),
                            setWins, setLosses, 0, 0, cycleGamesWon,
                            cycleGamesLost, 0, 0)));
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
     * Checks if the user is a sub, then adds a user's stats within
     * a spreadsheet.
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
        addUser(link, user, range, tableVals, args, checkForSub(args));
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

            // tab name of the spreadsheet
            String range = "'Current Leaderboard'";

            Values tableVals = link.getSheet().spreadsheets().values();
            TreeMap<Object, PlayerStats> table = link.readSection(
                    range, tableVals);
            if (table == null) {
                throw new IOException("The spreadsheet was empty.");
            }

            for (Member user : users) {
                if (table.containsKey(user.getId())) {
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
