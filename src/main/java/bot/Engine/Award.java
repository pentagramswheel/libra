package bot.Engine;

import bot.Tools.Command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    April 7, 2021
 * Project: Libra
 * Module:  Award.java
 * Purpose: Gives awards to players within MIT.
 */
public class Award extends Section implements Command {

    /**
     * Constructs the award attributes.
     * @param abbreviation the abbreviation of the section.
     */
    public Award(String abbreviation) {
        super(abbreviation);
    }

    /**
     * Awards players a leaderboard role.
     * @param sc the inputted slash command.
     * @param role the leaderboard role to give.
     * @param players the players to give the role to.
     */
    private void giveAward(SlashCommandEvent sc, String role,
                           List<OptionMapping> players) {
        StringBuilder listOfUsers = new StringBuilder();
        for (OptionMapping om : players) {
            Member player = om.getAsMember();
            String playerID = player.getId();
            if (role.startsWith("Past")) {
                removeRole(sc, playerID,
                        getRole(sc, "1st " + getSection() + " Leaderboard"));
                removeRole(sc, playerID,
                        getRole(sc, "2nd " + getSection() + " Leaderboard"));
                removeRole(sc, playerID,
                        getRole(sc, "3rd " + getSection() + " Leaderboard"));
                removeRole(sc, playerID,
                        getRole(sc, getSection() + " Leaderboard Top 10"));
            }

            addRole(sc, playerID,
                    getRole(sc, String.format(role, getSection())));

            Member finalPlayer = players.get(players.size() - 1).getAsMember();
            if (player.equals(finalPlayer)) {
                listOfUsers.append(player.getAsMention())
                        .append("\n\n")
                        .append("Congratulations on the awards!");
            } else {
                listOfUsers.append(player.getAsMention()).append(" ");
            }
        }

        sc.getHook().sendMessage(listOfUsers.toString()).queue();
    }

    /**
     * Runs any award commands.
     * @param sc the inputted slash command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        sc.deferReply().queue();
        List<OptionMapping> args = sc.getOptions();
        int award = (int) args.remove(0).getAsLong();

        switch (award) {
            case 1:
                giveAward(sc, "1st %s Leaderboard", args);
                break;
            case 2:
                giveAward(sc, "2nd %s Leaderboard", args);
                break;
            case 3:
                giveAward(sc, "3rd %s Leaderboard", args);
                break;
            case 4:
                giveAward(sc, "%s Leaderboard Top 10", args);
                break;
            case 5:
                giveAward(sc, "Past %s Leaderboard Podium", args);
                break;
            case 6:
                giveAward(sc, "Past %s Leaderboard Top 10", args);
                break;
        }

        log(args.size() + " " + getSection()
                + " award(s) given.", false);
    }
}
