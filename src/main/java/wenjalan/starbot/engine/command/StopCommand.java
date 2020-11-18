package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.entities.Message;
import wenjalan.starbot.engine.AudioEngine;

// stops music playback in a voice channel
public class StopCommand implements Command {
    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Stops music playback in a voice channel";
    }

    @Override
    public String getUsage() {
        return "!stop";
    }

    @Override
    public boolean isGuildCommand() {
        return true;
    }

    @Override
    public boolean isDmCommand() {
        return false;
    }

    @Override
    public boolean isAdminCommand() {
        return false;
    }

    @Override
    public void run(Message msg) {
        AudioEngine audio = AudioEngine.getInstance();
        audio.stopPlayback(msg.getGuild());
    }
}
