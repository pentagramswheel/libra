package bot;

import bot.Engine.Add;
import bot.Engine.Drafts.Log;
import bot.Engine.Drafts.Undo;
import bot.Engine.Graduate;
import bot.Tools.FileHandler;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * @author  Wil Aquino, Turtle
 * Date:    February 17, 2021
 * Project: LaunchPoint Bot
 * Module:  Events.java
 * Purpose: Builds the bot by processing commands and analyzing user input.
 */
public class Events extends ListenerAdapter {

    /** The current state of the bot. */
    public static JDABuilder BOT;

    /** The Discord server's current state. */
    public static Guild SERVER;

    /** The original place a command was sent in. */
    public static InteractionHook ORIGIN;

    /**
     * Checks if the game set parameters make sense.
     * @param args the parameters to analyze.
     * @return True if there were less wins than total games.
     *         False otherwise.
     */
    private boolean gamesPlayedValid(List<OptionMapping> args) {
        int gamesPlayed = (int) args.get(0).getAsLong();
        int gamesWon = (int) args.get(1).getAsLong();
        if (gamesPlayed < gamesWon) {
            ORIGIN.sendMessage("Total games won cannot go beyond the set. "
                    + "Try again.").queue();
            return false;
        }

        return true;
    }

    /**
     * Checks whether the command user has permission to use the
     * command or not.
     * @param cmd the formal name of the command.
     * @param author the user of the command.
     * @return True if the command is not a staff command.
     *         False otherwise.
     */
    private boolean isStaffCommand(String cmd, Member author) {
        String[] staffCommands = {"cycle", "sub", "undo", "add", "grad"};
        Role staffRole = SERVER.getRolesByName("Staff", true).get(0);

        if (cmd.isEmpty()) {
            return false;
        } else if (!author.getRoles().contains(staffRole))
            for (String staffCmd : staffCommands) {
                if (cmd.contains(staffCmd)) {
                    return true;
                }
        }

        return false;
    }

    /**
     * Prints troubleshooting information.
     */
    private void printTroubleshootString() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Command Troubleshooting");
        eb.setColor(Color.BLUE);
        eb.addField("Manual Draft Reporting",
                "If a match report is giving you an error message, \n"
                        + "it is most likely due to a row in the spreadsheet \n"
                        + "missing information. For example, one common \n"
                        + "problem is presetting or overextending the \n"
                        + "formulas past the bottommost row.",
                false);
        eb.addField("Entering and Graduating Players",
                "If the role for a player isn't showing up or seemingly \n"
                        + "isn't being added, try *refreshing the roles* by opening \n"
                        + "`Server Settings > User Management > Members`. \n"
                        + "A second layer of refreshing can be done by \n"
                        + "searching for a player's name in `... > Members`. \n"
                        + "The roles should exist; this is currently a bug within \n"
                        + "Discord.",
                false);

        ORIGIN.sendMessageEmbeds(eb.build()).queue();
    }

    /**
     * Find the undo file to load.
     * @param cmd the formal name of the command.
     * @return said file.
     */
    private FileHandler findSave(String cmd) {
        if (cmd.startsWith("lp")) {
            return new FileHandler("loadLP.txt");
        } else {
            return new FileHandler("loadIO.txt");
        }
    }

    /**
     * Structures a user into a mentionable ping.
     * @param om an argument from a command.
     * @return the formatted ping.
     */
    private String mentionableFor(OptionMapping om) {
        String id = om.getAsMember().getId();
        return String.format("<@%s>", id);
    }

    /**
     * Saves a cycle command to an undo file.
     * @param cmd the formal name of the command.
     * @param args the arguments of the command, if they exist.
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

        FileHandler save = findSave(cmd);
        save.writeContents(contents.toString());
    }

    /**
     * Runs one of the bot's commands.
     * @param sc the slash command to analyze.
     */
    @Override
    public void onSlashCommand(SlashCommandEvent sc) {
        Member author = sc.getMember();
        String cmd = sc.getName();
        String subCmd = sc.getSubcommandName();
        List<OptionMapping> args = sc.getOptions();
        if (subCmd == null) {
            subCmd = "";
        }
        String formalCmd = cmd + subCmd;

        SERVER = sc.getGuild();
        ORIGIN = sc.getHook();

        sc.deferReply().queue();
        if (isStaffCommand(formalCmd, author)) {
            ORIGIN.sendMessage(
                    "You do not have permission to use this command.").queue();
            return;
        }

        switch (formalCmd) {
            case "mitstatus":
                ORIGIN.sendMessageFormat(
                        "The bot is online. Welcome, %s.",
                        author.getEffectiveName()).queue();
                break;
            case "mithelp":
                printTroubleshootString();
                break;
            case "mitprofile":
                ORIGIN.sendMessage(
                        "This command has not been implemented yet.").queue();
                break;
            case "lpadd":
            case "ioadd":
                Add newcomer = new Add();
                newcomer.runCmd(null, formalCmd, args);
                break;
            case "lpgrad":
            case "iograd":
                Graduate grad = new Graduate();
                grad.runCmd(null, formalCmd, args);
                break;
            case "startdraft":
                System.out.println("A draft has been started.");
                List<Member> players = new ArrayList<>();
                players.add(sc.getMember());

//                StartDraft sd = new StartDraft();
//                sd.runCmd(null, author, null, e); // author - users to attach, e - slash command
            case "lpcycle":
            case "lpsub":
            case "iocycle":
            case "iosub":
                if (gamesPlayedValid(args)) {
                    Log log = new Log();
                    log.runCmd(null, formalCmd, args);

                    saveCycleCall(formalCmd, args);
                }
                break;
            case "lpundo":
            case "ioundo":
                Undo undo = new Undo();
                undo.runCmd(null, formalCmd, null);

                FileHandler save = findSave(formalCmd);
                save.writeContents("REDACTED");
                break;
        }
    }

    /**
     * Checks any button clicks.
     * @param bc the button click to analyze.
     */
    @Override
    public void onButtonClick(ButtonClickEvent bc){
        System.out.println("hi");
        if (bc.getButton().getId().equals("Join")){
            List<Member> author = new ArrayList<>();
            author.add(bc.getMember());

//            sd.runCmd2(null, author, e); // author - users to attach, e - slash command
        }
    }
}
