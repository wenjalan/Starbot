package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import wenjalan.starbot.engine.audio.MegaphoneHandler;

public class MegaphoneCommand implements Command {

    @Override
    public String getName() {
        return "megaphone";
    }

    @Override
    public String getDescription() {
        return "Starts repeating what you said in voice but louder (use command again to disconnect)";
    }

    @Override
    public String getUsage() {
        return "!megaphone <volume (float)>";
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
        // get the voice channel
        AudioManager manager = msg.getGuild().getAudioManager();
        TextChannel textChannel = msg.getTextChannel();
        GuildVoiceState state = msg.getMember().getVoiceState();
        if (!state.inVoiceChannel()) {
            textChannel.sendMessage("Join a voice channel idiot").queue();
        }
        VoiceChannel voiceChannel = state.getChannel();

        // if the user who issued the command is the one we were repeating, quit
        if (manager.isConnected()) {
            AudioReceiveHandler receivingHandler = manager.getReceivingHandler();
            // close if it was a music handler
            if (!(receivingHandler instanceof MegaphoneHandler)) {
                manager.closeAudioConnection();
            }
            else {
                // if it was the same user, disconnect
                MegaphoneHandler megaphoneHandler = (MegaphoneHandler) receivingHandler;
                long userId = megaphoneHandler.getUserId();
                if (msg.getAuthor().getIdLong() == userId) {
                    manager.closeAudioConnection();
                    return;
                }
            }
        }

        // get the specified volume
        String rawContent = msg.getContentRaw();
        String[] tokens = rawContent.split("\\s+");
        float volume = 2.0f;
        if (tokens.length > 1) {
            volume = Float.parseFloat(tokens[1]);
        }

        // set up megaphone handler
        MegaphoneHandler megaphoneHandler = new MegaphoneHandler(msg.getAuthor().getIdLong(), volume);
        manager.setSendingHandler(megaphoneHandler);
        manager.setReceivingHandler(megaphoneHandler);
        manager.openAudioConnection(voiceChannel);
    }
}
