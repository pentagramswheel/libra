package bot.Engine.Cycles;

import bot.Engine.Games.Drafts.DraftGame;
import bot.Engine.Games.Drafts.DraftPlayer;
import bot.Engine.Games.Drafts.DraftTeam;
import bot.Engine.Section;
import bot.Tools.GoogleSheetsAPI;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author  Wil Aquino
 * Date:    January 18, 2022
 * Project: Libra
 * Module:  AutoLog.java
 * Purpose: Logs MIT leaderboard information via the
 *          automatic draft system.
 */
public class AutoLog extends Section {

    /**
     * Constructs the cycle log attributes.
     * @param abbreviation the abbreviation of the section.
     */
    public AutoLog(String abbreviation) {
        super(abbreviation);
    }

    /**
     * Updates the stringed list of players for the draft cycle match
     * report summary.
     * @param team the current team from the draft.
     * @param playerList a store for building the string of the team's players.
     * @param subList a store for building the string of the draft's subs.
     * @param playerTypes array of types for each player
     *                    (0 if an existing player, 1 if a new player).
     * @param errorsFound array of errors found for each player, if any
     *                    (0 if no errors occurred, 1 otherwise).
     * @param offset an index to offset the type arrays, based on the
     *               current team.
     */
    private void updateLists(DraftTeam team,
                             StringBuilder playerList, StringBuilder subList,
                             int[] playerTypes, int[] errorsFound, int offset) {
        int i = 0;
        for (Map.Entry<String, DraftPlayer> player : team.getPlayers().entrySet()) {
            String currID = player.getKey();
            DraftPlayer currPlayer = player.getValue();

            String completionSymbol = ":white_check_mark: ";
            if (errorsFound[offset + i] == 1) {
                completionSymbol = ":no_entry: ";
            }

            StringBuilder list = playerList;
            String subScore = "";

            if (currPlayer.isSub()) {
                list = subList;
                subScore = String.format(" [%s-%s]",
                        currPlayer.getWins(), currPlayer.getLosses());
            }

            list.append(completionSymbol)
                    .append(currPlayer.getAsMention(currID))
                    .append(subScore);
            if (playerTypes[offset + i] == 0) {
                list.append("\n");
            } else {
                list.append(" (new)\n");
            }

            i++;
        }
    }

    /**
     * Prints the summary of the cycle match report.
     * @param log a ManualLog object for actual reporting.
     * @param draft the draft to report.
     * @param bc a button click to analyze.
     * @param team1 the first team of the draft.
     * @param team2 the second team of the draft.
     * @param playerTypes array of types for each player
     *                    (0 if an existing player, 1 if a new player).
     * @param errorsFound array of errors found for each player, if any
     *                    (0 if no errors occurred, 1 otherwise).
     */
    private void sendReport(ManualLog log, DraftGame draft, ButtonClickEvent bc,
                            DraftTeam team1, DraftTeam team2,
                            int[] playerTypes, int[] errorsFound) {
        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder teamList1 = new StringBuilder();
        StringBuilder teamList2 = new StringBuilder();
        StringBuilder subList = new StringBuilder();

        updateLists(team1, teamList1, subList, playerTypes, errorsFound, 0);
        updateLists(team2, teamList2, subList, playerTypes, errorsFound,
                team1.getPlayers().size());

        String winningTeam = "Team 1:";
        String losingTeam = "Team 2:";
        int wins = draft.getProcess().getTeam1().getScore();
        int losses = draft.getProcess().getTeam2().getScore();

        if (wins < losses) {
            winningTeam = "Team 2:";
            losingTeam = "Team 1:";
            wins = draft.getProcess().getTeam2().getScore();
            losses = draft.getProcess().getTeam1().getScore();

            StringBuilder tempList = teamList1;
            teamList1 = teamList2;
            teamList2 = tempList;
        }

        eb.setTitle("Summary of Report")
                .setColor(getColor())
                .addField("Score:", wins + " - " + losses, false)
                .addField(winningTeam, teamList1.toString(), false)
                .addField(losingTeam, teamList2.toString(), true)
                .addField("Subs:", subList.toString(), false);

        if (log.sum(errorsFound, errorsFound.length - 1) == 0) {
            eb.addField("Status:", "COMPLETE", false);
        } else {
            eb.setColor(Color.RED);
            eb.addField("Status:", "INCOMPLETE", false);
        }

        TextChannel channel = draft.getChannel(bc, getPrefix() + "-match-report");
        channel.sendMessageEmbeds(eb.build()).queue();
    }

    /**
     * Updates the cycle spreadsheet based on the draft
     * players' information.
     * @param log a ManualLog object for actual reporting.
     * @param draft the draft to report.
     * @param bc a button click to analyze.
     * @param team the current team to report.
     * @param playerTypes array of types for each player
     *                    (0 if an existing player, 1 if a new player).
     * @param errorsFound array of errors found for each player, if any
     *                    (0 if no errors occurred, 1 otherwise).
     * @param offset an index to offset the type arrays, based on the
     *               current team.
     * @param link a connection to the spreadsheet.
     * @param data a map of all rows of the spreadsheet.
     */
    private void updateSpreadsheet(ManualLog log, DraftGame draft,
                                   ButtonClickEvent bc, DraftTeam team,
                                   int[] playerTypes, int[] errorsFound, int offset,
                                   GoogleSheetsAPI link, TreeMap<Object, Object> data) {
        int i = 0;
        for (Map.Entry<String, DraftPlayer> player : team.getPlayers().entrySet()) {
            String currID = player.getKey();
            DraftPlayer currPlayer = player.getValue();
            Member user = draft.findMember(bc, currID);

            int gameWins = currPlayer.getWins();
            int gamesPlayed = gameWins + currPlayer.getLosses();

            String cmd = "log";
            if (currPlayer.isSub()) {
                cmd = "sub";
            }

            if (data.containsKey(currID)) {
                PlayerStats stats = (PlayerStats) data.get(currID);
                errorsFound[offset + i] = log.updateUser(
                        cmd, gamesPlayed, gameWins, user, link, stats);
                playerTypes[offset + i] = 0;
            } else {
                errorsFound[offset + i] = log.addUser(
                        cmd, gamesPlayed, gameWins, user, link);
                playerTypes[offset + i] = 1;
            }

            i++;
        }
    }

    /**
     * Reports the draft.
     * @param bc a button click to analyze.
     * @param draft the draft to report.
     */
    public void matchReport(ButtonClickEvent bc, DraftGame draft) {
        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(cyclesSheetID());
            TreeMap<Object, Object> data = link.readSection(bc, CYCLES_TAB);

            DraftTeam team1 = draft.getProcess().getTeam1();
            DraftTeam team2 = draft.getProcess().getTeam2();

            int totalSize = team1.getPlayers().size() + team2.getPlayers().size();
            int[] playerTypes = new int[totalSize];
            int[] errorsFound = new int[totalSize];

            ManualLog log = new ManualLog(getPrefix());
            updateSpreadsheet(log, draft, bc, team1, playerTypes,
                    errorsFound, 0, link, data);
            updateSpreadsheet(log, draft, bc, team2, playerTypes,
                    errorsFound, team1.getPlayers().size(), link, data);

            sendReport(log, draft, bc, team1, team2, playerTypes, errorsFound);
            draft.log(totalSize + " " + getPrefix().toUpperCase()
                    + " draft player(s) were automatically processed.", false);
        } catch (IOException | GeneralSecurityException e) {
            draft.editMessage(bc, "The leaderboard could not load.");
            draft.log("The " + getSection()
                    + " cycles spreadsheet could not load.", true);
        }
    }
}
