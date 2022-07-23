package bot.Engine.Games;

import bot.Engine.Profiles.*;
import bot.Tools.Components;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.Random;

/**
 * @author  Wil Aquino
 * Date:    July 20, 2022
 * Project: Libra
 * Module:  Process.java
 * Purpose: Processes a game, via a teams interface.
 *
 * Note:    "Draft" and "game" are used interchangeably here.
 */
public class Process<G extends Game<?, ?, T, P>, T extends Team<P>, P extends Player> {

    /** Flag for checking whether the game has started or not. */
    private boolean started;

    /** The game which is to be processed. */
    private final G game;

    /** The teams of the game. */
    private T team1, team2, team3;

    /** The current turn of the process. */
    private int turn;

    /** The players who have clicked the 'End Draft` button consecutively. */
    private final HashSet<String> endButtonClicked;

    /** The Discord message ID for this draft's teams interface. */
    private String messageID;

    /**
     * Constructs the game to process.
     * @param gameToProcess the game to process.
     * @param newTeam1 the first team of the game.
     * @param newTeam2 the second team of the game.
     */
    public Process(G gameToProcess, T newTeam1, T newTeam2, T newTeam3) {
        started = false;
        game = gameToProcess;

        team1 = newTeam1;
        team2 = newTeam2;
        team3 = newTeam3;

        turn = 0;

        endButtonClicked = new HashSet<>();
    }

    /**
     * Activates or deactivates the draft.
     * @param toStatus the status to set the draft to.
     */
    public void toggle(boolean toStatus) {
        started = toStatus;
    }

    /**
     * Checks whether the game has started or not.
     * @return True if it has started.
     *         False otherwise.
     */
    public boolean hasStarted() {
        return started;
    }

    /** Retrieves the draft's request. */
    public G getRequest() {
        return game;
    }

    /** Retrieves Team 1. */
    public T getTeam1() {
        return team1;
    }

    /** Retrieves Team 2. */
    public T getTeam2() {
        return team2;
    }

    /** Retrieves Team 3. */
    public T getTeam3() {
        return team3;
    }

    /**
     * Moves onto the next turn.
     * @param bc a button click to analyze.
     */
    public void incrementTurn(ButtonClickEvent bc) {
        turn++;

        int rotation = getRequest().getProperties().getRotation();
        if (turn % rotation == 0) {
            randomizeTeams(bc);
        }
    }

    /** Resets who have clicked the 'End Draft' button. */
    public void resetEndDraftButton() {
        endButtonClicked.clear();
    }

    /**
     * Stores the message ID of this teams interface.
     * @param id the ID to store.
     */
    public void setMessageID(String id) {
        messageID = id;
    }

    /** Retrieves the message ID of this teams interface. */
    public String getMessageID() {
        return messageID;
    }

    /** Retrieves the teams interface of the draft. */
    public Message getMessage() {
        MessageChannel channel =
                getRequest().getDraftChannel();
        return channel.retrieveMessageById(getMessageID()).complete();
    }

    /** Retrieves the actual caption of the process. */
    public String caption() {
        StringBuilder text = new StringBuilder();

        text.append("\n\nWelcome to the draft. Jump in a VC and ");
        if (!hasStarted() && !getRequest().getPrefix().equals("fs")) {
            text.append("have the captains alternate choosing teammates in a ")
                    .append("`1-2-1-1-...` pattern. Approve of a maplist ")
                    .append("using `/").append(getRequest().getPrefix()).append(" ")
                    .append("genmaps`, and **begin the draft** before ")
                    .append("playing.\n\n")
                    .append("__If any problems occur with teams, ")
                    .append("captains should reset the teams__! ");
        } else {
            text.append("approve of a maplist using `/")
                    .append(getRequest().getPrefix()).append(" genmaps`. ")
                    .append("Remember you can **check pins** to scroll back ")
                    .append("to this. ");
        }

        text.append("<:Wahoozones:766479174839173200>");
        return text.toString();
    }

    /** Rotates the teams. */
    public void rotateTeams() {
        T temp = getTeam1();

        if (getTeam3() == null) {
            team1 = getTeam2();
        } else {
            team1 = getTeam3();
            team3 = getTeam2();
        }

        team2 = temp;
    }

