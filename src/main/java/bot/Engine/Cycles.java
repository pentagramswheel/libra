package bot.Engine;

import bot.Tools.Command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

/**
 * @author  Wil Aquino, Turtle#1504
 * Date:    April 4, 2022
 * Project: Libra
 * Module:  Cycles.java
 * Purpose: Convenience features related to
 *          MIT Cycles.
 */
public class Cycles extends Section implements Command {

    /**
     * Constructs the Cycle's attributes.
     * @param abbreviation the abbreviation of the section.
     */
    public Cycles(String abbreviation) {
        super(abbreviation);
    }

    /**
     * Runs any Cycles commands.
     * @param sc the inputted slash command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        sc.deferReply().queue();
        String cmd = sc.getSubcommandName();
        List<OptionMapping> args = sc.getOptions();

//        switch (cmd) {
//            case
//        }


    }
}
