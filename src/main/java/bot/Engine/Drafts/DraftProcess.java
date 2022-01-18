package bot.Engine.Drafts;

import bot.Tools.ButtonBuilder;
import bot.Tools.SelectionMenuBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.*;

/**
 * @author  Wil Aquino, Turtle#1504
 * Date:    January 16, 2022
 * Project: Libra
 * Module:  DraftProcess.java
 * Purpose: Processes a draft.
 */
public class DraftProcess {

    private final Draft draft;
    private final List<DraftPlayer> regularPlayers;
    private final List<DraftPlayer> team1;
    private final List<DraftPlayer> team2;

    public DraftProcess(Draft draftToProcess) {
        draft = draftToProcess;

        regularPlayers = new ArrayList<>();
        team1 = new ArrayList<>();
        team2 = new ArrayList<>();
    }

    public void start(ButtonClickEvent bc) {
        StringBuilder ping = new StringBuilder();

        ping.append(draft.getEmote()).append(" ");
        for (int i = 0; i < draft.getPlayers().size(); i++) {
            DraftPlayer currPlayer = draft.getPlayers().get(i);
            ping.append(draft.findMember(bc, currPlayer.getID()).getAsMention())
                    .append(" ");

            if (i == draft.getCaptIndex1()) {
                team1.add(currPlayer);
            } else if (i == draft.getCaptIndex2()) {
                team2.add(currPlayer);
            } else {
                regularPlayers.add(currPlayer);
            }
        }
        ping.append(
                "\n\nWelcome to the draft. Jump in a VC and have your "
                        + "captains choose teammates. Approve of a maplist "
                        + "using `/mit genmaps`, and begin the draft when "
                        + "ready. Reset the selection for mistakes.");

//        TextChannel channel = draft.getDraftChannel();
        TextChannel channel = draft.getChannel(bc, "vc-text");
        String idSuffix = draft.getPrefix().toUpperCase() + draft.getNumDraft();
        List<SelectionMenu> menus = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();

        menus.add(DraftComponents.teamSelectionMenu(
                idSuffix, bc, draft, regularPlayers));
        buttons.add(DraftComponents.resetTeams(idSuffix));

        channel.sendMessage(ping).setActionRows(
                ActionRow.of(menus), ActionRow.of(buttons)).queue();
    }

    public void addPlayerToTeam(SelectionMenuEvent sm, Member captain, Member player){
        DraftPlayer dp = null;
        int indexOfPickedPlayer = 0;
        for(int i = 0; i < regularPlayers.size(); i++){
            if(regularPlayers.get(i).getID() == player.getId()){
                indexOfPickedPlayer = i;
                dp = regularPlayers.get(i);
            }
        }
        if(draft.getPlayers().get(draft.getCaptIndex1()).getID() == captain.getId()){
            team1.add(dp);
        }else if(draft.getPlayers().get(draft.getCaptIndex2()).getID() == captain.getId()){
            team2.add(dp);
        }else{
            //add reject statement saying you are not the captain.
        }

        ArrayList<String> nonCaptainPlayers = new ArrayList<>();
        for(int i = 0; i < draft.getPlayers().size(); i++){
            if(i == draft.getCaptIndex1() || i == draft.getCaptIndex2() || i == indexOfPickedPlayer) {
                if(i == indexOfPickedPlayer){
                    regularPlayers.remove(indexOfPickedPlayer);
                }
                continue;
            }
            nonCaptainPlayers.add(
                    draft.findMember(
                            sm, draft.getPlayers().get(i).getID()).getAsMention());

        }

        if(captain.getId() == draft.getPlayers().get(draft.getCaptIndex1()).getID()){
            Member otherCaptain = draft.findMember(
                    sm, draft.getPlayers().get(draft.getCaptIndex1()).getID());
            draft.sendReply(sm, otherCaptain.getAsMention() + "please pick a player", false);
        }else{
            Member otherCaptain = draft.findMember(
                    sm, draft.getPlayers().get(draft.getCaptIndex2()).getID());
            draft.sendReply(sm, otherCaptain.getAsMention() + "please pick a player", false);
        }
    }

    /**
     * Components for running the draft.
     */
    private static class DraftComponents {

        /**
         * Builds the team selection menu.
         * @param suffix the ID's suffix.
         * @param players the players to include within the menu.
         * @return said menu.
         */
        private static SelectionMenu teamSelectionMenu(
                String suffix, ButtonClickEvent bc,
                Draft draft, List<DraftPlayer> players) {
            List<String> labels = new ArrayList<>();
            List<String> values = new ArrayList<>();

            int i = 0;
            for (DraftPlayer player : players) {
                Member currPlayer = draft.findMember(bc, player.getID());

                // use these choices later (menu doesn't allow duplicates)
//                labels.add(currPlayer.getEffectiveName());
//                values.add(currPlayer.getId());

                labels.add(currPlayer.getId());
                values.add(String.format("%s", i++));
            }

            return new SelectionMenuBuilder("teamSelection" + suffix,
                    labels, values, null).getMenu();
        }

        /**
         * Builds the "Reset Teams" button.
         * @param suffix the ID's suffix.
         * @return said button.
         */
        private static Button resetTeams(String suffix) {
            return new ButtonBuilder("resetTeams" + suffix,
                    "Reset Teams", null, 3).getButton();
        }

    }
}