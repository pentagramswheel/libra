package bot.Engine.Cycles;

import bot.Config;
import bot.Engine.PlayerStats;
import bot.Tools.Command;
import bot.Tools.GoogleSheetsAPI;

import com.google.api.services.sheets.v4.model.ValueRange;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author  Wil Aquino
 * Date:    April 18, 2022
 * Project: Libra
 * Module:  PointsCalculator.java
 * Purpose: Calculates points for each of the players
 *          within a Cycle.
 */
public class PointsCalculator implements Command {

    /** Max score per category. */
    private final static int MAX_CATGEORY_POINTS = 10;

    /** Number of score categories to calculate. */
    private final static int NUM_TOTAL_SCORES = 5;

    /** Beginning lettered column of the score columns. */
    private final static char SCORE_COLUMNS_START = 'L';

    /**
     * Converts character-formatted column to its
     * integer format.
     * @param column the column to format.
     * @return the integer-valued column.
     */
    private int columnToInt(String column) {
        return column.charAt(0) - 'A';
    }

    /**
     * Converts an integer-formatted column to its
     * character-format.
     * @param column the column to format.
     * @return the lettered column.
     */
    private String intToColumn(int column) {
        char c = (char) column;
        return String.valueOf(c);
    }

    /**
     * Retrieves the lettered points columns.
     * @return said letters.
     */
    private List<String> getPointsColumns() {
        List<String> pointsColumns = new ArrayList<>(NUM_TOTAL_SCORES);
        for (int i = 0; i < NUM_TOTAL_SCORES; i++) {
            pointsColumns.add(intToColumn(SCORE_COLUMNS_START + i));
        }

        return pointsColumns;
    }

    /**
     * Updates the public leaderboard with points for each player.
     * @param sc the user's inputted commands.
     * @param scores the scores which were found during the
     *               points calculation.
     * @param tab the
     * @param link a connection to the leaderboard spreadsheet.
     */
    private void updateLeaderboard(SlashCommandEvent sc, TreeMap<Object, Integer> scores,
                                   String tab, GoogleSheetsAPI link) {
        try {
            editMessage(sc, "Updating leaderboard...");

            List<List<Object>> table = link.getSheetValues(tab);
            table.remove(0);
            List<Object> pointsPerPlayer = new ArrayList<>(table.size());

            for (List<Object> row : table) {
                Object playerID = row.get(0);
                pointsPerPlayer.add(scores.getOrDefault(playerID, 0));
            }

            String updateRange = link.buildRange(tab,
                    String.valueOf(SCORE_COLUMNS_START), 2,
                    String.valueOf(SCORE_COLUMNS_START), 2 + (table.size() - 1));
            ValueRange newColumn = link.buildColumn(pointsPerPlayer);
            link.updateRange(updateRange, newColumn);
        } catch (IOException e) {
            sendResponse(sc, "An error occurred while updating leaderboard.",
                    true);
            log("An error occurred while updating the public leaderboard.",
                    true);
        }
    }

