package bot.Engine.Games.Drafts;

import bot.Engine.Games.Game;
import bot.Engine.Games.GameProperties;
import bot.Engine.Games.GameType;
import bot.Engine.Games.Player;
import bot.Engine.Profiles.Profile;
import bot.Engine.Templates.GameReqs;
import bot.Engine.Templates.ProcessReqs;
import bot.Events;
import bot.Tools.Components;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
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
public class DraftGame extends Game<DraftGame, DraftProcess, DraftTeam, DraftPlayer>
        implements GameReqs {

    /**
     * Constructs a draft and initializes the draft start attributes.
     * @param sc the user's inputted command.
     * @param draft the numbered draft that this draft is.
     * @param abbreviation the abbreviation of the section.
     * @param initialPlayer the first player of the draft queue.
     */
    public DraftGame(SlashCommandEvent sc, int draft,
                 String abbreviation, Member initialPlayer) {
        super(sc, GameType.DRAFT, draft, abbreviation);

        String playerID = initialPlayer.getId();
        getPlayers().put(playerID, new DraftPlayer(
                initialPlayer.getEffectiveName(),
                getProperties().getWinningScore(), false));
        getHistory().add(playerID);
    }

    /**
     * Checks whether the request has been satisfied or not.
     * @return True if eight players have joined the queue.
     *         False otherwise.
     */
    @Override
    public boolean isInitialized() {
        return super.isInitialized();
    }

    /** Retrieves the draft's properties. */
    @Override
    public GameProperties getProperties() {
        return super.getProperties();
    }

    /** Retrieves the players of the draft. */
    @Override
    public TreeMap<String, DraftPlayer> getPlayers() {
        return super.getPlayers();
    }

    /**
     * Retrieves the request interface of the draft.
     * @param interaction the user interaction calling this method.
     */
    @Override
    public Message getMessage(GenericInteractionCreateEvent interaction) {
        return super.getMessage(interaction);
    }

    /** Retrieves the respective draft chat channel. */
    @Override
    public TextChannel getDraftChannel() {
        return super.getDraftChannel();
    }

    /**
     * Checks whether the request has timed out or not.
     * @param interaction the user interaction calling this method.
     * @return True if the request expired.
     *         False otherwise.
     */
    @Override
    public boolean timedOut(GenericInteractionCreateEvent interaction) {
        return super.timedOut(interaction);
    }

    /** Retrieves the field for executing the draft. */
    @Override
    public DraftProcess getProcess() {
        return super.getProcess();
    }

    /**
     * Sends a draft confirmation summary with all players of the draft.
     * @param interaction the user interaction calling this method.
     */
    @Override
    public void updateReport(GenericInteractionCreateEvent interaction) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(getProperties().getName() + " Queue " + getNumDraft());

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
    @Override
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
            wait(4000);

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
     * Repings for remaining players.
     * @param bc a button click to analyze.
     */
    @Override
    public void reping(ButtonClickEvent bc) {
        super.reping(bc);
    }

    /**
     * Removes a player from the draft queue.
     * @param bc a button click to analyze.
     */
    @Override
    public void removeFromQueue(ButtonClickEvent bc) {
        super.removeFromQueue(bc);
    }

    /**
     * Refreshes the draft request's caption.
     * @param bc a button click to analyze.
     */
    @Override
    public void refresh(ButtonClickEvent bc) {
        super.refresh(bc);
    }

    /**
     * Reassigns a captain of the draft.
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
     * Requests a sub for a draft.
     * @param bc a button click to analyze.
     */
    @Override
    public void requestSub(ButtonClickEvent bc) {
        if (canRequestSub(bc) && !draftStarted()) {
            if (!draftStarted()) {
                resetCaptainsIfNeeded(bc.getMember().getId());
            }

            refresh(bc);
        }
    }

    /**
     * Forcibly subs a person out of the draft.
     * @param sc a slash command to analyze.
     * @param playerID the Discord ID of the player to sub.
     */
    @Override
    public void forceSub(SlashCommandEvent sc, String playerID) {
        if (canForceSub(sc, playerID) && !draftStarted()) {
            resetCaptainsIfNeeded(playerID);
        }
    }

    /**
     * Adds a player to the draft's subs, if possible, via a button.
     * @param bc a button click to analyze.
     */
    @Override
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

    /**
     * Checks if the draft can be forcibly ended.
     * @param sc a slash command to analyze.
     * @return True if the draft was forcibly ended.
     *         False otherwise.
     */
    @Override
    public boolean canForceEnd(SlashCommandEvent sc) {
        return super.canForceEnd(sc);
    }

    /**
     * Runs the draft start command.
     * @param sc the user's inputted command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        super.runCmd(sc);
    }
}
