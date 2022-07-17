package bot.Tools;

public class DiscordWatch {

    /** Start time of the watch's first timer. */
    private long timerStart;

    /** Duration of the watch's first timer. */
    private long timerDuration;

    /** Start time of the watch's second timer. */
    private long timerStart2;

    /** Duration of the watch's second timer. */
    private long timerDuration2;

    /**
     * Starts the first timer.
     * @param min how long the timer should last in minutes.
     */
    public void startTimerOne(long min) {
        timerStart = System.currentTimeMillis();
        timerDuration = 1000 * 60 * min;
    }

    /**
     * Starts the second timer.
     * @param min how long the timer should last in minutes.
     */
    public void startTimerTwo(long min) {
        timerStart2 = System.currentTimeMillis();
        timerDuration2 = 1000 * 60 * min;
    }

    /**
     * Adds a certain amount of time to the first timer.
     * @param min the time in minutes to add.
     */
    public void timerOneAdd(long min) {
        timerDuration += 1000 * 60 * min;
    }

    /**
     * Adds a certain amount of time to the second timer.
     * @param min the time in minutes to add.
     */
    public void timerTwoAdd(long min) {
        timerDuration2 += 1000 * 60 * min;
    }

    /** Retrieves the end time of the first timer. */
    public long getTimerOneEnd() {
        return timerStart + timerDuration;
    }

    /** Retrieves the end time of the second timer. */
    public long getTimerTwoEnd() {
        return timerStart2 + timerDuration2;
    }

    /**
     * Checks whether the first timer expired or not.
     * @return True if it expired.
     *         False otherwise.
     */
    public boolean timerOneExpired() {
        return System.currentTimeMillis() - timerStart >= timerDuration;
    }

    /**
     * Checks whether the second timer expired or not.
     * @return True if it expired.
     *         False otherwise.
     */
    public boolean timerTwoExpired() {
        return System.currentTimeMillis() - timerStart2 >= timerDuration2;
    }

    /**
     * Translates a time into a Discord timestamp.
     * (e.g. 12:00 PM)
     * @param readTime the time to translate, if any.
     */
    public static String discordTime(long readTime) {
        if (readTime != 0) {
            return String.format("<t:%s:t>", readTime / 1000);
        } else {
            return String.format("<t:%s:t>", System.currentTimeMillis() / 1000);
        }
    }

    /**
     * Translates a time into a Discord date timestamp.
     * (e.g. January 1, 2022)
     * @param readTime the time to translate, if any.
     */
    public static String discordDate(long readTime) {
        if (readTime != 0) {
            return String.format("<t:%s:D>", readTime / 1000);
        } else {
            return String.format("<t:%s:D>", System.currentTimeMillis() / 1000);
        }
    }

    /**
     * Translates a time into a Discord numbered date timestamp.
     * (e.g. 1/1/2022)
     * @param readTime the time to translate, if any.
     */
    public static String discordNumberDate(long readTime) {
        if (readTime != 0) {
            return String.format("<t:%s:d>", readTime / 1000);
        } else {
            return String.format("<t:%s:d>", System.currentTimeMillis() / 1000);
        }
    }

    /**
     * Translates a time into a Discord date and time timestamp.
     * (e.g. January 1, 2022 12:00 PM)
     * @param readTime the time to translate, if any.
     */
    public static String discordDateTime(long readTime) {
        if (readTime != 0) {
            return String.format("<t:%s:f>", readTime / 1000);
        } else {
            return String.format("<t:%s:f>", System.currentTimeMillis() / 1000);
        }
    }

    /**
     * Translates a time into a Discord day, date, and time timestamp.
     * (e.g. Sunday, January 1, 2022 12:00 PM)
     * @param readTime the time to translate, if any.
     */
    public static String discordDayDateTime(long readTime) {
        if (readTime != 0) {
            return String.format("<t:%s:F>", readTime / 1000);
        } else {
            return String.format("<t:%s:F>", System.currentTimeMillis() / 1000);
        }
    }

    /**
     * Translates a time into a Discord "time until" timestamp.
     * (e.g. in 30 minutes)
     * @param readTime the time to translate, if any.
     */
    public static String discordTimeUntil(long readTime) {
        if (readTime != 0) {
            return String.format("<t:%s:R>", readTime / 1000);
        } else {
            return String.format("<t:%s:R>", System.currentTimeMillis() / 1000);
        }
    }

    /** Main method for testing. */
    public static void main(String[] args) {
        System.out.println(discordTime(0));

        DiscordWatch watch = new DiscordWatch();
        watch.startTimerOne(30);
        System.out.println(discordTimeUntil(watch.getTimerOneEnd()));
    }
}
