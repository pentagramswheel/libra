package bot.Tools;

import bot.Engine.Games.Player;
import bot.Engine.Templates.GameReqs;
import bot.Tools.Builders.ButtonBuilder;
import bot.Tools.Builders.SelectionMenuBuilder;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.Map;

/**
 * @author  Wil Aquino
 * Date:    January 31, 2022
 * Project: Libra
 * Module:  Component.java
 * Purpose: Stores components used throughout
 *          the bot.
 */
public class Components {

    /** Components for general functions throughout MIT. */
    public static class ForGeneral {

        /**
         * Builds the help menu.
         * @param suffix the menu ID's suffix.
         */
        public static SelectionMenu helpMenu(String suffix) {
            List<String> labels = new ArrayList<>();
            List<String> values = new ArrayList<>();
            List<Emoji> emojis = Arrays.asList(
                    Emoji.fromEmote("profilepurple", 937938482221965332L, false),
                    Emoji.fromEmote("profilepurple", 937938482221965332L, false),
                    Emoji.fromEmote("3minigames", 773676980015988747L, false),
                    Emoji.fromEmote("3minigames", 773676980015988747L, false),
                    Emoji.fromEmote("3quest", 773676979798933506L, false),
                    Emoji.fromEmote("3quest", 773676979798933506L, false),
                    Emoji.fromEmote("openbook", 988635067733643294L, false),
                    Emoji.fromEmote("faq", 694932182019473509L, false),
                    Emoji.fromEmote("writing", 622832106011230259L, false),
                    Emoji.fromEmote("7212roleadmin", 962720955505971200L, false));

            labels.add("I want to know more about profiles!");
            labels.add("I'm having trouble with my profile.");
            labels.add("I want to know about the minigame system!");
            labels.add("I want to know everything about the minigame system!");
            labels.add("I want to know about the draft system!");
            labels.add("I want to know everything about the draft system!");
            labels.add("I want to see Libra's full documentation.");
            labels.add("I want to see Libra's frequently asked questions!");
            labels.add("(Staff) I'm having trouble logging match reports.");
            labels.add("(Staff) I'm having trouble giving roles to players.");

            for (int i = 0; i < labels.size(); i++) {
                values.add(String.valueOf(i));
            }

            return new SelectionMenuBuilder("helpMenu" + suffix,
                    labels, values, emojis).getMenu();
        }
    }

    /**
     * Components for initializing a draft.
     */
    public static class ForDraft {

        /**
         * Builds the "Join Draft" button.
         * @param suffix the button ID's suffix.
         */
        public static Button joinDraft(String suffix) {
            return new ButtonBuilder("join" + suffix,
                    "Join Draft", null, 0).getButton();
        }

        /**
         * Builds the "Start Early" button.
         * @param suffix the button ID's suffix.
         */
        public static Button setupEarly(String suffix) {
            return new ButtonBuilder("setupEarly" + suffix,
                    "Start Early", null, 1).getButton();
        }

        /**
         * Builds the "Reping" button.
         * @param suffix the button ID's suffix.
         */
        public static Button reping(String suffix) {
            return new ButtonBuilder("reping" + suffix,
                    "Reping", null, 0).getButton();
        }

        /**
         * Builds the "Leave" button.
         * @param suffix the button ID's suffix.
         */
        public static Button leave(String suffix) {
            return new ButtonBuilder("leave" + suffix,
                    "Leave", null, 3).getButton();
        }

        /**
         * Builds the "Refresh" button.
         * @param suffix the button ID's suffix.
         */
        public static Button refresh(String suffix) {
            return new ButtonBuilder("requestRefresh" + suffix,
                    "update", "788354776999526410",
                    null, 2).getButton();
        }

        /**
         * Builds a captain reassignment button.
         * @param suffix the menu ID's suffix.
         */
        public static Button reassignCaptain(String suffix) {
            return new ButtonBuilder("reassign" + suffix,
                    "Reassign", null, 0).getButton();
        }

        /**
         * Builds the "Request Sub" button.
         * @param suffix the button ID's suffix.
         */
        public static Button requestSub(String suffix) {
            return new ButtonBuilder("requestSub" + suffix,
                    "Sub Out", null, 3).getButton();
        }

        /**
         * Builds the "Join As Sub" button.
         * @param suffix the button ID's suffix.
         */
        public static Button joinAsSub(String suffix) {
            return new ButtonBuilder("sub" + suffix,
                    "Join as Sub", null, 0).getButton();
        }
    }

    /**
     * Components for running a draft.
     */
    public static class ForProcess {

        /**
         * Builds the team selection menu.
         * @param suffix the menu ID's suffix.
         * @param players the players of the draft.
         */
        public static SelectionMenu teamSelectionMenu(
                String suffix, TreeMap<String, ? extends Player> players) {
            List<String> labels = new ArrayList<>();
            List<String> values = new ArrayList<>();

            for (Map.Entry<String, ? extends Player> mapping : players.entrySet()) {
                String id = mapping.getKey();
                Player player = mapping.getValue();

                if (player.isActive() && !player.hasTeam()) {
                    labels.add(player.getName());
                    values.add(id);
                }
            }

            labels.add("<end>");
            values.add("0");

            return new SelectionMenuBuilder("teamSelection" + suffix,
                    labels, values, null).getMenu();
        }

        /**
         * Builds the "Reset Teams" button.
         * @param suffix the button ID's suffix.
         */
        public static Button resetTeams(String suffix) {
            return new ButtonBuilder("resetTeams" + suffix,
                    "Reset Teams", null, 0).getButton();
        }

        /**
         * Builds the "Begin Draft" button.
         * @param suffix the button ID's suffix.
         */
        public static Button beginDraft(String suffix) {
            return new ButtonBuilder("beginDraft" + suffix,
                    "Begin Draft", null, 1).getButton();
        }

        /**
         * Builds the "Refresh" button.
         * @param suffix the button ID's suffix.
         */
        public static Button refresh(String suffix) {
            return new ButtonBuilder("processRefresh" + suffix,
                    "update", "788354776999526410",
                    null, 2).getButton();
        }

        /**
         * Builds the "+1" button.
         * @param suffix the button ID's suffix.
         */
        public static Button plusOne(String suffix) {
            return new ButtonBuilder("plusOne" + suffix,
                    "+1", null, 0).getButton();
        }

        /**
         * Builds the "-1" button.
         * @param suffix the button ID's suffix.
         */
        public static Button minusOne(String suffix) {
            return new ButtonBuilder("minusOne" + suffix,
                    "-1", null, 0).getButton();
        }

        /**
         * Builds the "Next Turn" button.
         * @param suffix the button ID's suffix.
         */
        public static Button nextTurn(String suffix) {
            return new ButtonBuilder("nextTurn" + suffix,
                    "Next Turn", null, 0).getButton();
        }

        /**
         * Retrieves the "Request Sub" link button, for games.
         * @param interaction the user interaction calling this method.
         * @param suffix the button ID's suffix.
         * @param draft the draft request to link to.
         */
        public static Button draftSubLink(GenericInteractionCreateEvent interaction,
                                          String suffix, GameReqs draft) {
            String url = draft.getMessage(interaction).getJumpUrl();
            return new ButtonBuilder("requestSubLink" + suffix,
                    "Request Sub", url, 4).getButton();
        }

        /**
         * Builds the "End Draft" button.
         * @param suffix the button ID's suffix.
         */
        public static Button endDraftProcess(String suffix) {
            return new ButtonBuilder("endDraftProcess" + suffix,
                    "End Draft", null, 3).getButton();
        }
    }
}
