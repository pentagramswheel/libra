package bot.Engine.Drafts;

import bot.Engine.Games.Game;
import bot.Engine.Games.GameType;
import bot.Engine.Profiles.PlayerInfo;
import bot.Engine.Profiles.Profile;
import bot.Events;
import bot.Tools.Components;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.TreeMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * @author  Wil Aquino, Turtle#1504
 * Date:    December 6, 2021
 * Project: Libra
 * Module:  DraftGame.java
 * Purpose: Formalizes and starts a draft, via a request interface.
 */
public class DraftGame extends Game<DraftGame, DraftProcess, DraftTeam, DraftPlayer> {

//    /** The formal process for executing this draft. */
//    private DraftProcess draftProcess;

    /**
     * Constructs a draft template and initializes the
     * draft start attributes.
     * @param sc the user's inputted command.
     * @param draft the numbered draft that this draft is.
     * @param abbreviation the abbreviation of the section.
     * @param initialPlayer the first player of the draft.
     */
    public DraftGame(SlashCommandEvent sc, int draft,
                 String abbreviation, Member initialPlayer) {
        super(sc, GameType.RANKED, draft, abbreviation);

        DraftPlayer newPlayer = new DraftPlayer(
                initialPlayer.getEffectiveName(),
                getProperties().getWinningScore(), false);
        getPlayers().put(initialPlayer.getId(), newPlayer);
    }

//    /** Retrieves the field for executing the draft. */
//    public DraftProcess getProcess() {
//        return draftProcess;
//    }

//    /**
//     * Checks if the draft has formally started.
//     * @return True if it has formally started.
//     *         False otherwise.
//     */
//    private boolean draftStarted() {
//        return isInitialized() && getProcess().hasStarted();
//    }

    /**
     * Sends a draft confirmation summary with all players of the draft.
     * @param interaction the user interaction calling this method.
     */
    @Override
    public void updateReport(GenericInteractionCreateEvent interaction) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Draft Queue " + getNumDraft());

        StringBuilder players = new StringBuilder();
        StringBuilder subs = new StringBuilder();

        for (Map.Entry<String, DraftPlayer> mapping : getPlayers().entrySet()) {
            String id = mapping.getKey();
            DraftPlayer player = mapping.getValue();
            boolean isCaptain = player.isCaptainForTeam1()
                    || player.isCaptainForTeam2();

            if (!player.isActive()) {
                subs.append(player.getAsMention(id))
                        .append(" (inactive)").append("\n");
            } else if (player.isSub()) {
                subs.append(player.getAsMention(id)).append("\n");
            } else if (isCaptain) {
                players.append(player.getAsMention(id))
                        .append(" (captain)").append("\n");
            } else {
                players.append(player.getAsMention(id)).append("\n");
            }
        }

        sendEmbed(interaction, buildEmbed(eb, players, subs));
    }

