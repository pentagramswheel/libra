package bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.Random;

/**
 * @author  Wil Aquino
 * Date:    February 17, 2021
 * Project: Libra
 * Module:  Main.java
 * Purpose: The entry point of the bot.
 */
public class Main {

    /** Name of the bot and application. */
    public static String NAME = "Libra";

    /**
     * Implement the bot's slash commands.
     * @param jda the bot in its built form.
     */
    public static void implementSlashCommands(JDA jda) {
        // general commands
        CommandData mit = new CommandData("mit",
                "General commands to use within MIT.");
        SubcommandData status = new SubcommandData("status",
                "Checks whether the bot is online or not.");
        SubcommandData help = new SubcommandData("help",
                "Displays troubleshooting information for the commands.");
        SubcommandData cycleCalculate = new SubcommandData("cycle calculate",
                "Calculates the points for MIT's current draft leaderboards.");
        SubcommandData draftdoc = new SubcommandData("draftdoc",
                "Retrieves the documentation for the automated draft system.");

        // profile commands
        SubcommandGroupData profile = new SubcommandGroupData("profile",
                "Finds profile information on a player within MIT.");
        // edit in subcommand data in here Robbinson
//        profile.addSubcommands(...)

        // section commands
        CommandData lp = new CommandData("lp",
                "Commands to use within LaunchPoint.");
        CommandData io = new CommandData("io",
                "Commands to use within Ink Odyssey.");

        SubcommandData genmaps = new SubcommandData("genmaps",
                "Generates a set map list.");
        SubcommandData startdraft = new SubcommandData("startdraft",
                "Requests an automatic draft with up to 8 players.");
        SubcommandData forcesub = new SubcommandData("forcesub",
                "Forces a player within a draft to become a sub.");
        SubcommandData forceend = new SubcommandData("forceend",
                "Forces a draft to end.");
        SubcommandData cycle = new SubcommandData("cycle",
                "Manually reports draft scores for up to four players.");
        SubcommandData sub = new SubcommandData("sub",
                "Manually reports draft scores for up to four players who subbed.");
        SubcommandData undo = new SubcommandData("undo",
                "Manually reverts the previous draft command, once and only once.");

        SubcommandData add = new SubcommandData("add",
                "Adds players into the designated area within MIT.");
        SubcommandData deny = new SubcommandData("deny",
                "Rejects players from a designated area within MIT.");
        SubcommandData grad = new SubcommandData("grad",
                "Graduates players from the designated section within MIT.");
        SubcommandData award = new SubcommandData("award",
                "Gives players leaderboard awards for the current MIT cycle.");

        int numMentions = 25;
        OptionData numDraft = new OptionData(
                OptionType.INTEGER, "numdraft", "The designated number of this draft", true);
        OptionData matches = new OptionData(
                OptionType.INTEGER, "matches", "Total games played", true);
        OptionData won = new OptionData(
                OptionType.INTEGER, "won", "Total games won", true);
        cycle.addOptions(matches, won);
        sub.addOptions(matches, won);

        OptionData maps = new OptionData(
                OptionType.INTEGER, "matches",
                "Amount of maps to generate.", true);
        genmaps.addOptions(maps);

        OptionData leaderboardAward = new OptionData(OptionType.INTEGER, "role",
                "The leaderboard role to give", true);
        leaderboardAward.addChoice("1st Place", 1);
        leaderboardAward.addChoice("2nd Place", 2);
        leaderboardAward.addChoice("3rd Place", 3);
        leaderboardAward.addChoice("Top 10", 4);
        leaderboardAward.addChoice("Past Podium", 5);
        leaderboardAward.addChoice("Past Top 10", 6);
        award.addOptions(leaderboardAward);

        // adding user parameters to any commands
        for (int i = 1; i <= numMentions; i++) {
            OptionData newMention;
            if (i == 1) {
                newMention = new OptionData(
                        OptionType.USER, "player", "Tag of a player", true);
                forcesub.addOptions(numDraft, newMention);
                forceend.addOptions(numDraft);
            } else {
                newMention = new OptionData(
                        OptionType.USER, String.format("player%s", i), "Tag of a player");
            }

            if (i <= 4) {
                cycle.addOptions(newMention);
                sub.addOptions(newMention);
            }
            if (i <= 24) {
                award.addOptions(newMention);
            }
            add.addOptions(newMention);
            deny.addOptions(newMention);
            grad.addOptions(newMention);
        }

        // implementing commands
        mit.addSubcommands(status, help, cycleCalculate, draftdoc);
        mit.addSubcommandGroups(profile);
        lp.addSubcommands(
                genmaps, startdraft, forcesub, forceend,
                cycle, sub, undo, add, deny, grad,
                award);
        io.addSubcommands(
                genmaps, startdraft, forcesub, forceend,
                cycle, sub, undo, add, deny, grad,
                award);

        jda.updateCommands().addCommands(mit, lp, io).queue();
    }

    /**
     * The main entry point of the bot (run this method).
     * @param args input arguments, if any.
     */
    public static void main(String[] args) {
        try {
            Random rGen = new Random();

            System.out.println();
            JDA jda = JDABuilder.createLight(Config.botToken)
                    .enableIntents(GatewayIntent.GUILD_PRESENCES)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .addEventListeners(new Events(rGen))
                    .build();

            // run only if all slash commands have not been implemented yet
            Main.implementSlashCommands(jda);

            String status = "Splatoon 3";
            jda.getPresence().setPresence(
                    OnlineStatus.IDLE,
                    Activity.playing(status));

            Thread.sleep(3000);
            System.out.println("\nUSAGE LOG:\n==========");
        } catch (LoginException le) {
            le.printStackTrace();
        } catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
