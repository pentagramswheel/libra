package bot.Engine.Profiles;

import bot.Config;
import bot.Engine.Cycles.PlayerStats;
import bot.Engine.Section;
import bot.Tools.Command;
import bot.Tools.GoogleSheetsAPI;

import com.google.api.services.sheets.v4.model.ValueRange;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.Color;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * @author  Wil Aquino
 * Date:    May 7, 2022
 * Project: Libra
 * Module:  Profile.java
 * Purpose: Manages a profile database within MIT.
 */
public class Profile implements Command {

    /** Google Sheets ID of the spreadsheet to save to. */
    private final String spreadsheetID;

    /** The spreadsheet's starting column with profile information. */
    private static final String START_COLUMN = "B";

    /** The spreadsheet's ending column with profile information. */
    private static final String END_COLUMN = "H";

    /** The tab name of the spreadsheet. */
    private static final String TAB = "Profiles";

    /**
     * Sets the spreadsheet ID.
     */
    public Profile() {
        spreadsheetID = Config.mitProfilesSheetID;
    }

    /**
     * Retrieves the parameter of a command, if any.
     * @param args the arguments of the command.
     * @param isMember flag for knowing if the parameter is a member or not.
     * @return the parameter, if it exists.
     *         null otherwise.
     */
    private String getParameter(List<OptionMapping> args, boolean isMember) {
        if (args.isEmpty()) {
            return null;
        } else if (isMember) {
            return args.remove(0).getAsMember().getId();
        } else {
            return args.remove(0).getAsString();
        }
    }

