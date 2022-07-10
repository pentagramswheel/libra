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
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.awt.Color;
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

    /** Colors for the bot to reference. */
    public static Color mitColor = new Color(0, 154, 255);
    public static Color launchpointColor = new Color(13, 255, 0);
    public static Color inkodysseyColor = new Color(255, 0, 144);
    public static Color freshwatershoalsColor = new Color(135, 0, 255);

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
        SubcommandData draftdoc = new SubcommandData("draftdoc",
                "Retrieves the documentation for the automated draft system.");

        // quick profile commands
        SubcommandData qprofile = new SubcommandData("qprofile",
                "Creates a quick profile within MIT.");

        OptionData fcParam = new OptionData(
                OptionType.STRING, "friendcode", "Your friend code", true);
        OptionData nicknameParam = new OptionData(
                OptionType.STRING, "name", "Your preferred nickname", true);
        OptionData pronounChoices = new OptionData(
                OptionType.STRING, "pronouns", "Your preferred pronouns", true);
        OptionData playstyleChoices = new OptionData(
                OptionType.STRING, "playstyle", "Your preferred playstyle/position", true);
        String[] playstyles = {"Slayer", "Skirmisher", "Support", "Anchor", "Flex"};
        for (int i = 1; i <= playstyles.length; i++) {
            playstyleChoices.addChoice(playstyles[i - 1], playstyles[i - 1]);
        }

        OptionData weaponsParam = new OptionData(
                OptionType.STRING, "weapons", "Your preferred weapon pool", true);
        OptionData rankChoices = new OptionData(
                OptionType.STRING, "rank", "Your average rank", true);
        String[] ranks = {"C", "B", "A", "S", "S+", "X 2000",
                "X 2100-2200", "X 2300-2400", "X 2500-2600", "X 2700+"};
        for (int i = 1; i <= ranks.length; i++) {
            rankChoices.addChoice(ranks[i - 1], ranks[i - 1]);
        }

        qprofile.addOptions(fcParam, nicknameParam, pronounChoices,
                playstyleChoices, weaponsParam, rankChoices);

        // profile commands
        SubcommandGroupData profile = new SubcommandGroupData("profile",
                "Finds or enters information about a player within MIT.");
        SubcommandData fc = new SubcommandData("fc",
                "Creates your profile by adding your friend code to it.");
        SubcommandData getfc = new SubcommandData("getfc",
                "Retrieves the friend code of another user, if provided.");
        SubcommandData view = new SubcommandData("view",
                "Looks up the profile of another user, if provided.");
        SubcommandData nickname = new SubcommandData("nickname",
                "Modifies the nickname of your profile.");
        SubcommandData pronouns = new SubcommandData("pronouns",
                "Modifies the pronouns of your profile.");
        SubcommandData playstyle = new SubcommandData("playstyle",
                "Modifies the playstyle of your profile.");
        SubcommandData weapons = new SubcommandData("weapons",
                "Modifies the main weapons of your profile.");
        SubcommandData rank = new SubcommandData("rank",
                "Modifies the average rank of your profile.");
        SubcommandData team = new SubcommandData("team",
                "Modifies the competitive team of your profile.");
        SubcommandData delete = new SubcommandData("delete",
                "Deletes your profile.");

        OptionData viewParam = new OptionData(
                OptionType.USER, "player", "The player to look up", false);
        OptionData teamParam = new OptionData(
                OptionType.STRING, "name", "Your team's name", true);

        fc.addOptions(fcParam);
        getfc.addOptions(viewParam);
        view.addOptions(viewParam);
        nickname.addOptions(nicknameParam);
        pronouns.addOptions(pronounChoices);

        playstyle.addOptions(playstyleChoices);
        weapons.addOptions(weaponsParam);
        rank.addOptions(rankChoices);
        team.addOptions(teamParam);

        profile.addSubcommands(fc, getfc, view, nickname, pronouns,
                playstyle, weapons, rank, team,
                delete);

        // section commands
        CommandData lp = new CommandData("lp",
                "Commands to use within LaunchPoint.");
        CommandData io = new CommandData("io",
                "Commands to use within Ink Odyssey.");

        SubcommandData genmaps = new SubcommandData("genmaps",
                "Generates a set map list.");
        SubcommandData leaderboard = new SubcommandData("leaderboard",
                "Retrieves the leaderboard for the MIT section.");

        SubcommandData startdraft = new SubcommandData("startdraft",
                "Requests an automatic draft with up to 8 players.");
        SubcommandData forcesub = new SubcommandData("forcesub",
                "Forces a player within a draft to become a sub.");
        SubcommandData forceend = new SubcommandData("forceend",
                "Forces a draft to end.");

        SubcommandData log = new SubcommandData("log",
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

        SubcommandData cycleCalculate = new SubcommandData("cyclescalc",
                "Performs a cycle change for the MIT section.");
        SubcommandData award = new SubcommandData("award",
                "Gives players leaderboard awards for the current MIT cycle.");

        int numMentions = 10;
        OptionData numDraft = new OptionData(
                OptionType.INTEGER, "numdraft", "The designated number of this draft", true);
        OptionData matches = new OptionData(
                OptionType.INTEGER, "matches", "Total games played", true);
        OptionData won = new OptionData(
                OptionType.INTEGER, "won", "Total games won", true);
        log.addOptions(matches, won);
        sub.addOptions(matches, won);

        OptionData maps = new OptionData(
                OptionType.INTEGER, "matches",
                "Amount of maps to generate.", true);
        genmaps.addOptions(maps);

        OptionData leaderboardAward = new OptionData(OptionType.INTEGER, "role",
                "The leaderboard role to give", true);
        String[] leaderboardChoices = {"1st Place", "2nd Place", "3rd Place", "Top 10"};
        for (int i = 1; i <= leaderboardChoices.length; i++) {
            leaderboardAward.addChoice(leaderboardChoices[i - 1], i);
        }
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
                log.addOptions(newMention);
                sub.addOptions(newMention);
            }

            award.addOptions(newMention);
            add.addOptions(newMention);
            deny.addOptions(newMention);
            grad.addOptions(newMention);
        }

        // implementing commands
        mit.addSubcommands(status, help,
                draftdoc, qprofile);
        mit.addSubcommandGroups(profile);
        lp.addSubcommands(
                genmaps, leaderboard,
                startdraft, forcesub, forceend,
                log, sub, undo,
                add, deny, grad,
                cycleCalculate, award);
        io.addSubcommands(
                genmaps, leaderboard,
                startdraft, forcesub, forceend,
                log, sub, undo,
                add, deny, grad,
                cycleCalculate, award);

        jda.updateCommands().addCommands(mit, lp, io).queue();
    }

    /**
     * The main entry point of the bot (run this method).
     * @param args input arguments, if any.
     */
    public static void main(String[] args) {
        try {
            System.out.println();
            JDA jda = JDABuilder.createLight(Config.botToken)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_PRESENCES)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .addEventListeners(new Events())
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
