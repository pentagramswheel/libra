package bot.Engine.Cycles;

import bot.Engine.Section;
import bot.Engine.Templates.Command;
import bot.Tools.FileHandler;
import bot.Tools.GoogleSheetsAPI;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import com.google.api.services.sheets.v4.model.ValueRange;

import java.awt.Color;
import java.util.List;
import java.util.Arrays;
import java.util.TreeMap;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author  Wil Aquino
 * Date:    February 17, 2021
 * Project: Libra
 * Module:  ManualLog.java
 * Purpose: Logs MIT leaderboard information via command.
 */
public class ManualLog extends Section implements Command {

    /**
     * Constructs the cycle log attributes.
     * @param abbreviation the abbreviation of the section.
     */
    public ManualLog(String abbreviation) {
        super(abbreviation);
    }

    /**
     * Formats a user into a mentionable ping, ignoring nicknames.
     * @param om an argument from a command.
     */
    private String mentionableFor(OptionMapping om) {
        String id = om.getAsMember().getId();
        return String.format("<@%s>", id);
    }

    /**
     * Saves a cycle command to an undo file.
     * @param cmd the formal name of the command.
     * @param args the arguments of the command.
     */
    private void saveCycleCall(String cmd, List<OptionMapping> args) {
        List<OptionMapping> userArgs = args.subList(2, args.size());
        StringBuilder contents = new StringBuilder();
        int lastIndex = userArgs.size() - 1;

        contents.append(cmd).append(" ")
                .append(args.get(0).getAsString()).append(" ")
                .append(args.get(1).getAsString()).append(" ");
        for (int i = 0; i < lastIndex; i++) {
            contents.append(mentionableFor(userArgs.get(i))).append(" ");
        }
        contents.append(mentionableFor(userArgs.get(lastIndex)));

        FileHandler save = new FileHandler(
                "load" + getPrefix().toUpperCase() + ".txt");
        save.writeContents(contents.toString());
    }

    /**
     * Checks if this is a draft or sub command.
     * @param cmd the formal name of the command.
     * @return True if a draft command was called.
     *         False if a sub command was called.
     */
    public boolean notSub(String cmd) {
        return cmd.contains("log");
    }

    /**
     * Retrieve the amount of draft games were played.
     * @param args the arguments of the command.
     */
    public int getGamesPlayed(List<OptionMapping> args) {
        return (int) args.get(0).getAsLong();
    }

