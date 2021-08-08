package bot.Tools;

import java.util.Date;

/**
 * @author  Wil Aquino
 * Date:    August 8, 2021
 * Project: LaunchPoint Bot
 * Module:  Time.java
 * Purpose: Outputs live timestamps, with respect to the location
 *          of the machine this program is running on.
 * @source  https://www.javatpoint.com/java-get-current-date
 */
public class Time {

    /**
     * Retrieves the current time on the running machine.
     * @return the current time.
     */
    public static String currentTime() {
        Date date = new Date();
        return date.toString();
    }
}
