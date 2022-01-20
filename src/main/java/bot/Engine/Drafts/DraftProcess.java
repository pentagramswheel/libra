package bot.Engine.Drafts;

import bot.Engine.Section;
import bot.Tools.ButtonBuilder;
import bot.Tools.SelectionMenuBuilder;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.List;
import java.util.ArrayList;

/**
 * @author  Wil Aquino, Turtle#1504
 * Date:    January 16, 2022
 * Project: Libra
 * Module:  DraftProcess.java
 * Purpose: Processes a draft.
 */
public class DraftProcess{

    private final Draft draft;

    private final List<DraftPlayer> regularPlayers;

    private final List<DraftPlayer> team1;

    private final List<DraftPlayer> team2;

    private int scoreTeam1 = 0;
    private int scoreTeam2 = 0;

    StringBuilder teamSelectionMsg;
    public DraftProcess(Draft draftToProcess) {
        draft = draftToProcess;

        regularPlayers = new ArrayList<>();
        team1 = new ArrayList<>();
        team2 = new ArrayList<>();
    }

    /**
     * Retrieves team 1.
     * @return said team.
     */
    public List<DraftPlayer> getTeam1() {
        return team1;
    }

    /**
     * Retrieves team 2.
     * @return said team.
     */
    public List<DraftPlayer> getTeam2() {
        return team2;
    }

    public void start(ButtonClickEvent bc) {
        if (!draft.inProgress()) {
            return;
        }

        StringBuilder ping = new StringBuilder();
        StringBuilder captains = new StringBuilder();

        ping.append(draft.getEmote()).append(" ");
        for (int i = 0; i < draft.getPlayers().size(); i++) {
            DraftPlayer currPlayer = draft.getPlayers().get(i);
            String currPing = draft.findMember(bc, currPlayer.getID()).getAsMention();
            ping.append(currPing).append(" ");

            if (i == draft.getCaptIndex1()) {
                getTeam1().add(currPlayer);
                captains.append(currPing).append(" ");
            } else if (i == draft.getCaptIndex2()) {
                getTeam2().add(currPlayer);
                captains.append(currPing).append(" ");
            } else {
                regularPlayers.add(currPlayer);
            }
        }
        ping.append(
                "\n\nWelcome to the draft. Jump in a VC and have your "
                        + "captains " + captains + "choose teammates. "
                        + "Approve of a maplist using `/mit genmaps`, "
                        + "and begin the draft when ready. Reset the "
                        + "selection for mistakes.");

//        TextChannel channel = draft.getDraftChannel();
        TextChannel channel = draft.getChannel(bc, "vc-text");
        String idSuffix = draft.getPrefix().toUpperCase() + draft.getNumDraft();
        List<SelectionMenu> menus = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();

        menus.add(DraftComponents.teamSelectionMenu(
                idSuffix, bc, draft, regularPlayers));
        buttons.add(DraftComponents.resetTeams(idSuffix));
        buttons.add(DraftComponents.beginDraft(idSuffix));
        buttons.add(DraftComponents.add1(idSuffix));
        buttons.add(DraftComponents.subtract1(idSuffix));
        channel.sendMessage(ping).setActionRows(
                ActionRow.of(menus), ActionRow.of(buttons)).queue();


        teamSelectionMsg = ping;
    }

    /**
     * Adds a match won point to each player of a team.
     * @param team the team to analyze.
     */
    public void addWinTo(List<DraftPlayer> team) {
        for (DraftPlayer player : team) {
            if (player.isActive()) {
                player.incrementWins();
            }
        }
    }

    /**
     * Adds a match lost point to each player of a team.
     * @param team the team to analyze.
     */
    public void addLossTo(List<DraftPlayer> team){
        for (DraftPlayer player : team) {
            if (player.isActive()) {
                player.incrementLosses();
            }
        }
    }

    /**
     * Adds a point from a team.
     * @param bc the button click to analyze.
     * @param author the member who clicked the button.
     */
    public void addPointToTeam(ButtonClickEvent bc, Member author){
        boolean found = false;
        for(int i = 0; i < getTeam1().size(); i++){
            if(author.getId().equals(getTeam1().get(i).getID())){
                scoreTeam1++;
                found = true;
                break;
            }
        }
       if(!found){
           for(int i = 0; i < getTeam2().size(); i++){
               if(author.getId().equals(getTeam2().get(i).getID())){
                   scoreTeam2++;
                   found = true;
                   break;
               }
           }
       }
       String message = "**Score**\nTeam 1 | " + scoreTeam1 + " - " + scoreTeam2 + " | Team 2";
       bc.editMessage(teamSelectionMsg.toString() + "\n\n" + message).queue();
    }

