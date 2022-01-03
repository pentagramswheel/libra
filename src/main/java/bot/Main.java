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
        // general commands
        CommandData status = new CommandData("mitstatus",
                "Checks whether the bot is online or not.");
        CommandData help = new CommandData("mithelp",
                "Displays troubleshooting information for the commands.");

        // profile commands
        CommandData profile = new CommandData("mitprofile",
                "Displays troubleshooting information for the commands.");
        // edit in subcommand data in here chaedr

        // draft commands
        CommandData lp = new CommandData("lp",
                "Commands to use within LaunchPoint.");
        CommandData io = new CommandData("io",
                "Commands to use within Ink Odyssey.");
        SubcommandData startdraft = new SubcommandData("startdraft",
                "Begins an automatic draft with up to 8 players.");
        SubcommandData cycle = new SubcommandData("cycle",
                "Manually reports draft scores for up to four players.");
        SubcommandData sub = new SubcommandData("sub",
                "Manually reports draft scores for up to four players who subbed.");
        SubcommandData undo = new SubcommandData("undo",
                "Manually reverts the previous draft command, once and only once.");
        SubcommandData add = new SubcommandData("add",
                "Adds players into an area within MIT.");
        SubcommandData grad = new SubcommandData("grad",
                "Graduates players from an area within MIT.");

        int numMentions = 25;
        OptionData matches = new OptionData(
                OptionType.INTEGER, "matches", "Total games played", true);
        OptionData won = new OptionData(
                OptionType.INTEGER, "won", "Total games won", true);
        cycle.addOptions(matches, won);
        sub.addOptions(matches, won);

        // adding user parameters to any commands
        for (int i = 1; i <= numMentions; i++) {
            OptionData newMention;
            if (i == 1) {
                newMention = new OptionData(
                        OptionType.USER, "player", "Tag of a player", true);
            } else {
                newMention = new OptionData(
                        OptionType.USER, String.format("player%s", i), "Tag of a player");
            }

            if (i <= 4) {
                cycle.addOptions(newMention);
                sub.addOptions(newMention);
            }
            add.addOptions(newMention);
            grad.addOptions(newMention);
        }

        // implementing commands
//        profile.addSubcommands(...);
        lp.addSubcommands(startdraft, cycle, sub, undo, add, grad);
        io.addSubcommands(startdraft, cycle, sub, undo, add, grad);
        jda.updateCommands().addCommands(
                status, help, lp, io, profile).queue();
    }

    public static void main(String[] args) {
        Events.BOT = JDABuilder.createLight(Discord.getToken())
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .enableIntents(GatewayIntent.GUILD_MEMBERS);

        Events.BOT.addEventListeners(new Events());

        try {
            JDA jda = Events.BOT.build();

            // run only if slash commands are not implemented yet
//            Main.implementSlashCommands(jda);

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
