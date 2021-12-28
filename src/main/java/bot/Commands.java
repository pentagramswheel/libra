package bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;

import bot.Engine.Add;
import bot.Engine.Drafts.Undo;
import bot.Engine.Drafts.Log;
import bot.Engine.Graduate;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

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
        int numPlayers = 100;

        OptionData matches = new OptionData(OptionType.INTEGER, "matches", "Total games played", true),
                won = new OptionData(OptionType.INTEGER, "won", "Total games won", true);
        pList.add(new OptionData(OptionType.MENTIONABLE, "player" + 1, playerString, true));
        for (int i = 2; i <= numPlayers; i++) {
            OptionData newPlayer = new OptionData(OptionType.MENTIONABLE, "player" + i, playerString);
            pList.add(newPlayer);
        }

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

        jda.updateCommands().addCommands(
                status, help,
                lpdraft, lpsub, lpundo, lpadd, lpcoach, lpgrad).queue();
    }

    /**
     * Saves a string to the undo file.
     * @param args the contents to save.
     */
    private static void saveContents(String[] args) {
        File save = new File("load.txt");
        try {
            save.createNewFile();
            FileWriter fw = new FileWriter(save);

            if (args.length == 1) {
                fw.write(args[0]);
            } else {
                StringBuilder contents = new StringBuilder();
                contents.append(args[0].toUpperCase()).append(" ");
                for (int i = 1; i < args.length - 1; i++) {
                    contents.append(args[i]).append(" ");
                }
                contents.append(args[args.length - 1]);

                fw.write(contents.toString());
            }

            fw.close();
        } catch (IOException ioe) {
            Events.ORIGIN.sendMessage(
                    "The undo file could not be loaded.").queue();
            ioe.printStackTrace();
        }
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

    /** Runs the "lpexit" command. */
    public static void runExitCmd() {
        try {
            Events.ORIGIN.sendMessage("The bot has been terminated.").queue();
            Thread.sleep(3000);
            System.out.println("----------");
            System.exit(0);
        } catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