    /**
     * Retrieve the amount of won draft games.
     * @param args the arguments of the command.
     */
    public int getGamesWon(List<OptionMapping> args) {
        return (int) args.get(1).getAsLong();
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
     * @param sc the user's inputted command.
     * @param players the players within the set.
     * @param playerTypes array of types for each player
     *                    (0 if an existing player, 1 if a new player).
     * @param errorsFound array of errors found for each player, if any
     *                    (0 if no errors occurred, 1 otherwise).
     */
    private void sendReport(SlashCommandEvent sc, List<OptionMapping> players,
                            int[] playerTypes, int[] errorsFound) {
        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder playerList = new StringBuilder();

        for (int i = 0; i < players.size(); i++) {
            Member player = players.get(i).getAsMember();
            String completionSymbol = ":white_check_mark: ";
            if (errorsFound[i] == 1) {
                completionSymbol = ":no_entry: ";
            }

            if (playerTypes[i] == 0) {
                playerList.append(completionSymbol)
                        .append(player.getAsMention())
                        .append("\n");
            } else {
                playerList.append(completionSymbol)
                        .append(player.getAsMention())
                        .append(" (new)\n");
            }
        }

        List<OptionMapping> args = sc.getOptions();
        int wins = getGamesWon(args);
        int losses = getGamesPlayed(args) - wins;

        eb.setTitle("Summary of Report")
                .setColor(getColor())
                .addField("Score:", wins + " - " + losses, false)
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
     * @param cmd the formal name of the command.
     * @param gamesPlayed the total games played.
     * @param gameWins the total games won.
     * @param user the player to update the stats of.
     * @param link a connection to the spreadsheet.
     * @param stats the stats of the player.
     * @return 0 if the player could be found in the spreadsheet.
     *         1 otherwise.
     */
    public int updateUser(String cmd, int gamesPlayed, int gameWins,
                          Member user, GoogleSheetsAPI link,
                          PlayerStats stats) {
        try {
            int gameLosses = gamesPlayed - gameWins;

            int setWins = stats.getSetWins();
            int setLosses = stats.getSetLosses();
            int setsPlayed = setWins + setLosses;
            double setWinrate = 0.0;
            if (notSub(cmd) && gamesPlayed > 0) {
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

            gameWins += stats.getGamesWon();
            gameLosses += stats.getGamesLost();
            gamesPlayed = gameWins + gameLosses;
            double gameWinrate = 0.0;
            if (gamesPlayed > 0) {
                gameWinrate =  (double) gameWins / gamesPlayed;
            }

            String updateRange = link.buildRange(CYCLES_TAB,
                    CYCLES_START_COLUMN, stats.getSpreadsheetPosition(),
                    CYCLES_END_COLUMN, stats.getSpreadsheetPosition());
            ValueRange newRow = link.buildRow(Arrays.asList(
                    user.getUser().getAsTag(), user.getEffectiveName(),
                    setWins, setLosses, setsPlayed, setWinrate,
                    gameWins, gameLosses, gamesPlayed, gameWinrate));
            link.updateRange(updateRange, newRow);

            return 0;
        } catch (IOException e) {
            log("Existing " + getPrefix().toUpperCase() + " cycle user error "
                    + "occurred with " + user.getUser().getAsMention() + ".", true);
            return 1;
        }
    }

    /**
     * Adds a user's stats within a spreadsheet.
     * @param cmd the formal name of the command.
     * @param gamesPlayed the total games played.
     * @param gameWins the total games won.
     * @param user the player to update the stats of.
     * @param link a connection to the spreadsheet.
     * @return 0 if the player could be found in the spreadsheet.
     *         1 otherwise.
     *
     * Note: Users will be added at the next EMPTY row in the spreadsheet.
     */
    public int addUser(String cmd, int gamesPlayed, int gameWins,
                       Member user, GoogleSheetsAPI link) {
        try {
            int gameLosses = gamesPlayed - gameWins;
            double gameWinrate = 0.0;
            if (gamesPlayed > 0) {
                gameWinrate =  (double) gameWins / gamesPlayed;
            }

            int setWins = 0;
            int setLosses = 0;
            int setsPlayed = 0;
            double setWinrate = 0.0;
            if (notSub(cmd) && gamesPlayed > 0) {
                if (cycleSetWon(gameWins, gamesPlayed)) {
                    setWins++;
                } else {
                    setLosses++;
                }

                setsPlayed = 1;
                setWinrate = (double) setWins / setsPlayed;
            }

            ValueRange newRow = link.buildRow(Arrays.asList(
                    user.getId(), user.getUser().getAsTag(), user.getEffectiveName(),
                    setWins, setLosses, setsPlayed, setWinrate,
                    gameWins, gameLosses, gamesPlayed, gameWinrate));
            link.appendRow(CYCLES_TAB, newRow);

            return 0;
        } catch (IOException e) {
            log("New " + getPrefix().toUpperCase() + " cycle user error "
                    + "occurred with " + user.getUser().getAsMention() + ".", true);
            return 1;
        }
    }

    /**
     * Runs the cycle logging command.
     * @param sc the user's inputted command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        sc.deferReply(false).queue();

        String cmd = sc.getSubcommandName();
        List<OptionMapping> args = sc.getOptions();

        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(cyclesSheetID());
            TreeMap<Object, Object> data = link.readSection(sc, CYCLES_TAB);

            List<OptionMapping> userArgs = extractUsers(sc);
            int numUsers = userArgs.size();
            int[] playerTypes = new int[numUsers];
            int[] errorsFound = new int[numUsers];
            for (int i = 0; i < numUsers; i++) {
                Member user = userArgs.get(i).getAsMember();
                if (data.containsKey(user.getId())) {
                    PlayerStats stats = (PlayerStats) data.get(user.getId());
                    errorsFound[i] = updateUser(
                            cmd, getGamesPlayed(args), getGamesWon(args),
                            user, link, stats);
                    playerTypes[i] = 0;
                } else {
                    errorsFound[i] = addUser(
                            cmd, getGamesPlayed(args), getGamesWon(args),
                            user, link);
                    playerTypes[i] = 1;
                }
            }

            sendReport(sc, userArgs, playerTypes, errorsFound);
            log(userArgs.size() + " " + getPrefix().toUpperCase() + " "
                    + "draft player(s) were manually processed by "
                    + sc.getUser().getAsTag(), false);
        } catch (IOException | GeneralSecurityException e) {
            editMessage(sc, "The leaderboard could not load.");
            log("The " + getSection()
                    + " cycles spreadsheet could not load.", true);
        }

        saveCycleCall(cmd, args);
    }
}