    /**
     * Calculates the Top 10 players of the leaderboard.
     * @param sc the user's inputted command.
     * @param size the amount of players eligible
     * @param tab the name of the spreadsheet tab to edit.
     * @param link a connection to the leaderboard spreadsheet.
     * @param section the designated MIT section for this Top 10.
     * @return a map of final scores for all players who were eligible
     *         for the Top 10.
     */
    public TreeMap<Object, Integer> findTopTen(SlashCommandEvent sc, int size,
                            String tab, GoogleSheetsAPI link, int section) {
        StringBuilder topTen = new StringBuilder();
        TreeMap<Object, Integer> finalScores = new TreeMap<>();

        String totalColumn = intToColumn(SCORE_COLUMNS_START + NUM_TOTAL_SCORES);
        int numTotalColumn = columnToInt(totalColumn);
        link.sortByDescending(tab, totalColumn, size);

        try {
            editMessage(sc, "Calculating Top 10...");

            int placing = 1;
            int lastScore = -1;

            List<List<Object>> table = link.getSheetValues(tab);
            table.remove(0);

            for (List<Object> row : table) {
                Object id = row.get(0);
                int currScore =
                        Integer.parseInt(row.get(numTotalColumn).toString());

                Member player = findMember(sc, String.valueOf(id));
                String playerTag = player.getUser().getAsTag();

                if (lastScore == -1) {
                    lastScore = currScore;

                    String placement = String.format("%s) @.%s\n",
                            placing, playerTag);
                    topTen.append(placement);
                } else if (currScore != lastScore && placing < 11) {
                    lastScore = currScore;
                    placing++;

                    String placement = String.format("%s) @.%s\n",
                            placing, playerTag);
                    topTen.append(placement);
                }

                finalScores.put(id, currScore);
            }
        } catch (IOException e) {
            sendResponse(sc, "An error occurred while calculating the Top 10 players.",
                    true);
            log("An error occurred with the Top 10 calculation.", true);
        }

        String output = "Top 10 for Section " + (section + 1) + "\n```"
                + topTen + "```";
        sendResponse(sc, output, false);
        return finalScores;
    }

    /**
     * Calculates the final leaderboard scores.
     * @param sc the user's inputted command.
     * @param size the amount of players eligible
     * @param tab the name of the spreadsheet tab to edit.
     * @param link a connection to the leaderboard spreadsheet.
     */
    public void calculatePoints(SlashCommandEvent sc, int size,
                                String tab, GoogleSheetsAPI link) {
        List<String> scoreColumns = new ArrayList<>(
                Arrays.asList("D", "F", "G", "H", "K"));
        List<String> pointsColumns = getPointsColumns();
        List<Object> pointsPerPlayer = new ArrayList<>(size);

        try {
            for (String column : pointsColumns) {
                editMessage(sc, "Calculating points for column "
                        + column + "...");

                int points = MAX_CATGEORY_POINTS;
                double lastVal = -1.0;

                String currScoreColumn = scoreColumns.remove(0);
                int numScoreCol = columnToInt(currScoreColumn);
                link.sortByDescending(tab, currScoreColumn, size);

                pointsPerPlayer.clear();

                List<List<Object>> table = link.getSheetValues(tab);
                table.remove(0);

                for (List<Object> row : table) {
                    double currVal = Double.parseDouble(
                            row.get(numScoreCol).toString());
                    if (lastVal == -1.0) {
                        lastVal = currVal;
                    } else if (currVal == 0.0) {
                        points = 0;
                    } else if (currVal < lastVal && points > 0) {
                        lastVal = currVal;
                        points--;
                    }

                    pointsPerPlayer.add(points);
                }

                String updateRange = link.buildRange(tab,
                        column, 2,
                        column, 2 + (size - 1));
                ValueRange newColumn = link.buildColumn(pointsPerPlayer);
                link.updateRange(updateRange, newColumn);

                wait(5000);
            }

            wait(5000);
        } catch (IOException e) {
            sendResponse(sc, "An error occurred while calculating points.",
                    true);
            log("An error occurred with the points calculation.", true);
        }
    }

