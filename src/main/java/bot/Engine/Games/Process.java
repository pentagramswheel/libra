package bot.Engine.Games;

import bot.Tools.Components;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

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
    private final T team1, team2;

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
    public Process(G gameToProcess, T newTeam1, T newTeam2) {
        started = false;
        game = gameToProcess;

        team1 = newTeam1;
        team2 = newTeam2;

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

    /**
     * Checks whether this draft is from Freshwater Shoals or not.
     * @return True if it is from LaunchPoint or Ink Odyssey.
     *         False if it is from Freshwater Shoals.
     *
     */
    private boolean notFS() {
        return !getRequest().getPrefix().equals("fs");
    }

    /** Retrieves the actual caption of the process. */
    public String caption() {
        StringBuilder text = new StringBuilder();

        text.append("\n\nWelcome to the draft. Jump in a VC and ");
        if (notFS() && !hasStarted()) {
            text.append("have the captains alternate choosing teammates in a ")
                    .append("`1-2-1-1-...` pattern. Approve of a maplist ")
                    .append("using `/").append(getRequest().getPrefix()).append(" ")
                    .append("genmaps`, and **begin the draft** before ")
                    .append("playing.\n\n");
            if (notFS()) {
                    text.append(" __If any problems occur with teams, ")
                        .append("captains should reset the teams__! ");
            }
        } else {
            text.append("make sure everyone is on a team before continuing! ")
                    .append("Remember you can **check pins** to scroll back ")
                    .append("to this. ");
        }

        text.append("<:Wahoozones:766479174839173200>");
        return text.toString();
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
     * @param eb a pre-built embed to send the details in.
     */
    public EmbedBuilder buildSummary(EmbedBuilder eb) {
        eb.setTitle(getRequest().getProperties().getName()
                + " Details");
        eb.setColor(getRequest().getColor());

        if (getRequest().isInitialized() && !hasStarted()) {
            eb.addField("Status:", "CHOOSING TEAMS", false);
        } else if (getRequest().isInitialized() && hasStarted()) {
            eb.addField("Status:", "IN PROGRESS", false);
        } else {
            eb.addField("Status:", "FINISHED", false);
        }

        eb.addField("Team 1:", buildTeamString(getTeam1()), false);
        eb.addField("Team 2:", buildTeamString(getTeam2()), false);

        return eb;
    }

//    public List<MessageEmbed> initializeTeams(TreeMap<String, PlayerInfo> profiles) {
//        Set<MessageEmbed> profileEmbeds = new HashSet<>();
//
//        List<String> ids = new ArrayList<>(profiles.keySet());
//        List<PlayerInfo> infoList = new ArrayList<>(profiles.values());
//
//        if (notFS()) {
////            for (PlayerInfo profile : profiles.values()) {
////                profileEmbeds.add(profile.getEmbed());
////            }
//        } else {
//            int anchorFound = 0;
//            while (getTeam1().needsPlayers() && getTeam2().needsPlayers()) {
//                int i = Events.RANDOM_GENERATOR.nextInt(ids.size());
//                String currID = ids.remove(i);
//                PlayerInfo currInfo = infoList.remove(i);
//
//                if (currInfo.getPlaystyle() != null
//                        && currInfo.getPlaystyle().equals("Anchor")) {
//                    int team = anchorFound % 2;
//                    if (team == 0 && getTeam1().needsPlayers()) {
//                        getTeam1().add(currID, getRequest().getPlayers().get(currID));
//                    } else {
//                        getTeam2().add(currID, getRequest().getPlayers().get(currID));
//                    }
//
//                    anchorFound++;
//                } else if (getTeam1().needsPlayers()) {
//                    getTeam1().add(currID, getRequest().getPlayers().get(currID));
//                } else {
//                    getTeam2().add(currID, getRequest().getPlayers().get(currID));
//                }
//
//            }
//        }
//
//        return new ArrayList<>(profileEmbeds);
//    }

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
