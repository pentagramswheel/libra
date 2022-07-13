package bot.Engine;

import bot.Config;
import bot.Main;

import java.awt.Color;

/**
 * @author  Wil Aquino
 * Date:    January 10, 2022
 * Project: Libra
 * Module:  Section.java
 * Purpose: Designates MIT section specific information.
 */
public class Section {

    /** The name of the MIT section. */
    private final String name;

    /** The abbreviation of the section. */
    private final String prefix;

    /** The role mention of this section */
    private final String role;

    /** The emote of the section. */
    private final String emote;

    /** The color of the section. */
    private final Color color;

    /** Graduates Google Sheets ID. */
    private final String gradSheetID;

    /** Public Cycles Google Sheets ID. */
    private final String cyclesSheetID;

    /** Private Top 10 Calculation Google Sheets ID. */
    private final String calculationsSheetID;

    /** The tab to reference on the Cycles Google Sheet. */
    public static final String CYCLES_TAB = "Current Cycle";

    /** The spreadsheet's starting column with leaderboard information. */
    public static final String CYCLES_START_COLUMN = "B";

    /** The spreadsheet's ending column with leaderboard information. */
    public static final String CYCLES_END_COLUMN = "K";

    /**
     * Constructs the section attributes.
     * @param abbreviation the abbreviation of the section.
     */
    public Section(String abbreviation) {
        prefix = abbreviation;

        switch (prefix) {
            case "fs":
                name = "Freshwater Shoals";
                role = "<@&996101473647743086>";
                emote = "emoteHere";
                color = Main.freshwatershoalsColor;
                gradSheetID = Config.fsGradSheetID;
                cyclesSheetID = null;
                calculationsSheetID = null;
                break;
            case "lp":
                name = "LaunchPoint";
                role = "<@&732850504463286352>";
                emote = "<:LaunchPoint:918936266190512168>";
                color = Main.launchpointColor;
                gradSheetID = Config.lpGradSheetID;
                cyclesSheetID = Config.lpCyclesSheetID;
                calculationsSheetID = Config.lpCyclesCalculationSheetID;
                break;
            default:
                name = "Ink Odyssey";
                role = "<@&918181354284408882>";
                emote = "<:InkOdyssey:918936305788923944>";
                color = Main.inkodysseyColor;
                gradSheetID = Config.ioGradSheetID;
                cyclesSheetID = Config.ioCyclesSheetID;
                calculationsSheetID = Config.ioCyclesCalculationSheetID;
                break;
        }
    }

    /**
     * Retrieves the MIT section name.
     */
    public String getSection() {
        return name;
    }

    /**
     * Retrieves the prefix of the MIT section.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Retrieves the role of this section.
     */
    public String getSectionRole() {
        return role;
    }

    /**
     * Retrieves the emote of the MIT section.
     */
    public String getEmote() {
        return emote;
    }

    /**
     * Retrieves the designated color of the MIT section.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Retrieves the section's graduates sheet ID.
     */
    public String gradSheetID() {
        return gradSheetID;
    }

    /**
     * Retrieves the section's cycles sheet ID.
     */
    public String cyclesSheetID() {
        return cyclesSheetID;
    }

    /**
     * Retrieves the section's Top 10 calculations sheet ID.
     */
    public String calculationsSheetID() {
        return calculationsSheetID;
    }
}
