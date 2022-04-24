package bot.Engine;

import bot.Tools.Command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

/**
 * @author
 * Date:
 * Project:
 * Module:
 * Purpose:
 */
public class Profile implements Command {

    /**
     * Runs the profile command.
     * @param sc the inputted slash command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        String subCmd = sc.getSubcommandName();
        if (subCmd == null) {
            subCmd = "";
        }

        switch (subCmd) {

        }
    }
}
