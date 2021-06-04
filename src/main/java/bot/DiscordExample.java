package bot;

/**
 * @author  Wil Aquino
 * Date:    June 3, 2021
 * Project: LaunchPoint Bot
 * Module:  DiscordExample.java
 * Purpose: A template for the class Discord.java.
 */
public class DiscordExample {

    /** LaunchPoint bot token. */
    private final static String botToken = "token";

    /** LaunchPoint graduates Google Sheets ID. */
    private final static String gradSheetID = "id";

    /** LaunchPoint Cycles Google Sheets ID. */
    private final static String cyclesSheetID = "id";

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