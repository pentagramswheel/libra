package bot;

/**
 * @author  Wil Aquino
 * Date:    April 6, 2021
 * Project: LaunchPoint Bot
 * Module:  Discord.java
 * Purpose: Info specific to the bot and private to everyone
 *          but the local computer.
 */
public final class DiscordExample {

    /** LaunchPoint bot token. */
    private final static String botToken = "TOKEN";

    /** LaunchPoint graduates Google Sheets ID. */
    private final static String lpGradSheetID ="ID";

    /** Ink Odyssey graduates Google Sheets ID. */
    private final static String ioGradSheetID = "ID";

    /** LaunchPoint Cycles Google Sheets ID. */
    private final static String lpCyclesSheetID = "ID";

    /** Ink Odyssey Google Sheets ID */
    private final static String ioCyclesSheetID ="ID";

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
        return lpGradSheetID;
    }

    /**
     * Retrieves the IO graduates sheet ID.
     * @return said ID.
     */
    public static String getIOGradSheetID() {
        return ioGradSheetID;
    }

    /**
     * Retrieves the LP cycles sheet ID.
     * @return said ID.
     */
    public static String getLPCyclesSheetID() {
        return lpCyclesSheetID;
    }

    /**
     * Retrieves the IO cycles sheet ID.
     * @return said ID.
     */
    public static String getIOCyclesSheetID() {
        return ioCyclesSheetID;
    }
}