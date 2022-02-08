package bot.Engine.Drafts;

import bot.Tools.Components;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
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
import java.util.Map;
import java.util.TreeMap;

/**
 * @author  Wil Aquino, Turtle#1504
 * Date:    January 16, 2022
 * Project: Libra
 * Module:  DraftProcess.java
 * Purpose: Processes a draft.
 */
public class DraftProcess {

    /** Flag for checking whether the draft has started or not. */
    private boolean started;

    /** The draft which is to be processed. */
    private final Draft draft;

    /** The Discord IDs of the captains of the draft. */
    private String captainID1, captainID2;

    /** The non-captain players of the draft. */
    private final TreeMap<String, DraftPlayer> regularPlayers;

    /** The teams of the draft. */
    private final TreeMap<String, DraftPlayer> team1;
    private final TreeMap<String, DraftPlayer> team2;

    /** The max amount of won matches within a draft. */
    private final static int MAX_SCORE = 4;

    /** The team's scores of the draft. */
    private int scoreTeam1, scoreTeam2;

    /** The players who have clicked the 'End Draft` button consecutively. */
    private List<String> endButtonClicked;

    /** The number of players required to formally end the draft. */
    private final static int NUM_PLAYERS_TO_END_DRAFT = 3;
//    private final static int NUM_PLAYERS_TO_END_DRAFT = 1;

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
        started = false;
        draft = draftToProcess;

        int i = 0;
        for (String id : draft.getPlayers().keySet()) {
            if (i == draft.getCaptIndex1()) {
                captainID1 = id;
            } else if (i == draft.getCaptIndex2()) {
                captainID2 = id;
            }

            i++;
        }

        regularPlayers = new TreeMap<>();
        team1 = new TreeMap<>();
        team2 = new TreeMap<>();

