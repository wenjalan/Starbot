package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.managers.AudioManager;
import wenjalan.starbot.engine.audio.MusicHandler;

public class SeekCommand implements Command {
    @Override
    public String getName() {
        return "seek";
    }

    @Override
    public String getDescription() {
        return "Seeks to a certain position in an audio track";
    }

    @Override
    public String getUsage() {
        return "!seek <hh:mm:ss>";
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
            // get the input
            String query = msg.getContentRaw().substring("!seek".length()).trim();

            // calculate the position in the track
            long position = jonoGetPosition(query);

            // navigate to that position
            handler.seekTo(position);
        }

        // delete the message
        msg.delete().queue();
    }

    // format: hh:mm:ss
    // returns: a long equivalent to the number of hours, minutes, and seconds provided in milliseconds
    // author: https://github.com/m0rticus
    public static long jonoGetPosition(String str) {
        String[] arr = str.split(":");
        int factor = 1;
        int sum = 0;
        for (int i = arr.length; i > 0; i--) {
            sum += factor * Integer.parseInt(arr[i-1]) * 1000;
            factor *= 60;
        }
        return sum;
    }

}
