package bot.Engine;

import bot.Engine.Profiles.Profile;
import bot.Engine.Templates.Command;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    August 4, 2022
 * Project: Libra
 * Module:  DraftCup.java
 * Purpose: Manages work within draft cups.
 */
public class DraftCup implements Command {

    /**
     * Views Draft Cup players' profiles.
     * @param sc the inputted slash command.
     * @param args the arguments of the command.
     */
    private void lookup(SlashCommandEvent sc, List<OptionMapping> args) {
        sc.deferReply(false).queue();

        List<String> playerIDs = new ArrayList<>();
        for (OptionMapping om : args) {
            playerIDs.add(om.getAsMember().getId());
        }

        List<MessageEmbed> profiles = new Profile().viewMultiple(sc,
                playerIDs, "Their", true, false, false);
        if (profiles != null) {
            sc.getHook().sendMessageEmbeds(profiles).queue();
        }

        log("A draft cup team was looked up.", false);
    }

    /**
     * Runs a draft cup command.
     * @param sc the inputted slash command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        String subCmd = sc.getSubcommandName();
        List<OptionMapping> args = sc.getOptions();

        switch (subCmd) {
            case "lookup":
                lookup(sc, args);
                break;
        }
    }
}
