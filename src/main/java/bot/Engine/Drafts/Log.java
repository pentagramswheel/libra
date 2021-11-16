package bot.Engine.Drafts;

import bot.Discord;
import bot.Engine.PlayerStats;
import bot.Tools.Command;
import bot.Tools.GoogleAPI;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;

import java.awt.Color;
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
 * Module:  Log.java
 * Purpose: Logs cycle information via command.
 */
public class Log extends bot.Events implements Command {

    /**
     * Checks if this it the lpcycle or lpsub command.
     * @param args the user input.
     * @return True if the lpsub command was called.
     *         False if the lpcycle command was called.
     */
    public boolean checkForSub(String[] args) {
        return args[0].equals("LPCYCLE");
    }

    /**
     * Retrieve the amount of set games were played.
     * @param args the user input.
     * @return said amount.
     */
    public int getGamesPlayed(String[] args) {
        return Integer.parseInt(args[args.length - 2]);
    }

    /**
     * Retrieve the amount of won set games.
     * @param args the user input
     * @return said amount.
     */
    public int getGamesWon(String[] args) {
        return Integer.parseInt(args[args.length - 1]);
    }

    /**
     * Checks if a set was won.
     * @param won the amount of won games.
     * @param played the total amount of games played.
     * @return True if the set was won.
     *         False if the set was lost.
     */
    public boolean cycleSetWon(int won, int played) {
        double halfPlayed = (double) played / 2;
        return won >= halfPlayed;
    }

    /**
     * Sums the values of an array.
     * @param arr the array to find the sum of.
     * @param i the index of the array to add.
     * @return the sum of the values within the array.
     */
    public int sum(int[] arr, int i) {
        if (i == 0) {
            return arr[i];
        } else {
            return arr[i] + sum(arr, i - 1);
        }
    }

    /**
     * Print the summary of the cycle match report.
     * @param wins the amount of games won by the players.
     * @param losses the amount of games lost by the players.
     * @param players the players within the set.
     * @param playerTypes array of types for each player
     *                    (0 if an existing player, 1 if a new player).
     * @param errorsFound array of errors found for each player, if any
     *                    (0 if no errors occurred, 1 otherwise).
     */
    private void sendReport(int wins, int losses, List<Member> players,
                            int[] playerTypes, int[] errorsFound) {
        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder playerList = new StringBuilder();

        for (int i = 0; i < players.size(); i++) {
            Member player = players.get(i);
            String completionSymbol = ":white_check_mark: ";
            if (errorsFound[i] == 1) {
                completionSymbol = ":no_entry: ";
            }

            if (playerTypes[i] == 0) {
                playerList.append(completionSymbol)
                        .append(player.getUser().getAsMention())
                        .append("\n");
            } else {
                playerList.append(completionSymbol)
                        .append(player.getUser().getAsMention())
                        .append(" (new)\n");
            }
        }

        eb.setTitle("Summary of Report");
        eb.setColor(Color.GREEN);
        eb.addField("Score:", wins + " - " + losses, false);
        eb.addField("Players Updated:", playerList.toString(), false);

        if (sum(errorsFound, errorsFound.length - 1) == 0) {
            eb.addField("Status:", "COMPLETE", false);
        } else {
            eb.setColor(Color.RED);
            eb.addField("Status:", "INCOMPLETE", false);
        }

        ORIGIN.sendMessageEmbeds(eb.build()).queue();
    }

    /**
     * Updates a user's stats within a spreadsheet.
     * @param args the user input.
     * @param link a connection to the spreadsheet.
     * @param user the user to update the stats of.
     * @param tab the name of the spreadsheet section.
     * @param spreadsheet the values of the spreadsheet section.
     * @param data a map of all rows of the spreadsheet.
     * @param notSub a flag to check if the user is a sub or not.
     * @return 0 if the player could be found in the spreadsheet.
     *         1 otherwise.
     */
    private int updateUser(String[] args, GoogleAPI link, Member user,
                           String tab, Values spreadsheet,
                           TreeMap<Object, PlayerStats> data, boolean notSub) {
        try {
            String userTag = user.getUser().getAsTag();
            PlayerStats player = data.get(user.getId());

            int gamesPlayed = getGamesPlayed(args);
            int gameWins = getGamesWon(args);
            int gameLosses = gamesPlayed - gameWins;

            int setWins = player.getSetWins();
            int setLosses = player.getSetLosses();
            int setsPlayed = setWins + setLosses;
            double setWinrate = 0.0;

            if (notSub) {
                if (cycleSetWon(gameWins, gamesPlayed)) {
                    setWins++;
                } else {
                    setLosses++;
                }

                setsPlayed++;
            }
            if (setsPlayed > 0) {
                setWinrate = (double) setWins / setsPlayed;
            }

            gameWins += player.getGamesWon();
            gameLosses += player.getGamesLost();
            gamesPlayed = gameWins + gameLosses;
            double gameWinrate = (double) gameWins / gamesPlayed;

            String updateRange = tab + "!B" + player.getPositionLP()
                    + ":K" + player.getPositionLP();

            ValueRange newRow = new ValueRange().setValues(
                    Collections.singletonList(Arrays.asList(
                            userTag, user.getEffectiveName(),
                            setWins, setLosses, setsPlayed, setWinrate,
                            gameWins, gameLosses, gamesPlayed, gameWinrate)));
            link.updateRow(updateRange, spreadsheet, newRow);

            return 0;
        } catch (IOException e) {
            log("Existing cycle user error occurred with "
                    + user.getUser().getAsTag());
            return 1;
        }
    }

