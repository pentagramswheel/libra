package bot.Engine.Cycles;

import bot.Engine.Section;
import bot.Tools.Command;
import bot.Tools.GoogleSheetsAPI;

import com.google.api.services.sheets.v4.model.ValueRange;
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
public class PointsCalculator extends Section implements Command {

    /** Max score per category. */
    private final static int MAX_CATGEORY_POINTS = 10;

    /** Number of score categories to calculate. */
    private final static int NUM_TOTAL_SCORES = 5;

    /** Beginning lettered column of the score columns. */
    private final static char SCORE_COLUMNS_START = 'L';

    /**
     * Constructs the calculator's attributes.
     * @param abbreviation the abbreviation of the section.
     */
    public PointsCalculator(String abbreviation) {
        super(abbreviation);
    }

    /**
     * Converts character-formatted column to its integer format.
     * @param column the column to format.
     */
    private int columnToInt(String column) {
        return column.charAt(0) - 'A';
    }

    /**
     * Converts an integer-formatted column to its character format.
     * @param column the column to format.
     */
    private String intToColumn(int column) {
        char c = (char) column;
        return String.valueOf(c);
    }

    /** Retrieves a list of the lettered points columns. */
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
     * @param tab the name of the spreadsheet tab to edit.
     * @param link a connection to the leaderboard spreadsheet.
     * @return True if leaderboard update ran without error.
     *         False otherwise.
     */
    private boolean updateLeaderboardUnsuccessful(SlashCommandEvent sc,
                                   TreeMap<Object, Integer> scores,
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

            link.sortByDescending(tab, String.valueOf(SCORE_COLUMNS_START), table.size());
            return false;
        } catch (IOException e) {
            editMessage(sc, "An error occurred while updating the leaderboard.");
            log("An error occurred while updating the public leaderboard.",
                    true);
        }