    /**
     * Gets the team members and formats it into mentionable text.
     * @param team the team to use.
     * @return the team's mentions of the players.
     */
    private String buildTeamString(T team) {
        StringBuilder teamBuilder = new StringBuilder();
        for (Map.Entry<String, P> mapping : team.getPlayers().entrySet()) {
            String playerID = mapping.getKey();
            P player = mapping.getValue();

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
     * Retrieves the details of the overall team selection with
     * the players of the draft.
     * @param eb the pre-built summary to embed the details in.
     */
    public EmbedBuilder buildSummary(EmbedBuilder eb) {
        eb.setTitle(getRequest().getProperties().getName()
                + " Details");
        eb.setColor(getRequest().getColor());

        GameType type = getRequest().getProperties().getGameType();
        if (type.equals(GameType.DRAFT) || type.equals(GameType.RANKED)
                || type.equals(GameType.TURF_WAR)) {
            String score = String.format("%s - %s",
                    getTeam1().getScore(), getTeam2().getScore());
            eb.addField("Score:", score, false);
        }

        int rotation = getRequest().getProperties().getRotation();
        if (getRequest().isInitialized() && !hasStarted()) {
            eb.addField("Status:", "CHOOSING TEAMS", false);
        } else if (getRequest().isInitialized() && hasStarted()) {
            if (rotation != 0) {
                eb.addField("Status:", String.format("`Turn %s/%s`",
                        (turn % (rotation)) + 1, rotation), false);
            } else {
                eb.addField("Status:", "IN PROGRESS", false);
            }
        } else {
            eb.addField("Status:", "FINISHED", false);
        }

        switch (type) {
            case DRAFT:
            case RANKED:
            case TURF_WAR:
                eb.addField("Team 1:", buildTeamString(getTeam1()), false);
                eb.addField("Team 2:", buildTeamString(getTeam2()), false);
                break;
            case HIDE_AND_SEEK:
                eb.addField("Hiders:", buildTeamString(getTeam1()), false);
                eb.addField("Seekers:", buildTeamString(getTeam2()), false);
                break;
            case JUGGERNAUT:
                eb.addField("Juggernauts:", buildTeamString(getTeam1()), false);
                eb.addField("Hunters:", buildTeamString(getTeam2())
                        + buildTeamString(getTeam3()), false);
                break;
            default:
                eb.addField("Rushers:", buildTeamString(getTeam1()), false);
                eb.addField("Chargers:", buildTeamString(getTeam2()), false);
                break;
        }

        return eb;
    }

    /**
     * Balances a list of player IDs where the Support and Anchor
     * players are prioritized first.
     * @param database a player database map to access.
     * @return the balanced list.
     */
    private List<String> getBalancedPlayerList(
            TreeMap<Object, Object> database) {
        List<String> supportsAnchors = new ArrayList<>();
        List<String> otherPlayers = new ArrayList<>();

        for (String id : getRequest().getPlayers().keySet()) {
            PlayerInfo profile = (PlayerInfo) database.get(id);
            if (profile != null && (profile.getPlaystyle().equals("Support")
                    || profile.getPlaystyle().equals("Anchor"))) {
                supportsAnchors.add(id);
            } else {
                otherPlayers.add(id);
            }
        }

        Collections.shuffle(supportsAnchors, new Random());
        Collections.shuffle(otherPlayers, new Random());

        List<String> balancedList = new ArrayList<>(
                supportsAnchors.size() + otherPlayers.size());
        balancedList.addAll(supportsAnchors);
        balancedList.addAll(otherPlayers);

        return balancedList;
    }

    /**
     * Randomize the teams, but balance them.
     * @param bc a button click to analyze.
     */
    public void randomizeBalancedTeams(ButtonClickEvent bc) {
        Profile profiles = new Profile();
        TreeMap<Object, Object> database =
                profiles.onlyGetDatabase(bc, null);

        getTeam1().clear();
        getTeam2().clear();

        int anchorFound = 0;
        int supportFound = 0;

        for (String id : getBalancedPlayerList(database)) {
            P player = getRequest().getPlayers().get(id);
            PlayerInfo profile = (PlayerInfo) database.get(id);

            if (profile != null && profile.getPlaystyle().equals("Anchor")) {
                if (getTeam1().needsPlayers() && anchorFound % 2 == 0) {
                    getTeam1().add(id, player);
                } else if (getTeam2().needsPlayers()) {
                    getTeam2().add(id, player);
                }

                anchorFound++;
            } else if (profile != null && profile.getPlaystyle().equals("Support")) {
                if (getTeam1().needsPlayers() && supportFound % 2 == 0) {
                    getTeam1().add(id, player);
                } else {
                    getTeam2().add(id, player);
                }

                supportFound++;
            } else if (getTeam1().needsPlayers()) {
                getTeam1().add(id, player);
            } else {
                getTeam2().add(id, player);
            }
        }
    }

    /**
     * Randomizes a list of player IDs.
     * @return the randomized list.
     */
    private List<String> getRandomPlayerList() {
        List<String> randomList = new ArrayList<>
                (getRequest().getPlayers().keySet());
        Collections.shuffle(randomList, new Random());

        return randomList;
    }

    /** Completely randomize the teams. */
    public void randomizeUnbalancedTeams() {
        getTeam1().clear();
        getTeam2().clear();
        if (getTeam3() != null) {
            getTeam3().clear();
        }

        for (String id : getRandomPlayerList()) {
            P player = getRequest().getPlayers().get(id);

            if (!player.isActive()) {
                continue;
            } else if (getTeam1().needsPlayers()) {
                getTeam1().add(id, player);
            } else if (getTeam2().needsPlayers()) {
                getTeam2().add(id, player);
            } else {
                getTeam3().add(id, player);
            }
        }
    }

    /**
     * Completely randomize the teams.
     * @param bc a button click to analyze.
     */
    public void randomizeTeams(ButtonClickEvent bc) {
        GameType type = getRequest().getProperties().getGameType();
        if (type.equals(GameType.RANKED)|| type.equals(GameType.TURF_WAR)) {
            randomizeBalancedTeams(bc);
        } else {
            randomizeUnbalancedTeams();
        }
    }

    /**
     * Adds points to teams.
     * @param team the winning team.
     */
    private void givePoints(ButtonClickEvent bc, T team) {
        if (team.hasMaximumScore()) {
            getRequest().sendResponse(bc,
                    "You have already hit the point limit!", true);
        } else {
            team.incrementScore();
        }
    }

    /**
     * Subtracts points from teams.
     * @param team the "winning" team.
     */
    private void revertPoints(ButtonClickEvent bc, T team) {
        if (team.hasMinimumScore()) {
            getRequest().sendResponse(bc, "You cannot have less than zero points!", true);
        } else {
            team.decrementScore();
        }
    }

    /**
     * Adjusts the points for players within a team.
     * @param bc a button click to analyze.
     * @param team the specific team to adjust the points of.
     * @param increment True if a point should be added to the author's team.
     *                  False if a point should be deducted from the author's team.
     */
    private void changePoints(ButtonClickEvent bc, T team, boolean increment) {
        if (increment) {
            givePoints(bc, team);
        } else {
            revertPoints(bc, team);
        }
    }

    /**
     * Adjusts the points for players within teams.
     * @param bc a button click to analyze.
     * @param authorID the Discord ID of the player which pressed the button.
     * @param increment True if a point should be added to the author's team.
     *                  False if a point should be deducted from the author's team.
     */
    public boolean attemptedToChangePoints(ButtonClickEvent bc, String authorID,
                                           boolean increment, String errorMsg) {
        bc.deferEdit().queue();
        resetEndDraftButton();

        P author = getRequest().getPlayers().get(authorID);
        if (author == null || !author.isActive()) {
            getRequest().sendResponse(bc,
                    "You are not in this draft!", true);
            return false;
        } else if (getTeam1().needsPlayers() || getTeam2().needsPlayers()) {
            getRequest().sendResponse(bc, errorMsg, true);
        } else if (getTeam1().contains(authorID)) {
            changePoints(bc, getTeam1(), increment);
        } else {
            changePoints(bc, getTeam2(), increment);
        }

        return true;
    }

    /**
     * Determines if the game meets the requirements to be ended.
     * @param bc the button click to analyze.
     * @return True if the draft has ended.
     *         False otherwise.
     */
    public boolean hasEnded(ButtonClickEvent bc) {
        String authorID = bc.getMember().getId();
        setMessageID(bc.getMessageId());

        P foundAuthor = getRequest().getPlayers().get(authorID);
        if (foundAuthor == null || !foundAuthor.isActive()) {
            getRequest().sendReply(bc, "You are not in the draft!", true);
            return false;
        }

        endButtonClicked.add(authorID);
        int numClicksLeft = getRequest().getProperties().getMaximumPlayersToEnd()
                - endButtonClicked.size();
        if (hasStarted()) {
            numClicksLeft = getRequest().getProperties().getMinimumPlayersToEnd()
                    - endButtonClicked.size();
        }

        if (numClicksLeft <= 0) {
            bc.deferEdit().queue();
            getRequest().toggle(false);

            if (hasStarted()) {
                getRequest().unpinDraftChannelPins();

                String idSuffix = getRequest().getPrefix()
                        + getRequest().getNumDraft();
                List<Button> buttons = new ArrayList<>();
                buttons.add(Components.ForProcess.endDraftProcess(idSuffix)
                        .withStyle(ButtonStyle.SECONDARY).asDisabled());

                getRequest().sendButtons(bc, "This draft has ended.", buttons);
                getRequest().getMessage(bc).editMessage("This draft has ended.")
                        .setActionRow(Components.ForDraft.refresh(idSuffix)
                                .asDisabled()).queue();

                getRequest().getDraftChannel().sendMessage(
                        "Ggs! Thanks for playing (:").queue();
                getRequest().log("A " + getRequest().getSection()
                        + " draft was finished.", false);
            } else {
                getRequest().unpinDraftChannelPins();
                getMessage().delete().queue();

                String idSuffix = getRequest().getPrefix()
                        + getRequest().getNumDraft();

                getRequest().getDraftChannel().sendMessage(
                        "The draft has ended. Sorry about the early stop! "
                                + "Feel free to request a new one!").queue();
                getRequest().getMessage(bc).editMessage("This draft ended early.")
                        .setActionRow(Components.ForDraft.refresh(idSuffix)
                                .asDisabled()).queue();

                getRequest().log("A " + getRequest().getSection()
                        + " draft was ended before it started.", false);
            }

            return true;
        } else {
            String reply = "You need `" + numClicksLeft
                    + "` other player(s) to end the draft.";
            getRequest().sendReply(bc, reply, true);

            return false;
        }
    }
}
