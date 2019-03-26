package wenjalan.starbot.servers;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import wenjalan.starbot.Starbot;
import wenjalan.starbot.Users;
import wenjalan.starbot.engines.AudioEngine;
import wenjalan.starbot.engines.CommandEngine;

import java.time.*;

// features specific to The Lakeside Lounge
public class LakesideLounge {

    // fireplace voice channel
    public static final long FIREPLACE_ID_LONG = 552252429908049930L;

    // server id
    public static final long ID_LONG = 419598386933530636L;
    public static final String ID_STRING = "419598386933530636";

    // when someone joins #fireplace, start playing a fireplace track
    public static class FireplaceListener extends ListenerAdapter {

        // when someone joins a voice channel
        @Override
        public void onGuildVoiceJoin(GuildVoiceJoinEvent e) {
            // check if the channel is #fireplace
            if (e.getChannelJoined().getIdLong() != FIREPLACE_ID_LONG) {
                // return
                return;
            }

            // if it is, start playing a fireplace video
            playFireplace(e.getGuild(), e.getChannelJoined());
        }

        // plays a fireplace sound
        protected void playFireplace(Guild g, VoiceChannel c) {
            // get the handler
            AudioEngine.SendHandler handler = CommandEngine.Command.getSendHandler(g);

            // if none
            if (handler == null) {
                // connect a new AudioEngine
                // check if the author is in a VoiceChannel
                VoiceChannel voiceChannel = c;

                // if none, quit
                if (voiceChannel == null) {
                    // e.getChannel().sendMessage("fucking where").queue();
                    return;
                }

                // create an AudioEngine for this guild
                AudioEngine engine = CommandEngine.Command.createAudioEngine(g, voiceChannel);

                // sout
                System.out.println("connected to " + voiceChannel.getName() + " in " + g.getName() + " for audio playback");

                // retrieve the handler again
                handler = engine.sendHandler();

                // serr if still null
                if (handler == null) {
                    System.err.println("couldn't find the SendHandler to play the fireplace.");
                    return;
                }
            }

            // play fireplace
            handler.play("https://www.youtube.com/watch?v=UgHKb_7884o");
        }
    }

    // update the main channel description with a random response
    public static class ChannelDescriptionUpdater implements Runnable {

        // Starbot
        protected Starbot starbot;

        // constructor
        public ChannelDescriptionUpdater(Starbot starbot) {
            this.starbot = starbot;
        }

        @Override
        public void run() {
            try {
                // get the ZoneId for PST
                ZoneId zoneId = ZoneId.of(ZoneId.SHORT_IDS.get("PST"));
                // the last numerical hour we updated the desc
                int lastUpdateHour = -1;
                // now
                LocalDateTime now;
                // loop forever
                for (;;) {
                    // update now
                    now = LocalDateTime.now(zoneId);
                    // if the hour now is different from the last one
                    if (now.getHour() != lastUpdateHour) {
                        // update
                        update();
                        // update the last update
                        lastUpdateHour = now.getHour();
                    }
                    // sleep for 10 seconds
                    Thread.sleep(10000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                // tell me
                User me = starbot.jda().getUserById(Users.ALAN);
                me.openPrivateChannel().complete().sendMessage("LakesideLounge.ChannelDescriptionUpdater was interrupted, come check it out").queue();
            }

        }

        // updates the description of #lounge with a random response
        protected void update() {
            // get a description
            String description;
            do {
                description = starbot.getResponseEngine().getNextResponse();
            } while (description.length() >= 1000);

            // update it in Lakeside
            Guild lakeside = starbot.jda().getGuildById(ID_STRING);
            if (lakeside == null) {
                System.err.println("couldn't find lakeside lounge.");
                return;
            }
            TextChannel lounge = lakeside.getTextChannelById(419598386933530638L);
            lounge.getManager().setTopic(description).queue();
        }

    }

}
