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
     * Retrieves the parameter of a command, if any.
     * @param args the arguments of the command.
     * @param isMember flag for knowing if the parameter is a member or not.
     * @return the parameter, if it exists.
     *         null otherwise.
     */
    private String getParameter(List<OptionMapping> args, boolean isMember) {
        if (args.isEmpty()
                || args.get(0).getType().equals(OptionType.BOOLEAN)) {
            return null;
        } else if (isMember
                && args.get(0).getType().equals(OptionType.MENTIONABLE)) {
            return args.remove(0).getAsMember().getId();
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
     * @return the formatted pronouns.
     */
    private String reformatPhrase(String phrase, int limit) {
        String[] splitPhrase = phrase.split(", ");
        StringBuilder formattedPhrase = new StringBuilder();

        for (int i = 1; i <= splitPhrase.length; i++) {
            formattedPhrase.append(splitPhrase[i - 1]);

            if (i < splitPhrase.length) {
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
     * Quickly registers a new player into the database by
     * loading multiple parameters
     * @param sc the user's inputted command.
     */
    private void quickRegister(SlashCommandEvent sc) {
        sc.deferReply(true).queue();

        List<OptionMapping> args = sc.getOptions();
        String fc = getParameter(args, false);
        String nickname = getParameter(args, false);
        String pronouns = getParameter(args, false);
        String playstyle = getParameter(args, false);
        String weapons = getParameter(args, false);
        String rank = getParameter(args, false);

        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(spreadsheetID);
            TreeMap<Object, Object> database = link.readSection(sc, TAB);
            if (database == null) {
                sendResponse(sc, "The profiles database could not load.", false);
            } else if (database.containsKey(sc.getMember().getId())) {
                sendResponse(sc,
                        "You cannot use `qprofile`, because your profile "
                                + "already exists. Use the other `profile` "
                                + "commands as needed.", true);
            } else if (!fc.matches("\\d{4}-\\d{4}-\\d{4}")) {
                sendResponse(sc,
                        "Friend code should be in the format: "
                                + "`8888-8888-8888`", true);
            } else if (phraseTooLong(nickname) || phraseTooLong(pronouns)) {
                sendResponse(sc,
                        "Lengthy nickname/pronouns detected. Please use "
                                + "another one or ask Technical Staff "
                                + "about it!", true);
            } else if (blacklistedPhrase(nickname) || blacklistedPhrase(pronouns)) {
                sendResponse(sc,
                        "Inappropriate nickname/pronouns detected. Please use "
                                + "another one or ask Technical Staff "
                                + "about it!", true);
            } else {
                Member user = sc.getMember();
                String discordTag = user.getUser().getAsTag();

                ValueRange newRow = link.buildRow(Arrays.asList(
                        user.getId(), discordTag, nickname,
                        fc, reformatPhrase(pronouns.toLowerCase(), 1),
                        playstyle, reformatPhrase(weapons, 3), rank, "N/A"));
                link.appendRow(TAB, newRow);

                sendResponse(sc, "Your MIT profile has been created! "
                        + "Use `/mit profile view` to view your profile.", true);
                log("Quick profile created for " + discordTag + ".", false);
            }
        } catch (IOException | GeneralSecurityException e) {
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
            if (database == null) {
                sendResponse(sc, "The profiles database could not load.", false);
                return;
            }

            Member user = sc.getMember();
            if (!fc.matches("\\d{4}-\\d{4}-\\d{4}")) {
                sendResponse(sc,
                        "Friend code should be in the format: "
                                + "`8888-8888-8888`", false);
            } else if (database.containsKey(user.getId())) {
                PlayerInfo profile = (PlayerInfo) database.get(user.getId());

                String updateRange = link.buildRange(TAB,
                        START_COLUMN, profile.getSpreadsheetPosition(),
                        END_COLUMN, profile.getSpreadsheetPosition());
                ValueRange newRow = link.buildRow(Arrays.asList(
                        profile.getAsTag(), profile.getNickname(),
                        fc, profile.getPlaystyle(),
                        profile.getWeaponPool(), profile.getRank(),
                        profile.getTeam()));
                link.updateRange(updateRange, newRow);

                sendResponse(sc,
                        "Friend code updated!", false);
                log("Profile FC updated for " + profile.getAsTag() + ".", false);
            } else {
                String discordTag = user.getUser().getAsTag();

                ValueRange newRow = link.buildRow(Arrays.asList(
                        user.getId(), discordTag, user.getEffectiveName(),
                        fc, "Unset",
                        "Unset", "Unset", "Unset", "N/A"));
                link.appendRow(TAB, newRow);

                sendResponse(sc, "Your MIT profile has been created! "
                        + "Use `/mit profile view` to view your profile.", false);
                log("Profile created for " + discordTag + ".", false);
            }
        } catch (IOException | GeneralSecurityException e) {
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

            if (database == null) {
                sendResponse(sc, "The profiles database could not load.", false);
                return;
            } else if (id == null) {
                id = sc.getMember().getId();
                pronoun = "Your";
            }

            if (database.containsKey(id)) {
                PlayerInfo profile = (PlayerInfo) database.get(id);

                sendResponse(sc, pronoun + " friend code is `SW-"
                        + profile.getFC() + "`.", false);
                log("Profile FC retrieved for "
                        + profile.getAsTag() + ".", false);
            } else {
                sendResponse(sc, pronoun + " MIT profile does not exist yet. "
                        + "Register with\n`/mit qprofile ...` "
                        + "or `/mit profile fc` to proceed.", true);
            }
        } catch (IOException | GeneralSecurityException e) {
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
        if (roles.contains(getRole(interaction, "LaunchPoint"))) {
            eb.setColor(Main.launchpointColor);
            return Config.lpCyclesSheetID;
        } else if (roles.contains(getRole(interaction, "Ink Odyssey"))) {
            eb.setColor(Main.inkodysseyColor);
            return Config.ioCyclesSheetID;
        } else {
            eb.setColor(Main.mitColor);
            return null;
        }
    }

    /**
     * Checks whether to display a player's friend code in
     * their profile.
     * @param args the arguments of the command.
     * @return True to display.
     *         False otherwise.
     */
    private boolean displayFC(List<OptionMapping> args) {
        if (args.isEmpty()) {
            return true;
        } else {
            return args.remove(0).getAsBoolean();
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

            if (leaderboard == null) {
                throw new IOException("The spreadsheet was empty.");
            } else if (leaderboard.containsKey(id)) {
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
     * @param showFC flag for checking whether to show a player's
     *               friend code or not.
     * @return the pre-built summary.
     */
    private EmbedBuilder buildProfile(GenericInteractionCreateEvent interaction,
                                      String pronoun, String id, PlayerInfo profile,
                                      boolean fullDisplay, boolean showFC) {
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
            eb.setTitle(profile.getNickname() + " [" + profile.getAsTag() + "]");
            eb.setThumbnail(player.getEffectiveAvatarUrl());
            if (showFC) {
                eb.setFooter("FC SW-" + profile.getFC(),
                        "https://images.squarespace-cdn.com/content/v1/5ce2bf96d2bf17000192fe2c/1596048502214-WL5LU68IOLM8WILBA57N/Friends+Icon.png?format=1000w");
            }

            eb.addField("Pronouns", profile.getPronouns(), true);
            eb.addField("Playstyle", "`" + profile.getPlaystyle() + "`", true);
            eb.addField("Weapon Pool", "`" + profile.getWeaponPool() + "`", true);
        }

        if (fullDisplay) {
            if (profile != null) {
                eb.addField("Score",
                        getScore(interaction, id, leaderboardID), true);
                eb.addField("Team", profile.getTeam(), true);
                eb.addField("Rank", profile.getRank(), true);
            }

            sendEmbed(interaction, eb);
        }

        return eb;
    }

    /**
     * Views a player's profile.
     * @param interaction the user interaction calling this method.
     * @param id a player's Discord ID.
     * @param fullDisplay a flag for knowing whether to display
     *                    the entire profile or not.
     * @param showFC flag for checking whether to show a player's
     *               friend code or not.
     * @return a built summary of the player's profile.
     */
    public MessageEmbed view(GenericInteractionCreateEvent interaction,
                             String id, boolean fullDisplay, boolean showFC) {
        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(spreadsheetID);
            TreeMap<Object, Object> database = link.readSection(interaction, TAB);

            if (database == null) {
                sendResponse(interaction, "The profiles database could not load.", false);
            } else if (id == null) {
                String userID = interaction.getMember().getId();
                PlayerInfo profile = (PlayerInfo) database.get(userID);
                String pronoun = "Your";

                return buildProfile(interaction, pronoun,
                        userID, profile, fullDisplay, showFC).build();
            } else {
                PlayerInfo profile = (PlayerInfo) database.get(id);
                String pronoun = "Their";

                return buildProfile(interaction, pronoun,
                        id, profile, fullDisplay, showFC).build();
            }
        } catch (IOException | GeneralSecurityException e) {
            log("The profiles spreadsheet could not load.", true);
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
            weapons = foundNewInfo = reformatPhrase(newWeapons, 3);
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
            if (database == null) {
                sendResponse(sc, "The profiles database could not load.", false);
                throw new IOException("The database could not load.");
            } else if (phraseTooLong(nickname) || phraseTooLong(pronouns)
                    || phraseTooLong(team)) {
                sendResponse(sc,
                        "Lengthy team name detected. Please use another one "
                                + "or ask Technical Staff about it!", true);
            } else if (blacklistedPhrase(nickname) || blacklistedPhrase(pronouns)
                || blacklistedPhrase(team) || blacklistedPhrase(weapons)) {
                sendResponse(sc,
                        "Inappropriate input detected. Please use another one "
                                + "or ask Technical Staff about it!", true);
            } else if (database.containsKey(sc.getMember().getId())) {
                PlayerInfo profile = (PlayerInfo) database.get(
                        sc.getMember().getId());

                String updateRange = link.buildRange(TAB,
                        START_COLUMN, profile.getSpreadsheetPosition(),
                        END_COLUMN, profile.getSpreadsheetPosition());
                List<Object> updatedRow = withNewInfo(profile, nickname,
                        pronouns, playstyle, weapons, rank, team);
                String changedField = (String) updatedRow.remove(0);

                ValueRange newRow = link.buildRow(updatedRow);
                link.updateRange(updateRange, newRow);

                String cmd = sc.getSubcommandName();
                sendResponse(sc, "Your " + cmd + " has been updated to `"
                        + changedField.replaceAll("\n", " ") + "`.", false);
                log(sc.getUser().getAsTag() + "'s " + cmd + " was updated.", false);
            } else {
                sendResponse(sc, "Your MIT profile does not exist yet. "
                        + "Register with\n`/mit qprofile ...` "
                        + "or `/mit profile fc` to proceed.", true);
            }
        } catch (IOException | GeneralSecurityException e) {
            log("The profiles spreadsheet could not load.", true);
        }
    }

    /**
     * Deletes the player's profile.
     * @param sc the user's inputted command.
     */
    private void delete(SlashCommandEvent sc) {
        sc.deferReply(false).queue();

        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(spreadsheetID);

            TreeMap<Object, Object> database = link.readSection(sc, TAB);
            if (database == null) {
                sendResponse(sc, "The profiles database could not load.", false);
            }

            String userID = sc.getMember().getId();
            if (database.containsKey(userID)) {
                PlayerInfo profile = (PlayerInfo) database.get(userID);
                link.deleteRow(TAB, profile.getSpreadsheetPosition());

                sendResponse(sc, "Your MIT profile has been deleted.", false);
                log("Profile deleted for " + sc.getUser().getAsTag()
                        + ".", false);
            } else {
                sendResponse(sc, "Your MIT profile does not exist.", true);
            }
        } catch (IOException | GeneralSecurityException e) {
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
                register(sc, getParameter(args, false));
                break;
            case "getfc":
                onlyGetFC(sc, getParameter(args, true));
                break;
            case "view":
                sc.deferReply(false).queue();
                view(sc, getParameter(args, true), true, displayFC(args));
                break;
            case "nickname":
                setField(sc, getParameter(args, false), null,
                        null, null, null, null);
                break;
            case "pronouns":
                setField(sc, null, getParameter(args, false),
                        null, null, null, null);
                break;
            case "playstyle":
                setField(sc, null, null,
                        getParameter(args, false), null, null, null);
                break;
            case "weapons":
                setField(sc, null, null,
                        null, getParameter(args, false), null, null);
                break;
            case "rank":
                setField(sc, null, null,
                        null, null, getParameter(args, false), null);
                break;

            case "team":
                setField(sc, null, null,
                        null, null, null, getParameter(args, false));
                break;
            case "delete":
                delete(sc);
                break;
        }
    }
}
