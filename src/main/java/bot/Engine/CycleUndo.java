package bot.Engine;

import bot.Discord;
import bot.Tools.Command;
import bot.Tools.GoogleAPI;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    June 1, 2021
 * Project: LaunchPoint Bot
 * Module:  CycleUndo.java
 * Purpose: Reverts the Cycle spreadsheet to the previous state.
 */
public class CycleUndo extends CycleLog implements Command {

    /**
     * Retrieves the previous "lpcycle" or "lpsub" command.
     * @return said comand.
     */
    private String retrieveLastMessage() {
        File save = new File("load.txt");
        try {
            Scanner load = new Scanner(save);
            String message = load.nextLine();
            load.close();

            return message;
        } catch (FileNotFoundException ioe) {
            sendToDiscord("Something went wrong with the undo file.");
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * Print the summary of the cycle revert.
     * @param lastInput the previously inputted command.
     * @param userArgs the amount of user arguments within the input.
     * @param errorsFound array of errors found for each player, if any
     *                    (0 if no errors occurred, 1 otherwise).
     */
    private void sendReport(String[] lastInput, int userArgs, int[] errorsFound) {
        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder input = new StringBuilder();
        StringBuilder playerList = new StringBuilder();

        lastInput[0] = lastInput[0].toLowerCase();
        for (int i = 0; i < lastInput.length; i++) {
            String currentArg = lastInput[i];
            input.append(currentArg).append(" ");

            if (i > 0 && i < userArgs + 1) {
                String completionSymbol = ":white_check_mark: ";
                if (errorsFound[i - 1] == 1) {
                    completionSymbol = ":no_entry: ";
                }

                playerList.append(completionSymbol)
                        .append(currentArg).append("\n");
            }
        }

        eb.setTitle("Summary of Revert");
        eb.setColor(Color.WHITE);
        eb.addField("Previous Input:", input.toString(), false);
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
     * @param user the user to revert the stats of.
     * @param tab the name of the spreadsheet section
     * @param spreadsheet the values of the spreadsheet section.
     * @param data a map of all rows of the spreadsheet.
     * @param notSub a flag to check if the user is a sub or not.
     * @return 0 if the player could be found in the spreadsheet.
     *         1 otherwise.
     */
    private int undoUser(String[] args, GoogleAPI link, String user,
                          String tab, Values spreadsheet,
                          TreeMap<Object, PlayerStats> data, boolean notSub) {
        try {
            String userID = user.substring(3, user.length() - 1);
            PlayerStats player = data.get(userID);

            int gamesPlayed = getGamesPlayed(args);
            int gameWins = getGamesWon(args);
            int gameLosses = gamesPlayed - gameWins;
            double gameWinrate = 0.0;

            int setWins = player.getSetWins();
            int setLosses = player.getSetLosses();
            int setsPlayed = setWins + setLosses;
            double setWinrate = 0.0;

            if (notSub) {
                if (cycleSetWon(gameWins, gamesPlayed)) {
                    setWins--;
                } else {
                    setLosses--;
                }

                setsPlayed--;
            }
            if (setsPlayed > 0) {
                setWinrate = (double) setWins / setsPlayed;
            }

            gameWins = player.getGamesWon() - gameWins;
            gameLosses = player.getGamesLost() - gameLosses;
            gamesPlayed = gameWins + gameLosses;
            if (gamesPlayed > 0) {
                gameWinrate = (double) gameWins / gamesPlayed;
            }

            String updateRange = tab + "!B" + player.getPosition()
                    + ":K" + player.getPosition();
            ValueRange newRow = new ValueRange().setValues(
                    Collections.singletonList(Arrays.asList(
                            player.getName(), player.getNickname(),
                            setWins, setLosses, setsPlayed, setWinrate,
                            gameWins, gameLosses, gamesPlayed, gameWinrate)));
            link.updateRow(updateRange, spreadsheet, newRow);

            return 0;
        } catch (IOException e) {
            return 1;
        }
    }

    /**
     * Checks if the user is a sub, then reverts a user's stats
     * within a spreadsheet.
     * @param args the user input.
     * @param link a connection to the spreadsheet.
     * @param user the user to revert the stats of.
     * @param tab the name of the spreadsheet section
     * @param spreadsheet the values of the spreadsheet section.
     * @param data a map of all rows of the spreadsheet.
     * @return 0 if the player could be found in the spreadsheet.
     *         1 otherwise.
     */
    private int undoUser(String[] args, GoogleAPI link, String user,
                          String tab, Values spreadsheet,
                          TreeMap<Object, PlayerStats> data) {
        return undoUser(args, link, user, tab, spreadsheet, data,
                checkForSub(args));
    }

    /**
     * Runs the cycle undoing command.
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
            String tab = "'Current Leaderboard'";

            Values spreadsheet = link.getSheet().spreadsheets().values();
            TreeMap<Object, PlayerStats> data = link.readSection(
                    tab, spreadsheet);
            if (data == null) {
                throw new IOException("The spreadsheet was empty.");
            }

            String lastMessage = retrieveLastMessage();
            if (lastMessage == null) {
                throw new IOException();
            } else if (lastMessage.equals("REDACTED")) {
                sendToDiscord("There is nothing to revert.");
                return;
            }

            String[] messageArgs = lastMessage.split("\\s+", 7);
            int userArgs = messageArgs.length - 3;

            int[] errorsFound = new int[userArgs];
            for (int i = 1; i < userArgs + 1; i++) {
                errorsFound[i - 1] = undoUser(messageArgs, link,
                        messageArgs[i], tab, spreadsheet, data);
            }

//            sendToDiscord("The revert was processed.");
            sendReport(messageArgs, userArgs, errorsFound);
        } catch (IOException | GeneralSecurityException e) {
            sendToDiscord("The save could not load.");
        }

        log("Cycle undo was processed.");
    }
}
