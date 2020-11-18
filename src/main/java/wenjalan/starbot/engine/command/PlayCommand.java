package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.entities.Message;
import wenjalan.starbot.engine.AudioEngine;

// starts music playback in a voice channel
public class PlayCommand implements Command {
    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getDescription() {
        return "Starts music playback in a voice channel";
    }

    @Override
    public String getUsage() {
        return "!play <search query | YouTube, SoundCloud, or direct mp3 URL>";
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
        audio.startPlayback(msg);
    }
}
