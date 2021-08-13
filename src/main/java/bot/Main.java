package bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;

/**
 * @author  Wil Aquino
 * Date:    February 17, 2021
 * Project: LaunchPoint Bot
 * Module:  Main.java
 * Purpose: The entry point of the bot's backend.
 */
public class Main {
    public static void main(String[] args) {
        Events.BOT = JDABuilder.createDefault(Discord.getToken())
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .enableIntents(GatewayIntent.GUILD_MEMBERS);

        Events.BOT.addEventListeners(new Events());

        try {
            JDA jda = Events.BOT.build();

            String status = "lphelp | simping for @everyone";
            jda.getPresence().setActivity(Activity.playing(
                    status));

            Thread.sleep(3000);
            System.out.println("\nUSAGE LOG:\n==========");
        } catch (LoginException le) {
            le.printStackTrace();
        } catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
