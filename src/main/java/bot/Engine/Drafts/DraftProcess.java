package bot.Engine.Drafts;

import bot.Engine.Section;
import bot.Tools.ButtonBuilder;
import bot.Tools.SelectionMenuBuilder;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
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

//    StringBuilder teamSelectionMsg;

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
        List<String> captains = new ArrayList<>(2);

        ping.append(draft.getEmote()).append(" ");
        for (int i = 0; i < draft.getPlayers().size(); i++) {
            DraftPlayer currPlayer = draft.getPlayers().get(i);
            String currPing = draft.findMember(bc, currPlayer.getID()).getAsMention();
            ping.append(currPing).append(" ");

            if (i == draft.getCaptIndex1()) {
                getTeam1().add(currPlayer);
                captains.add(currPing);
            } else if (i == draft.getCaptIndex2()) {
                getTeam2().add(currPlayer);
                captains.add(currPing);
            } else {
                regularPlayers.add(currPlayer);
            }
        }
        ping.append(
                "\n\nWelcome to the draft. Jump in a VC and have the captains "
                        + "alternate choosing teammates. Join Team 1 ("
                        + captains.get(0) + ") or Team 2 (" + captains.get(1)
                        + ") based on their selection. Approve of a maplist using the "
                        + "`/mit genmaps`, and begin the draft when everyone is ready. "
                        + "The captains will reset the team selection as needed.");

//        TextChannel channel = draft.getDraftChannel();
        TextChannel channel = draft.getChannel(bc, "vc-text");
        String idSuffix = draft.getPrefix().toUpperCase() + draft.getNumDraft();
        List<SelectionMenu> menus = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();

        menus.add(DraftComponents.teamSelectionMenu(
                idSuffix, bc, draft, regularPlayers));
        buttons.add(DraftComponents.resetTeams(idSuffix));
        buttons.add(DraftComponents.beginDraft(idSuffix));

        channel.sendMessage(ping).setActionRows(
                ActionRow.of(menus), ActionRow.of(buttons)).queue();

