package main.bot;

/**
 * @author  Wil Aquino
 * Date:    February 17, 2021
 * Project: LaunchPoint Bot
 * Module:  Discord.java
 * Purpose: Info specific to the bot and private to everyone
 *          but the local computer.
 */
public class Discord {

    /** Eidos bot token. */
    private final static String botToken =
            "ODExMzE5NzM0ODA0MjgzNDAy.YCweYg.49AxOxQ4e1H4yh1xsqNSlzotp9o";


    /**
     * Retrieves the bot's token.
     * @return said token.
     */
    public static String getToken() {
        return botToken;
    }
}
