package bot.Engine.Profiles;

import bot.Config;
import bot.Engine.Cycles.PlayerStats;
import bot.Engine.Section;
import bot.Main;
import bot.Tools.Command;
import bot.Tools.FileHandler;
import bot.Tools.GoogleSheetsAPI;

import com.google.api.services.sheets.v4.model.ValueRange;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author  Wil Aquino
 * Date:    May 7, 2022
 * Project: Libra
 * Module:  Profile.java
 * Purpose: Manages a profile database within MIT.
 */
public class Profile implements Command {

    /** Google Sheets ID of the spreadsheet to save to. */
    private static final String spreadsheetID = Config.mitProfilesSheetID;

    /** The spreadsheet's starting column with profile information. */
    private static final String START_COLUMN = "B";

    /** The spreadsheet's ending column with profile information. */
    private static final String END_COLUMN = "I";

    /** The tab name of the spreadsheet. */
    private static final String TAB = "Profiles";

    /**
     * Checks the name of a parameter of a command.
     * @param args the arguments of the command.
     * @param name the name to check for.
     * @return True if the parameter has the correct name.
     *         False otherwise.
     */
    private boolean parameterGood(List<OptionMapping> args, String name) {
        if (args.isEmpty()) {
            return false;
        } else {
            return args.get(0).getName().equals(name);
        }
    }

    /**
     * Retrieves a parameter of a command, if any.
     * @param args the arguments of the command.
     * @param isMember flag for knowing if the parameter is a member or not.
     * @return the parameter, if it exists.
     *         null otherwise.
     */
    private Object getParameter(List<OptionMapping> args, boolean isMember) {
        if (isMember && args.get(0).getType().equals(OptionType.MENTIONABLE)) {
            return args.remove(0).getAsMember().getId();
        } else if (args.get(0).getType().equals(OptionType.BOOLEAN)) {
            return args.remove(0).getAsBoolean();
        } else {
            return args.remove(0).getAsString();
        }
    }

