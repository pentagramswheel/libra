package bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

/**
 * @author  Wil Aquino
 * Date:    February 17, 2021
 * Project: LaunchPoint Bot
 * Module:  Main.java
 * Purpose: The entry point of the bot.
 */
public class Main {

    /** Name of the bot and application. */
    public static String NAME = "LaunchPoint Simp";

    /**
     * Implement the bot's slash commands.
     * @param jda the bot in its built form.
     */
    public static void implementSlashCommands(JDA jda) {
//        String playerString = "Tag of a player";
//        ArrayList<OptionData> pList = new ArrayList<>();
//        int numPlayers = 25;
//
//        // construct parameters
//        OptionData matches = new OptionData(OptionType.INTEGER, "matches", "Total games played", true),
//                won = new OptionData(OptionType.INTEGER, "won", "Total games won", true);
//        pList.add(new OptionData(OptionType.USER, String.format("player%s", 1), playerString, true));
//        for (int i = 2; i <= numPlayers; i++) {
//            OptionData newPlayer = new OptionData(OptionType.USER, String.format("player%s", i), playerString);
//            pList.add(newPlayer);
//        }

        CommandData status = new CommandData("status",
                "Checks whether the bot is online or not.");
        CommandData help = new CommandData("help",
                "Displays troubleshooting information for the commands.");

//        // LP commands
//        CommandData lpcycle = new CommandData("lpcycle",
//                "Reports LaunchPoint scores for up to four players.")
//                .addOptions(matches, won);
//        CommandData lpsub = new CommandData("lpsub",
//                "Reports LaunchPoint scores for up to four players who subbed.")
//                .addOptions(matches, won);
//        CommandData lpundo = new CommandData("lpundo",
//                "Reverts the previous LaunchPoint draft command, once and only once.");
//        CommandData lpadd = new CommandData("lpadd",
//                "Adds players into LaunchPoint.");
//        CommandData lpgrad = new CommandData("lpgrad",
//                "Graduates players from LaunchPoint.");
//
//        // IO commands
//        CommandData iocycle = new CommandData("iocycle",
//                "Reports Ink Odyssey scores for up to four players.")
//                .addOptions(matches, won);
//        CommandData iosub = new CommandData("iosub",
//                "Reports Ink Odyssey scores for up to four players who subbed.")
//                .addOptions(matches, won);
//        CommandData ioundo = new CommandData("ioundo",
//                "Reverts the previous Ink Odyssey draft command, once and only once.");
//        CommandData ioadd = new CommandData("ioadd",
//                "Adds players into Ink Odyssey.");
//        CommandData iograd = new CommandData("iograd",
//                "Graduates players from Ink Odyssey.");

        CommandData lp = new CommandData("lp",
                "Commands to use within LaunchPoint.");
        CommandData io = new CommandData("io",
                "Commands to use within Ink Odyssey.");

        SubcommandData startdraft = new SubcommandData("startdraft",
                "Begins an automatic draft with up to 8 players");
        SubcommandData cycle = new SubcommandData("cycle",
                "Reports draft scores for up to four players.");
        SubcommandData sub = new SubcommandData("cycle",
                "Reports draft scores for up to four players who subbed.");
        SubcommandData undo = new SubcommandData("undo",
                "Reverts the previous draft command, once and only once.");
        SubcommandData add = new SubcommandData("add",
                "Adds players into an area within MIT.");
        SubcommandData grad = new SubcommandData("grad",
                "Graduates players from an area within MIT.");

//        // add on player parameters to specific commands
//        for (int i = 0; i < numPlayers; i++) {
//            OptionData currentPlayer = pList.get(i);
//
//            if (i < 4) {
////                lpdraft.addOptions(currentPlayer);
////                lpsub.addOptions(currentPlayer);
////                iodraft.addOptions(currentPlayer);
////                iosub.addOptions(currentPlayer);
//                cycle.addOptions(currentPlayer);
//                sub.addOptions(currentPlayer);
//            }
////            lpadd.addOptions(currentPlayer);
////            lpgrad.addOptions(currentPlayer);
////            ioadd.addOptions(currentPlayer);
////            iograd.addOptions(currentPlayer);
//        }

        int numMentions = 25;
        OptionData matches = new OptionData(OptionType.INTEGER, "matches", "Total games played", true);
        OptionData won = new OptionData(OptionType.INTEGER, "won", "Total games won", true);
        cycle.addOptions(matches, won);
        sub.addOptions(matches, won);
        for (int i = 0; i < numMentions; i++) {
            OptionData newMention;
            if (i == 0) {
                newMention = new OptionData(OptionType.USER, "player", "Tag of a player", true);
            } else {
                newMention = new OptionData(OptionType.USER, String.format("player%s", i), "Tag of a player");
            }

            if (i < 4) {
                cycle.addOptions(newMention);
                sub.addOptions(newMention);
            }
            add.addOptions(newMention);
            grad.addOptions(newMention);
        }

        lp.addSubcommands(cycle, sub, undo, add);
        io.addSubcommands(cycle, sub, undo, add);

        // implement slash commands
//        jda.updateCommands().addCommands(
//                status, help,
//                lpdraft, lpsub, lpundo, lpadd, lpgrad,
//                iodraft, iosub, ioundo, ioadd, iograd).queue();
        jda.updateCommands().addCommands(
                status, help, lp, io).queue();
    }

    public static void main(String[] args) {
        Events.BOT = JDABuilder.createLight(Discord.getToken())
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .enableIntents(GatewayIntent.GUILD_MEMBERS);

        Events.BOT.addEventListeners(new Events());

        try {
            JDA jda = Events.BOT.build();

            // run only if slash commands are not implemented yet
            Main.implementSlashCommands(jda);

            String status = "lphelp | simping for @everyone";
            jda.getPresence().setActivity(Activity.playing(
                    status));

            Thread.sleep(4000);
            System.out.println("\nUSAGE LOG:\n==========");
        } catch (LoginException le) {
            le.printStackTrace();
        } catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
