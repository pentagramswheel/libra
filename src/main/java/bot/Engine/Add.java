package bot.Engine;

import bot.Tools.Command;
import net.dv8tion.jda.api.entities.Member;
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

    /**
     * Allows a user entry into LaunchPoint.
     * @param user the user to add.
     * @return the entrance welcome message.
     */
    private String enterLP(Member user) {
        Role lpRole = getRole("LaunchPoint");
        addRole(user, lpRole);

        String rulesChannel = getChannel("lp-draft-rules").getAsMention();
        return "Welcome to LaunchPoint! Make sure to read "
                + rulesChannel + "!";
    }

    /**
     * Allows a user entry into Ink Odyssey.
     * @param user the user to add.
     * @return the entrance welcome message.
     */
    private String enterIO(Member user) {
        Role ioRole = getRole("Ink Odyssey");
        addRole(user, ioRole);

        String rulesChannel = getChannel("io-draft-rules").getAsMention();
        return "Welcome to Ink Odyssey! Make sure to read "
                + rulesChannel + "!";
    }

    /**
     * Runs any role commands.
     * @param cmd the formal name of the command.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(String cmd, List<OptionMapping> args) {
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
