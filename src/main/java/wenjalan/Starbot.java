package wenjalan;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import wenjalan.command.*;

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
        final String TOKEN = args[0];

        // initialize the bot
        JDA jda = JDABuilder.createLight(TOKEN)
                .addEventListeners(new Starbot())
                .build();

        // create a command listener and register commands
        CommandListener commandListener = new CommandListener();
        jda.addEventListener(commandListener);

        // this is where all slash commands are registered //
        commandListener.addCommand(new VersionCommand());
        commandListener.addCommand(new PollCommand());
        commandListener.addCommand(new SomeoneCommand());
        commandListener.addCommand(new InviteCommand());
        commandListener.addCommand(new ClearCommand());

        // register all commands with Discord
        for (SlashCommand c : commandListener.getRegisteredCommands()) {
            System.out.println("Registered command '" + c.getName() + "'");
            jda.upsertCommand(c.getData()).queue();
        }
    }

    // on bot ready
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        // print the bot invite to the console
        System.out.println("Starbot online!");
        String inviteUrl = event.getJDA().getInviteUrl();
        System.out.println("Invite: " + inviteUrl);
    }
}
