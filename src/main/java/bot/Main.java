package bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

/**
 * @author  Wil Aquino
 * Date:    February 17, 2021
 * Project: LaunchPoint Bot
 * Module:  Main.java
 * Purpose: The entry point of the bot.
 */
public class Main {

    /** Name of the bot and application. */
    public static String NAME = "LaunchPoint Simp";

    public static void main(String[] args) {
        Events.BOT = JDABuilder.createLight(Discord.getToken())
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .enableIntents(GatewayIntent.GUILD_MEMBERS);

        Events.BOT.addEventListeners(new Events());

        try {
            JDA jda = Events.BOT.build();

            // slash command testing
//            jda.upsertCommand("ping", "Calculate ping of the bot").queue();

            String status = "lphelp | simping for @everyone";
            jda.getPresence().setActivity(Activity.playing(
                    status));

            Thread.sleep(4000);
            System.out.println("\nUSAGE LOG:\n==========");
        } catch (LoginException le) {
            le.printStackTrace();
        } catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