    /**
     * Checks whether a phrase is inappropriate or not.
     * @param phrase the phrase to check.
     * @return True if it is inappropriate.
     *         False otherwise.
     */
    private boolean blacklistedPhrase(String phrase) {
        if (phrase == null) {
            return false;
        }

        FileHandler badWordFile = new FileHandler("badwords.txt");
        phrase = phrase.toLowerCase();

        for (String badRegex : badWordFile.readContents()) {
            Pattern pattern = Pattern.compile(badRegex);
            Matcher matcher = pattern.matcher(phrase);

            if (matcher.find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a phrase is too long or not.
     * @param phrase the phrase to check.
     * @return True if it is too long.
     *         False otherwise.
     */
    private boolean phraseTooLong(String phrase) {
        if (phrase == null) {
            return false;
        }

        return phrase.length() > 35;
    }

    /**
     * Reformats phrase into lines of set length.
     * @param phrase the phrase to format.
     * @param limit the number of items per line.
     *              (0 indicates a reformat based on length/line).
     * @return the formatted pronouns.
     */
    private String reformatPhrase(String phrase, int limit) {
        String[] splitPhrase = phrase.split(", ");
        StringBuilder formattedPhrase = new StringBuilder();

        StringBuilder line = new StringBuilder();
        for (int i = 1; i <= splitPhrase.length; i++) {
            String currentSplit = splitPhrase[i - 1];
            formattedPhrase.append(currentSplit);
            line.append(currentSplit);

            if (limit == 0 && i < splitPhrase.length) {
                String nextSplit = splitPhrase[i];
                formattedPhrase.append(",");
                line.append(",");

                if (line.length() + nextSplit.length() >= 23) {
                    formattedPhrase.append("\n");
                    line = new StringBuilder();
                } else {
                    formattedPhrase.append(" ");
                    line.append(" ");
                }
            } else if (i < splitPhrase.length) {
                formattedPhrase.append(",");
                if (i % limit == 0) {
                    formattedPhrase.append("\n");
                } else {
                    formattedPhrase.append(" ");
                }

            }
        }

        return formattedPhrase.toString();
    }

    /**
     * Retrieves the Profiles database.
     * @param interaction the user interaction calling this method.
     * @param link a link to the Profiles spreadsheet.
     * @return the database.
     *         null otherwise.
     */
    public TreeMap<Object, Object> onlyGetDatabase(
            GenericInteractionCreateEvent interaction,
            GoogleSheetsAPI link) {
        try {
            if (link == null) {
                link = new GoogleSheetsAPI(spreadsheetID);
            }
            return link.readSection(interaction, TAB);
        } catch (IOException | GeneralSecurityException e) {
            log("The profiles spreadsheet could not load.", true);
            return null;
        }
    }

    /**
     * Looks up a player's profile.
     * @param id the player's Discord ID.
     * @param database a map of players' profiles, indexed by Discord ID.
     * @return the player's profile.
     *         null if it could not be found.
     */
    public PlayerInfo lookup(String id, TreeMap<Object, Object> database) {
        return (PlayerInfo) database.get(id);
    }

    /**
     * Quickly registers a new player into the database by
     * loading multiple parameters
     * @param sc the user's inputted command.
     */
    private void quickRegister(SlashCommandEvent sc) {
        sc.deferReply(true).queue();

        List<OptionMapping> args = sc.getOptions();
        String fc = (String) getParameter(args, false);
        String nickname = (String) getParameter(args, false);
        String pronouns = (String) getParameter(args, false);
        String playstyle = (String) getParameter(args, false);
        String weapons = (String) getParameter(args, false);
        String rank = (String) getParameter(args, false);

        String fcPattern = "\\d{4}-\\d{4}-\\d{4}";
        String listPattern =
                "[^\\s][\\w\\s\\/\\-\\.\\']+(?:,\\s[\\w\\']+[\\w\\s\\/\\-\\.\\']*)*";

        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(spreadsheetID);
            TreeMap<Object, Object> database = link.readSection(sc, TAB);
            if (database.containsKey(sc.getMember().getId())) {
                editMessage(sc, "You cannot use `qprofile`, because your "
                                + "profile already exists. Use the other "
                                + "`profile` commands as needed.");
            } else if (!fc.matches(fcPattern)) {
                editMessage(sc,"Friend code should be in the format: "
                                + "`8888-8888-8888`.");
            } else if (phraseTooLong(nickname) || phraseTooLong(pronouns)) {
                editMessage(sc, "Lengthy nickname/pronouns detected. Please "
                                + "use another one or ask Technical Staff "
                                + "about it!");
            } else if (blacklistedPhrase(nickname) || blacklistedPhrase(pronouns)) {
                editMessage(sc,"Inappropriate nickname/pronouns detected. "
                                + "Please use another one or ask Technical "
                                + "Staff about it!");
            } else if (!pronouns.matches(listPattern) && !weapons.matches(listPattern)) {
                editMessage(sc,"Invalid pronouns/weapons format detected. "
                                + "Please **strictly** use the following "
                                + "format: `option 1, option 2, option 3, "
                                + "...` (note the `, `).");
            } else {
                Member user = sc.getMember();
                String discordTag = user.getUser().getAsTag();

                ValueRange newRow = link.buildRow(Arrays.asList(
                        user.getId(), discordTag, nickname,
                        fc, reformatPhrase(pronouns.toLowerCase(), 1),
                        playstyle, reformatPhrase(weapons, 0), rank, "N/A"));
                link.appendRow(TAB, newRow);

                editMessage(sc, "Your MIT profile has been created! "
                        + "Use `/mit profile view` to view your profile.");
                log("Quick profile created for " + discordTag + ".", false);
            }
        } catch (IOException | GeneralSecurityException e) {
            editMessage(sc, "The profiles database could not load.");
            log("The profiles spreadsheet could not load.", true);
        }
    }

    /**
     * Registers a new player into the database or changes
     * their friend code.
     * @param sc the user's inputted command.
     * @param fc the player's Nintendo Switch friend code.
     */
    private void register(SlashCommandEvent sc, String fc) {
        sc.deferReply(true).queue();

        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(spreadsheetID);
            TreeMap<Object, Object> database = link.readSection(sc, TAB);

            Member user = sc.getMember();
            if (!fc.matches("\\d{4}-\\d{4}-\\d{4}")) {
                editMessage(sc,
                        "Friend code should be in the format: "
                                + "`8888-8888-8888`.");
            } else if (database.containsKey(user.getId())) {
                PlayerInfo profile = lookup(user.getId(), database);

                String updateRange = link.buildRange(TAB,
                        START_COLUMN, profile.getSpreadsheetPosition(),
                        END_COLUMN, profile.getSpreadsheetPosition());
                ValueRange newRow = link.buildRow(Arrays.asList(
                        profile.getAsTag(), profile.getNickname(),
                        fc, profile.getPlaystyle(),
                        profile.getWeaponPool(), profile.getRank(),
                        profile.getTeam()));
                link.updateRange(updateRange, newRow);

                editMessage(sc,
                        "Friend code updated to `" + "`.");
                log("Profile FC updated for " + profile.getAsTag() + ".", false);
            } else {
                String discordTag = user.getUser().getAsTag();

                ValueRange newRow = link.buildRow(Arrays.asList(
                        user.getId(), discordTag, user.getEffectiveName(),
                        fc, "Unset",
                        "Unset", "Unset", "Unset", "N/A"));
                link.appendRow(TAB, newRow);

                editMessage(sc, "Your MIT profile has been created! "
                        + "Use `/mit profile view` to view your profile.");
                log("Profile created for " + discordTag + ".", false);
            }
        } catch (IOException | GeneralSecurityException e) {
            editMessage(sc, "The profiles database could not load.");
            log("The profiles spreadsheet could not load.", true);
        }
    }

    /**
     * Retrieves only the friend code of a player.
     * @param sc the user's inputted command.
     * @param id a player's Discord ID.
     */
    private void onlyGetFC(SlashCommandEvent sc, String id) {
        sc.deferReply(false).queue();

        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(spreadsheetID);
            TreeMap<Object, Object> database = link.readSection(sc, TAB);
            String pronoun = "Their";

            if (id == null) {
                id = sc.getMember().getId();
                pronoun = "Your";
            }

            if (database.containsKey(id)) {
                PlayerInfo profile = lookup(id, database);

                editMessage(sc, pronoun + " friend code is `SW-"
                        + profile.getFC() + "`.");
                log("Profile FC retrieved for "
                        + profile.getAsTag() + ".", false);
            } else {
                editMessage(sc, "Your MIT profile does not exist yet. "
                        + "Register with `/mit qprofile ...` or "
                        + "`/mit profile fc` to proceed.");
            }
        } catch (IOException | GeneralSecurityException e) {
            editMessage(sc, "The profiles database could not load.");
            log("The profiles spreadsheet could not load.", true);
        }
    }