        return true;
    }

    /**
     * Retrieves the placement string of a player's Top 10 standing.
     * @param tag the Discord tag of the player.
     * @param placing the standing of the player.
     * @return the formatted placement string.
     */
    private String placement(String tag, int placing) {
        return String.format("@.%s (%s)\n", tag, placing);
    }

    /**
     * Calculates the Top 10 players of the leaderboard.
     * @param sc the user's inputted command.
     * @param section the designated MIT section for this Top 10.
     * @param size the amount of players eligible
     * @param tab the name of the spreadsheet tab to edit.
     * @param link a connection to the leaderboard spreadsheet.
     * @return a map of final scores for all players who were eligible,
     *         for the Top 10.
     *         null if an error occurred.
     */
    public TreeMap<Object, Integer> findTopTen(SlashCommandEvent sc,
                                               String section, int size,
                                               String tab, GoogleSheetsAPI link) {
        StringBuilder topTen = new StringBuilder();
        TreeMap<Object, Integer> finalScores = new TreeMap<>();

        String totalColumn = intToColumn(SCORE_COLUMNS_START + NUM_TOTAL_SCORES);
        int numTotalColumn = columnToInt(totalColumn);

        try {
            link.sortByDescending(tab, totalColumn, size);
            editMessage(sc, "Calculating Top 10...");

            int placing, offset;
            placing = offset = 1;

            int lastScore = -1;

            List<List<Object>> table = link.getSheetValues(tab);
            table.remove(0);

            for (List<Object> row : table) {
                Object id = row.get(0);
                String playerTag = String.valueOf(row.get(1));
                int currScore =
                        Integer.parseInt(row.get(numTotalColumn).toString());

                if (lastScore == -1) {
                    lastScore = currScore;
                    topTen.append(placement(playerTag, placing));
                } else if (currScore == lastScore) {
                    offset++;
                    topTen.append(placement(playerTag, placing));
                } else if (placing + offset <= 10) {
                    lastScore = currScore;
                    placing += offset;
                    offset = 1;

                    topTen.append(placement(playerTag, placing));
                }

                finalScores.put(id, currScore);
            }
        } catch (IOException e) {
            editMessage(sc, "An error occurred while calculating the Top 10 players.");
            log("An error occurred with the Top 10 calculation.", true);
            return null;
        }

        String output = "Top 10 for " + section + ":\n```"
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
     * @return True if the points calculation ran without error.
     *         False otherwise.
     */
    public boolean calculatePointsUnsuccessful(SlashCommandEvent sc, int size,
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
            return false;
        } catch (IOException e) {
            editMessage(sc, "An error occurred while calculating points.");
            log("An error occurred with the points calculation.", true);
        }

        return true;
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
     *         -1 if an error occurred.
     */
    public int initializeCopy(SlashCommandEvent sc, String tab, int minimumSets,
                               GoogleSheetsAPI fromLink, GoogleSheetsAPI toLink) {
        try {
            TreeMap<Object, Object> data = fromLink.readSection(sc, tab);

            int size = 0;
            for (Map.Entry<Object, Object> mapping : data.entrySet()) {
                PlayerStats player = (PlayerStats) mapping.getValue();

                int setWins = player.getSetWins();
                int setLosses = player.getSetLosses();
                int setsPlayed = setWins + setLosses;
                if (setsPlayed < minimumSets) {
                    continue;
                }

                double setWinrate = 0.0;
                if (setsPlayed > 0) {
                    setWinrate = (double) setWins / setsPlayed;
                }

                int gameWins = player.getGamesWon();
                int gameLosses = player.getGamesLost();
                int gamesPlayed = gameWins + gameLosses;
                double gameWinrate = 0.0;
                if (gamesPlayed > 0) {
                    gameWinrate =  (double) gameWins / gamesPlayed;
                }

                String updateRange = toLink.buildRange(tab,
                        "A", size + 2,
                        intToColumn(SCORE_COLUMNS_START - 1), size + 2);
                ValueRange newRow = fromLink.buildRow(Arrays.asList(
                        mapping.getKey(), player.getName(), player.getNickname(),
                        setWins, setLosses, setsPlayed, setWinrate,
                        gameWins, gameLosses, gamesPlayed, gameWinrate));
                toLink.updateRange(updateRange, newRow);
                size++;
                wait(2500);
            }

            editMessage(sc, "Calculating points...");
            return size;
        } catch (IOException e) {
            editMessage(sc, "An error occurred while copying over the "
                    + "leaderboard data.");
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
        sc.deferReply(false).queue();

        // tab names of the spreadsheets
        String currentTab = CYCLES_TAB;
        String previousTab = "Previous Cycle";
        String templateTab = "Blank";

        try {
            int minimumSets = 3;
            GoogleSheetsAPI leaderboard = new GoogleSheetsAPI(cyclesSheetID());
            GoogleSheetsAPI points = new GoogleSheetsAPI(calculationsSheetID());
            if (getSection().equals("Ink Odyssey")) {
                minimumSets = 0;
            }

            points.duplicateTab(templateTab, currentTab);

            wait(10000);
            editMessage(sc, "Copying " + getSection() + " spreadsheet...");

            log("(Cycle Change) A leaderboard is being copied to the "
                    + getSection() + " points spreadsheet.", false);
            int totalPlayers = initializeCopy(
                    sc, currentTab, minimumSets, leaderboard, points);
            if (totalPlayers == -1) {
                throw new IOException();
            }

            log("(Cycle Change) Points are being calculated...", false);
            if (calculatePointsUnsuccessful(sc, totalPlayers, currentTab, points)) {
                throw new IOException();
            }

            log("(Cycle Change) Retrieving Top 10 players...", false);
            TreeMap<Object, Integer> scores =
                    findTopTen(sc, getSection(), totalPlayers, currentTab, points);
            if (scores == null) {
                throw new IOException();
            }

            log("(Cycle Change) Updating public leaderboard...", false);
            if (updateLeaderboardUnsuccessful(sc, scores, currentTab, leaderboard)) {
                throw new IOException();
            }

            // update the points spreadsheet up to the previous cycle
            points.renameTab(previousTab, "2 Cycles Ago");
            points.renameTab(currentTab, previousTab);

            // update the leaderboard spreadsheet up to the new cycle
            leaderboard.renameTab(currentTab, previousTab);
            leaderboard.duplicateTab(templateTab, currentTab);

            log("(Cycle Change) Top 10 for " + getSection() + " completed.", false);
            wait(2000);

            editMessage(sc, "Here are your " + getSection() + " Cycle Top 10s!");
            log("Cycle change has been completed.", false);
        } catch (GeneralSecurityException | IOException e) {
            sendResponse(sc, "An error occurred during the calculation.", false);
            log("A spreadsheet during calculations could not load.", true);
        }
    }
}
