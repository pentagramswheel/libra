package bot.Engine.Drafts;

import bot.Engine.PlayerStats;
import bot.Engine.Section;
import bot.Tools.GoogleAPI;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;

import java.awt.Color;
import java.util.List;
import java.util.TreeMap;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author  Wil Aquino
 * Date:    January 18, 2022
 * Project: Libra
 * Module:  AutoLog.java
 * Purpose: Logs cycle information via the automatic
 *          draft system.
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
     * report summary
     * @param draft the draft to report.
     * @param bc a button click to analyze.
     * @param team the current team to build the list of.
     * @param playerList a store for building the string of the team's players.
     * @param subs the list of subs from the draft.
     * @param subList a store for building the string of the draft's subs.
     * @param playerTypes array of types for each player
     *                    (0 if an existing player, 1 if a new player).
     * @param errorsFound array of errors found for each player, if any
     *                    (0 if no errors occurred, 1 otherwise).
     * @param offset an index to offset the type arrays, based on the
     *               current team.
     */
    public void updateLists(Draft draft, ButtonClickEvent bc,
                           List<DraftPlayer> team, StringBuilder playerList,
                           List<DraftPlayer> subs, StringBuilder subList,
                           int[] playerTypes, int[] errorsFound, int offset) {
        for (int i = 0; i < team.size(); i++) {
            DraftPlayer currPlayer = team.get(i);
            Member player = draft.findMember(bc, currPlayer.getID());
            String completionSymbol = ":white_check_mark: ";
            if (errorsFound[offset + i] == 1) {
                completionSymbol = ":no_entry: ";
            }

            StringBuilder list = playerList;
            if (subs.contains(currPlayer)) {
                list = subList;
            }

            if (playerTypes[offset + i] == 0) {
                list.append(completionSymbol)
                        .append(player.getAsMention())
                        .append("\n");
            } else {
                list.append(completionSymbol)
                        .append(player.getAsMention())
                        .append(" (new)\n");
            }
        }
    }

    /**
     * Prints the summary of the cycle match report.
     * @param log a ManualLog object for actual reporting.
     * @param draft the draft to report.
     * @param bc a button click to analyze.
     * @param team1 the first team of the draft.
     * @param team2 the second team of the draft.
     * @param subs the list of subs from the draft.
     * @param playerTypes array of types for each player
     *                    (0 if an existing player, 1 if a new player).
     * @param errorsFound array of errors found for each player, if any
     *                    (0 if no errors occurred, 1 otherwise).
     */
    public void sendReport(ManualLog log, Draft draft, ButtonClickEvent bc,
                           List<DraftPlayer> team1, List<DraftPlayer> team2,
                           List<DraftPlayer> subs, int[] playerTypes,
                           int[] errorsFound) {
        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder teamList1 = new StringBuilder();
        StringBuilder teamList2 = new StringBuilder();
        StringBuilder subList = new StringBuilder();

        updateLists(draft, bc, team1, teamList1, subs, subList,
                playerTypes, errorsFound, 0);
        updateLists(draft, bc, team2, teamList2, subs, subList,
                playerTypes, errorsFound, team1.size());

        int wins = team1.get(0).getWins();
        int losses = team1.get(0).getLosses();

        if (wins < losses) {
            int tempScore = wins;
            wins = losses;
            losses = tempScore;

            StringBuilder tempList = teamList1;
            teamList1 = teamList2;
            teamList2 = tempList;
        }

        eb.setTitle("Summary of Report")
                .setColor(getColor())
                .addField("Score:", wins + " - " + losses, false)
                .addField("Team 1:", teamList1.toString(), false)
                .addField("Team 2:", teamList2.toString(), true)
                .addField("Subs:", subList.toString(), false);
        if (log.sum(errorsFound, errorsFound.length - 1) == 0) {
            eb.addField("Status:", "COMPLETE", true);
        } else {
            eb.setColor(Color.RED);
            eb.addField("Status:", "INCOMPLETE", true);
        }

//        TextChannel channel = draft.getChannel(bc, getPrefix() + "-match-report");
        TextChannel channel = draft.getChannel(bc, "bot-testing");
        channel.sendMessageEmbeds(eb.build()).queue();
    }

    /**
     * Updates the cycle spreadsheet based on the draft
     * players' information.
     * @param log a ManualLog object for actual reporting.
     * @param draft the draft to report.
     * @param bc a button click to analyze.
     * @param team the current team to report.
     * @param subs the list of subs from the draft.
     * @param playerTypes array of types for each player
     *                    (0 if an existing player, 1 if a new player).
     * @param errorsFound array of errors found for each player, if any
     *                    (0 if no errors occurred, 1 otherwise).
     * @param offset an index to offset the type arrays, based on the
     *               current team.
     * @param link a connection to the spreadsheet.
     * @param tab the name of the spreadsheet section.
     * @param spreadsheet the values of the spreadsheet section.
     * @param data a map of all rows of the spreadsheet.
     */
    public void updateSpreadsheet(ManualLog log, Draft draft, ButtonClickEvent bc,
                                  List<DraftPlayer> team, List<DraftPlayer> subs,
                                  int[] playerTypes, int[] errorsFound, int offset,
                                  GoogleAPI link, String tab, Values spreadsheet,
                                  TreeMap<Object, PlayerStats> data) {
        for (int i = 0; i < team.size(); i++) {
            DraftPlayer currPlayer = team.get(i);
            Member user = draft.findMember(bc, currPlayer.getID());
            int gameWins = currPlayer.getWins();
            int gamesPlayed = gameWins + currPlayer.getLosses();

            String cmd = "cycle";
            if (subs.contains(currPlayer)) {
                cmd = "sub";
            }

            if (data.containsKey(user.getId())) {
                errorsFound[offset + i] = log.updateUser(
                        cmd, gamesPlayed, gameWins,
                        user, link, tab, spreadsheet, data);
                playerTypes[offset + i] = 0;
            } else {
                errorsFound[offset + i] = log.addUser(
                        cmd, gamesPlayed, gameWins,
                        user, link, tab, spreadsheet);
                playerTypes[offset + i] = 1;
            }
        }
    }

    /**
     * Reports the draft.
     * @param bc a button click to analyze.
     * @param draft the draft to report.
     */
    public void matchReport(ButtonClickEvent bc, Draft draft) {
        bc.deferReply().queue();

        try {
            GoogleAPI link = new GoogleAPI(cyclesSheetID());

            // tab name of the spreadsheet
            String tab = "'Current Cycle'";

            Values spreadsheet = link.getSheet().spreadsheets().values();
            TreeMap<Object, PlayerStats> data = link.readSection(
                    bc, tab, spreadsheet);
            if (data == null) {
                throw new IOException("The spreadsheet was empty.");
            }

            List<DraftPlayer> team1 = draft.getProcess().getTeam1();
            List<DraftPlayer> team2 = draft.getProcess().getTeam2();

            int totalSize = team1.size() + team2.size();
            int[] playerTypes = new int[totalSize];
            int[] errorsFound = new int[totalSize];

            ManualLog log = new ManualLog(getPrefix());
            List<DraftPlayer> subs = draft.getSubs();
            updateSpreadsheet(log, draft, bc, team1, subs, playerTypes,
                    errorsFound, 0, link, tab, spreadsheet, data);
            updateSpreadsheet(log, draft, bc, team2, subs, playerTypes,
                    errorsFound, team1.size(), link, tab, spreadsheet, data);

            sendReport(log, draft, bc, team1, team2, subs, playerTypes, errorsFound);
            draft.log(totalSize + " " + getPrefix().toUpperCase()
                    + " draft player(s) were automatically processed.", false);
        } catch (IOException | GeneralSecurityException e) {
            draft.sendResponse(bc, "The spreadsheet could not load.", true);
            draft.log("The " + getSection()
                    + " cycles spreadsheet could not load.", true);
        }
    }
}