    /**
     * Checks whether a name is inappropriate or not.
     * @param name the name to check.
     * @return True if it is inappropriate.
     *         False otherwise.
     */
    private boolean blacklistedName(String name) {
        name = name.toLowerCase();
        String[] bannedWords = {"shit", "fuck", "fuk", "fk", "fck", "bitch", "whore",
                "nigga", "chigga", "ching", "chong", "chink", "cholo",
                "cracker", "nigger"};

        for (String word : bannedWords) {
            if (name.equals(word)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Reformats weapon pool entry into lines of set length.
     * @param weapons the pool to format.
     * @return the formatted pool.
     */
    private String reformatWeapons(String weapons) {
        String[] splitPool = weapons.split(", ");
        StringBuilder formattedPool = new StringBuilder();

        for (int i = 1; i <= splitPool.length; i++) {
            formattedPool.append(splitPool[i - 1]);

            if (i < splitPool.length) {
                formattedPool.append(",");
                if (i % 3 == 0) {
                    formattedPool.append("\n");
                } else {
                    formattedPool.append(" ");
                }
            }
        }

        return formattedPool.toString();
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
        String playstyle = getParameter(args, false);
        String weapons = getParameter(args, false);
        String rank = getParameter(args, false);

        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(spreadsheetID);
            TreeMap<Object, Object> database = link.readSection(sc, TAB);
            if (database == null) {
                sendResponse(sc, "The profiles database could not load.", false);
                return;
            }

            Member user = sc.getMember();
            if (database.containsKey(user.getId())) {
                sendResponse(sc,
                        "You cannot use `qprofile`, because your profile "
                                + "already exists. Use the other `profile` "
                                + "commands as needed.", true);
            } else if (!fc.matches("\\d{4}-\\d{4}-\\d{4}")) {
                sendResponse(sc,
                        "Friend code should be in the format: "
                                + "`8888-8888-8888`", true);
            } else if (blacklistedName(nickname)) {
                sendResponse(sc,
                        "Inappropriate nickname detected. "
                        + "Please use another one.", true);
            } else {
                String discordTag = user.getUser().getAsTag();

                ValueRange newRow = link.buildRow(Arrays.asList(
                        user.getId(), discordTag, nickname,
                        fc, playstyle, reformatWeapons(weapons), rank, "N/A"));
                link.appendRow(TAB, newRow);

                sendResponse(sc, "Your MIT profile has been created! "
                        + "Use `/mit profile view` to view your profile.", true);
                log("Profile created for " + discordTag + ".", false);
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
                        "Unset", "Unset",
                        "N/A"));
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
                        + "Register with `/mit profile fc` "
                        + "or `/mit qprofile ...` to proceed.", true);
            }
        } catch (IOException | GeneralSecurityException e) {
            log("The profiles spreadsheet could not load.", true);
        }
    }

    /**
     * Retrieves the win-loss score of a player within their
     * draft section, if it exists.
     * @param interaction the user interaction calling this method.
     * @param id the player's Discord ID.
     * @param roles the player's server roles.
     */
    private String getScore(GenericInteractionCreateEvent interaction,
                            String id, List<Role> roles) {
        String leaderboardID;
        if (roles.contains(getRole(interaction, "LaunchPoint"))) {
            leaderboardID = Config.lpCyclesSheetID;
        } else if (roles.contains(getRole(interaction, "Ink Odyssey"))) {
            leaderboardID = Config.ioCyclesSheetID;
        } else {
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
     * @return the pre-built summary.
     */
    private EmbedBuilder buildProfile(GenericInteractionCreateEvent interaction,
                                      String pronoun, String id, PlayerInfo profile,
                                      boolean fullDisplay) {
        EmbedBuilder eb = new EmbedBuilder();
        Member player = findMember(interaction, id);

        eb.setColor(new Color(17, 157, 247));
        if (profile == null) {
            eb.setTitle(player.getEffectiveName()
                    + " [" + player.getUser().getAsTag() + "]");
            eb.setDescription(pronoun + " MIT profile does not exist. "
                    + "Register with `/mit profile fc`\n"
                    + "or `/mit qprofile ...` to proceed.");
        } else {
            eb.setTitle(profile.getNickname() + " [" + profile.getAsTag() + "]");
            eb.setThumbnail(player.getEffectiveAvatarUrl());
            eb.setFooter("FC SW-" + profile.getFC(),
                    "https://images.squarespace-cdn.com/content/v1/5ce2bf96d2bf17000192fe2c/1596048502214-WL5LU68IOLM8WILBA57N/Friends+Icon.png?format=1000w");

            eb.addField("Playstyle", "`" + profile.getPlaystyle() + "`", true);
            eb.addField("Weapon Pool", "`" + profile.getWeaponPool() + "`", true);
        }

        if (fullDisplay) {
            if (profile != null) {
                eb.addField("Score",
                        getScore(interaction, id, player.getRoles()), true);
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
     * @return a built summary of the player's profile.
     */
    public MessageEmbed view(GenericInteractionCreateEvent interaction,
                             String id, boolean fullDisplay) {
        if (!interaction.isAcknowledged()) {
            interaction.deferReply(false).queue();
        }

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
                        userID, profile, fullDisplay).build();
            } else {
                PlayerInfo profile = (PlayerInfo) database.get(id);
                String pronoun = "Their";

                return buildProfile(interaction, pronoun,
                        id, profile, fullDisplay).build();
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
     * @param newPlaystyle the playstyle to change to, if any.
     * @param newWeapons the weapon pool to change to, if any.
     * @param newRank the rank to change to, if any.
     * @param newTeam the team to change to, if any.
     * @return the built row, with the actual change in the first slot.
     */
    private List<Object> withNewInfo(PlayerInfo profile, String newNickname,
                                     String newPlaystyle, String newWeapons,
                                     String newRank, String newTeam) {
        String foundNewInfo = null;

        String nickname = profile.getNickname();
        if (newNickname != null) {
            nickname = foundNewInfo = newNickname;
        }

        String playstyle = profile.getPlaystyle();
        if (newPlaystyle != null) {
            playstyle = foundNewInfo = newPlaystyle;
        }

        String weapons = profile.getWeaponPool();
        if (newWeapons != null) {
            weapons = foundNewInfo = reformatWeapons(newWeapons);
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
                playstyle, weapons, rank, team));
    }

    /**
     * Updates a single field within a player's profile.
     * @param sc the user's inputted command.
     * @param nickname the new nickname to change to, if any.
     * @param playstyle the new playstyle to change to, if any.
     * @param weapons the new weapon pool to change to, if any.
     * @param rank the new rank to change to, if any.
     * @param team the new team to change to, if any.
     */
    private void setField(SlashCommandEvent sc,
                         String nickname, String playstyle, String weapons,
                         String rank, String team) {
        sc.deferReply(true).queue();

        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(spreadsheetID);

            TreeMap<Object, Object> database = link.readSection(sc, TAB);
            if (database == null) {
                sendResponse(sc, "The profiles database could not load.", false);
                throw new IOException("The database could not load.");
            }

            String userID = sc.getMember().getId();
            if (nickname != null && blacklistedName(nickname)) {
                sendResponse(sc,
                        "Inappropriate nickname detected. "
                                + "Please use another one.", true);
            } else if (database.containsKey(userID)) {
                PlayerInfo profile = (PlayerInfo) database.get(userID);

                String updateRange = link.buildRange(TAB,
                        "B", profile.getSpreadsheetPosition(),
                        "H", profile.getSpreadsheetPosition());
                List<Object> updatedRow = withNewInfo(profile, nickname,
                        playstyle, weapons, rank, team);
                String changedField = (String) updatedRow.remove(0);

                ValueRange newRow = link.buildRow(updatedRow);
                link.updateRange(updateRange, newRow);

                String cmd = sc.getSubcommandName();
                sendResponse(sc, "Your " + cmd + " has been updated to `"
                        + changedField.replaceAll("\n", " ") + "`.", false);
                log(sc.getUser().getAsTag() + "'s " + cmd + " was updated.", false);
            } else {
                sendResponse(sc, "Your MIT profile does not exist yet. "
                        + "Register with `/mit profile fc`\n"
                        + "or `/mit qprofile ...` to proceed.", true);
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
                view(sc, getParameter(args, true), true);
                break;
            case "nickname":
                setField(sc, getParameter(args, false), null,
                        null, null, null);
                break;
            case "playstyle":
                setField(sc, null, getParameter(args, false),
                        null, null, null);
                break;
            case "weapons":
                setField(sc, null, null,
                        getParameter(args, false), null, null);
                break;
            case "rank":
                setField(sc, null, null,
                        null, getParameter(args, false), null);
                break;
            case "team":
                setField(sc, null, null,
                        null, null, getParameter(args, false));
                break;
            case "delete":
                delete(sc);
                break;
        }
    }
}
