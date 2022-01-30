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

    /** The draft which is to be processed. */
    private final Draft draft;

    /** The captains of the draft. */
    private final DraftPlayer captain1;
    private final DraftPlayer captain2;

    /** The non-captain players of the draft. */
    private final List<DraftPlayer> regularPlayers;

    /** The teams of the draft. */
    private final List<DraftPlayer> team1;
    private final List<DraftPlayer> team2;

    /** The scoreboard of the draft. */
    private int scoreTeam1;
    private int scoreTeam2;

    /** The players who have clicked the 'End Draft` button consecutively. */
    private List<String> endButtonClicked;

    /** The number of players required to formally end the draft. */
//    private final static int NUM_PLAYERS_TO_END_DRAFT = 3;
    private final static int NUM_PLAYERS_TO_END_DRAFT = 1;

    /**
     * Resets who have clicked the 'End Draft' button.
     */
    private void resetEndDraftButton() {
        endButtonClicked = new ArrayList<>();
    }

    /**
     * Assigns the corresponding instance variables from the Draft class.
     * @param draftToProcess the draft to process.
     */
    public DraftProcess(Draft draftToProcess) {
        draft = draftToProcess;

        captain1 = new DraftPlayer(
                draft.getPlayers().get(draft.getCaptIndex1()).getID());
        captain2 = new DraftPlayer(
                draft.getPlayers().get(draft.getCaptIndex2()).getID());

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

    /**
     * Initializes the team picking process and sends a message to players.
     * @param bc
     */
    public void initialize(ButtonClickEvent bc) {
        StringBuilder ping = new StringBuilder();
        List<String> captains = new ArrayList<>(2);

        ping.append(draft.getEmote()).append(" ");
        for (DraftPlayer currPlayer : draft.getPlayers()) {
            String currPing = draft.findMember(bc, currPlayer.getID()).getAsMention();
            ping.append(currPing).append(" ");

            if (currPlayer.equals(captain1)) {
                getTeam1().add(currPlayer);
                captains.add(currPing);
            } else if (currPlayer.equals(captain2)) {
                getTeam2().add(currPlayer);
                captains.add(currPing);
            } else {
                regularPlayers.add(currPlayer);
            }
        }
        ping.append("\n\n")
                .append("Welcome to the draft. Jump in a VC and have the ")
                .append("captains alternate choosing teammates. Join Team 1 (")
                .append(captains.get(0))
                .append(") or Team 2 (")
                .append(captains.get(1))
                .append(") based on their selection. Approve of a maplist ")
                .append("using `/mit genmaps`, and begin the draft when ")
                .append("everyone is ready. The captains will reset the team ")
                .append("selection as needed.");

//        TextChannel channel = draft.getDraftChannel();
        TextChannel channel = draft.getChannel(bc, "vc-text");
        String idSuffix = draft.getPrefix().toUpperCase() + draft.getNumDraft();
        List<SelectionMenu> menus = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();

        menus.add(DraftComponents.teamSelectionMenu(
                idSuffix, bc, draft, captain1, captain2));
        buttons.add(DraftComponents.resetTeams(idSuffix));
        buttons.add(DraftComponents.beginDraft(idSuffix));
        buttons.add(DraftComponents.relink(idSuffix));

        channel.sendMessage(ping).setActionRows(
                ActionRow.of(menus), ActionRow.of(buttons)).queue();
    }

    /**
     * Gets the team members and formats it into mentionable text.
     * @param interaction the interaction to analyze.
     * @param team the draft team to use.
     * @return the team's mentions of the players
     */
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

    /**
     * Sends the details of the overall team selection with
     * the players of the draft.
     * @param interaction the user interaction calling this method.
     */
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
            eb.addField("Notice:", "Please end the the original draft request\n"
                    + "to allow others to queue more drafts.", false);
        }

        eb.addField("Team 1:", buildTeamString(interaction, getTeam1()), false);
        eb.addField("Team 2:", buildTeamString(interaction, getTeam2()), true);

        draft.sendEmbed(interaction, eb);
    }

    /**
     * Updates the overall progress of the draft process.
     * @param bc a button click to analyze.
     */
    private void updateProgress(ButtonClickEvent bc) {
        String idSuffix = draft.getPrefix().toUpperCase() + draft.getNumDraft();
        List<SelectionMenu> menus = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();

        menus.add(DraftComponents.teamSelectionMenu(
                idSuffix, bc, draft, captain1, captain2));
        buttons.add(DraftComponents.plusOne(idSuffix));
        buttons.add(DraftComponents.minusOne(idSuffix));
        buttons.add(DraftComponents.draftSubLink(draft));
        buttons.add(DraftComponents.relink(idSuffix));
        buttons.add(DraftComponents.endDraft(idSuffix));

        bc.getHook().editOriginalComponents().setActionRows(
                ActionRow.of(menus), ActionRow.of(buttons)).queue();
    }

    /**
     * Checks whether a player is a captain or not.
     * @param player the player to check.
     * @return True if they are a captain.
     *         False otherwise.
     */
    private boolean notCaptain(DraftPlayer player) {
        return !player.equals(captain1)
                || !player.equals(captain2);
    }

    /**
     * Adds a player to a team, given a captain.
     * @param player the player to add.
     * @param captain the captain of the team to add to.
     */
    private void addPlayer(DraftPlayer player, Member captain) {
        if (captain.getId().equals(captain1.getID())) {
            getTeam1().add(player);
        } else {
            getTeam2().add(player);
        }
    }

    /**
     * Adds a player to a team.
     * @param sm the menu selection to analyze.
     */
    public void addPlayerToTeam(SelectionMenuEvent sm) {
        SelectOption chosenPlayer = sm.getInteraction().getSelectedOptions().get(0);
//        Member captain = draft.findMember(sm, chosenPlayer.getValue());
        Member captain = draft.findMember(sm, chosenPlayer.getLabel());
        DraftPlayer potentialPlayer = new DraftPlayer(sm.getMember().getId());

//        if (!draft.getPlayers().contains(potentialPlayer)
//            || !draft.getSubs().contains((potentialPlayer))) {
//            String reply =
//                    "You are either already in this draft or not in it at all.";
//            draft.sendReply(sm, reply, true);
//            return;
//        }
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

    /**
     * Resets the teams list.
     * @param bc the button click to analyze.
     */
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

            getTeam1().add(captain1);
            getTeam2().add(captain2);

            updateReport(bc);
        }
    }

    /**
     * Determines if the draft meets the requirements to be started.
     * @param bc the button click to analyze.
     */
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

    /**
     * Gives points to teams.
     * @param team the winning team.
     * @param otherTeam the losing team.
     */
    private void givePoints(List<DraftPlayer> team,
                            List<DraftPlayer> otherTeam) {
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

    /**
     * Takes away points from teams.
     * @param team the "winning" team.
     * @param otherTeam the "losing" team.
     */
    private void revertPoints(List<DraftPlayer> team, List<DraftPlayer> otherTeam) {
        for (DraftPlayer player : team) {
            if (player.isActive()) {
                player.decrementWins();
            }
        }
        for (DraftPlayer player : otherTeam) {
            if (player.isActive()) {
                player.decrementLosses();
            }
        }

        if (team == getTeam1()) {
            scoreTeam1--;
        } else {
            scoreTeam2--;
        }
    }

    /**
     * Determines points to give or take away from teams.
     * @param team the team to base the decision on.
     * @param author the player who clicked the initial button.
     * @param increment True for giving points to the team above.
     *                  False for taking away points from the team above.
     * @return True if the author was found in the team.
     *         False otherwise.
     */
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
     * @param author the player who clicked the button.
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

    /**
     * Determines if the draft meets the requirements to be ended.
     * @param bc the button click to analyze.
     */
    public void attemptEnd(ButtonClickEvent bc) {
        endButtonClicked.add(bc.getMember().getId());
        int numClicksLeft = NUM_PLAYERS_TO_END_DRAFT - endButtonClicked.size();

        if (numClicksLeft <= 0) {
            bc.deferEdit().queue();
            draft.toggleDraft();

            updateReport(bc);

            String idSuffix = draft.getPrefix() + draft.getNumDraft();
            List<Button> buttons = new ArrayList<>();
            buttons.add(DraftComponents.draftSubLink(draft)
                    .withLabel("Original Request"));
            buttons.add(DraftComponents.endDraft(idSuffix)
                    .withStyle(ButtonStyle.SECONDARY).asDisabled());

            draft.sendButtons(bc, "This draft has ended.", buttons);

            AutoLog log = new AutoLog(draft.getPrefix());
            log.matchReport(bc, draft);
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
         * @param bc a button click to analyze.
         * @param draft the draft pertaining to this menu.
         * @param captain1 the first captain of the draft.
         * @param captain2 the second captain of the draft.
         * @return said menu.
         */
        private static SelectionMenu teamSelectionMenu(
                String suffix, ButtonClickEvent bc, Draft draft,
                DraftPlayer captain1, DraftPlayer captain2) {
            List<String> labels = new ArrayList<>();
            List<String> values = new ArrayList<>();

            int i = 0;

            Member currPlayer = draft.findMember(bc, captain1.getID());
//                labels.add(currPlayer.getEffectiveName() + "'s Team");
//                values.add(currPlayer.getId());

            labels.add(currPlayer.getId());
            values.add(String.format("%s", i++));

            currPlayer = draft.findMember(bc, captain2.getID());
//                labels.add(currPlayer.getEffectiveName() + "'s Team");
//                values.add(currPlayer.getId());

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

        /**
         * Builds the "Request Sub" button.
         * @param draft the draft pertaining to this button.
         * @return said button.
         */
        private static Button draftSubLink(Draft draft) {
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