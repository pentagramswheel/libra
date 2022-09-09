package bot.Engine.Games.Minigames;

import bot.Engine.Games.*;
import bot.Engine.Profiles.Profile;
import bot.Engine.Templates.GameReqs;
import bot.Tools.Components;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author  Wil Aquino
 * Date:    July 21, 2022
 * Project: Libra
 * Module:  MiniGame.java
 * Purpose: Formalizes and starts a minigame, via a request interface.
 */
public class MiniGame extends Game<MiniGame, MiniProcess, Team<Player>, Player>
        implements GameReqs {

    /** The number of players to let in for a draft. */
    private int cappedSize;

    /**
     * Constructs a minigame and initializes the minigame start attributes.
     * @param sc the user's inputted command.
     * @param type the type of minigame this is.
     * @param draft the numbered draft that this minigame is.
     * @param abbreviation the abbreviation of the section.
     * @param initialPlayer the first player of the minigame queue.
     */
    public MiniGame(SlashCommandEvent sc, GameType type, int draft,
                    String abbreviation, Member initialPlayer) {
        super(sc, type, draft, abbreviation);

        getWatch().startTimerOne(40);

        String playerID = initialPlayer.getId();
        getPlayers().put(playerID, new Player(
                initialPlayer.getEffectiveName(),false));
        getHistory().add(playerID);

        cappedSize = getProperties().getMaximumPlayersToStart();
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

    /** Retrieves the minigame's properties. */
    @Override
    public GameProperties getProperties() {
        return super.getProperties();
    }

    /** Retrieves the players of the minigame. */
    @Override
    public TreeMap<String, Player> getPlayers() {
        return super.getPlayers();
    }

    /**
     * Retrieves the request interface of the minigame.
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

    /** Retrieves the maximum number of players of this draft. */
    public int getCappedSize() {
        return cappedSize;
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

    /** Starts the minigame's process. */
    public void startProcess() {
        cappedSize = getPlayers().size();

        switch (getProperties().getGameType()) {
            case RANKED:
            case TURF_WAR:
            case HIDE_AND_SEEK:
                setProcess(new MiniProcess(this,
                        getProperties().getPlayersPerTeam()));
                break;
            case JUGGERNAUT:
                setProcess(new MiniProcess(this));
                break;
            default:
                if (cappedSize % 2 == 0) {
                    setProcess(new MiniProcess(this,
                            cappedSize / 2));
                } else {
                    setProcess(new MiniProcess(this,
                            cappedSize / 2, (cappedSize / 2) + 1));
                }

                break;
        }
    }

    /** Retrieves the field for executing the minigame. */
    @Override
    public MiniProcess getProcess() {
        return super.getProcess();
    }

    /**
     * Sends a minigame confirmation summary with all players of the game.
     * @param interaction the user interaction calling this method.
     */
    @Override
    public void updateReport(GenericInteractionCreateEvent interaction) {
        super.updateReport(interaction);
    }

    /** Formats a request ping for gathering players. */
    private String newPing() {
        int activePlayers = getPlayers().size() - getNumInactive();
        int pingsLeft = getCappedSize() - activePlayers;

        if (draftStarted()) {
            pingsLeft = 0;
        }

        return getSectionRole() + " +" + pingsLeft;
    }

    /**
     * Sets up the minigame for processing.
     * @param bc a button click to analyze.
     */
    public void setup(ButtonClickEvent bc) {
        if (!getPlayers().containsKey(bc.getMember().getId())) {
            sendReply(bc, "You are not in this draft!", true);
            return;
        }

        startProcess();

        List<Button> buttons = new ArrayList<>();
        buttons.add(Components.ForDraft.requestSub(suffix()));
        buttons.add(Components.ForDraft.joinAsSub(suffix()));
        buttons.add(Components.ForDraft.refresh(suffix()));

        sendButtons(bc, bc.getInteraction().getMessage().getContentRaw(),
                buttons);
        toggle(true);
        refresh(bc);

        getProcess().randomizeTeams(bc);
        getProcess().toggle(true);
        getProcess().refresh(null);

        wait(4000);

        List<MessageEmbed> profiles = new Profile().viewMultiple(bc,
                getPlayers().keySet(), "Their", false, true, false);
        if (profiles != null) {
            getDraftChannel().sendMessageEmbeds(profiles).queue();
        }
    }

    /**
     * Attempts to start a minigame.
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

        getPlayers().put(playerID, new Player(
                bc.getMember().getEffectiveName(), false));
        getHistory().add(playerID);

        if (getPlayers().size() == getProperties().getMaximumPlayersToStart()) {
            setup(bc);
        } else if (getPlayers().size() >= getProperties().getMinimumPlayersToStart()) {
            List<Button> buttons = new ArrayList<>();
            buttons.add(Components.ForDraft.setupEarly(suffix()));
            buttons.add(Components.ForDraft.joinDraft(suffix()));
            buttons.add(Components.ForDraft.reping(suffix()));
            buttons.add(Components.ForDraft.leave(suffix()));
            buttons.add(Components.ForDraft.refresh(suffix()));

            sendButtons(bc, bc.getInteraction().getMessage().getContentRaw(),
                    buttons);
            refresh(bc);
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
     * Removes a player from the minigame queue.
     * @param bc a button click to analyze.
     */
    @Override
    public void removeFromQueue(ButtonClickEvent bc) {
        String playerID = bc.getMember().getId();
        setMessageID(bc.getMessageId());

        if (!getPlayers().containsKey(playerID)) {
            sendReply(bc, "You are not in this draft!", true);
        } else {
            getPlayers().remove(playerID);

            if (getPlayers().size() < getProperties().getMinimumPlayersToStart()) {
                List<Button> buttons = new ArrayList<>();
                buttons.add(Components.ForDraft.joinDraft(suffix()));
                buttons.add(Components.ForDraft.reping(suffix()));
                buttons.add(Components.ForDraft.leave(suffix()));
                buttons.add(Components.ForDraft.refresh(suffix()));

                sendButtons(bc, bc.getInteraction().getMessage().getContentRaw(),
                        buttons);
            }

            refresh(bc);
        }
    }

    /**
     * Refreshes the minigame request's caption.
     * @param bc a button click to analyze.
     *
     * Note: The interaction must not have been acknowledged
     *       before this method.
     */
    @Override
    public void refresh(ButtonClickEvent bc) {
        bc.deferEdit().queue();
        setMessageID(bc.getMessageId());

        int activePlayers = getPlayers().size() - getNumInactive();

        if (activePlayers == getCappedSize() || !draftStarted()) {
            editMessage(bc, newPing());
        } else {
            int subsNeeded = getCappedSize() - activePlayers;
            editMessage(bc,
                    newPing() + "   // " + subsNeeded + " sub(s) needed");
        }

        updateReport(bc);
    }

    /**
     * Requests a sub for a minigame.
     * @param bc a button click to analyze.
     */
    @Override
    public void requestSub(ButtonClickEvent bc) {
        if (canRequestSub(bc)) {
            refresh(bc);
        }
    }

    /**
     * Forcibly subs a person out of the minigame.
     * @param sc a slash command to analyze.
     * @param playerID the Discord ID of the player to sub.
     */
    @Override
    public void forceSub(SlashCommandEvent sc, String playerID) {
        canForceSub(sc, playerID);
    }

    /**
     * Fixes teams a player was previously on before subbing.
     * @param playerID the Discord ID of the player.
     */
    private void fixTeams(String playerID) {
        Team<Player> team1 = getProcess().getTeam1();
        Team<Player> team2 = getProcess().getTeam2();
        Team<Player> team3 = getProcess().getTeam3();

        if (team1.contains(playerID)) {
            team1.remove(playerID);
        } else if (team2.contains(playerID)) {
            team2.remove(playerID);
        } else if (team3 != null && team3.contains(playerID)) {
            team3.remove(playerID);
        }
    }

    /**
     * Automatically adds a player to a team which needs a sub.
     * @param playerID the Discord ID of the player.
     */
    private void seedToTeam(String playerID) {
        if (isInitialized()) {
            Team<Player> team1 = getProcess().getTeam1();
            Team<Player> team2 = getProcess().getTeam2();
            Team<Player> team3 = getProcess().getTeam3();

            if (team1.needsPlayers()) {
                team1.add(playerID, getPlayers().get(playerID));
            } else if (team2.needsPlayers()) {
                team2.add(playerID, getPlayers().get(playerID));
            } else {
                team3.add(playerID, getPlayers().get(playerID));
            }
        }
    }

    /**
     * Adds a player to the minigame's subs, if possible, via a button.
     * @param bc a button click to analyze.
     */
    @Override
    public void addSub(ButtonClickEvent bc) {
        String id = bc.getMember().getId();
        String name = bc.getMember().getEffectiveName();
        int activePlayers = getPlayers().size() - getNumInactive();

        if (inWrongSection(bc)) {
            sendReply(bc, "You don't have access to this section's drafts!", true);
        } else if (activePlayers == getCappedSize()) {
            sendReply(bc, "This draft doesn't need subs right now.", true);
        } else if (canSubIn(bc, id)) {
            if (!getPlayers().containsKey(id)) {
                getPlayers().put(id, new Player(name, draftStarted()));
            } else {
                fixTeams(id);
            }

            seedToTeam(id);
            refresh(bc);
        }
    }

    /**
     * Checks if the minigame can be forcibly ended.
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
        if (inWrongSection(sc)) {
            return;
        }

        List<Button> buttons = new ArrayList<>();
        buttons.add(Components.ForDraft.joinDraft(suffix()));
        buttons.add(Components.ForDraft.reping(suffix()));
        buttons.add(Components.ForDraft.leave(suffix()));
        buttons.add(Components.ForDraft.refresh(suffix()));

        String caption = getSectionRole() + " +"
                + (getProperties().getMaximumPlayersToStart() - 1)
                + " (" + getProperties().getName() + ")";

        sc.reply(caption).addActionRow(buttons).queue();
        updateReport(sc);

        log("A " + getPrefix().toUpperCase()
                + " draft has been requested.", false);
    }
}