        scoreTeam1 = scoreTeam2 = 0;
        resetEndDraftButton();
    }

    public boolean hasStarted() {
        return started;
    }

    /**
     * Retrieves team 1.
     * @return said team.
     */
    public TreeMap<String, DraftPlayer> getTeam1() {
        return team1;
    }

    /**
     * Retrieves team 2.
     * @return said team.
     */
    public TreeMap<String, DraftPlayer> getTeam2() {
        return team2;
    }

    public int getScoreTeam1() {
        return scoreTeam1;
    }

    public int getScoreTeam2() {
        return scoreTeam2;
    }

    /**
     * Initializes the team picking process and sends a message to players.
     * @param bc a button click to analyze.
     */
    public void initialize(ButtonClickEvent bc) {
        List<String> captains = new ArrayList<>(2);
        StringBuilder ping = new StringBuilder();
        ping.append(draft.getEmote()).append(" ");

        for (Map.Entry<String, DraftPlayer> player : draft.getPlayers().entrySet()) {
            String currID = player.getKey();
            DraftPlayer currPlayer = player.getValue();
            String currPing = draft.findMember(bc, currID).getAsMention();
            ping.append(currPing).append(" ");

            if (currID.equals(captainID1)) {
                captains.add(currPing);
                getTeam1().put(currID, currPlayer);
            } else if (currID.equals(captainID2)) {
                captains.add(currPing);
                getTeam2().put(currID, currPlayer);
            } else {
                regularPlayers.put(currID, currPlayer);
            }
        }
        ping.append("\n\n")
                .append("Welcome to the draft. Jump in a VC and have the ")
                .append("captains alternate choosing teammates. Join ")
                .append(captains.get(0))
                .append("'s or ")
                .append(captains.get(1))
                .append("'s team based on their selection. Approve of a maplist ")
                .append("using `/mit genmaps`, and begin the draft when ")
                .append("everyone is ready. The captains will reset the team ")
                .append("selection as needed.");

        TextChannel channel = draft.getDraftChannel();
//        TextChannel channel = draft.getChannel(bc, "vc-text");
        String idSuffix = draft.getPrefix().toUpperCase() + draft.getNumDraft();
        List<SelectionMenu> menus = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();

        menus.add(Components.ForDraftProcess.teamSelectionMenu(
                idSuffix, bc, draft, captainID1, captainID2));
        buttons.add(Components.ForDraftProcess.resetTeams(idSuffix));
        buttons.add(Components.ForDraftProcess.beginDraft(idSuffix));

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
                                  TreeMap<String, DraftPlayer> team) {
        StringBuilder teamBuilder = new StringBuilder();
        for (String playerID : team.keySet()) {
            Member currPlayer = draft.findMember(interaction, playerID);
            teamBuilder.append(currPlayer.getAsMention());

            for (String subID : draft.getSubs().keySet()) {
                if (playerID.equals(subID)) {
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

        String score = String.format("%s - %s",
                getScoreTeam1(), getScoreTeam2());
        eb.addField("Score:", score, false);

        if (draft.isInitialized() && !hasStarted()) {
            eb.addField("Status:", "CHOOSING TEAMS", false);
        } else if (draft.isInitialized() && hasStarted()) {
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

        menus.add(Components.ForDraftProcess.teamSelectionMenu(
                idSuffix, bc, draft, captainID1, captainID2));
        buttons.add(Components.ForDraftProcess.plusOne(idSuffix));
        buttons.add(Components.ForDraftProcess.minusOne(idSuffix));
        buttons.add(Components.ForDraftProcess.draftSubLink(bc, idSuffix, draft));
        buttons.add(Components.ForDraftProcess.endDraftProcess(idSuffix));

        bc.getHook().editOriginalComponents().setActionRows(
                ActionRow.of(menus), ActionRow.of(buttons)).queue();
    }

    /**
     * Checks whether a player is a captain or not.
     * @param playerID the Discord ID of the player to check.
     * @return True if they are a captain.
     *         False otherwise.
     */
    private boolean notCaptain(String playerID) {
        return !playerID.equals(captainID1) && !playerID.equals(captainID2);
    }

    /**
     * Adds a player to a team, given a captain.
     * @param player the player to add.
     * @param captainID the Discord ID of the captain of the team to add to.
     * @return True if the player could be added.
     *         False otherwise.
     */
    private boolean addPlayer(String playerID, DraftPlayer player,
                           String captainID) {
        if (hasStarted()) {
            if (captainID.equals(captainID1)
                    && draft.getSubsNeededTeam1() > 0) {
                draft.decrementSubs(playerID);
                getTeam1().put(playerID, player);
                return true;
            } else if (captainID.equals(captainID2)
                    && draft.getSubsNeededTeam2() > 0) {
                draft.decrementSubs(playerID);
                getTeam2().put(playerID, player);
                return true;
            }
        } else {
            if (captainID.equals(captainID1)) {
                getTeam1().put(playerID, player);
            } else {
                getTeam2().put(playerID, player);
            }
            return true;
        }

        return false;
    }

    /**
     * Adds a player to a team.
     * @param sm a menu selection to analyze.
     */
    public void addPlayerToTeam(SelectionMenuEvent sm) {
        SelectOption chosenCaptain = sm.getInteraction().getSelectedOptions().get(0);
        String captainID = chosenCaptain.getValue();
        String playerID = sm.getMember().getId();

        if (getTeam1().containsKey(playerID)
                || getTeam2().containsKey(playerID)) {
            draft.sendReply(sm, "You are already in a team.", true);
        } else if (!draft.getPlayers().containsKey(playerID)
            && !draft.getSubs().containsKey(playerID)) {
            draft.sendReply(sm, "You are not in this draft!", true);
        } else {
            sm.deferEdit().queue();

            if (regularPlayers.containsKey(playerID)
                && addPlayer(playerID, draft.getPlayers().get(playerID), captainID)) {
                regularPlayers.remove(playerID);
                updateReport(sm);
            } else if (draft.getSubs().containsKey(playerID)
                && addPlayer(playerID, draft.getSubs().get(playerID), captainID)) {
                updateReport(sm);
            } else {
                draft.sendResponse(sm, "That team does not need subs!", true);
            }

//            // for testing
//            if (getTeam1().size() < 4 || getTeam2().size() < 4) {
//                getTeam1().put("350386286256848896", new DraftPlayer());
//                getTeam1().put("524592272411459584", new DraftPlayer());
//                getTeam1().put("97288493029416960", new DraftPlayer());
//                getTeam2().put("407939462325207044", new DraftPlayer());
//                getTeam2().put("388507632480157696", new DraftPlayer());
//                getTeam2().put("191016647543357440", new DraftPlayer());
//            }
//            updateReport(sm);
        }
    }

    /**
     * Resets the teams list.
     * @param bc a button click to analyze.
     */
    public void resetTeams(ButtonClickEvent bc) {
        if (notCaptain(bc.getMember().getId())) {
            draft.sendReply(bc, "Only captains can reset the teams.", true);
        } else {
            bc.deferEdit().queue();

            regularPlayers.putAll(getTeam1());
            regularPlayers.putAll(getTeam2());

            getTeam1().clear();
            getTeam2().clear();

            getTeam1().put(captainID1, draft.getPlayers().get(captainID1));
            getTeam2().put(captainID2, draft.getPlayers().get(captainID2));

            updateReport(bc);
        }
    }

    /**
     * Determines if the draft meets the requirements to be started.
     * @param bc the button click to analyze.
     */
    public void start(ButtonClickEvent bc) {
        if (notCaptain(bc.getMember().getId())) {
            draft.sendReply(bc, "Only captains can start the draft.", true);
        } else if (getTeam1().size() + getTeam2().size()
                != draft.getPlayers().size()) {
            draft.sendReply(bc, "Not everyone is in the draft yet.", true);
        } else {
            bc.deferEdit().queue();

            started = true;
            updateProgress(bc);
            updateReport(bc);
        }
    }

    /**
     * Gives points to teams.
     * @param team the winning team.
     * @param otherTeam the losing team.
     */
    private void givePoints(ButtonClickEvent bc,
                            TreeMap<String, DraftPlayer> team,
                            TreeMap<String, DraftPlayer> otherTeam) {
        if (team == getTeam1() && scoreTeam1 < MAX_SCORE) {
            scoreTeam1++;
        } else if (team == getTeam2() && scoreTeam2 < MAX_SCORE) {
            scoreTeam2++;
        } else {
            draft.sendResponse(bc, "You've already hit the point limit!", true);
            return;
        }

        for (DraftPlayer player : team.values()) {
            player.incrementWins();
        }
        for (DraftPlayer player : otherTeam.values()) {
            player.incrementLosses();
        }
    }

    /**
     * Takes away points from teams.
     * @param team the "winning" team.
     * @param otherTeam the "losing" team.
     */
    private void revertPoints(ButtonClickEvent bc,
                              TreeMap<String, DraftPlayer> team,
                              TreeMap<String, DraftPlayer> otherTeam) {
        if (team == getTeam1() && scoreTeam1 > 0) {
            scoreTeam1--;
        } else if (team == getTeam2() && scoreTeam2 > 0) {
            scoreTeam2--;
        } else {
            draft.sendResponse(bc, "You cannot have less than zero points!", true);
            return;
        }

        for (DraftPlayer player : team.values()) {
            player.decrementWins();
        }
        for (DraftPlayer player : otherTeam.values()) {
            player.decrementLosses();
        }
    }

    /**
     * Determines points to give or take away from teams.
     * @param team the team to base the decision on.
     * @param increment True for giving points to the team above.
     *                  False for taking away points from the team above.
     */
    private void determinePoints(ButtonClickEvent bc,
                                 TreeMap<String, DraftPlayer> team,
                                 boolean increment) {
        TreeMap<String, DraftPlayer> otherTeam = getTeam2();
        if (team == getTeam2()) {
            otherTeam = getTeam1();
        }

        if (increment) {
            givePoints(bc, team, otherTeam);
        } else {
            revertPoints(bc, team, otherTeam);
        }

        updateReport(bc);
    }

    /**
     * Adds a point to a team.
     * @param bc the button click to analyze.
     * @param authorID the Discord ID of the player who clicked the button.
     */
    public void addPointToTeam(ButtonClickEvent bc, String authorID){
        bc.deferEdit().queue();
        resetEndDraftButton();

        if (getTeam1().containsKey(authorID)) {
            determinePoints(bc, getTeam1(), true);
        } else if (getTeam2().containsKey(authorID)) {
            determinePoints(bc, getTeam2(), true);
        } else {
            draft.sendResponse(bc, "You're not part of this draft!", true);
        }
    }

    /**
     * Subtracts a point from a team.
     * @param bc the button click to analyze.
     * @param authorID the Discord ID of the member who clicked the button.
     */
    public void subtractPointFromTeam(ButtonClickEvent bc, String authorID) {
        bc.deferEdit().queue();
        resetEndDraftButton();

        if (getTeam1().containsKey(authorID)) {
            determinePoints(bc, getTeam1(), false);
        } else if (getTeam2().containsKey(authorID)) {
            determinePoints(bc, getTeam2(), false);
        } else {
            draft.sendResponse(bc, "You're not part of this draft!", true);
        }
    }

    /**
     * Determines if the draft meets the requirements to be ended.
     * @param bc the button click to analyze.
     */
    public void attemptEnd(ButtonClickEvent bc) {
        String authorID = bc.getMember().getId();
        if (!getTeam1().containsKey(authorID)
                && !getTeam2().containsKey(authorID)) {
            draft.sendReply(bc, "You're not part of this draft!", true);
            return;
        }

        endButtonClicked.add(authorID);
        int numClicksLeft = NUM_PLAYERS_TO_END_DRAFT - endButtonClicked.size();

        if (numClicksLeft <= 0) {
            bc.deferEdit().queue();
            draft.toggleDraft();

            String idSuffix = draft.getPrefix() + draft.getNumDraft();
            List<Button> buttons = new ArrayList<>();
            buttons.add(Components.ForDraftProcess.draftSubLink(bc, idSuffix, draft)
                    .withLabel("Original Request"));
            buttons.add(Components.ForDraftProcess.endDraftProcess(idSuffix)
                    .withStyle(ButtonStyle.SECONDARY).asDisabled());

            draft.sendButtons(bc, "This draft has ended.", buttons);

            updateReport(bc);
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
}