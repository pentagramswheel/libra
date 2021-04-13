package bot.Engine;

import bot.Discord;
import bot.Events;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author  Wil Aquino
 * Date:    February 17, 2021
 * Project: LaunchPoint Bot
 * Module:  Events.java
 * Purpose: Logs draft information via command.
 */
public class DraftLog implements Command {

    /**
     * Runs the draft logging command.
     * @param inChannel the channel the command was sent in.
     * @param outChannel the channel to output to, if it exists.
     * @param users the users to attach to the command output, if they exist.
     * @param args the arguments of the command, if they exist.
     */
    @Override
    public void runCmd(MessageChannel inChannel, MessageChannel outChannel,
                       List<Member> users, String[] args) {
        if (args[0].equals("lpwin")) {
            inChannel.sendMessage("The win report was made.").queue();
        } else {
            inChannel.sendMessage("The loss report was made.").queue();
        }
    }
}
