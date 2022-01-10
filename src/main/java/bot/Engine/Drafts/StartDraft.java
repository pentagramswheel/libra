package bot.Engine.Drafts;

import bot.Tools.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author  Turtle#1504, Wil Aquino
 * Date:    December 22, 2021
 * Project: LaunchPoint Bot
 * Module:  StartDraft.java
 * Purpose: Starts a LaunchPoint draft.
 */
public class StartDraft implements Command {
    /**
     * True if someone else already used the command to start the draft.
     * False if previous startDraft command reached 8 players.
     */
//    public boolean startDraftExists = false;
    /**
     * Number of people who reacted to the last @LaunchPoint message by the bot.
     */
//    int numPlayers = 1;

    /** People who reacted to the last @LaunchPoint message by the bot are stored in this List. */
    private final ArrayList<Member> players;

    /** The pinged role for this draft. */
    private Role draftRole;

    /**
     * Constructs the initialized draft.
     * @param initialPlayer the first player of the draft.
     */
    public StartDraft(Member initialPlayer) {
        players = new ArrayList<>();
        players.add(initialPlayer);
    }

    /**
     * Sends a draft confirmation summary with all players
     * of the draft.
     * @param color the color of the summary.
     */
    private void sendReport(Color color) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Draft Confirmation");
        eb.setColor(color);

        StringBuilder queue = new StringBuilder();
        Random r = new Random();
        int capt1 = r.nextInt(8);
        int capt2 = r.nextInt(8);
        while (capt1 == capt2) {
            capt2 = r.nextInt(8);
        }

        for (int i = 0; i < players.size(); i++) {
            String currPlayer = players.get(i).getAsMention();
            queue.append(currPlayer);
            if (i == capt1 || i == capt2) {
                queue.append(" (captain)");
            }

            if (i < players.size()) {
                queue.append("\n");
            }
        }
        queue.append(players.get(players.size() - 1).getAsMention());

        eb.addField("Players", queue.toString(), false);
        eb.addField("Notice", "Go to the pinged draft chat \n"
                + "to begin the draft.", false);

        sendEmbed(eb);
    }

    /**
     * Attempts to start a draft.
     * @param bc a button click to analyze.
     */
    public void attemptDraft(ButtonClickEvent bc) {
        // prevents repeats, uncomment after everything's working
//        if (players.contains(bc.getMember())) {
//            return;
//        }

        players.add(bc.getMember());
        String newCaption = draftRole.getAsMention() + " +" + (8 - players.size());
        bc.editMessage(newCaption).queue();

        if (players.size() == 8) {
            disableButton(bc, "Draft queue full.");

            if (bc.getButton().getId().endsWith("LP")) {
                sendReport(Color.GREEN);
            } else {
                sendReport(Color.MAGENTA);
            }
        }
    }

    /**
     * Runs the draft start command.
     * @param cmd the formal name of the command.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(String cmd, List<OptionMapping> args) {
        String prefix = cmd.substring(0, 2);
        if (prefix.equals("lp")) {
            draftRole = getRole("LaunchPoint");
        } else {
            draftRole = getRole("Ink Odyssey");
        }

        String caption = draftRole.getAsMention() + " +7";
        String id = "Join" + prefix.toUpperCase();
        String label = "Join Draft";
        sendButton(caption, id, label, 0);
    }
}
