package bot.Engine;

import bot.Engine.Templates.Command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
     * Modifies roles from the previous Cycle.
     * @param sc the user's inputted command.
     * @param role the role to focus on.
     * @param playerID the Discord ID of the player to
     *                 adjust the roles of.
     */
    private void adjustLastCycleRoles(SlashCommandEvent sc,
                                      String role, String playerID) {
        boolean isPodiumRole = role.startsWith("1st")
                || role.startsWith("2nd") || role.startsWith("3rd");

        if (isPodiumRole) {
            modifyRoles(sc, playerID,
                    Arrays.asList(
                            getRole(sc, "Past " + getSection() + " Leaderboard Podium"),
                            getRole(sc, "Past " + getSection() + " Leaderboard Top 10")),
                    Arrays.asList(
                            getRole(sc, role),
                            getRole(sc, getSection() + " Leaderboard Top 10")));
        } else {
            modifyRoles(sc, playerID,
                    Collections.singletonList(
                            getRole(sc, "Past " + getSection() + " Leaderboard Top 10")),
                    Collections.singletonList(
                            getRole(sc, role)));
        }
    }

    /**
     * Retrieves the new current roles to add to a player.
     * @param sc the user's inputted command.
     * @param listOfUsers the list of players awarded by the command.
     * @param role the role to focus on.
     * @param playerID the Discord ID of the player.
     */
    private List<Role> getNewRolesToAdd(SlashCommandEvent sc,
                                        StringBuilder listOfUsers,
                                        String role, String playerID) {
        Member player = findMember(sc, playerID);

        Role firstPlaceRole =
                getRole(sc, "1st " + getSection() + " Leaderboard");
        Role secondPlaceRole =
                getRole(sc, "2nd " + getSection() + " Leaderboard");
        Role thirdPlaceRole =
                getRole(sc, "3rd " + getSection() + " Leaderboard");
        Role currentTopTenRole =
                getRole(sc, getSection() + " Leaderboard Top 10");
        Role pastPodiumRole =
                getRole(sc, "Past " + getSection() + " Leaderboard Podium");
        Role pastTopTenRole =
                getRole(sc, "Past " + getSection() + " Leaderboard Top 10");

        List<Role> rolesToAdd = new ArrayList<>(Arrays.asList(
                currentTopTenRole, pastPodiumRole, pastTopTenRole,
                getRole(sc, role)));

        if (rolesToAdd.get(0).equals(rolesToAdd.get(3))) {
            rolesToAdd.remove(currentTopTenRole);
        }

        List<Role> playerRoles = player.getRoles();
        if (!playerRoles.contains(currentTopTenRole)) {
            rolesToAdd.remove(pastTopTenRole);
        }
        if (!playerRoles.contains(firstPlaceRole)
                && !playerRoles.contains(secondPlaceRole)
                && !playerRoles.contains(thirdPlaceRole)) {
            rolesToAdd.remove(pastPodiumRole);
        }

        listOfUsers.append(player.getAsMention()).append(" ");
        return rolesToAdd;
    }

    /**
     * Modifies roles for the current Cycle.
     * @param sc the user's inputted command.
     * @param listOfUsers the list of players awarded by the command.
     * @param newPlacingsIDs the IDs of the players to give the role to.
     * @param role the role to focus on.
     */
    private void adjustCurrentCycleRoles(SlashCommandEvent sc,
                                         StringBuilder listOfUsers,
                                         List<String> newPlacingsIDs,
                                         String role) {
        List<Role> podiumRoles = new ArrayList<>(Arrays.asList(
                getRole(sc, "1st " + getSection() + " Leaderboard"),
                getRole(sc, "2nd " + getSection() + " Leaderboard"),
                getRole(sc, "3rd " + getSection() + " Leaderboard")));

        for (String playerID : newPlacingsIDs) {
            List<Role> rolesToAdd = getNewRolesToAdd(sc,
                    listOfUsers, role, playerID);
            for (Role newRole : rolesToAdd) {
                podiumRoles.remove(newRole);
            }

            modifyRoles(sc, playerID, rolesToAdd, podiumRoles);
        }
    }

    /**
     * Awards players a leaderboard role.
     * @param sc the user's inputted command.
     * @param role the leaderboard role to give.
     * @param newPlacingsIDs the IDs of the players to give the role to.
     */
    private void giveAward(SlashCommandEvent sc, String role,
                           List<String> newPlacingsIDs) {
        sc.deferReply(true).queue();

        try {
            Guild server = sc.getGuild();
            role = String.format(role, getSection());
            if (server == null) {
                throw new NullPointerException("Server link disconnected.");
            }

            List<String> oldPlacingsIDs = new ArrayList<>();
            for (Member player : server.getMembersWithRoles(getRole(sc, role))) {
                oldPlacingsIDs.add(player.getId());
            }
            for (String playerID : oldPlacingsIDs) {
                if (newPlacingsIDs.contains(playerID)) {
                    continue;
                }

                adjustLastCycleRoles(sc, role, playerID);
            }

            StringBuilder listOfUsers = new StringBuilder();
            adjustCurrentCycleRoles(sc, listOfUsers, newPlacingsIDs, role);
            listOfUsers.delete(listOfUsers.length() - 1, listOfUsers.length())
                    .append("!");

            editMessage(sc, "Award(s) given to " + listOfUsers);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            log("The role, " + role + ", could not be found.", true);
        }
    }

    /**
     * Runs any award commands.
     * @param sc the user's inputted command.
     */
    @Override
    public void runCmd(SlashCommandEvent sc) {
        int award = (int) sc.getOptions().remove(0).getAsLong();
        List<String> ids = new ArrayList<>();
        for (OptionMapping om : sc.getOptions()) {
            ids.add(om.getAsMember().getId());
        }

        switch (award) {
            case 1:
                giveAward(sc, "1st %s Leaderboard", ids);
                break;
            case 2:
                giveAward(sc, "2nd %s Leaderboard", ids);
                break;
            case 3:
                giveAward(sc, "3rd %s Leaderboard", ids);
                break;
            case 4:
                giveAward(sc, "%s Leaderboard Top 10", ids);
                break;
        }

        log(sc.getOptions().size() + " " + getSection()
                + " award(s) given.", false);
    }
}
