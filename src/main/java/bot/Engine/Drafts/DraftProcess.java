package bot.Engine.Drafts;

import bot.Engine.PlayerStats;
import bot.Engine.Section;
import bot.Tools.BuiltButton;
import bot.Tools.Command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;

import java.util.*;

/**
 * @author  Wil Aquino, Turtle#1504
 * Date:    January 16, 2022
 * Project: Libra
 * Module:  Draft.java
 * Purpose: Formalizes and starts a draft.
 */

public class DraftProcess extends Section implements Command {
    /** The formal number of the draft. */
    private final int numDraft;
    private TreeMap<Integer, Draft> lpdraft;
    /** The players of the draft. */
    private final List<DraftPlayer> players;
    /** removes players from this array if it's added to team1 or team2 **/
    private final List<DraftPlayer> players2;
    private final List<DraftPlayer> subs;
    private final List<DraftPlayer> team1;
    private final List<DraftPlayer> team2;

    /**index of captain 1 players array **/
    private int captain1;
    /**index of captain 2 from players array **/
    private int captain2;

    /** The pinged role for this draft. */
    private final Role draftRole;

    /** The draft chat channel this draft is occurring in. */
    private final TextChannel draftChat;

    /**
     * Retrieves which draft number this is.
     * @return said number.
     */
    public int getNumDraft() {
        return numDraft;
    }

    /**
     * Retrieves the players of the draft.
     * @return said players.
     */
    public List<DraftPlayer> getPlayers() {
        return players;
    }

    /**
     * Retrieves the REMOVABLE players of the draft.
     * @return said players.
     */
    public List<DraftPlayer> getPlayers2() {
        return players2;
    }

    /**
     * Retrieves the team1 players of the draft.
     * @return said players.
     */
    public List<DraftPlayer> getTeam1() {
        return team1;
    }
    /**
     * Retrieves the team2 players of the draft.
     * @return said players.
     */
    public List<DraftPlayer> getTeam2() {
        return team2;
    }


    /** Retrieves the subs of the draft. */
    public List<DraftPlayer> getSubs() {
        return subs;
    }

    /**
     * Retrieves the pinged role for this draft.
     * @return said role.
     */
    public Role getDraftRole() {
        return draftRole;
    }

    /**
     * Retrieves the draft chat channel which this draft
     * is occurring in.
     * @return said channel.
     */
    public TextChannel getDraftChannel() {
        return draftChat;
    }

    public DraftProcess(SlashCommandEvent sc, int draft,
                        String abbreviation, Member initialPlayer, TreeMap<Integer, Draft> lpdraft) {
        super(abbreviation);
        numDraft = lpdraft.get(draft).getNumDraft();
        this.lpdraft = lpdraft;
        players =  lpdraft.get(draft).getPlayers();
        players2 =  lpdraft.get(draft).getPlayers2();
        subs =  lpdraft.get(draft).getSubs();
        team1 = lpdraft.get(draft).getTeam1();
        team2 = lpdraft.get(draft).getTeam2();
        DraftPlayer newPlayer = new DraftPlayer(initialPlayer);
        players.add(newPlayer);

        draftRole = lpdraft.get(draft).getRole(sc, getSection());
        draftChat = lpdraft.get(draft).getChannel(sc, getPrefix() + "-draft-chat-" + draft);
    }
    public void addPlayerToTeam(SelectionMenuEvent sm, Member captain, Member player){
        DraftPlayer dp = null;
        int indexOfPickedPlayer = 0;
        for(int i = 0; i < getPlayers2().size(); i++){
            if(getPlayers2().get(i).getAsMember().getId() == player.getId()){
                indexOfPickedPlayer = i;
                dp = getPlayers2().get(i);
            }
        }
        if(getPlayers().get(captain1).getAsMember().getId() == captain.getId()){
            team1.add(dp);
        }else if(getPlayers().get(captain2).getAsMember().getId() == captain.getId()){
            team2.add(dp);
        }else{
            //add reject statement saying you are not the captain.
        }

        ArrayList<String> nonCaptainPlayers = new ArrayList<>();
        for(int i = 0; i < getPlayers().size(); i++){
            if(i == captain1 || i == captain2 || i == indexOfPickedPlayer) {
                if(i == indexOfPickedPlayer){
                    getPlayers2().remove(indexOfPickedPlayer);
                }
                continue;
            }
            nonCaptainPlayers.add(getPlayers().get(i).getAsMember().getAsMention());

        }
        if(getPlayers2().size() == 0){
            lpdraft.get(getNumDraft()).finishedPicking(sm);
        }

        if(captain.getId() == getPlayers().get(captain1).getAsMember().getId()){
            Member otherCaptain = sm.getGuild().retrieveMemberById(getPlayers().get(captain2).getAsMember().getId()).complete();
            sendReply(sm, otherCaptain.getAsMention() + "please pick a player", false);
        }else{
            Member otherCaptain = sm.getGuild().retrieveMemberById(getPlayers().get(captain1).getAsMember().getId()).complete();
            sendReply(sm, otherCaptain.getAsMention() + "please pick a player", false);
        }
        // sendSelectionMenu(sm, sm.getInteraction().getMessage().getContentRaw(), nonCaptainPlayers, getNumDraft(), getPrefix().toUpperCase());

    }

    @Override
    public void runCmd(SlashCommandEvent sc) {

    }
}