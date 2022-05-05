package bot.Engine.Cycles;

import bot.Tools.Command;
import bot.Tools.GoogleSheetsAPI;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import com.google.api.services.sheets.v4.model.ValueRange;

import java.awt.Color;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.TreeMap;
import java.util.Arrays;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author  Wil Aquino
 * Date:    June 1, 2021
 * Project: Libra
 * Module:  Undo.java
 * Purpose: Reverts the Cycle spreadsheet to the previous state.
 */
public class Undo extends ManualLog implements Command {

    /**
     * Constructs the cycle undo attributes.
     * @param abbreviation the abbreviation of the section.
     */
    public Undo(String abbreviation) {
        super(abbreviation);
    }

    /**
     * Retrieves the previous cycle command.
     * @param save the undo file to analyze.
     * @return said command.
     */
    private String retrieveLastMessage(File save) {
        try {
            Scanner load = new Scanner(save);
            String message = load.nextLine();
            load.close();

            return message;
        } catch (FileNotFoundException ioe) {
            return null;
        }
    }

    /**
     * Checks if this is a draft or sub command.
     * @param args the user input.
     * @return True if a draft command was called.
     *         False if a sub command was called.
     */
    public boolean notSub(String[] args) {
        return args[0].contains("log");
    }

    /**
     * Retrieve the amount of set games were played.
     * @param args the user input.
     * @return said amount.
     */
    public int getGamesPlayed(String[] args) {
        return Integer.parseInt(args[1]);
    }

    /**
     * Retrieve the amount of won set games.
     * @param args the user input.
     * @return said amount.
     */
    public int getGamesWon(String[] args) {
        return Integer.parseInt(args[2]);
    }

    /**
     * Print the summary of the cycle revert.
     * @param sc the user's inputted command.
     * @param lastInput the previously inputted command.
     * @param userArgs the amount of user arguments within the input.
     * @param errorsFound array of errors found for each player, if any
     *                    (0 if no errors occurred, 1 otherwise).
     */
    private void sendReport(SlashCommandEvent sc, String[] lastInput,
                            int userArgs, int[] errorsFound) {
        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder input = new StringBuilder();
        StringBuilder playerList = new StringBuilder();

        for (int i = 0; i < lastInput.length; i++) {
            String currentArg = lastInput[i];
            input.append(currentArg).append(" ");

            if (i > 2 && i < userArgs + 3) {
                String completionSymbol = ":white_check_mark: ";
                if (errorsFound[i - 3] == 1) {
                    completionSymbol = ":no_entry: ";
                }

                playerList.append(completionSymbol)
                        .append(currentArg).append("\n");
            }
        }

        eb.setTitle("Summary of Revert")
                .setColor(Color.WHITE)
                .addField("Previous Input:", input.toString(), false)
                .addField("Players Updated:", playerList.toString(), false);
        if (sum(errorsFound, errorsFound.length - 1) == 0) {
            eb.addField("Status:", "COMPLETE", false);
        } else {
            eb.setColor(Color.RED);
            eb.addField("Status:", "INCOMPLETE", false);
        }

        sendEmbed(sc, eb);
    }

    /**
     * Updates a user's stats within a spreadsheet.
     * @param args the arguments of the command.
     * @param link a connection to the spreadsheet.
     * @param user the user to revert the stats of.
     * @param tab the name of the spreadsheet tab to edit.
     * @param stats the stats of the player.
     * @return 0 if the player could be found in the spreadsheet.
     *         1 otherwise.
     */
    private int undoUser(String[] args, GoogleSheetsAPI link,
                         String user, String tab,
                         PlayerStats stats) {
        String userID = user.substring(2, user.length() - 1);

        try {
            int gamesPlayed = getGamesPlayed(args);
            int gameWins = getGamesWon(args);
            int gameLosses = gamesPlayed - gameWins;
            double gameWinrate = 0.0;

            int setWins = stats.getSetWins();
            int setLosses = stats.getSetLosses();
            int setsPlayed = setWins + setLosses;
            double setWinrate = 0.0;
            if (notSub(args)) {
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

            gameWins = stats.getGamesWon() - gameWins;
            gameLosses = stats.getGamesLost() - gameLosses;
            gamesPlayed = gameWins + gameLosses;
            if (gamesPlayed > 0) {
                gameWinrate = (double) gameWins / gamesPlayed;
            }

            String updateRange = link.buildRange(tab,
                    "B", stats.getDraftPosition(),
                    "K", stats.getDraftPosition());
            ValueRange newRow = link.buildRow(Arrays.asList(
                    stats.getName(), stats.getNickname(),
                    setWins, setLosses, setsPlayed, setWinrate,
                    gameWins, gameLosses, gamesPlayed, gameWinrate));
            link.updateRange(updateRange, newRow);

            return 0;
        } catch (IOException e) {
            log(getPrefix() + " cycle undo error"
                    + "occurred with <@" + userID + ">.", true);
            return 1;
        }
    }

    /**
     * Runs a cycle undoing command.
     * @param sc the user's inputted command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        sc.deferReply().queue();

        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(cyclesSheetID());
            File undoFile = new File(
                    "load" + getPrefix().toUpperCase() + ".txt");

            // tab name of the spreadsheet
            String tab = "'Current Cycle'";

            TreeMap<Object, Object> data = link.readSection(sc, tab);
            if (data == null) {
                throw new IOException("The spreadsheet was empty.");
            }

            String lastMessage = retrieveLastMessage(undoFile);
            if (lastMessage == null) {
                throw new IOException("An error occurred with the undo file.");
            } else if (lastMessage.equals("REDACTED")) {
                sendResponse(sc, "There is nothing to revert.", true);
                return;
            }

            String[] messageArgs = lastMessage.split("\\s+", 7);
            int userArgs = messageArgs.length - 3;

            int[] errorsFound = new int[userArgs];
            for (int i = 3; i < userArgs + 3; i++) {
                String userID = messageArgs[i].substring(
                        2, messageArgs[i].length() - 1);
                PlayerStats stats = (PlayerStats) data.get(userID);
                errorsFound[i - 3] = undoUser(messageArgs, link,
                        messageArgs[i], tab, stats);
            }

            sendReport(sc, messageArgs, userArgs, errorsFound);
            log(getPrefix().toUpperCase()
                    + " draft undo was processed.", false);
        } catch (IOException | GeneralSecurityException e) {
            sendResponse(sc, "The save could not load.", true);
            log("The saved " + getPrefix().toUpperCase()
                    + " draft data could not load.", true);
        }
    }
}