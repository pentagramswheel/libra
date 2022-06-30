package bot.Engine;

import bot.Config;

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

    /** The tab to reference on the Cycles Google Sheet. */
    public static final String CYCLES_TAB = "'Current Cycle'";

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

        if (prefix.equals("lp")) {
            name = "LaunchPoint";
            role = "<@&732850504463286352>";
            emote = "<:LaunchPoint:918936266190512168>";
            color = Color.GREEN;
            gradSheetID = Config.lpGradSheetID;
            cyclesSheetID = Config.lpCyclesSheetID;
        } else {
            name = "Ink Odyssey";
            role = "<@&918181354284408882>";
            emote = "<:InkOdyssey:918936305788923944>";
            color = Color.MAGENTA;
            gradSheetID = Config.ioGradSheetID;
            cyclesSheetID = Config.ioCyclesSheetID;
        }
    }

    /**
     * Retrieves the MIT section name.
     * @return said section.
     */
    public String getSection() {
        return name;
    }

    /**
     * Retrieves the prefix of the MIT section.
     * @return said prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Retrieves the role of this section.
     * @return said role.
     */
    public String getSectionRole() {
        return role;
    }

    /**
     * Retrieves the emote of the MIT section.
     * @return said emote.
     */
    public String getEmote() {
        return emote;
    }

    /**
     * Retrieves the designated color of the MIT section.
     * @return said color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Retrieves the section's graduates sheet ID.
     * @return said ID.
     */
    public String gradSheetID() {
        return gradSheetID;
    }

    /**
     * Retrieves the section's cycles sheet ID.
     * @return said ID.
     */
    public String cyclesSheetID() {
        return cyclesSheetID;
    }
}
