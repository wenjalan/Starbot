package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import wenjalan.starbot.engine.CommandEngine;

import java.awt.*;

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
    public boolean isAdminCommand() {
        return false;
    }

    @Override
    public void run(Message msg) {
        // respond with some version information
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Starbot Beta 5.3", "https://github.com/wenjalan/Starbot");
        embed.setDescription("For latest info, visit the GitHub Repo");
        embed.setColor(Color.CYAN);
        msg.getChannel().sendMessage(embed.build()).queue();
    }
}
