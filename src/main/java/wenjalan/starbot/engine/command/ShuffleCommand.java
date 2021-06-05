package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.managers.AudioManager;
import wenjalan.starbot.engine.audio.MusicHandler;

public class ShuffleCommand implements Command {
    @Override
    public String getName() {
        return "shuffle";
    }

    @Override
    public String getDescription() {
        return "Shuffles the currently queued tracks";
    }

    @Override
    public String getUsage() {
        return "!shuffle";
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
        // check that we're in a voice channel
        Guild g = msg.getGuild();
        AudioManager audioManager = g.getAudioManager();
        if (audioManager.isConnected()) {
            MusicHandler handler = (MusicHandler) audioManager.getSendingHandler();
            handler.shuffle();
        }
        msg.delete().queue();
    }
}
