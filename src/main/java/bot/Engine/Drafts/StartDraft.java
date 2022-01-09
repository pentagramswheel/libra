package bot.Engine.Drafts;

import bot.Tools.Command;
import bot.Tools.SlashCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonInteraction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author  Turtle#1504
 * Date:    February 17, 2021
 * Project: LaunchPoint Bot
 * Module:  StartDraft.java
 * Purpose: Starts a LaunchPoint draft.
 */
public class StartDraft extends bot.Events implements SlashCommand {
    /**
     * True if someone else already used the command to start the draft.
     * False if previous startDraft command reached 8 players.
     */
    public boolean startDraftExists = false;
    /**
     * Number of people who reacted to the last @LaunchPoint message by the bot.
     */
    int numPlayers = 1;

    /**
     * People who reacted to the last @LaunchPoint message by the bot are stored in this List.
     */
    private List<Member> players = new ArrayList<Member>();

    public void runCmd2(Message outChannel, List<Member> users, ButtonInteraction e){
        System.out.println(startDraftExists);
        if(startDraftExists){
            players.add(users.get(0));
            if(players.size() == 2){
                startDraftExists = false;
                String ping = "";
                for(int i = 0; i < players.size(); i++){
                    ping += "<@" + players.get(i).getId() + "> ";
                }
                ORIGIN.sendMessage(ping + "Go to an available draft chat and start your game!").queue();
            }
        }
    }
    /**
     * Runs the startDraft command.
     * @param outChannel the channel to output to, if it exists.
     * @param users the users to attach to the command output, if they exist.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(MessageChannel outChannel, List<Member> users,
                       String[] args, SlashCommandEvent e) {
        System.out.println(startDraftExists);
        if(!startDraftExists){
            players.add(users.get(0));
            startDraftExists = true;
           e.reply("<@LaunchPointRoleID> +7").addActionRow(Button.primary("Join", "Join")).queue();
            System.out.println(startDraftExists);

        }else{
            replyToDiscord("Someone else is waiting for people. Scroll up and click join to my last @LaunchPoint message!", e);
            log(users.size() + " Someone used the startDraft command while someone else already used it and was waiting for people.");
        }
    }
}
