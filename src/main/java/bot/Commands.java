package bot;

import bot.Tools.FileHandler;
import net.dv8tion.jda.api.JDA;
import bot.Engine.Add;
import bot.Engine.Drafts.Undo;
import bot.Engine.Drafts.Log;
import bot.Engine.Graduate;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    November 15, 2021
 * Project: LaunchPoint Bot
 * Module:  Commands.java
 * Purpose: Runs commands in the form of static methods.
 */
public class Commands {

    /**
     * Implement the bot's slash commands.
     * @param jda the bot in its built form.
     */
    public static void implementSlashCommands(JDA jda) {
        String playerString = "Tag of a player";
        ArrayList<OptionData> pList = new ArrayList<>();
        int numPlayers = 25;

        // construct parameters
        OptionData matches = new OptionData(OptionType.STRING, "matches", "Total games played", true),
                won = new OptionData(OptionType.STRING, "won", "Total games won", true);
        pList.add(new OptionData(OptionType.MENTIONABLE, String.format("player%s", 1), playerString, true));
        for (int i = 2; i <= numPlayers; i++) {
            OptionData newPlayer = new OptionData(OptionType.MENTIONABLE, String.format("player%s", i), playerString);
            pList.add(newPlayer);
        }

        // construct slash commands
        CommandData status = new CommandData("status",
                "Checks whether the bot is online or not.");
        CommandData help = new CommandData("help",
                "Displays troubleshooting information for the commands.");
        CommandData lpdraft = new CommandData("lpdraft",
                "Reports LaunchPoint scores for up to four players.")
                .addOptions(matches, won);
        CommandData lpsub = new CommandData("lpsub",
                "Reports LaunchPoint scores for up to four players who subbed.")
                .addOptions(matches, won);
        CommandData lpundo = new CommandData("lpundo",
                "Reverts the previous LaunchPoint draft command, once and only once.");
        CommandData lpadd = new CommandData("lpadd",
                "Adds players into LaunchPoint.");
        CommandData lpcoach = new CommandData("lpcoach",
                "Adds players to the LaunchPoint coaches.");
        CommandData lpgrad = new CommandData("lpgrad",
                "Graduates players from LaunchPoint.");

        // add on player parameters to slash commands
        for (int i = 0; i < numPlayers; i++) {
            OptionData currentPlayer = pList.get(i);

            if (i < 4) {
                lpdraft.addOptions(currentPlayer);
                lpsub.addOptions(currentPlayer);
            }
            lpadd.addOptions(currentPlayer);
            lpcoach.addOptions(currentPlayer);
            lpgrad.addOptions(currentPlayer);
        }

        // implement slash commands
        jda.updateCommands().addCommands(
                status, help,
                lpdraft, lpsub, lpundo, lpadd, lpcoach, lpgrad).queue();
    }

    /**
     * Saves a string to the undo file.
     * @param args the contents to save.
     */
    private static void saveContents(String[] args) {
        FileHandler save = new FileHandler("load.txt");
        String newContents;
        if (args.length == 1) {
            newContents = args[0];
        } else {
            StringBuilder contents = new StringBuilder();
            for (int i = 0; i < args.length - 1; i++) {
                contents.append(args[i]).append(" ");
            }
            contents.append(args[args.length - 1]);
            newContents = contents.toString();
        }

        save.writeContents(newContents);
    }

    /**
     * Runs the "lpdraft" command.
     * @param players the mentioned players.
     * @param args the user input.
     */
    public static void runCyclesCmd(List<Member> players, String[] args) {
        Log log = new Log();
        log.runCmd(null, players, args);

        saveContents(args);
    }

    /**
     * Runs the `lpundo` command.
     */
    public static void runUndoCmd() {
        Undo undo = new Undo();
        undo.runCmd(null, null, null);

        saveContents(new String[]{"REDACTED"});
    }

    /**
     * Runs the "lpadd" or "lpcoach" command.
     * @param players the mentioned players.
     * @param args the user input.
     */
    public static void runAddCmd(List<Member> players, String[] args) {
        Add newcomer = new Add();
        newcomer.runCmd(null, players, args);
    }

    /**
     * Runs the "lpgrad" command.
     * @param players the mentioned players.
     */
    public static void runGradCmd(List<Member> players) {
        Graduate grad = new Graduate();
        grad.runCmd(null, players, null);
    }
}
