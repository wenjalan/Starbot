package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.managers.AudioManager;
import wenjalan.starbot.engine.audio.MusicHandler;

public class RepeatCommand implements Command {
    @Override
    public String getName() {
        return "repeat";
    }

    @Override
    public String getDescription() {
        return "Toggles repeat the current playing track";
    }

    @Override
    public String getUsage() {
        return "!repeat";
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
        // check if we're playing
        Guild g = msg.getGuild();
        AudioManager audioManager = g.getAudioManager();
        if (audioManager.isConnected()) {
            MusicHandler handler = (MusicHandler) audioManager.getSendingHandler();
            handler.setRepeating(!handler.isRepeat());
        }

        // delete the message
        msg.delete().queue();
    }
}
