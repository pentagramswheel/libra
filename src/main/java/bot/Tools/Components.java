package bot.Tools;

import bot.Engine.Drafts.Draft;
import bot.Engine.Drafts.DraftProcess;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    January 31, 2022
 * Project: Libra
 * Module:  Component.java
 * Purpose: Stores components used throughout
 *          the bot.
 */
public class Components {

    /**
     * Buttons for initializing a draft.
     */
    public static class ForDraft {

        /**
         * Builds the "Join Draft" button.
         * @param suffix the button ID's suffix.
         * @return said button.
         */
        public static Button joinDraft(String suffix) {
            return new ButtonBuilder("join" + suffix,
                    "Join Draft", null, 0).getButton();
        }

        /**
         * Builds the "Refresh" button.
         * @param suffix the button ID's suffix.
         * @return said button.
         */
        public static Button refresh(String suffix) {
            return new ButtonBuilder("refresh" + suffix,
                    "Refresh", null, 2).getButton();
        }

        /**
         * Builds the "Reping" button.
         * @param suffix the button ID's suffix.
         * @return said button.
         */
        public static Button reping(String suffix) {
            return new ButtonBuilder("reping" + suffix,
                    "Reping", null, 0).getButton();
        }

        /**
         * Builds the "Leave" button.
         * @param suffix the button ID's suffix.
         * @return the button.
         */
        public static Button leave(String suffix) {
            return new ButtonBuilder("leave" + suffix,
                    "Leave", null, 3).getButton();
        }

        /**
         * Builds the "Draft Channel" link button.
         * @param serverID the ID of the server with the channel in it.
         * @param channelID the ID of the channel to link to.
         * @param suffix the button ID's suffix.
         * @return said button.
         */
        public static Button draftLink(String serverID, String channelID,
                                       String suffix) {
            String url = String.format("https://discord.com/channels/%s/%s",
                    serverID, channelID);
            return new ButtonBuilder("draftLink" + suffix,
                    "Draft Channel", url, 4).getButton();
        }

        /**
         * Builds the "Request Sub" button.
         * @param suffix the button ID's suffix.
         * @return said button.
         */
        public static Button requestSub(String suffix) {
            return new ButtonBuilder("requestSub" + suffix,
                    "Request Sub", null, 1).getButton();
        }

        /**
         * Builds the "Join As Sub" button.
         * @param suffix the button ID's suffix.
         * @return said button.
         */
        public static Button joinAsSub(String suffix) {
            return new ButtonBuilder("sub" + suffix,
                    "Join as Sub", null, 0).getButton();
        }

        /**
         * Builds the initial "End Draft" button.
         * @param suffix the button ID's suffix.
         * @return the button.
         */
        public static Button endDraft(String suffix) {
            return new ButtonBuilder("endDraft" + suffix,
                    "End Draft", null, 3).getButton();
        }
    }

    /**
     * Components for running a draft.
     */
    public static class ForDraftProcess {

        /**
         * Builds the team selection menu.
         * @param suffix the menu ID's suffix.
         * @param bc a button click to analyze.
         * @param draft the draft pertaining to this menu.
         * @param captainID1 the Discord ID of the first captain of the draft.
         * @param captainID2 the Discord ID of the second captain of the draft.
         * @return said menu.
         */
        public static SelectionMenu teamSelectionMenu(
                String suffix, ButtonClickEvent bc,
                Draft draft, String captainID1, String captainID2) {
            List<String> labels = new ArrayList<>();
            List<String> values = new ArrayList<>();

            Member currPlayer = draft.findMember(bc, captainID1);
            labels.add(currPlayer.getEffectiveName() + "'s Team");
            values.add(captainID1);

            currPlayer = draft.findMember(bc, captainID2);
            labels.add(currPlayer.getEffectiveName() + "'s Team");
            values.add(captainID2);

            return new SelectionMenuBuilder("teamSelection" + suffix,
                    labels, values, null).getMenu();
        }

        /**
         * Builds the "Reset Teams" button.
         * @param suffix the button ID's suffix.
         * @return said button.
         */
        public static Button resetTeams(String suffix) {
            return new ButtonBuilder("resetTeams" + suffix,
                    "Reset Teams", null, 3).getButton();
        }

        /**
         * Builds the "Begin Draft" button.
         * @param suffix the button ID's suffix.
         * @return said button.
         */
        public static Button beginDraft(String suffix) {
            return new ButtonBuilder("beginDraft" + suffix,
                    "Begin Draft", null, 1).getButton();
        }

        /**
         * Builds the "+1" button.
         * @param suffix the button ID's suffix.
         * @return said button.
         */
        public static Button plusOne(String suffix){
            return new ButtonBuilder("plusOne" + suffix,
                    "+1", null, 0).getButton();
        }

        /**
         * Builds the "-1" button.
         * @param suffix the button ID's suffix.
         * @return said button.
         */
        public static Button minusOne(String suffix){
            return new ButtonBuilder("minusOne" + suffix,
                    "-1", null, 0).getButton();
        }

        /**
         * Builds the "Request Sub" link button.
         * @param draft the draft request to link to.
         * @return said button.
         */
        public static Button draftSubLink(ButtonClickEvent bc, String suffix,
                                           bot.Engine.Drafts.Draft draft) {
            String url = draft.getMessage(bc).getJumpUrl();
            return new ButtonBuilder("requestSubLink" + suffix,
                    "Leave/Request Sub", url, 4).getButton();
        }

        /**
         * Builds the "End Draft" button.
         * @param suffix the button ID's suffix.
         * @return the button.
         */
        public static Button endDraftProcess(String suffix) {
            return new ButtonBuilder("endDraftProcess" + suffix,
                    "End Draft", null, 3).getButton();
        }
    }
}