    /**
     * Initializes the Points Calculation spreadsheet.
     * @param sc the user's inputted command.
     * @param tab the name of the spreadsheet tab to edit.
     * @param minimumSets the minimum number of sets to be considered
     *                    for point eligibility.
     * @param fromLink a connection to the leaderboard spreadsheet.
     * @param toLink a connection to the points spreadsheet.
     * @return the amount of players eligible for points.
     */
    public int initializeCopy(SlashCommandEvent sc, String tab, int minimumSets,
                               GoogleSheetsAPI fromLink, GoogleSheetsAPI toLink) {
        try {
            TreeMap<Object, PlayerStats> data = fromLink.readSection(sc, tab);
            if (data == null) {
                throw new IOException("The spreadsheet was empty.");
            }

            int size = 0;
            for (Map.Entry<Object, PlayerStats> mapping : data.entrySet()) {
                PlayerStats player = mapping.getValue();

                int setWins = player.getSetWins();
                int setLosses = player.getSetLosses();
                int setsPlayed = setWins + setLosses;
                if (setsPlayed < minimumSets) {
                    continue;
                }

                wait(2000);
                double setWinrate = (double) setWins / setsPlayed;

                int gameWins = player.getGamesWon();
                int gameLosses = player.getGamesLost();
                int gamesPlayed = gameWins + gameLosses;
                double gameWinrate = (double) gameWins / gamesPlayed;

                String updateRange = toLink.buildRange(tab,
                        "A", size + 2,
                        intToColumn(SCORE_COLUMNS_START - 1), size + 2);
                ValueRange newRow = fromLink.buildRow(Arrays.asList(
                        mapping.getKey(), player.getName(), player.getNickname(),
                        setWins, setLosses, setsPlayed, setWinrate,
                        gameWins, gameLosses, gamesPlayed, gameWinrate));
                toLink.updateRange(updateRange, newRow);
                size++;
            }

            editMessage(sc, "Calculating points...");
            return size;
        } catch (IOException e) {
            sendResponse(sc, "An error occurred while copying over the "
                    + "leaderboard data.",
                    true);
            log("The cycles data could not be copied over.", true);
            return -1;
        }
    }

    /**
     * Runs the cycles calculation command.
     * @param sc the command to analyze.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        sc.deferReply().queue();

        // tab name of the spreadsheets
        String tab = "'Current Cycle'";

        try {
            ArrayList<Integer> minimumSets = new ArrayList<>(
                    Arrays.asList(3, 0));

            ArrayList<GoogleSheetsAPI> leaderboards = new ArrayList<>(
                    Arrays.asList(new GoogleSheetsAPI(Config.lpCyclesSheetID),
                            new GoogleSheetsAPI(Config.ioCyclesSheetID)));

            ArrayList<GoogleSheetsAPI> points = new ArrayList<>(
                    Arrays.asList(new GoogleSheetsAPI(Config.lpCyclesCalculationSheetID),
                            new GoogleSheetsAPI(Config.ioCyclesCalculationSheetID)));

            for (int i = 0; i < leaderboards.size(); i++) {
                wait(10000);
                String update = "Copying spreadsheet " + (i + 1) + " over...";
                editMessage(sc, update);

                update = "(Cycle Change) A leaderboard is being copied "
                        + "to points spreadsheet..." + (i + 1);
                log(update, false);
                int totalPlayers = initializeCopy(
                        sc, tab, minimumSets.get(i),
                        leaderboards.get(i), points.get(i));
                if (totalPlayers == -1) {
                    throw new IOException("Total players invalid.");
                }

                update = "(Cycle Change) Points are being calculated...";
                log(update, false);
                calculatePoints(sc, totalPlayers, tab, points.get(i));

                update = "(Cycle Change) Retrieving Top 10 players...";
                log(update, false);
                TreeMap<Object, Integer> scores =
                        findTopTen(sc, totalPlayers, tab, points.get(i), i);

                update = "(Cycle Change) Updating public leaderboard...";
                log(update, false);
                updateLeaderboard(sc, scores, tab, leaderboards.get(i));

                update = "(Cycle Change) Top 10 for Section "
                        + (i + 1) + " completed.";
                log(update, false);
            }

            log("Cycle change has been completed.", false);
            sendResponse(sc,
                    "\n```Sections:\nSection 1 - LP\nSection 2 - IO```", false);
        } catch (GeneralSecurityException | IOException e) {
            sendResponse(sc, "A spreadsheet could not load.", true);
            log("A spreadsheet during calculations could not load.", true);
        }
    }
}