    /**
     * Retrieves a section's spreadsheet ID to reference, if any.
     * @param interaction the user interaction calling this method.
     * @param eb the pre-built embed to reference.
     * @param roles a player's roles.
     */
    private String getSpreadsheetID(GenericInteractionCreateEvent interaction,
                              EmbedBuilder eb, List<Role> roles) {
        if (roles.contains(getRole(interaction, "Freshwater Shoals"))) {
            eb.setColor(Main.freshwatershoalsColor);
            return null;
        } else if (roles.contains(getRole(interaction, "LaunchPoint"))) {
            eb.setColor(Main.launchpointColor);
            return Config.lpCyclesSheetID;
        } else if (roles.contains(getRole(interaction, "Ink Odyssey"))) {
            eb.setColor(Main.inkodysseyColor);
            return Config.ioCyclesSheetID;
        } else if (roles.contains(getRole(interaction, "Ink Odyssey Graduate"))) {
            eb.setColor(Main.inkodysseygraduateColor);
            return null;
        } else {
            eb.setColor(Main.mitColor);
            return null;
        }
    }

    /**
     * Retrieves the win-loss score of a player within their
     * draft section, if it exists.
     * @param interaction the user interaction calling this method.
     * @param id the player's Discord ID.
     * @param leaderboardID the leaderboard spreadsheet ID to reference.
     */
    private String getScore(GenericInteractionCreateEvent interaction,
                            String id, String leaderboardID) {
        if (leaderboardID == null) {
            return "N/A";
        }

        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(leaderboardID);
            TreeMap<Object, Object> leaderboard =
                    link.readSection(interaction, Section.CYCLES_TAB);

            if (leaderboard.containsKey(id)) {
                PlayerStats stats = (PlayerStats) leaderboard.get(id);
                return String.format(
                        "%s-%s", stats.getSetWins(), stats.getSetLosses());
            } else {
                return "0-0";
            }
        } catch (IOException | GeneralSecurityException e) {
            log("The leaderboard spreadsheet could not load.", true);
            return "N/A";
        }
    }

    /**
     * Builds a player's profile into a nice summary.
     * @param interaction the user interaction calling this method.
     * @param pronoun the player's neutral pronoun.
     * @param id the player's Discord ID.
     * @param profile the player's profile.
     * @param fullDisplay a flag for knowing whether to display
     *                    the entire profile or not.
     * @param showInfo a flag for checking whether to show a player's
     *                 additional info or not.
     * @param shouldPrint a flag for checking whether the profile
     *                   should be printed immediately or not.
     * @return the pre-built summary.
     */
    private EmbedBuilder buildProfile(GenericInteractionCreateEvent interaction,
                                      String pronoun, String id, PlayerInfo profile,
                                      boolean fullDisplay, boolean showInfo,
                                      boolean shouldPrint) {
        EmbedBuilder eb = new EmbedBuilder();
        Member player = findMember(interaction, id);

        String leaderboardID = getSpreadsheetID(
                interaction, eb, player.getRoles());
        if (profile == null) {
            eb.setTitle(player.getEffectiveName()
                    + " [" + player.getUser().getAsTag() + "]");
            eb.setDescription(pronoun + " MIT profile does not exist. "
                    + "Register with\n`/mit qprofile ...` "
                    + "or `/mit profile fc` to proceed.");
        } else {
            eb.setTitle(profile.getNickname());
            eb.setThumbnail(player.getEffectiveAvatarUrl());
            if (showInfo) {
                eb.setTitle(profile.getNickname() + " [" + profile.getAsTag() + "]");
                eb.setFooter("FC SW-" + profile.getFC(),
                        "https://images.squarespace-cdn.com/content/v1/5ce2bf96d2bf17000192fe2c/1596048502214-WL5LU68IOLM8WILBA57N/Friends+Icon.png?format=1000w");
            }

            eb.addField("Pronouns", profile.getPronouns(), true);
            eb.addField("Playstyle", "`" + profile.getPlaystyle() + "`", true);
            eb.addField("Weapon Pool", "`" + profile.getWeaponPool() + "`", true);
            if (fullDisplay) {
                eb.addField("Score",
                        getScore(interaction, id, leaderboardID), true);
                eb.addField("Team", profile.getTeam(), true);
                eb.addField("Rank", profile.getRank(), true);
            }
        }

        if (shouldPrint) {
            sendEmbed(interaction, eb);
        }

        return eb;
    }

    /**
     * Views players' profiles.
     * @param interaction the user interaction calling this method.
     * @param ids the players' Discord IDs.
     * @param pronoun the player's neutral pronoun.
     * @param fullDisplay a flag for knowing whether to display
     *                    the entire profile or not.
     * @param showInfo flag for checking whether to show a player's
     *                 additional info or not.
     * @return a built summary of the players' profiles.
     */
    public List<MessageEmbed> viewMultiple(GenericInteractionCreateEvent interaction,
                                           Iterable<String> ids, String pronoun,
                                           boolean fullDisplay, boolean showInfo,
                                           boolean shouldPrint) {
        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(spreadsheetID);
            TreeMap<Object, Object> database = link.readSection(interaction, TAB);
            List<MessageEmbed> profiles = new ArrayList<>();
            if (pronoun == null) {
                pronoun = "Their";
            }

            for (String id : ids) {
                PlayerInfo profile = lookup(id, database);

                profiles.add(buildProfile(interaction, pronoun, id,
                        profile, fullDisplay, showInfo, shouldPrint).build());
            }

            return profiles;
        } catch (IOException | GeneralSecurityException e) {
            log("The profiles spreadsheet could not load.", true);
        }

        return null;
    }

    /**
     * Views a player's profile.
     * @param interaction the user interaction calling this method.
     * @param id a player's Discord ID.
     * @param fullDisplay a flag for knowing whether to display
     *                    the entire profile or not.
     * @param showInfo flag for checking whether to show a player's
     *                 additional info or not.
     * @param shouldPrint a flag for checking whether the profile
     *                    should be printed immediately or not.
     * @return a built summary of the player's profile.
     */
    public MessageEmbed view(GenericInteractionCreateEvent interaction,
                             String id, boolean fullDisplay,
                             boolean showInfo, boolean shouldPrint) {
        if (id == null) {
            id = interaction.getMember().getId();
            List<MessageEmbed> profiles = viewMultiple(interaction,
                    Collections.singleton(id), "Your",
                    fullDisplay, showInfo, shouldPrint);

            if (profiles != null) {
                return profiles.remove(0);
            }
        } else {
            List<MessageEmbed> profiles = viewMultiple(interaction,
                    Collections.singleton(id), "Their",
                    fullDisplay, showInfo, shouldPrint);

            if (profiles != null) {
                return profiles.remove(0);
            }
        }

        return null;
    }

    /**
     * Rebuilds a row within the profile database.
     * @param profile a player's profile.
     * @param newNickname the nickname to change to, if any.
     * @param newPronouns the pronouns to change to, if any.
     * @param newPlaystyle the playstyle to change to, if any.
     * @param newWeapons the weapon pool to change to, if any.
     * @param newRank the rank to change to, if any.
     * @param newTeam the team to change to, if any.
     * @return the built row, with the actual change in the first slot.
     */
    private List<Object> withNewInfo(PlayerInfo profile, String newNickname,
                                     String newPronouns, String newPlaystyle,
                                     String newWeapons, String newRank,
                                     String newTeam) {
        String foundNewInfo = null;

        String nickname = profile.getNickname();
        if (newNickname != null) {
            nickname = foundNewInfo = newNickname;
        }

        String pronouns = profile.getPronouns();
        if (newPronouns != null) {
            pronouns = foundNewInfo = reformatPhrase(newPronouns, 1);
        }

        String playstyle = profile.getPlaystyle();
        if (newPlaystyle != null) {
            playstyle = foundNewInfo = newPlaystyle;
        }

        String weapons = profile.getWeaponPool();
        if (newWeapons != null) {
            foundNewInfo = reformatPhrase(newWeapons, 0);
            weapons = foundNewInfo;
            if (foundNewInfo.charAt(0) == '\'') {
                weapons = "'" + foundNewInfo;
            }
        }

        String rank = profile.getRank();
        if (newRank != null) {
            rank = foundNewInfo = newRank;
        }

        String team = profile.getTeam();
        if (newTeam != null) {
            team = foundNewInfo = newTeam;
        }

        return new ArrayList<>(Arrays.asList(foundNewInfo,
                profile.getAsTag(), nickname, profile.getFC(),
                pronouns.toLowerCase(), playstyle, weapons, rank, team));
    }

    /**
     * Updates a single field within a player's profile.
     * @param sc the user's inputted command.
     * @param nickname the new nickname to change to, if any.
     * @param pronouns the new pronouns to change to, if any.
     * @param playstyle the new playstyle to change to, if any.
     * @param weapons the new weapon pool to change to, if any.
     * @param rank the new rank to change to, if any.
     * @param team the new team to change to, if any.
     */
    private void setField(SlashCommandEvent sc,
                          String nickname, String pronouns, String playstyle,
                          String weapons, String rank, String team) {
        sc.deferReply(true).queue();

        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(spreadsheetID);
            TreeMap<Object, Object> database = link.readSection(sc, TAB);
            String listPattern =
                    "[^\\s][\\w\\s\\/\\-\\.\\']+(?:,\\s[\\w\\']+[\\w\\s\\/\\-\\.\\']*)*";

            if (phraseTooLong(nickname) || phraseTooLong(pronouns)
                    || phraseTooLong(team)) {
                editMessage(sc,
                        "Lengthy team name detected. Please use another one "
                                + "or ask Technical Staff about it!");
            } else if (blacklistedPhrase(nickname) || blacklistedPhrase(pronouns)
                || blacklistedPhrase(team) || blacklistedPhrase(weapons)) {
                editMessage(sc,
                        "Inappropriate input detected. Please use another one "
                                + "or ask Technical Staff about it!");
            } else if ((pronouns != null && !pronouns.matches(listPattern))
                    || (weapons != null && !weapons.matches(listPattern))) {
                editMessage(sc,
                        "Invalid pronouns/weapons format detected. Please "
                                + "**strictly** use the following format: "
                                + "`option 1, option 2, option 3, ...` (note "
                                + "the `, `).");
            } else if (database.containsKey(sc.getMember().getId())) {
                PlayerInfo profile = lookup(sc.getMember().getId(), database);

                String updateRange = link.buildRange(TAB,
                        START_COLUMN, profile.getSpreadsheetPosition(),
                        END_COLUMN, profile.getSpreadsheetPosition());
                List<Object> updatedRow = withNewInfo(profile, nickname,
                        pronouns, playstyle, weapons, rank, team);
                String changedField = (String) updatedRow.remove(0);

                ValueRange newRow = link.buildRow(updatedRow);
                link.updateRange(updateRange, newRow);

                String cmd = sc.getSubcommandName();
                editMessage(sc, "Your " + cmd + " has been updated to `"
                        + changedField.replaceAll("\n", " ") + "`.");
                log(sc.getUser().getAsTag() + "'s " + cmd + " was updated.", false);
            } else {
                editMessage(sc, "Your MIT profile does not exist yet. "
                        + "Register with `/mit qprofile ...` or "
                        + "`/mit profile fc` to proceed.");
            }
        } catch (IOException | GeneralSecurityException e) {
            editMessage(sc, "The profiles database could not load.");
            log("The profiles spreadsheet could not load.", true);
        }
    }

    /**
     * Deletes the player's profile.
     * @param sc the user's inputted command.
     */
    private void delete(SlashCommandEvent sc) {
        sc.deferReply(true).queue();

        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(spreadsheetID);
            TreeMap<Object, Object> database = link.readSection(sc, TAB);

            String userID = sc.getMember().getId();
            if (database.containsKey(userID)) {
                PlayerInfo profile = lookup(userID, database);
                link.deleteRow(TAB, profile.getSpreadsheetPosition());

                editMessage(sc, "Your MIT profile has been deleted.");
                log("Profile deleted for " + sc.getUser().getAsTag()
                        + ".", false);
            } else {
                editMessage(sc, "Your MIT profile does not exist.");
            }
        } catch (IOException | GeneralSecurityException e) {
            editMessage(sc, "The profiles database could not load.");
            log("The profiles spreadsheet could not load.", true);
        }
    }

    /**
     * Runs the profile command.
     * @param sc the user's inputted command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        String subCmd = sc.getSubcommandName();
        List<OptionMapping> args = sc.getOptions();

        switch (subCmd) {
            case "qprofile":
                quickRegister(sc);
                break;
            case "fc":
                register(sc, (String) getParameter(args, false));
                break;
            case "getfc":
                if (parameterGood(args, "player")) {
                    onlyGetFC(sc, (String) getParameter(args, true));
                } else {
                    onlyGetFC(sc, null);
                }
                break;
            case "view":
                sc.deferReply(false).queue();
                String id = null;
                if (parameterGood(args, "player")) {
                    id = (String) getParameter(args, true);
                }

                boolean fullDisplay = true;
                if (parameterGood(args, "fullview")) {
                    fullDisplay = (Boolean) getParameter(args, false);
                }

                boolean showInfo = true;
                if (parameterGood(args, "includeinfo")) {
                    showInfo = (Boolean) getParameter(args, false);
                }

                view(sc, id, fullDisplay, showInfo, true);
                break;
            case "nickname":
                setField(sc, (String) getParameter(args, false), null,
                        null, null, null, null);
                break;
            case "pronouns":
                setField(sc, null, (String) getParameter(args, false),
                        null, null, null, null);
                break;
            case "playstyle":
                setField(sc, null, null,
                        (String) getParameter(args, false), null, null, null);
                break;
            case "weapons":
                setField(sc, null, null,
                        null, (String) getParameter(args, false), null, null);
                break;
            case "rank":
                setField(sc, null, null,
                        null, null, (String) getParameter(args, false), null);
                break;

            case "team":
                setField(sc, null, null,
                        null, null, null, (String) getParameter(args, false));
                break;
            case "delete":
                delete(sc);
                break;
        }
    }
}
