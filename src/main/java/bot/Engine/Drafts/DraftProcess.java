package bot.Engine.Drafts;

import bot.Tools.ButtonBuilder;
import bot.Tools.SelectionMenuBuilder;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
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

    private int scoreTeam1;
    private int scoreTeam2;

    private List<String> endButtonClicked;

    private final static int NUM_PLAYERS_TO_END_DRAFT = 1;

    private void resetEndDraftButton() {
        endButtonClicked = new ArrayList<>();
    }

    public DraftProcess(Draft draftToProcess) {
        draft = draftToProcess;

        regularPlayers = new ArrayList<>();
        team1 = new ArrayList<>();
        team2 = new ArrayList<>();

        scoreTeam1 = scoreTeam2 = 0;
        resetEndDraftButton();
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

    public void initialize(ButtonClickEvent bc) {
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
        buttons.add(DraftComponents.relink(idSuffix));

        channel.sendMessage(ping).setActionRows(
                ActionRow.of(menus), ActionRow.of(buttons)).queue();
    }

    public String buildTeamString(GenericInteractionCreateEvent interaction,
                                  List<DraftPlayer> team) {
        StringBuilder teamBuilder = new StringBuilder();
        for (DraftPlayer player : team) {
            Member currPlayer = draft.findMember(interaction, player.getID());
            teamBuilder.append(currPlayer.getAsMention());

            for (DraftPlayer sub : draft.getSubs()) {
                if (player.getID().equals(sub.getID())) {
                    teamBuilder.append(" (sub)");
                    break;
                }
            }
            teamBuilder.append("\n");
        }

        return teamBuilder.toString();
    }

    private void updateReport(GenericInteractionCreateEvent interaction) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Draft Details");
        eb.setColor(draft.getColor());

        String score = String.format("%s - %s", scoreTeam1, scoreTeam2);
        eb.addField("Score:", score, false);

        if (draft.inProgress()) {
            eb.addField("Status:", "IN PROGRESS", false);
        } else {
            eb.addField("Status:", "FINISHED", false);
        }

        eb.addField("Team 1:", buildTeamString(interaction, getTeam1()), false);
        eb.addField("Team 2:", buildTeamString(interaction, getTeam2()), true);

        draft.sendEmbed(interaction, eb);
    }

    public void updateProgress(ButtonClickEvent bc) {
        String idSuffix = draft.getPrefix().toUpperCase() + draft.getNumDraft();
        List<SelectionMenu> menus = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();

        menus.add(DraftComponents.teamSelectionMenu(
                idSuffix, bc, draft, regularPlayers));
        buttons.add(DraftComponents.plusOne(idSuffix));
        buttons.add(DraftComponents.minusOne(idSuffix));
        buttons.add(DraftComponents.draftSubLink(idSuffix, draft));
        buttons.add(DraftComponents.relink(idSuffix));
        buttons.add(DraftComponents.endDraft(idSuffix));

        bc.getHook().editOriginalComponents().setActionRows(
                ActionRow.of(menus), ActionRow.of(buttons)).queue();
    }

    private boolean notCaptain(DraftPlayer player) {
        return !draft.getPlayers().get(draft.getCaptIndex1()).equals(player)
                || !draft.getPlayers().get(draft.getCaptIndex2()).equals(player);
    }

    private void addPlayer(DraftPlayer player, Member captain) {
        String captID1 = draft.getPlayers().get(draft.getCaptIndex1()).getID();

        if (captain.getId().equals(captID1)) {
            getTeam1().add(player);
        } else {
            getTeam2().add(player);
        }
    }

    /**
     * adds a player to a team.
     * @param sm the menu selection to analyze.
     */
    public void addPlayerToTeam(SelectionMenuEvent sm) {
        SelectOption chosenPlayer = sm.getInteraction().getSelectedOptions().get(0);
        Member captain = draft.findMember(sm, chosenPlayer.getLabel());
        DraftPlayer potentialPlayer = new DraftPlayer(sm.getMember().getId());

        if (!draft.getPlayers().contains(potentialPlayer)
            || !draft.getSubs().contains((potentialPlayer))) {
            String reply =
                    "You are either already in this draft or not in it at all.";
            draft.sendReply(sm, reply, true);
            return;
        }
        sm.deferEdit().queue();

        int foundCoreIndex = regularPlayers.indexOf(potentialPlayer);
        int foundSubIndex = draft.getSubs().indexOf(potentialPlayer);
        if (foundCoreIndex != -1) {
            addPlayer(regularPlayers.remove(foundCoreIndex), captain);
        } else if (foundSubIndex != -1) {
            addPlayer(draft.getSubs().get(foundSubIndex), captain);
        }

        updateReport(sm);
    }

    public void resetTeams(ButtonClickEvent bc) {
        DraftPlayer author = new DraftPlayer(bc.getMember().getId());
        if (notCaptain(author)) {
            draft.sendReply(bc, "Only captains can reset the teams.", true);
        } else {
            bc.deferEdit().queue();

            regularPlayers.addAll(getTeam1());
            regularPlayers.addAll(getTeam2());

            getTeam1().clear();
            getTeam2().clear();

            getTeam1().add(draft.getPlayers().get(draft.getCaptIndex1()));
            getTeam2().add(draft.getPlayers().get(draft.getCaptIndex2()));

            updateReport(bc);
        }
    }

    public void start(ButtonClickEvent bc) {
        DraftPlayer author = new DraftPlayer(bc.getMember().getId());
        if (notCaptain(author)) {
            draft.sendReply(bc, "Only captains can start the draft.", true);
        } else if (getTeam1().size() + getTeam2().size()
                != draft.getPlayers().size()) {
            draft.sendReply(bc, "Not everyone is in the draft yet.", true);
        } else {
            bc.deferEdit().queue();
            updateProgress(bc);
        }
    }

    private void givePoints(List<DraftPlayer> team, List<DraftPlayer> otherTeam) {
        for (DraftPlayer player : team) {
            player.incrementWins();
        }
        for (DraftPlayer player : otherTeam) {
            player.incrementLosses();
        }

        if (team == getTeam1()) {
            scoreTeam1++;
        } else {
            scoreTeam2++;
        }
    }

    private void revertPoints(List<DraftPlayer> team, List<DraftPlayer> otherTeam) {
        for (DraftPlayer player : team) {
            player.decrementWins();
        }
        for (DraftPlayer player : otherTeam) {
            player.decrementLosses();
        }

        if (team == getTeam1()) {
            scoreTeam1--;
        } else {
            scoreTeam2--;
        }
    }

    private boolean determinePoints(List<DraftPlayer> team, Member author,
                                 boolean increment) {
        List<DraftPlayer> otherTeam = getTeam2();
        if (team == getTeam2()) {
            otherTeam = getTeam1();
        }

        for (DraftPlayer teamPlayer : team) {
            if (author.getId().equals(teamPlayer.getID())) {
                if (increment) {
                    givePoints(team, otherTeam);
                } else {
                    revertPoints(team, otherTeam);
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a point to a team.
     * @param bc the button click to analyze.
     * @param author the member who clicked the button.
     */
    public void addPointToTeam(ButtonClickEvent bc, Member author){
        bc.deferEdit().queue();
        resetEndDraftButton();

        if (!determinePoints(getTeam1(), author, true)) {
            determinePoints(getTeam2(), author, true);
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
        resetEndDraftButton();

        if (!determinePoints(getTeam1(), author, false)) {
            determinePoints(getTeam2(), author, false);
        }

        updateReport(bc);
    }

    public void attemptEnd(ButtonClickEvent bc) {
        endButtonClicked.add(bc.getMember().getId());
        int numClicksLeft = NUM_PLAYERS_TO_END_DRAFT - endButtonClicked.size();

        if (numClicksLeft <= 0) {
            bc.deferEdit().queue();

            draft.sendButton(bc, "This draft has ended.",
                    DraftComponents.endDraft(draft.getPrefix() + draft.getNumDraft())
                            .withStyle(ButtonStyle.SECONDARY).asDisabled());

            AutoLog log = new AutoLog(draft.getPrefix());
            log.matchReport(bc, draft);
            draft.toggleDraft();
        } else if (endButtonClicked.contains(bc.getMember().getId())) {
            String reply = "You need " + numClicksLeft
                    + " *other* player(s) to end the draft.";
            draft.sendReply(bc, reply, true);
        } else {
            String reply = numClicksLeft
                    + " more players are needed to end the draft.";

            draft.sendReply(bc, reply, true);
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
            //for captains adding players
            /*
            for (DraftPlayer player : players) {
                Member currPlayer = draft.findMember(bc, player.getID());

                // use these choices later (menu doesn't allow duplicates)
//                labels.add(currPlayer.getEffectiveName());
//                values.add(currPlayer.getId());

                labels.add(currPlayer.getId());
                values.add(String.format("%s", i++));
            }
            */
            //for players adding captains

                Member currPlayer = draft.findMember(bc,  draft.getPlayers().get(draft.getCaptIndex1()).getID());

                // use these choices later (menu doesn't allow duplicates)
//                labels.add(currPlayer.getEffectiveName());
//                values.add(currPlayer.getId());

                labels.add(currPlayer.getId());
                values.add(String.format("%s", i++));

                currPlayer = draft.findMember(bc,  draft.getPlayers().get(draft.getCaptIndex2()).getID());

                labels.add(currPlayer.getId());
                values.add(String.format("%s", i++));




            return new SelectionMenuBuilder("teamSelection" + suffix,
                    labels, values, null).getMenu();
        }

        /**
         * Builds the "Relink" button.
         * @param suffix the ID's suffix.
         * @return said button.
         */
        private static Button relink(String suffix) {
            return new ButtonBuilder("relink" + suffix,
                    "Relink", null, 2).getButton();
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