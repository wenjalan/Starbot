package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.entities.Message;
import wenjalan.starbot.engine.CommandEngine;

// reports the version information about Starbot
public class VersionCommand implements Command {

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public String getDescription() {
        return "Reports version information about this build of Starbot";
    }

    @Override
    public String getUsage() {
        return CommandEngine.COMMAND_PREFIX + "version";
    }

    @Override
    public boolean isGuildCommand() {
        return true;
    }

    @Override
    public boolean isDmCommand() {
        return true;
    }

    @Override
    public void run(Message msg) {
        // respond with some version information
        msg.getChannel().sendMessage(
                "Starbot Beta 5.0\n" +
                "For latest updates, visit GitHub: https://github.com/wenjalan/Starbot"
        ).queue();
    }
}
