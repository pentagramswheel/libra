package bot.Engine.Games.Minigames;

import bot.Engine.Games.Player;
import bot.Engine.Games.Process;
import bot.Engine.Games.Team;
import bot.Engine.Templates.ProcessReqs;
import bot.Tools.Components;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author  Wil Aquino
 * Date:    January 16, 2022
 * Project: Libra
 * Module:  MiniProcess.java
 * Purpose: Processes a minigame, via a teams interface.
 */
public class MiniProcess extends Process<MiniGame, Team<Player>, Player>
        implements ProcessReqs {

    /** The players who have clicked the 'Next Turn` button consecutively. */
    private final HashSet<String> nextButtonClicked;

    /** Resets who have clicked the 'End Draft' button. */
    public void resetNextButtonClicked() {
        nextButtonClicked.clear();
    }

    /**
     * Constructs a two-team minigame to process, with balanced teams.
     * @param minigameToProcess the draft to process.
     * @param playerLimit the player ceiling for both teams.
     */
    public MiniProcess(MiniGame minigameToProcess, int playerLimit) {
        super(minigameToProcess,
                new Team<>(playerLimit,
                        minigameToProcess.getProperties().getWinningScore()),
                new Team<>(playerLimit,
                        minigameToProcess.getProperties().getWinningScore()),
                null);

        nextButtonClicked = new HashSet<>();
    }

    /**
     * Constructs a two-team minigame to process, with offset teams.
     * @param minigameToProcess the draft to process.
     * @param playerLimit1 the player ceiling for Team 1.
     * @param playerLimit2 the player ceiling for Team 2.
     */
    public MiniProcess(MiniGame minigameToProcess,
                       int playerLimit1, int playerLimit2) {
        super(minigameToProcess,
                new Team<>(playerLimit1,
                        minigameToProcess.getProperties().getWinningScore()),
                new Team<>(playerLimit2,
                        minigameToProcess.getProperties().getWinningScore()),
                null);

        nextButtonClicked = new HashSet<>();
    }

    /**
     * Constructs a three-team minigame to process.
     * @param minigameToProcess the draft to process.
     */
    public MiniProcess(MiniGame minigameToProcess) {
        super(minigameToProcess,
                new Team<>(minigameToProcess.getProperties()
                        .getPlayersPerTeam(),
                        minigameToProcess.getProperties().getWinningScore()),
                new Team<>(minigameToProcess.getProperties()
                        .getPlayersPerTeam(),
                        minigameToProcess.getProperties().getWinningScore()),
                new Team<>(minigameToProcess.getProperties()
                        .getPlayersPerTeam(),
                        minigameToProcess.getProperties().getWinningScore()));

        nextButtonClicked = new HashSet<>();
    }

    /** Retrieves the caption ping of the minigame process. */
    @Override
    public String getPing() {
        StringBuilder ping = new StringBuilder();

        ping.append(getRequest().getEmote()).append(" ")
                .append(String.format("*`| %s |`*",
                        getRequest().getProperties().getName()))
                .append("\n");

        for (Map.Entry<String, Player> mapping : getRequest().getPlayers().entrySet()) {
            String id = mapping.getKey();
            Player player = mapping.getValue();

            if (player.isActive()) {
                ping.append(player.getAsMention(id)).append(" ");
            }
        }

        return ping.append(caption()).toString();
    }

    /**
     * Sends the details of the overall team selection with
     * the players of the draft.
     * @param interaction the user interaction calling this method.
     */
    @Override
    public void updateReport(GenericInteractionCreateEvent interaction) {
        getRequest().sendEmbed(interaction, buildSummary(new EmbedBuilder()));
    }

    /**
     * Adds the main buttons to a list of buttons.
     * @param interaction the user interaction calling this method.
     * @param idSuffix the identifying suffix of the button.
     * @param buttons the list of buttons to add to.
     */
    private void addMainButtons(GenericInteractionCreateEvent interaction,
                                String idSuffix, List<Button> buttons) {
        switch (getRequest().getProperties().getGameType()) {
            case RANKED:
            case TURF_WAR:
                buttons.add(Components.ForProcess.plusOne(idSuffix));
                buttons.add(Components.ForProcess.minusOne(idSuffix));
                break;
            default:
                buttons.add(Components.ForProcess.nextTurn(idSuffix));
                break;
        }

        if (interaction != null) {
            buttons.add(Components.ForProcess.draftSubLink(
                    interaction, idSuffix, getRequest()));
        }
    }

    /**
     * Refreshes the process's interface.
     * @param interaction the user interaction calling this method.
     *
     * Note: The interaction must have been acknowledged
     *       before this method.
     */
    @Override
    public void refresh(GenericInteractionCreateEvent interaction) {
        String idSuffix = getRequest().getPrefix().toUpperCase()
                + getRequest().getNumDraft();
        List<Button> buttons = new ArrayList<>();
        resetEndDraftButton();

        addMainButtons(interaction, idSuffix, buttons);
        buttons.add(Components.ForProcess.endDraftProcess(idSuffix));
        buttons.add(Components.ForProcess.refresh(idSuffix));

        if (interaction != null) {
            interaction.getHook().editOriginal(getPing()).setActionRows(
                    ActionRow.of(buttons)).queue();
            updateReport(interaction);
        } else {
            TextChannel channel = getRequest().getDraftChannel();
            channel.sendMessage(getPing()).setEmbeds(
                    buildSummary(new EmbedBuilder()).build()).setActionRows(
                    ActionRow.of(buttons)).queue(
                            message -> {
                                message.pin().queue();
                                getRequest().wait(1000);
                            });
        }
    }

    /**
     * Rotates the minigame to the next turn.
     * @param bc a button click to analyze.
     */
    public void rotateTurns(ButtonClickEvent bc) {
        String authorID = bc.getMember().getId();
        Player author = getRequest().getPlayers().get(authorID);

        if (author == null || !author.isActive()) {
            getRequest().sendReply(bc, "You are not in this draft!", true);
            return;
        } else if (getTeam1().needsPlayers() || getTeam2().needsPlayers()
            || (getTeam3() != null && getTeam3().needsPlayers())) {
            getRequest().sendReply(bc,
                    "Add your subs before continuing (See draft "
                    + "details).", true);
            return;
        }

        nextButtonClicked.add(authorID);
        int numClicksLeft = (getRequest().getCappedSize() / 2) + 1
                - nextButtonClicked.size();

        if (numClicksLeft <= 0) {
            bc.deferEdit().queue();

            rotateTeams();
            incrementTurn(bc);
            resetNextButtonClicked();

            refresh(bc);
        } else {
            String reply = "You need `" + numClicksLeft
                    + "` other player(s) to go to the next turn.";
            getRequest().sendReply(bc, reply, true);
        }
    }

    /**
     * Adjusts the points for players within teams.
     * @param bc a button click to analyze.
     * @param authorID the Discord ID of the player which pressed the button.
     * @param increment True if a point should be added to the author's team.
     *                  False if a point should be deducted from the author's team.
     */
    @Override
    public void changePointsForTeam(ButtonClickEvent bc, String authorID,
                                    boolean increment) {
        String errorMsg = "Add your subs before continuing (See draft "
                + "details).";

        if (attemptedToChangePoints(bc, authorID, increment, errorMsg)) {
            refresh(bc);
        }
    }

    /**
     * Determines if the draft meets the requirements to be ended.
     * @param bc the button click to analyze.
     * @return True if the draft has ended.
     *         False otherwise.
     */
    @Override
    public boolean hasEnded(ButtonClickEvent bc) {
        return super.hasEnded(bc);
    }
}