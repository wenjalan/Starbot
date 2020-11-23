package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import wenjalan.starbot.engine.AudioEngine;
import wenjalan.starbot.engine.audio.MusicHandler;

public class ControllerCommand implements Command {

    @Override
    public String getName() {
        return "controller";
    }

    @Override
    public String getDescription() {
        return "Recreates the music controller";
    }

    @Override
    public String getUsage() {
        return "!controller";
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
        // check if we're playing something
        TextChannel channel = msg.getTextChannel();
        Guild g = msg.getGuild();
        AudioManager manager = g.getAudioManager();
        if (!manager.isConnected()) {
            channel.sendMessage("there isn't anything playing").queue();
            return;
        }

        // recreate the player
        MusicHandler handler = (MusicHandler) manager.getSendingHandler();
        handler.recreateController(channel);
        msg.delete().queue();
    }
}
