package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import wenjalan.starbot.engine.audio.MusicHandler;

public class ClearQueueCommand implements Command {
    @Override
    public String getName() {
        return "clearqueue";
    }

    @Override
    public String getDescription() {
        return "Clears the queue of tracks";
    }

    @Override
    public String getUsage() {
        return "!clearqueue";
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
        // clear the queue
        Guild g = msg.getGuild();
        MusicHandler handler = (MusicHandler) g.getAudioManager().getSendingHandler();
        handler.clearQueue(msg);
    }
}
