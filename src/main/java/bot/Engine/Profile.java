package bot.Engine;

import bot.Config;
import bot.Tools.Command;
import bot.Tools.GoogleSheetsAPI;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * @author Turtle#1504
 * Date: May 7, 2022
 * Project: Libra
 * Module: Profile.java
 * Purpose: Manages the profile of the users.
 */
public class Profile implements Command {

    /** Google Sheets ID of the spreadsheet to save to. */
    private final String spreadsheetID;

    /**
     * Sets the spreadsheet ID.
     */
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

        // tab name of the spreadsheet
        String tab = "'Profiles'";
        List<OptionMapping> args = sc.getOptions();

        switch (subCmd) {
            case "create":
                break;
        }
    }
}