//        teamSelectionMsg = ping;
    }

    public String buildTeamString(GenericInteractionCreateEvent interaction,
                                  List<DraftPlayer> team) {
        StringBuilder teamBuilder = new StringBuilder();
        for (DraftPlayer player : team) {
            Member currPlayer = draft.findMember(interaction, player.getID());
            teamBuilder.append(currPlayer.getAsMention()).append("\n");
        }

        return teamBuilder.toString();
    }

    private void updateReport(GenericInteractionCreateEvent interaction) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Draft Details");
        eb.setColor(draft.getColor());

        StringBuilder queue = new StringBuilder();
        StringBuilder logList = new StringBuilder();

        String score = String.format("%s - %s", scoreTeam1, scoreTeam2);
        eb.addField("Score:", score, false);

        if (draft.inProgress()) {
            eb.addField("Status:", "IN PROGRESS", false);
        } else {
            eb.addField("Status:", "FINISHED", false);
        }

        eb.addField("Team 1:", buildTeamString(interaction, team1), false);
        eb.addField("Team 2:", buildTeamString(interaction, team2), true);

        draft.sendEmbed(interaction, eb);
    }

    /**
     * Sets the draft scores for a team.
     * @param team the team to analyze.
     */
    public void setScoresFor(List<DraftPlayer> team) {
        int wins, losses;
        if (team == team1) {
            wins = scoreTeam1;
            losses = scoreTeam2;
        } else {
            wins = scoreTeam2;
            losses = scoreTeam1;
        }

        for (DraftPlayer player : team) {
            player.setWins(wins);
            player.setLosses(losses);
        }
    }

    /**
     * Adds a point from a team.
     * @param bc the button click to analyze.
     * @param author the member who clicked the button.
     */
    public void addPointToTeam(ButtonClickEvent bc, Member author){
        bc.deferEdit().queue();

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

        updateReport(bc);
    }

    /**
     * Subtracts a point from a team.
     * @param bc the button click to analyze.
     * @param author the member who clicked the button.
     */
    public void subtractPointFromTeam(ButtonClickEvent bc, Member author){
        bc.deferEdit().queue();

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

        updateReport(bc);
    }

    public void updateProgress(ButtonClickEvent bc) {
        bc.deferEdit().queue();

        String idSuffix = draft.getPrefix().toUpperCase() + draft.getNumDraft();
        List<SelectionMenu> menus = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();

        menus.add(DraftComponents.teamSelectionMenu(
                idSuffix, bc, draft, regularPlayers));
        buttons.add(DraftComponents.plusOne(idSuffix));
        buttons.add(DraftComponents.minusOne(idSuffix));
        buttons.add(DraftComponents.draftSubLink(idSuffix, draft));
        buttons.add(DraftComponents.endDraft(idSuffix));

        bc.getHook().editOriginalComponents().setActionRows(
                ActionRow.of(menus), ActionRow.of(buttons)).queue();
    }

    /**
     * adds a player to a team.
     * @param sm the menu selection to analyze.
     */
    public void addPlayerToTeam(SelectionMenuEvent sm) {
        sm.deferEdit().queue();

        SelectOption chosenPlayer = sm.getInteraction().getSelectedOptions().get(0);
        Member player = draft.findMember(sm, chosenPlayer.getLabel());
        Member captain = sm.getMember();

        // replace with this block later
//            String chosenPlayerID = sm.getInteraction().getSelectedOptions().get(0).getValue();
//            Member chosenPlayer = sm.getGuild().retrieveMemberById(playerID).complete();

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

//        if(captain.getId().equals(draft.getPlayers().get(draft.getCaptIndex1()).getID())){
//            Member otherCaptain = draft.findMember(
//                    sm, draft.getPlayers().get(draft.getCaptIndex1()).getID());
//            draft.sendReply(sm, mention + "\n" + otherCaptain.getAsMention() + " please pick a player", false);
//        }else{
//            Member otherCaptain = draft.findMember(
//                    sm, draft.getPlayers().get(draft.getCaptIndex2()).getID());
//            draft.sendReply(sm,mention + "\n" +  otherCaptain.getAsMention() + " please pick a player", false);
//
//        }

        updateReport(sm);
    }

    public void resetTeams(ButtonClickEvent bc){
        bc.deferEdit().queue();

        team1.clear();
        team2.clear();
        team1.add(draft.getPlayers().get(draft.getCaptIndex1()));
        team2.add(draft.getPlayers().get(draft.getCaptIndex2()));

        updateReport(bc);
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
         * Builds the "Begin Draft" button.
         * @param suffix the ID's suffix.
         * @return said button.
         */
        private static Button beginDraft(String suffix) {
            return new ButtonBuilder("beginDraft" + suffix,
                    "Begin Draft", null, 1).getButton();
        }

        /**
         * Builds the "+1" button.
         * @param suffix the ID's suffix.
         * @return said button.
         */
        private static Button plusOne(String suffix){
            return new ButtonBuilder("plusOne" + suffix,
                    "+1", null, 0).getButton();
        }

        /**
         * Builds the "-1" button.
         * @param suffix the ID's suffix.
         * @return said button.
         */
        private static Button minusOne(String suffix){
            return new ButtonBuilder("minusOne" + suffix,
                    "-1", null, 0).getButton();
        }

        private static Button draftSubLink(String suffix, Draft draft) {
            return new ButtonBuilder("requestSubLink",
                    "Request Sub", draft.getURL(), 4).getButton();
        }

        /**
         * Builds the "End Draft" button.
         * @param suffix the ID's suffix.
         * @return the button.
         */
        private static Button endDraft(String suffix) {
            return new ButtonBuilder("endDraft" + suffix,
                    "End Draft", null, 3).getButton();
        }
    }
}