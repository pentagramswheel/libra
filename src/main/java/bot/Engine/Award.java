package bot.Engine;

import bot.Tools.Command;

import net.dv8tion.jda.api.entities.Guild;
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
     * @param sc the user's inputted command.
     * @param role the leaderboard role to give.
     * @param newPlacings the players to give the role to.
     */
    private void giveAward(SlashCommandEvent sc, String role,
                           List<OptionMapping> newPlacings) {
        try {
            Guild server = sc.getGuild();
            role = String.format(role, getSection());
            if (server == null) {
                throw new NullPointerException("Server link disconnected.");
            }

            List<Member> oldPlacings = server.getMembersWithRoles(getRole(sc, role));
            for (Member player : oldPlacings) {
                String playerID = player.getId();
                if (role.startsWith("1st") || role.startsWith("2nd")
                        || role.startsWith("3rd")) {
                    removeRole(sc, playerID, getRole(sc, role));
                    addRole(sc, playerID,
                            getRole(sc, "Past " + getSection() + " Leaderboard Podium"));
                }

                addRole(sc, playerID,
                        getRole(sc, "Past " + getSection() + " Leaderboard Top 10"));
            }

            StringBuilder listOfUsers = new StringBuilder();
            for (OptionMapping om : newPlacings) {
                Member player = om.getAsMember();
                String playerID = player.getId();

                addRole(sc, playerID,
                        getRole(sc, String.format(role, getSection())));
                listOfUsers.append(player.getAsMention()).append(" ");
            }

            sendReply(sc, "Award(s) given to " + listOfUsers + "!", true);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            log("The role, " + role + ", could not be found.", true);
        }
    }

    /**
     * Runs any award commands.
     * @param sc the inputted slash command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
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
        }

        log(args.size() + " " + getSection()
                + " award(s) given.", false);
    }
}