    /**
     * Checks if the user is a sub, then updates a user's stats
     * within a spreadsheet.
     * @param args the user input.
     * @param link a connection to the spreadsheet.
     * @param user the user to update the stats of.
     * @param tab the name of the spreadsheet section.
     * @param spreadsheet the values of the spreadsheet section.
     * @param data a map of all rows of the spreadsheet.
     * @return 0 if the player could be found in the spreadsheet.
     *         1 otherwise.
     */
    private int updateUser(String[] args, GoogleAPI link, Member user,
                           String tab, Values spreadsheet,
                           TreeMap<Object, PlayerStats> data) {
        return updateUser(args, link, user, tab, spreadsheet, data,
                checkForSub(args));
    }

    /**
     * Adds a user's stats within a spreadsheet.
     * @param args the user input.
     * @param link a connection to the spreadsheet.
     * @param user the user to update the stats of.
     * @param tab the name of the spreadsheet section.
     * @param spreadsheet the values of the spreadsheet section.
     * @param notSub a flag to check if the user is a sub or not.
     * @return 0 if the player could be found in the spreadsheet.
     *         1 otherwise.
     *
     * Note: Users will be added at the next EMPTY row in the spreadsheet.
     */
    private int addUser(String[] args, GoogleAPI link, Member user,
                        String tab, Values spreadsheet, boolean notSub) {
        try {
            String userTag = user.getUser().getAsTag();

            int gamesPlayed = getGamesPlayed(args);
            int gameWins = getGamesWon(args);
            int gameLosses = gamesPlayed - gameWins;
            double gameWinrate = (double) gameWins / gamesPlayed;

            int setWins = 0;
            int setLosses = 0;
            int setsPlayed = 0;
            double setWinrate = 0.0;
            if (notSub) {
                if (cycleSetWon(gameWins, gamesPlayed)) {
                    setWins++;
                } else {
                    setLosses++;
                }

                setsPlayed = 1;
                setWinrate = (double) setWins / setsPlayed;
            }

            ValueRange newRow = new ValueRange().setValues(
                    Collections.singletonList(Arrays.asList(
                            user.getId(), userTag, user.getEffectiveName(),
                            setWins, setLosses, setsPlayed, setWinrate,
                            gameWins, gameLosses, gamesPlayed, gameWinrate)));
            link.appendRow(tab, spreadsheet, newRow);

            return 0;
        } catch (IOException e) {
            log("New cycle user error occurred with "
                    + user.getUser().getAsTag());
            return 1;
        }
    }

    /**
     * Checks if the user is a sub, then adds a user's stats within
     * a spreadsheet.
     * @param args the user input.
     * @param link a connection to the spreadsheet.
     * @param user the user to update the stats of.
     * @param tab the name of the spreadsheet section.
     * @param spreadsheet the values of the spreadsheet section.
     * @return 0 if the player could be found in the spreadsheet.
     *         1 otherwise.
     *
     * Note: Users will be added at the next EMPTY row in the spreadsheet.
     */
    private int addUser(String[] args, GoogleAPI link, Member user,
                        String tab, Values spreadsheet) {
        return addUser(args, link, user, tab, spreadsheet,
                checkForSub(args));
    }

    /**
     * Runs the cycle logging command.
     * @param outChannel the channel to output to, if it exists.
     * @param users the users to attach to the command output, if they exist.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(MessageChannel outChannel, List<Member> users,
                       String[] args) {
        try {
            GoogleAPI link = new GoogleAPI(Discord.getCyclesSheetID());

            // tab name of the spreadsheet
            String tab = "'Current Leaderboard'";

            Values spreadsheet = link.getSheet().spreadsheets().values();
            TreeMap<Object, PlayerStats> data = link.readSection(
                    tab, spreadsheet);
            if (data == null) {
                throw new IOException("The spreadsheet was empty.");
            }

            int[] playerTypes = new int[users.size()];
            int[] errorsFound = new int[users.size()];
            for (int i = 0; i < users.size(); i++) {
                Member user = users.get(i);
                if (data.containsKey(user.getId())) {
                    errorsFound[i] =
                            updateUser(args, link, user, tab, spreadsheet, data);
                    playerTypes[i] = 0;
                } else {
                    errorsFound[i] =
                            addUser(args, link, user, tab, spreadsheet);
                    playerTypes[i] = 1;
                }
            }

            int gamesWon = getGamesWon(args);
            int gamesLost = getGamesPlayed(args) - gamesWon;
            sendReport(gamesWon, gamesLost, users, playerTypes, errorsFound);
        } catch (IOException | GeneralSecurityException e) {
            sendToDiscord("The spreadsheet could not load.");
            log("The spreadsheet could not load.");
            return;
        }

        log(users.size() + " cycle match(es) were processed.");
    }
}
