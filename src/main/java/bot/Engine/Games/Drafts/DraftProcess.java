package bot.Engine.Drafts;

import bot.Engine.Games.Process;
import bot.Engine.Cycles.AutoLog;
import bot.Engine.Templates.ProcessReqs;
import bot.Tools.Components;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author  Wil Aquino, Turtle#1504
 * Date:    January 16, 2022
 * Project: Libra
 * Module:  DraftProcess.java
 * Purpose: Processes a draft, via a Teams interface.
 */
public class DraftProcess extends Process<DraftGame, DraftTeam, DraftPlayer>
        implements ProcessReqs {

    /**
     * Constructs the draft to process.
     * @param draftToProcess the draft to process.
     */
    public DraftProcess(DraftGame draftToProcess) {
        super(draftToProcess,
                new DraftTeam(
                        draftToProcess.getProperties().getMaximumPlayersToStart() / 2,
                        draftToProcess.getProperties().getWinningScore()),
                new DraftTeam(
                        draftToProcess.getProperties().getMaximumPlayersToStart() / 2,
                        draftToProcess.getProperties().getWinningScore()));

        getTeam1().setOpponents(getTeam2());
        getTeam2().setOpponents(getTeam1());
    }

    /** Retrieves the caption ping of the draft process. */
    @Override
    public String getPing() {
        StringBuilder ping = new StringBuilder();
        TreeMap<Integer, String> captainIDs = getRequest().determineCaptains(null);
        DraftPlayer captain1 = getRequest().getPlayers().get(captainIDs.get(1));
        DraftPlayer captain2 = getRequest().getPlayers().get(captainIDs.get(2));

        ping.append(getRequest().getEmote()).append(" ");
        if (!hasStarted()) {
            ping.append(String.format("*`| captain 1 - %s | captain 2 - %s |`*",
                            captain1.getName(),
                            captain2.getName()))
                    .append("\n");
        }

        for (Map.Entry<String, DraftPlayer> mapping : getRequest().getPlayers().entrySet()) {
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

        return ping.append(caption()).toString();
    }

    /**
     * Sends the details of the overall team selection with
     * the players of the draft.
     * @param interaction the user interaction calling this method.
     */
    @Override
    public void updateReport(GenericInteractionCreateEvent interaction) {
        EmbedBuilder eb = new EmbedBuilder();

        String score = String.format("%s - %s",
                getTeam1().getScore(), getTeam2().getScore());
        eb.addField("Score:", score, false);

        getRequest().sendEmbed(interaction, buildSummary(eb));
    }

    /**
     * Refreshes the process's interface.
     * @param interaction the user interaction calling this method.
     */
    @Override
    public void refresh(GenericInteractionCreateEvent interaction) {
        String idSuffix = getRequest().getPrefix().toUpperCase()
                + getRequest().getNumDraft();
        List<SelectionMenu> menus = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();
        resetEndDraftButton();

        menus.add(Components.ForDraftProcess.teamSelectionMenu(
                idSuffix, getRequest().getPlayers()));
        if (hasStarted()) {
            buttons.add(Components.ForDraftProcess.plusOne(idSuffix));
            buttons.add(Components.ForDraftProcess.minusOne(idSuffix));
            buttons.add(Components.ForDraftProcess.draftSubLink(
                    interaction, idSuffix, getRequest()));
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
            TextChannel channel = getRequest().getDraftChannel();
            channel.sendMessage(getPing()).setActionRows(
                    ActionRow.of(menus), ActionRow.of(buttons)).queue(
                            message -> {
                                message.pin().queue();
                                getRequest().wait(1000);
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
            getRequest().sendResponse(sm,
                    "Your team doesn't need players right now!", true);
        }
    }

    /**
     * Adds a player to a team.
     * @param sm a menu selection to analyze.
     */
    public void addPlayerToTeam(SelectionMenuEvent sm) {
        resetEndDraftButton();
        setMessageID(sm.getMessageId());

        String authorID = sm.getMember().getId();
        DraftPlayer author = getRequest().getPlayers().get(authorID);

        SelectOption chosenPlayer = sm.getInteraction().getSelectedOptions().get(0);
        String playerName = chosenPlayer.getLabel();
        String playerID = chosenPlayer.getValue();


        getRequest().getPlayers().get("440059670170959874").setCaptainForTeam1(true);
        getTeam1().clear();
        getTeam2().clear();
        getTeam2().add("440059670170959874", getRequest().getPlayers().get("440059670170959874"));
        getTeam1().add("140942181023219713", getRequest().getPlayers().get("140942181023219713"));
        getTeam1().add("350386286256848896", getRequest().getPlayers().get("350386286256848896"));
        getTeam2().add("455295354318094347", getRequest().getPlayers().get("455295354318094347"));
        getTeam2().add("422790410566500352", getRequest().getPlayers().get("422790410566500352"));
        getTeam1().add("392468487857111044", getRequest().getPlayers().get("392468487857111044"));
        getTeam1().add("191016647543357440", getRequest().getPlayers().get("191016647543357440"));


        if (author == null) {
            getRequest().sendReply(sm, "You are not in this draft!", true);
        } else if (!hasStarted() && !author.isCaptainForTeam1()
            && !author.isCaptainForTeam2()) {
            getRequest().sendReply(sm, "Only captains can choose players.", true);
        } else if (playerName.equals("<end>")) {
            getRequest().sendReply(sm,
                    "That is the end of the list. Refresh if needed.", true);
        } else {
            sm.deferEdit().queue();
            DraftPlayer foundPlayer = getRequest().getPlayers().get(playerID);
            determineTeam(sm, authorID, playerID, foundPlayer);

            refresh(sm);
        }
    }

    /**
     * Resets the teams list.
     * @param bc a button click to analyze.
     */
    public void resetTeams(ButtonClickEvent bc) {
        DraftPlayer author = getRequest().getPlayers().get(bc.getMember().getId());
        resetEndDraftButton();

        if (author == null) {
            getRequest().sendReply(bc, "You are not in this draft!", true);
        } else if (!author.isCaptainForTeam1() && !author.isCaptainForTeam2()) {
            getRequest().sendReply(bc, "Only captains can reset the teams.", true);
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
        DraftPlayer author = getRequest().getPlayers().get(bc.getMember().getId());
        resetEndDraftButton();

        if (author == null) {
            getRequest().sendReply(bc, "You are not in this draft!", true);
        } else if (!author.isCaptainForTeam1() && !author.isCaptainForTeam2()) {
            getRequest().sendReply(bc, "Only captains can start the draft.", true);
        } else if (getTeam1().needsPlayers() || getTeam2().needsPlayers()) {
            getRequest().sendReply(bc, "Not everyone is in the draft yet.", true);
        } else {
            bc.deferEdit().queue();

            toggle(true);
            refresh(bc);
        }
    }

    /**
     * Adds points to teams.
     * @param team the winning team.
     */
    private void givePoints(ButtonClickEvent bc, DraftTeam team) {
        if (team.hasMaximumScore()) {
            getRequest().sendResponse(bc,"You have already hit the point limit!", true);
        } else {
            team.incrementScore();
        }
    }

    /**
     * Subtracts points from teams.
     * @param team the "winning" team.
     */
    private void revertPoints(ButtonClickEvent bc, DraftTeam team) {
        if (team.hasMinimumScore()) {
            getRequest().sendResponse(bc, "You cannot have less than zero points!", true);
        } else {
            team.decrementScore();
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

        DraftTeam team;
        DraftPlayer author = getRequest().getPlayers().get(authorID);
        if (author == null || !author.isActive()) {
            getRequest().sendResponse(bc,
                    "You are not in this draft!", true);
            return;
        } else if (getTeam1().needsPlayers() || getTeam2().needsPlayers()) {
            getRequest().sendResponse(bc,
                    "Add your subs before continuing (Check the "
                            + "the selection menu).", true);
            refresh(bc);
            return;
        } else if (getTeam1().contains(authorID)) {
            team = getTeam1();
        } else {
            team = getTeam2();
        }

        if (increment) {
            givePoints(bc, team);
        } else {
            revertPoints(bc, team);
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
        if (super.hasEnded(bc)) {
            if (hasStarted()) {
                updateReport(bc);
                new AutoLog(getRequest().getPrefix()).matchReport(
                        bc, getRequest());
            }

            return true;
        } else {
            return false;
        }
    }
}