//    /** Formats a request ping for gathering players. */
//    private String newPing() {
//        int activePlayers = players.size() - numInactive;
//        int pingsLeft = NUM_PLAYERS_TO_START_DRAFT - activePlayers;
//        if (draftStarted()) {
//            pingsLeft = 0;
//        }
//
//        return getSectionRole() + " +" + pingsLeft;
//    }

    /**
     * Determines the captains of the draft.
     * @param oldCaptainID the Discord ID of a previous captain.
     * @return the captains of the draft.
     */
    public TreeMap<Integer, String> determineCaptains(String oldCaptainID) {
        List<String> ids = new ArrayList<>(getPlayers().keySet());
        if (oldCaptainID != null) {
            ids.remove(oldCaptainID);
        }

        TreeMap<Integer, String> captainIDs = new TreeMap<>();
        int numCaptains = 0;

        if (isInitialized()) {
            for (Map.Entry<String, DraftPlayer> mapping : getPlayers().entrySet()) {
                String playerID = mapping.getKey();
                DraftPlayer player = mapping.getValue();

                if (player.isCaptainForTeam1()) {
                    captainIDs.put(1, playerID);
                    numCaptains++;
                } else if (player.isCaptainForTeam2()) {
                    captainIDs.put(2, playerID);
                    numCaptains++;
                }
            }
        }

        while (numCaptains < 2) {
            int size = ids.size();
            String randomID = ids.get(Events.RANDOM_GENERATOR.nextInt(size));
            DraftPlayer randomPlayer = getPlayers().get(randomID);
            boolean isCaptain = randomPlayer.isCaptainForTeam1()
                    || randomPlayer.isCaptainForTeam2();

            if (!randomPlayer.isSub() && !isCaptain) {
                if (captainIDs.get(1) == null) {
                    randomPlayer.setCaptainForTeam1(true);
                    captainIDs.put(1, randomID);
                } else if (captainIDs.get(2) == null) {
                    randomPlayer.setCaptainForTeam2(true);
                    captainIDs.put(2, randomID);
                }

                numCaptains++;
            }
        }

        return captainIDs;
    }

    /**
     * Attempts to start a draft.
     * @param bc a button click to analyze.
     */
    public void attemptDraft(ButtonClickEvent bc) {
        String playerID = bc.getMember().getId();

        if (inWrongSection(bc)) {
            sendReply(bc, "You don't have access to this section's drafts!", true);
            return;
        } else if (getPlayers().containsKey(playerID)) {
            sendReply(bc, "You are already in this draft!", true);
            return;
        } else if (!getHistory().contains(playerID)) {
            if (getPlayers().size()
                    == getProperties().getMaximumPlayersToStart() - 2) {
                getWatch().timerOneAdd(10);
            } else if (getPlayers().size()
                    >= (getProperties().getMaximumPlayersToStart() / 2) - 1) {
                getWatch().timerOneAdd(5);
            }
        }

        getPlayers().put(playerID, new DraftPlayer(
                bc.getMember().getEffectiveName(),
                getProperties().getWinningScore(), false));
        getHistory().add(playerID);

        if (getPlayers().size() == getProperties().getMaximumPlayersToStart()) {
            setProcess(new DraftProcess(this));
            List<Button> buttons = new ArrayList<>();

            String idSuffix = getPrefix().toUpperCase() + getNumDraft();

            buttons.add(Components.ForDraft.reassignCaptain(idSuffix));
            buttons.add(Components.ForDraft.requestSub(idSuffix));
            buttons.add(Components.ForDraft.joinAsSub(idSuffix));
            buttons.add(Components.ForDraft.refresh(idSuffix));

            sendButtons(bc, bc.getInteraction().getMessage().getContentRaw(),
                    buttons);
            determineCaptains(null);
            toggle(true);
            refresh(bc);

            getProcess().refresh(null);

            List<MessageEmbed> profiles = new Profile().viewMultiple(bc,
                    getPlayers().keySet(), "Their", false, true, false);
            if (profiles != null) {
                getDraftChannel().sendMessageEmbeds(profiles).queue();
            }
        } else {
            refresh(bc);
            setMessageID(bc.getMessageId());
        }
    }

    /**
     * Reassigns a captain of the draft, via a button.
     * @param bc a button click to analyze.
     */
    public void reassignCaptain(ButtonClickEvent bc) {
        String authorID = bc.getMember().getId();
        DraftPlayer author = getPlayers().get(authorID);

        if (author == null) {
            sendReply(bc, "You are not in this draft!", true);
            return;
        } else if (!author.isCaptainForTeam1() && !author.isCaptainForTeam2()) {
            sendReply(bc, "Only captains can reassign themselves.", true);
            return;
        } else if (author.isCaptainForTeam1()){
            author.setCaptainForTeam1(false);
            getProcess().getTeam1().clear();
        } else {
            author.setCaptainForTeam2(false);
            getProcess().getTeam2().clear();
        }

        determineCaptains(authorID);
        updateReport(bc);
        refresh(bc);
    }

//    /**
//     * Repings for remaining players.
//     * @param bc a button click to analyze.
//     */
//    public void reping(ButtonClickEvent bc) {
//        String authorID = bc.getMember().getId();
//        setMessageID(bc.getMessageId());
//
//        if (!getPlayers().containsKey(authorID)) {
//            sendReply(bc, "You are not in this draft!", true);
//        } else if (!getWatch().timerTwoExpired()) {
//            sendReply(bc, String.format("Wait until %s to reping!",
//                    DiscordWatch.discordTime(getWatch().getTimerTwoEnd())),
//                    true);
//        } else if (getProperties().getMaximumPlayersToStart() - getPlayers().size()
//                > (getProperties().getMaximumPlayersToStart() / 2) + 1) {
//            int numPlayersLeft =
//                    (getProperties().getMaximumPlayersToStart() / 2) + 1;
//            sendReply(bc,
//                    String.format("Reping only when you need +%s or less!",
//                            numPlayersLeft), true);
//        } else {
//            bc.deferEdit().queue();
//
//            String idSuffix = getPrefix().toUpperCase() + getNumDraft();
//            bc.editButton(
//                    Components.ForDraft.reping(idSuffix).asDisabled()).queue();
//            sendResponse(bc, newPing() + " (reping)", false);
//        }
//    }

