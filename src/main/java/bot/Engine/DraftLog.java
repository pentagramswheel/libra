package bot.Engine;

import com.google.api.services.sheets.v4.model.ValueRange;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

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


        Member graduate = users.get(0);

//        try {
//            sheetsService = getSheetsService();
//            String range = "grads!A2:B3";
//
//            ValueRange response = sheetsService.spreadsheets().values().get(
//                    SPREADSHEET_ID, range).execute();
//
//            List<List<Object>> values = response.getValues();
//
//            if (values == null || values.isEmpty()) {
//                System.out.println("No spreadhsheet data was found.");
//            } else {
//                for ()
//            }
//        } catch (IOException | GeneralSecurityException e) {
//            outputError(inChannel, "The spreadsheet could not be accessed.");
//        }
    }
}
