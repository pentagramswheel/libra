package bot;

import net.dv8tion.jda.api.entities.Member;

import bot.Engine.Add;
import bot.Engine.Drafts.Undo;
import bot.Engine.Drafts.Log;
import bot.Engine.Graduate;

import java.io.File;
import java.io.FileWriter;
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
     * Runs the "lpwin" or "lplose" command.
     * @param players the mentioned players.
     * @param args the arguments of the command.
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
     * @param args the arguments of the command.
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
