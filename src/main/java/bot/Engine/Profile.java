package bot.Engine;

import bot.Config;
import bot.Tools.Command;

import bot.Tools.GoogleSheetsAPI;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author
 * Date:
 * Project:
 * Module:
 * Purpose:
 */
public class Profile implements Command {

    /** Google Sheets ID of the spreadsheet to save to. */
    private final String spreadsheetID;

    public Profile() {
        spreadsheetID = Config.mitProfilesSheetID;
    }

    private GoogleSheetsAPI getSpreadsheet() {
        try {
            return new GoogleSheetsAPI(spreadsheetID);
        } catch (GeneralSecurityException | IOException e) {
            log("The MIT profiles spreadsheet could not load.", true);
            return null;
        }
    }

    /**
     * Runs the profile command.
     * @param sc the user's inputted command.
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
