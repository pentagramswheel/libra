package bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;

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
        String playerString = "Tag of a player";
        ArrayList<OptionData> pList = new ArrayList<>();
        int numPlayers = 25;

        // construct parameters
        OptionData matches = new OptionData(OptionType.INTEGER, "matches", "Total games played", true),
                won = new OptionData(OptionType.INTEGER, "won", "Total games won", true);
        pList.add(new OptionData(OptionType.USER, String.format("player%s", 1), playerString, true));
        for (int i = 2; i <= numPlayers; i++) {
            OptionData newPlayer = new OptionData(OptionType.USER, String.format("player%s", i), playerString);
            pList.add(newPlayer);
        }

        // general commands
        CommandData status = new CommandData("status",
                "Checks whether the bot is online or not.");
        CommandData help = new CommandData("help",
                "Displays troubleshooting information for the commands.");

        // LP commands
        CommandData lpdraft = new CommandData("lpcycle",
                "Reports LaunchPoint scores for up to four players.")
                .addOptions(matches, won);
        CommandData lpsub = new CommandData("lpsub",
                "Reports LaunchPoint scores for up to four players who subbed.")
                .addOptions(matches, won);
        CommandData lpundo = new CommandData("lpundo",
                "Reverts the previous LaunchPoint draft command, once and only once.");
        CommandData lpadd = new CommandData("lpadd",
                "Adds players into LaunchPoint.");
        CommandData lpgrad = new CommandData("lpgrad",
                "Graduates players from LaunchPoint.");

        // IO commands
        CommandData iodraft = new CommandData("iocycle",
                "Reports Ink Odyssey scores for up to four players.")
                .addOptions(matches, won);
        CommandData iosub = new CommandData("iosub",
                "Reports Ink Odyssey scores for up to four players who subbed.")
                .addOptions(matches, won);
        CommandData ioundo = new CommandData("ioundo",
                "Reverts the previous Ink Odyssey draft command, once and only once.");
        CommandData ioadd = new CommandData("ioadd",
                "Adds players into Ink Odyssey.");
        CommandData iograd = new CommandData("iograd",
                "Graduates players from Ink Odyssey.");

        // add on player parameters to specific commands
        for (int i = 0; i < numPlayers; i++) {
            OptionData currentPlayer = pList.get(i);

            if (i < 4) {
                lpdraft.addOptions(currentPlayer);
                lpsub.addOptions(currentPlayer);
                iodraft.addOptions(currentPlayer);
                iosub.addOptions(currentPlayer);
            }
            lpadd.addOptions(currentPlayer);
            lpgrad.addOptions(currentPlayer);
            ioadd.addOptions(currentPlayer);
            iograd.addOptions(currentPlayer);
        }

        // implement slash commands
        jda.updateCommands().addCommands(
                status, help,
                lpdraft, lpsub, lpundo, lpadd, lpgrad,
                iodraft, iosub, ioundo, ioadd, iograd).queue();
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