    /**
     * Subtracts a point from a team.
     * @param bc the button click to analyze.
     * @param author the member who clicked the button.
     */
    public void subtractPointFromTeam(ButtonClickEvent bc, Member author){
        boolean found = false;
        for(int i = 0; i < getTeam1().size(); i++){
            if(author.getId().equals(getTeam1().get(i).getID())){
                scoreTeam1--;
                found = true;
                break;
            }
        }
        if(!found){
            for(int i = 0; i < getTeam2().size(); i++){
                if(author.getId().equals(getTeam2().get(i).getID())){
                    scoreTeam2--;
                    found = true;
                    break;
                }
            }
        }

        String message = "**Score**\nTeam 1 | " + scoreTeam1 + " - " + scoreTeam2 + " | Team 2";
        bc.editMessage(teamSelectionMsg.toString() + "\n\n" + message).queue();

    }
    /**
     * adds a player to a team.
     * @param sm The selection menu click to analyze.
     * @param captain The person who made the selection.
     * @param player The player who got picked by the captain.
     */
    public void addPlayerToTeam(SelectionMenuEvent sm, Member captain, Member player){
        DraftPlayer dp = null;
        int indexOfPickedPlayer = 0;
        for(int i = 0; i < regularPlayers.size(); i++) {
            if (regularPlayers.get(i).getID().equals(player.getId())) {
                indexOfPickedPlayer = i;
                dp = regularPlayers.get(i);
            }
        }


        if(draft.getPlayers().get(draft.getCaptIndex1()).getID().equals(captain.getId())){
            team1.add(dp);

        }else if(draft.getPlayers().get(draft.getCaptIndex2()).getID().equals(captain.getId())){
            team2.add(dp);


        }else{
            draft.sendReply(sm, "You are not the captain, so you can not pick.", true);
            System.out.println(captain.getId() + " is not the captain. Rejected their pick.");

            return;
        }
        String mention = "Team 1:\n";
        for(int i = 0; i < team1.size(); i++){
            mention += sm.getGuild().retrieveMemberById(team1.get(i).getID()).complete().getAsMention() + "\n";

        }
        mention += "Team 2:\n";
        for(int i = 0; i < team2.size(); i++){
            mention += sm.getGuild().retrieveMemberById(team2.get(i).getID()).complete().getAsMention() + "\n";

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

        if(captain.getId().equals(draft.getPlayers().get(draft.getCaptIndex1()).getID())){
            Member otherCaptain = draft.findMember(
                    sm, draft.getPlayers().get(draft.getCaptIndex1()).getID());
            draft.sendReply(sm, mention + "\n" + otherCaptain.getAsMention() + " please pick a player", false);
        }else{
            Member otherCaptain = draft.findMember(
                    sm, draft.getPlayers().get(draft.getCaptIndex2()).getID());
            draft.sendReply(sm,mention + "\n" +  otherCaptain.getAsMention() + " please pick a player", false);

        }


    }
    public void resetTeams(ButtonClickEvent bc){
        team1.clear();
        team2.clear();
        team1.add(draft.getPlayers().get(draft.getCaptIndex1()));
        team2.add(draft.getPlayers().get(draft.getCaptIndex2()));

        draft.sendReply(bc, "The teams has been reset by a captain. Please choose again.", false);
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
        /**
         * Builds the "Add 1 Point" button.
         * @param suffix the ID's suffix.
         * @return said button.
         */
        private static Button add1(String suffix){
            return new ButtonBuilder("add1" + suffix,
                    "Add 1 Point", null, 3).getButton();
        }
        /**
         * Builds the "Subtract 1 Point" button.
         * @param suffix the ID's suffix.
         * @return said button.
         */
        private static Button subtract1(String suffix){
            return new ButtonBuilder("subtract1" + suffix,
                    "Subtract 1 Point", null, 3).getButton();
        }
        /**
         * Builds the "Begin Draft" button.
         * @param suffix the ID's suffix.
         * @return said button.
         */
        private static Button beginDraft(String suffix) {
            return new ButtonBuilder("beginDraft" + suffix,
                    "Begin Draft", null, 1).getButton();
        }



        /**
         * Builds the "End Draft" button.
         * @param suffix the ID's suffix.
         * @return the button.
         */
        private static Button end(String suffix) {
            return new ButtonBuilder("end" + suffix,
                    "End Draft", null, 3).getButton();
        }
    }
}