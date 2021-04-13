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
 * Purpose: The entry point of the bot's backend.
 */
public class Main {
    public static void main(String[] args) {
        Events.BOT = JDABuilder.createDefault(Discord.getToken())
                .enableIntents(GatewayIntent.GUILD_PRESENCES);
        JDA jda = null;

        Events.BOT.addEventListeners(new Events());

        try {
            jda = Events.BOT.build();

            String funMessage = "lphelp | simping for @everyone";
            jda.getPresence().setActivity(Activity.playing(
                    funMessage));
        } catch (LoginException le) {
            le.printStackTrace();
        }
    }
}