//    /**
//     * Removes a player from the draft, if possible, via a button.
//     * @param bc a button click to analyze.
//     */
//    public void removeFromQueue(ButtonClickEvent bc) {
//        String playerID = bc.getMember().getId();
//        setMessageID(bc.getMessageId());
//
//        if (!getPlayers().containsKey(playerID)) {
//            sendReply(bc, "You are not in this draft!", true);
//        } else {
//            getPlayers().remove(playerID);
//            refresh(bc);
//        }
//    }

//    /**
//     * Removes a player from the draft, if possible, via a button.
//     * @param bc a button click to analyze.
//     */
//    public void removeFromQueue(ButtonClickEvent bc) {
//        if (canRemoveFromQueue(bc)) {
//            refresh(bc);
//        }
//    }

//    /**
//     * Attempts to sub out a player from the draft.
//     * @param interaction the user interaction calling this method.
//     * @param playerID the Discord ID of the player.
//     * @param player the player to sub out.
//     * @param notFoundString a string to output if the player could not be found.
//     * @param subbedTwiceString a string to output if the player has already
//     *                          been subbed out from the draft.
//     * @return True if the substitution was successful.
//     *         False otherwise.
//     */
//    private boolean subOut(GenericInteractionCreateEvent interaction,
//                        String playerID, DraftPlayer player,
//                        String notFoundString, String subbedTwiceString) {
//        if (player == null) {
//            sendResponse(interaction, notFoundString, true);
//            return false;
//        } else if (!player.isActive()) {
//            sendResponse(interaction, subbedTwiceString, true);
//            return false;
//        } else if (teamOneContains(playerID)) {
//            getProcess().getTeam1().requestSub();
//        } else if (teamTwoContains(playerID)) {
//            getProcess().getTeam2().requestSub();
//        }
//
//        player.setSubStatus(true);
//        player.setActiveStatus(false);
//        player.incrementSubs();
//
//        if (!draftStarted()) {
//            player.setCaptainForTeam1(false);
//            player.setCaptainForTeam2(false);
//            determineCaptains(playerID);
//        }
//
//        numInactive++;
//
//        return true;
//    }

    /**
     * Resets any captains if a captain was subbed out.
     * @param oldCaptainID the Discord ID of the former captain.
     */
    private void resetCaptainsIfNeeded(String oldCaptainID) {
        DraftPlayer oldCaptain = getPlayers().get(oldCaptainID);

        oldCaptain.setCaptainForTeam1(false);
        oldCaptain.setCaptainForTeam2(false);
        determineCaptains(oldCaptainID);
    }

    /**
     * Requests a sub for a draft, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void requestSub(ButtonClickEvent bc) {
        if (canRequestSub(bc)) {
            resetCaptainsIfNeeded(bc.getMember().getId());
        }
    }

    /**
     * Forcibly subs a person out of the draft.
     * @param sc a slash command to analyze.
     * @param playerID the Discord ID of the player to sub.
     */
    public void forceSub(SlashCommandEvent sc, String playerID) {
        if (canForceSub(sc, playerID) && !draftStarted()) {
            resetCaptainsIfNeeded(playerID);
        }
    }

    /**
     * Adds a player to the draft's subs, if possible, via a button.
     * @param bc a button click to analyze.
     */
    public void addSub(ButtonClickEvent bc) {
        String id = bc.getMember().getId();
        String name = bc.getMember().getEffectiveName();
        int activePlayers = getPlayers().size() - getNumInactive();

        if (inWrongSection(bc)) {
            sendReply(bc, "You don't have access to this section's drafts!", true);
        } else if (activePlayers == getProperties().getMaximumPlayersToStart()) {
            sendReply(bc, "This draft doesn't need subs right now.", true);
        } else if (canSubIn(bc, id)) {
            if (!getPlayers().containsKey(id)) {
                getPlayers().put(id, new DraftPlayer(name,
                        getProperties().getWinningScore(), draftStarted()));
            }
            refresh(bc);
        }
    }

//    /**
//     * Forcibly ends the draft.
//     * @param sc a slash command to analyze.
//     * @return True if the draft was forcibly ended.
//     *         False otherwise.
//     */
//    public boolean canForceEnd(SlashCommandEvent sc) {
//        if (isInitialized() && getProcess().getMessageID() == null) {
//            sendReply(sc, "Press the `End Draft` button in "
//                    + getDraftChannel().getAsMention() + " "
//                    + "once then try again.", true);
//            return false;
//        } else if (super.canForceEnd(sc)) {
//            if (isInitialized()) {
//                getProcess().getMessage().delete().queue();
//            }
//
//            return true;
//        }
//
//        return false;
//    }
}
