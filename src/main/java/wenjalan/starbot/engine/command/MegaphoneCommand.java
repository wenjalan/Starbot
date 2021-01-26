package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;
import wenjalan.starbot.engine.audio.MegaphoneHandler;

import java.util.stream.Collectors;

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
                // if it was the same user and no args were provided, disconnect
                MegaphoneHandler megaphoneHandler = (MegaphoneHandler) receivingHandler;
                long userId = megaphoneHandler.getUserId();
                if (msg.getAuthor().getIdLong() == userId && msg.getContentRaw().split("\\s+").length == 1) {
                    manager.closeAudioConnection();
                    return;
                }
            }
        }

        // get the specified volume, and/or person that was mentioned
        // todo: less spaghet
        String rawContent = msg.getContentRaw();
        String[] tokens = rawContent.split("\\s+");
        long targetId = msg.getAuthor().getIdLong();
        float volume = 2.0f;
        if (tokens.length > 1) {
            // if the message contained a mention, assume the first token was a @
            if (msg.getMentionedUsers().size() > 0) {
                targetId = msg.getMentionedUsers().get(0).getIdLong();
                // if the target isn't in the channel, complain
                if (!voiceChannel.getMembers().stream().map(ISnowflake::getIdLong).collect(Collectors.toList()).contains(targetId)) {
                    textChannel.sendMessage("that person isn't in voice").queue();
                    return;
                }

                // if there was another token after that, get the volume
                if (tokens.length > 2) {
                    volume = Float.parseFloat(tokens[2]);
                }
            }
            else {
                volume = Float.parseFloat(tokens[1]);
            }
        }

        // set up megaphone handler
        MegaphoneHandler megaphoneHandler = new MegaphoneHandler(targetId, volume);
        manager.setSendingHandler(megaphoneHandler);
        manager.setReceivingHandler(megaphoneHandler);
        manager.openAudioConnection(voiceChannel);
    }
}
