package bot;

/**
 * @author  Wil Aquino
 * Date:    April 6, 2021
 * Project: LaunchPoint Bot
 * Module:  Discord.java
 * Purpose: Info specific to the bot and private to everyone
 *          but the local computer.
 */
public class Discord {

    /** LaunchPoint bot token. */
    private final static String botToken =
            "ODExMzE5NzM0ODA0MjgzNDAy.YCweYg.IfH1ucuyIvB4d2x7BXaCUXRiP3A";

    /** LaunchPoint graduates Google Sheets ID. */
    private final static String gradSheetID =
            "1qrwf3411d9izZ47gyg4JSSZL1VpsyrFzNJ3a0yg-EBk";

    /** LaunchPoint Cycles Google Sheets ID. */
    private final static String cyclesSheetID =
            "1eNvD8cMee8bALHgHbPAe54DP5bPgu25ljMyMCzhMC4w";

    /**
     * Retrieves the bot's token.
     * @return said token.
     */
    public static String getToken() {
        return botToken;
    }

    /**
     * Retrieves the graduates sheet ID.
     * @return said ID.
     */
    public static String getGradSheetID() {
        return gradSheetID;
    }

    /**
     * Retrieves the cycles sheet ID.
     * @return said ID.
     */
    public static String getCyclesSheetID() {
        return cyclesSheetID;
    }
}