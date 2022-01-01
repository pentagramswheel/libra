package bot;

/**
 * @author  Wil Aquino
 * Date:    June 3, 2021
 * Project: LaunchPoint Bot
 * Module:  DiscordExample.java
 * Purpose: A template for the class Discord.java.
 */
public final class DiscordExample {

    /** LaunchPoint bot token. */
    private final static String botToken = "TOKEN";

    //** LaunchPoint graduates Google Sheets ID. */
    private final static String lpGradSheet = "ID";

    /** Ink Odyssey graduates Google Sheets ID. */
    private final static String ioGradSheet = "ID";

    /** LaunchPoint Cycles Google Sheets ID. */
    private final static String cyclesSheetID = "ID";

    /** Ink Odyssey Google Sheets ID */
    private final static String ioSheetID = "ID";

    /**
     * Retrieves the bot's token.
     * @return said token.
     */
    public static String getToken() {
        return botToken;
    }

    /**
     * Retrieves the LP graduates sheet ID.
     * @return said ID.
     */
    public static String getLPGradSheetID() {
        return lpGradSheet;
    }

    /**
     * Retrieves the IO graduates sheet ID.
     * @return said ID.
     */
    public static String getIOGradSheetID() {
        return ioGradSheet;
    }

    /**
     * Retrieves the LP cycles sheet ID.
     * @return said ID.
     */
    public static String getLPCyclesSheetID() {
        return cyclesSheetID;
    }

    /**
     * Retrieves the IO drafts sheet ID.
     * @return said ID.
     */
    public static String getIOSheetID() {
        return ioSheetID;
    }
}