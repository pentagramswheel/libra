package bot.Engine;

import bot.Config;
import bot.Engine.Cycles.PlayerStats;
import bot.Tools.Command;
import bot.Tools.GoogleSheetsAPI;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.TreeMap;

/**
 * @author Wil Aquino
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

        try {
            GoogleSheetsAPI link = new GoogleSheetsAPI(spreadsheetID);

            // tab name of the spreadsheet
            String tab = "'Profiles'";

            TreeMap<Object, Object> data = link.readSection(sc, tab);
            if (data == null) {
                throw new IOException("The spreadsheet was empty.");
            }
            List<OptionMapping> args = sc.getOptions();

            switch (subCmd) {
                case "fc":
                    break;
                case "view":
                    break;
                case "nickname":
                    break;
                case "rank":
                    break;
                case "team":
                    break;
                case "playstyle":
                    break;
                case "weapons":
                    break;
                case "delete":
                    break;
            }
        } catch (IOException | GeneralSecurityException e) {
            sendResponse(sc, "The spreadsheet could not load.", true);
            log("The MIT profiles spreadsheet could not load.", true);
        }
    }
}
