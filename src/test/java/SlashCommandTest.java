import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.junit.Test;
import wenjalan.command.*;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.stream.Collectors;

public class SlashCommandTest {

    public static final long LAKESIDE_ID = 419598386933530636L;

    public static void main(String[] args) throws LoginException, InterruptedException {
        // get token from env
        String token = System.getenv("rin");

        // set up bot
        JDA jda = JDABuilder.createLight(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .build()
                .awaitReady();

        // create new Command Listener and register some commands
        CommandListener commandListener = new CommandListener();
        jda.addEventListener(commandListener);
        commandListener.addCommand(new VersionCommand());
        commandListener.addCommand(new PollCommand());
        commandListener.addCommand(new SomeoneCommand());

        // register commands with Discord on Lakeside Lounge
        Guild lakeside = jda.getGuildById(LAKESIDE_ID);
        List<CommandData> data = commandListener.getRegisteredCommands().stream().map(SlashCommand::getData).collect(Collectors.toList());
        lakeside.updateCommands().addCommands(data).queue();
        System.out.println("Updated " + data.size() + " commands");
    }
}
