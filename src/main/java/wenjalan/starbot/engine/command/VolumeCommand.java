package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.managers.AudioManager;
import wenjalan.starbot.engine.AudioEngine;
import wenjalan.starbot.engine.audio.MusicHandler;

// sets the volume of the music playback
public class VolumeCommand implements Command {
    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public String getDescription() {
        return "Sets the playback volume for music";
    }

    @Override
    public String getUsage() {
        return "!volume <0-100>";
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

            // parse arg
            String rawContent = msg.getContentRaw();
            String arg = rawContent.substring("!volume".length()).trim();
            try {
                int volume = Integer.parseInt(arg);
                volume = Math.max(0, volume);
                volume = Math.min(volume, 100);
                handler.setVolume(volume);
            } catch (NumberFormatException e) {
                msg.getChannel().sendMessage("Invalid volume: " + arg).queue();
            }
        }

        // delete the command
        msg.delete().queue();
    }
}
