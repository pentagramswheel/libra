package bot.Engine;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    April 26, 2021
 * Project: LaunchPoint Bot
 * Module:  Add.java
 * Purpose: Adds roles to users in LaunchPoint.
 */
public class Add extends bot.Events implements Command {

    /** The Discord LaunchPoint role. */
    private final Role lpRole = getRole("LaunchPoint");

    /** The Discord LP Coach role. */
    private final Role coachRole = getRole("Coaches");

    /**
     * Allows a user entry into LaunchPoint.
     * @param user the user to add.
     * @return the entrance welcome message.
     */
    private String enter(Member user) {
        while (!user.getRoles().contains(lpRole)) {
            addRole(user, lpRole);

            // prevent Discord rate limiting
            wait (2000);
            user = SERVER.retrieveMemberById(user.getId()).complete();
        }

        String rulesChannel = SERVER.getTextChannelsByName(
                        "lp-draft-rules", true).get(0).getAsMention();
        return "Welcome to LaunchPoint! Make sure to read "
                + rulesChannel + "!";
    }

    /**
     * Allows a user to be a coach within LaunchPoint.
     * @param user the user to add.
     * @return the coach welcome message.
     */
    private String coach(Member user) {
        while (!user.getRoles().contains(coachRole)) {
            addRole(user, coachRole);

            // prevent Discord rate limiting
            wait (2000);
            user = SERVER.retrieveMemberById(user.getId()).complete();
        }

        return "Welcome to the LaunchPoint coaches!";
    }

    /**
     * Runs the add or coach command.
     * @param outChannel the channel to output to, if it exists.
     * @param users the users to attach to the command output, if they exist.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(MessageChannel outChannel, List<Member> users,
                       String[] args) {
        String cmd = args[0];
        StringBuilder listOfUsers = new StringBuilder();
        listOfUsers.append("```\n");

        sendToDiscord("Processing users...");
        for (Member user : users) {
            String welcomeMessage = "";

            switch (cmd) {
                case "LPADD":
                    welcomeMessage = enter(user);
                    break;
                case "LPCOACH":
                    welcomeMessage = coach(user);
                    break;
            }

            if (user.equals(users.get(users.size() - 1))) {
                listOfUsers.append(user.getUser().getAsTag())
                        .append("\n```")
                        .append(welcomeMessage);
            } else {
                listOfUsers.append(user.getUser().getAsTag()).append(", ");
            }
        }

        sendToDiscord(listOfUsers.toString());
        log(users.size() + " new user(s)/coach(es) were processed.");
    }
}
