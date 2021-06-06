import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

/*
 * Main Starbot class, and program entry point
 */
public class Starbot extends ListenerAdapter {

    // program entry point and bot initialization
    public static void main(String[] args) throws LoginException {
        // get token from args
        if (args.length < 1) {
            System.err.println("Provide a bot token");
            System.exit(1);
        }
        final String TOKEN = args[1];

        // initialize the bot
        JDA jda = JDABuilder.createLight(TOKEN)
                .addEventListeners(new Starbot())
                .build();

        // todo: add slash commands here
        // jda.upsertCommand()
    }

    // on bot ready
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        // print the bot invite to the console
        System.out.println("Starbot 6.0 online!");
        String inviteUrl = event.getJDA().getInviteUrl();
        System.out.println("Invite: " + inviteUrl);
    }
}
