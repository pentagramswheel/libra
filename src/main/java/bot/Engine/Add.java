package bot.Engine;

import bot.Tools.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

/**
 * @author  Wil Aquino
 * Date:    April 26, 2021
 * Project: LaunchPoint Bot
 * Module:  Add.java
 * Purpose: Adds roles to users in LaunchPoint.
 */
public class Add extends Section implements Command {

    /**
     * Constructs the add attributes.
     * @param abbreviation the abbreviation of the section.
     */
    public Add(String abbreviation) {
        super(abbreviation);
    }

    /**
     * Allows a user entry into a section.
     * @param user the user to add.
     * @return the entrance welcome message.
     */
    private String enter(Member user) {
        addRole(user, getRole(getSection()));
        String rulesChannelName = getPrefix() + "-draft-rules";

        String rulesChannel = getChannel(rulesChannelName).getAsMention();
        return "Welcome to " + getSection() + "! Make sure to read "
                + rulesChannel + " before playing in any drafts!";
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
            String welcomeMessage = enter(user);

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
