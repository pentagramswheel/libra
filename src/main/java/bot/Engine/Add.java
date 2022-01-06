package bot.Engine;

import bot.Tools.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

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

    /** The Discord Ink Odyssey role. */
    private final Role ioRole = getRole("Ink Odyssey");

    /**
     * Allows a user entry into LaunchPoint.
     * @param user the user to add.
     * @return the entrance welcome message.
     */
    private String enterLP(Member user) {
        addRole(user, lpRole);

        String rulesChannel = SERVER.getTextChannelsByName(
                        "lp-draft-rules", true).get(0).getAsMention();
        return "Welcome to LaunchPoint! Make sure to read "
                + rulesChannel + "!";
    }

    /**
     * Allows a user entry into Ink Odyssey.
     * @param user the user to add.
     * @return the entrance welcome message.
     */
    private String enterIO(Member user) {
        addRole(user, ioRole);

        String rulesChannel = SERVER.getTextChannelsByName(
                "io-draft-rules", true).get(0).getAsMention();
        return "Welcome to Ink Odyssey! Make sure to read "
                + rulesChannel + "!";
    }

    /**
     * Runs any role commands.
     * @param outChannel the channel to output to, if it exists.
     * @param cmd the formal name of the command.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(MessageChannel outChannel, String cmd,
                       List<OptionMapping> args) {
        StringBuilder listOfUsers = new StringBuilder();

        for (OptionMapping om : args) {
            Member user = om.getAsMember();
            String welcomeMessage = "";

            // parse different role commands
            switch (cmd) {
                case "lpadd":
                    welcomeMessage = enterLP(user);
                    break;
                case "ioadd":
                    welcomeMessage = enterIO(user);
            }

            Member finalUser = args.get(args.size() - 1).getAsMember();
            if (user.equals(finalUser)) {
                listOfUsers.append(user.getAsMention())
                        .append("\n\n")
                        .append(welcomeMessage);
            } else {
                listOfUsers.append(user.getAsMention()).append(" ");
            }
        }

        sendReply(listOfUsers.toString());
        log(args.size() + " new user(s) processed.");
    }
}
