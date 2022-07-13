package bot.Engine.Drafts;

import bot.Engine.Cycles.AutoLog;
import bot.Tools.Components;

import net.dv8tion.jda.api.EmbedBuilder;
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

import java.util.TreeMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

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

    /** The teams of the draft. */
    private final DraftTeam team1, team2;

    /** The max amount of won matches within a draft. */
    private final static int MAX_SCORE = 4;

    /** The number of players required to formally end the draft. */
    private final static int NUM_PLAYERS_TO_END_DRAFT_BEFORE_START = 5;
    private final static int NUM_PLAYERS_TO_END_DRAFT_AFTER_START = 3;

    /** The players who have clicked the 'End Draft` button consecutively. */
    private final HashSet<String> endButtonClicked;

    /** The Discord message ID for this draft's processing interface. */
    private String messageID;

    /**
     * Assigns the corresponding instance variables from the Draft class.
     * @param draftToProcess the draft to process.
     */
    public DraftProcess(Draft draftToProcess) {
        started = false;
        draft = draftToProcess;

        team1 = new DraftTeam();
        team2 = new DraftTeam();

        endButtonClicked = new HashSet<>();
    }

    /**
     * Checks whether the draft has started or not.
     * @return True if it has started.
     *         False otherwise.
     */
    public boolean hasStarted() {
        return started;
    }

    /** Retrieves Team 1. */
    public DraftTeam getTeam1() {
        return team1;
    }

    /** Retrieves Team 2. */
    public DraftTeam getTeam2() {
        return team2;
    }

    /** Resets who have clicked the 'End Draft' button. */
    private void resetEndDraftButton() {
        endButtonClicked.clear();
    }

    /** Retrieves the message ID of this teams interface. */
    public String getMessageID() {
        return messageID;
    }

    /** Retrieves the teams interface of the draft. */
    public Message getMessage() {
        MessageChannel channel =
                draft.getDraftChannel();
        return channel.retrieveMessageById(getMessageID()).complete();
    }

    /** Retrieves the caption ping of the draft process. */
    private String getPing() {
        StringBuilder ping = new StringBuilder();
        TreeMap<Integer, String> captainIDs = draft.determineCaptains(null);
        DraftPlayer captain1 = draft.getPlayers().get(captainIDs.get(1));
        DraftPlayer captain2 = draft.getPlayers().get(captainIDs.get(2));

        ping.append(draft.getEmote()).append(" ");
        if (!hasStarted()) {
            ping.append(String.format("*`| captain 1 - %s | captain 2 - %s |`*",
                            captain1.getName(),
                            captain2.getName()))
                    .append("\n");
        }

        for (Map.Entry<String, DraftPlayer> mapping : draft.getPlayers().entrySet()) {
            String id = mapping.getKey();
            DraftPlayer player = mapping.getValue();

            if (!player.isActive()) {
                continue;
            } else if (!hasStarted()) {
                if (player.isCaptainForTeam1() && getTeam1().isEmpty()) {
                    getTeam1().add(id, player);
                } else if (player.isCaptainForTeam2() && getTeam2().isEmpty()) {
                    getTeam2().add(id, player);
                }
            }

            ping.append(player.getAsMention(id)).append(" ");
        }

        ping.append("\n\nWelcome to the draft. Jump in a VC and ");
        if (!hasStarted()) {
            ping.append("have the captains alternate choosing teammates in a ")
                    .append("`1-2-1-1-...` pattern. Approve of a maplist ")
                    .append("using `/").append(draft.getPrefix()).append(" ")
                    .append("genmaps`, and **begin the draft** before ")
                    .append("playing.\n\n")
                    .append(" __If any problems occur with teams, ")
                    .append("captains should reset the teams!__ ")
                    .append("<:Wahoozones:766479174839173200>");
        } else {
            ping.append("make sure everyone is on a team before continuing! ")
                    .append("Remember you can check pins to scroll back to this.");
        }

        return ping.toString();
    }

    /**
     * Gets the team members and formats it into mentionable text.
     * @param team the draft team to use.
     * @return the team's mentions of the players.
     */
    public String buildTeamString(DraftTeam team) {
        StringBuilder teamBuilder = new StringBuilder();
        for (Map.Entry<String, DraftPlayer> mapping : team.getPlayers().entrySet()) {
            String playerID = mapping.getKey();
            DraftPlayer player = mapping.getValue();

            teamBuilder.append(player.getAsMention(playerID));
            if (!player.isActive()) {
                teamBuilder.append(" (inactive)");
            } else if (player.isSub()) {
                teamBuilder.append(" (sub)");
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
                getTeam1().getScore(), getTeam2().getScore());
        eb.addField("Score:", score, false);

        if (draft.isInitialized() && !hasStarted()) {
            eb.addField("Status:", "CHOOSING TEAMS", false);
        } else if (draft.isInitialized() && hasStarted()) {
            eb.addField("Status:", "IN PROGRESS", false);
        } else {
            eb.addField("Status:", "FINISHED", false);
        }

        eb.addField("Team 1:", buildTeamString(getTeam1()), false);
        eb.addField("Team 2:", buildTeamString(getTeam2()), true);

        draft.sendEmbed(interaction, eb);
    }

    /**
     * Refreshes the process's interface
     * @param interaction the user interaction calling this method.
     */
    public void refresh(GenericInteractionCreateEvent interaction) {
        String idSuffix = draft.getPrefix().toUpperCase() + draft.getNumDraft();
        List<SelectionMenu> menus = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();
        resetEndDraftButton();

        menus.add(Components.ForDraftProcess.teamSelectionMenu(
                idSuffix, draft.getPlayers()));
        if (hasStarted()) {
            buttons.add(Components.ForDraftProcess.plusOne(idSuffix));
            buttons.add(Components.ForDraftProcess.minusOne(idSuffix));
            buttons.add(Components.ForDraftProcess.draftSubLink(interaction, idSuffix, draft));
        } else {
            buttons.add(Components.ForDraftProcess.resetTeams(idSuffix));
            buttons.add(Components.ForDraftProcess.beginDraft(idSuffix));
        }
        buttons.add(Components.ForDraftProcess.endDraftProcess(idSuffix));
        buttons.add(Components.ForDraftProcess.refresh(idSuffix));

        if (interaction != null) {
            interaction.getHook().editOriginal(getPing()).setActionRows(
                    ActionRow.of(menus), ActionRow.of(buttons)).queue();
            updateReport(interaction);
        } else {
            TextChannel channel = draft.getDraftChannel();
            channel.sendMessage(getPing()).setActionRows(
                    ActionRow.of(menus), ActionRow.of(buttons)).queue(
                            message -> {
                                message.pin().queue();
                                draft.wait(1000);
                            });
        }
    }

    /**
     * Determines the team to add a player to.
     * @param sm a menu selection to analyze.
     * @param captainID the Discord ID of the captain of the team to add to.
     * @param playerID the Discord ID of the player to add.
     * @param player the player to add.
     */
    private void determineTeam(SelectionMenuEvent sm, String captainID,
                              String playerID, DraftPlayer player) {
        if (getTeam1().contains(captainID) && getTeam1().needsPlayers()) {
            getTeam1().add(playerID, player);
        } else if (getTeam2().contains(captainID) && getTeam2().needsPlayers()) {
            getTeam2().add(playerID, player);
        } else {
            draft.sendResponse(sm,
                    "Your team doesn't need players right now!", true);
        }
    }

    /**
     * Adds a player to a team.
     * @param sm a menu selection to analyze.
     */
    public void addPlayerToTeam(SelectionMenuEvent sm) {
        resetEndDraftButton();
        messageID = sm.getMessageId();

        String authorID = sm.getMember().getId();
        DraftPlayer author = draft.getPlayers().get(authorID);

        SelectOption chosenPlayer = sm.getInteraction().getSelectedOptions().get(0);
        String playerName = chosenPlayer.getLabel();
        String playerID = chosenPlayer.getValue();

        if (author == null) {
            draft.sendReply(sm, "You are not in this draft!", true);
        } else if (!hasStarted() && !author.isCaptainForTeam1()
            && !author.isCaptainForTeam2()) {
            draft.sendReply(sm, "Only captains can choose players.", true);
        } else if (playerName.equals("<end>")) {
            draft.sendReply(sm,
                    "That is the end of the list. Refresh if needed.", true);
        } else {
            sm.deferEdit().queue();
            DraftPlayer foundPlayer = draft.getPlayers().get(playerID);
            determineTeam(sm, authorID, playerID, foundPlayer);

            refresh(sm);
        }
    }

    /**
     * Resets the teams list.
     * @param bc a button click to analyze.
     */
    public void resetTeams(ButtonClickEvent bc) {
        DraftPlayer author = draft.getPlayers().get(bc.getMember().getId());
        resetEndDraftButton();

        if (author == null) {
            draft.sendReply(bc, "You are not in this draft!", true);
        } else if (!author.isCaptainForTeam1() && !author.isCaptainForTeam2()) {
            draft.sendReply(bc, "Only captains can reset the teams.", true);
        } else {
            bc.deferEdit().queue();

            getTeam1().clear();
            getTeam2().clear();

            refresh(bc);
        }
    }

    /**
     * Determines if the draft meets the requirements to be started.
     * @param bc the button click to analyze.
     */
    public void start(ButtonClickEvent bc) {
        DraftPlayer author = draft.getPlayers().get(bc.getMember().getId());
        resetEndDraftButton();

        if (author == null) {
            draft.sendReply(bc, "You are not in this draft!", true);
        } else if (!author.isCaptainForTeam1() && !author.isCaptainForTeam2()) {
            draft.sendReply(bc, "Only captains can start the draft.", true);
        } else if (getTeam1().needsPlayers() || getTeam2().needsPlayers()) {
            draft.sendReply(bc, "Not everyone is in the draft yet.", true);
        } else {
            bc.deferEdit().queue();

            started = true;
            refresh(bc);
        }
    }

    /**
     * Adds points to teams.
     * @param team the winning team.
     * @param otherTeam the losing team.
     */
    private void givePoints(ButtonClickEvent bc,
                            DraftTeam team, DraftTeam otherTeam) {
        if (team.getScore() < MAX_SCORE) {
            team.incrementScore();
        } else {
            draft.sendResponse(bc, "You've already hit the point limit!", true);
            return;
        }

        for (DraftPlayer player : team.getPlayers().values()) {
            player.incrementWins();
        }
        for (DraftPlayer player : otherTeam.getPlayers().values()) {
            player.incrementLosses();
        }
    }

    /**
     * Subtracts points from teams.
     * @param team the "winning" team.
     * @param otherTeam the "losing" team.
     */
    private void revertPoints(ButtonClickEvent bc,
                              DraftTeam team, DraftTeam otherTeam) {
        if (team.getScore() > 0) {
            team.decrementScore();
        } else {
            draft.sendResponse(bc, "You cannot have less than zero points!", true);
            return;
        }

        for (DraftPlayer player : team.getPlayers().values()) {
            player.decrementWins();
        }
        for (DraftPlayer player : otherTeam.getPlayers().values()) {
            player.decrementLosses();
        }
    }

    /**
     * Adjusts the points for players within teams.
     * @param bc a button click to analyze.
     * @param authorID the Discord ID of the player which pressed the button.
     * @param increment True if a point should be added to the author's team.
     *                  False if a point should be deducted from the author's team.
     */
    public void changePointsForTeam(ButtonClickEvent bc, String authorID,
                                    boolean increment) {
        bc.deferEdit().queue();
        resetEndDraftButton();

        DraftTeam team, otherTeam;
        if (getTeam1().needsPlayers() || getTeam2().needsPlayers()) {
            draft.sendResponse(bc,
                    "Add your subs before continuing (Check the "
                            + "the selection menu).", true);
            refresh(bc);
            return;
        } else if (getTeam1().contains(authorID)) {
            team = getTeam1();
            otherTeam = getTeam2();
        } else if (getTeam2().contains(authorID)) {
            team = getTeam2();
            otherTeam = getTeam1();
        } else {
            draft.sendResponse(bc, "You are not part of this draft!", true);
            return;
        }

        if (increment) {
            givePoints(bc, team, otherTeam);
        } else {
            revertPoints(bc, team, otherTeam);
        }

        refresh(bc);
    }

    /**
     * Determines if the draft meets the requirements to be ended.
     * @param bc the button click to analyze.
     * @return True if the draft has ended.
     *         False otherwise.
     */
    public boolean hasEnded(ButtonClickEvent bc) {
        String authorID = bc.getMember().getId();
        messageID = bc.getMessageId();

        DraftPlayer foundAuthor = draft.getPlayers().get(authorID);
        if (foundAuthor == null || !foundAuthor.isActive()) {
            draft.sendReply(bc, "You are not in the draft!", true);
            return false;
        }

        endButtonClicked.add(authorID);
        int numClicksLeft =
                NUM_PLAYERS_TO_END_DRAFT_BEFORE_START - endButtonClicked.size();
        if (hasStarted()) {
            numClicksLeft =
                    NUM_PLAYERS_TO_END_DRAFT_AFTER_START - endButtonClicked.size();
        }

        if (numClicksLeft <= 0) {
            bc.deferEdit().queue();
            draft.toggleRequest(false);

            if (hasStarted()) {
                draft.unpinDraftChannelPins();

                String idSuffix = draft.getPrefix() + draft.getNumDraft();
                List<Button> buttons = new ArrayList<>();
                buttons.add(Components.ForDraftProcess.endDraftProcess(idSuffix)
                        .withStyle(ButtonStyle.SECONDARY).asDisabled());

                draft.sendButtons(bc, "This draft has ended.", buttons);
                draft.getMessage(bc).editMessage("This draft has ended.")
                        .setActionRow(Components.ForDraft.refresh(
                                        draft.getPrefix() + draft.getNumDraft())
                                .asDisabled()).queue();

                updateReport(bc);
                AutoLog log = new AutoLog(draft.getPrefix());
                log.matchReport(bc, draft);
            } else {
                draft.unpinDraftChannelPins();
                getMessage().delete().queue();

                draft.getDraftChannel().sendMessage(
                        "The draft has ended. Sorry about the early stop! "
                        + "Feel free to request a new one!").queue();
                draft.getMessage(bc).editMessage("This draft ended early.")
                        .setActionRow(Components.ForDraft.refresh(
                                        draft.getPrefix() + draft.getNumDraft())
                                .asDisabled()).queue();

                draft.log("A " + draft.getSection()
                        + " draft was ended before it started.", false);
            }

            return true;
        } else {
            String reply = "You need `" + numClicksLeft
                    + "` other player(s) to end the draft.";
            draft.sendReply(bc, reply, true);

            return false;
        }
    }
}