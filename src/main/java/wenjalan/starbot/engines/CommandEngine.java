package wenjalan.starbot.engines;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import wenjalan.starbot.listeners.MessageListener;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

// contains all the commands and their implementations
public class CommandEngine {

    // the available commands and their implementations
    public enum Command {

        clear {
            // clears the last n messages
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // check for permissions
                if (!e.getGuild().getMember(e.getJDA().getSelfUser()).hasPermission(
                        Permission.MESSAGE_HISTORY,
                        Permission.MESSAGE_MANAGE
                )) {
                    // send an error into chat
                    e.getTextChannel().sendMessage("give me perms.").queue();
                    return;
                }

                // default message clearing
                int victims = 10;
                try {
                    if (args.length > 0) victims = Integer.parseInt(args[0]);
                } catch (NumberFormatException ex) {
                    // do nothing
                }

                // get the channel
                TextChannel channel = e.getTextChannel();

                // announce
                channel.sendMessage("clearing " + victims + " messages...").queue();

                // get messages
                List<Message> history = channel.getHistory().retrievePast(victims).complete();

                // feedback
                // channel.sendMessage("retrieved " + history.size() + " messages").queue();

                // iterate
                Iterator<Message> iter = history.iterator();
                int deleted = 0;
                Message msg;
                while (iter.hasNext()) {
                    // get the next message
                    msg = iter.next();

                    // if message came from Starbot
                    if (msg.getAuthor().getIdLong() == e.getJDA().getSelfUser().getIdLong()) {
                        // delete it
                        msg.delete().queue();
                        deleted++;
                    }
                    // if the message is a command
                    else if (MessageListener.isCommand(msg.getContentRaw())) {
                        // delete it
                        msg.delete().queue();
                        deleted++;
                    }
                    // if the message mentions Starbot
                    else if (msg.getMentionedUsers().contains(e.getJDA().getSelfUser())) {
                        // delete it
                        msg.delete().queue();
                        deleted++;
                    }
                }
                // send a done message
                channel.sendMessage("deleted " + deleted + " messages").queue();
            }
        },

        mute {
            // mutes the mentioned users
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // check for permissions
                if (!e.getGuild().getMember(e.getJDA().getSelfUser()).hasPermission(
                        Permission.VOICE_MUTE_OTHERS
                )) {
                    e.getTextChannel().sendMessage("give me perms").queue();
                    return;
                }

                // get the mentioned members
                List<Member> members;
                // if @everyone was mentioned
                if (args[0].equalsIgnoreCase("all")) {
                    // if the user doesn't have admin, return
                    if (!e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                        e.getTextChannel().sendMessage("you're not important enough").queue();
                        return;
                    }
                    members = e.getMember().getVoiceState().getChannel().getMembers();
                }
                // otherwise
                else {
                    // if the user doesn't have mute permissions, return
                    if (!e.getMember().hasPermission(Permission.VOICE_MUTE_OTHERS)) {
                        e.getTextChannel().sendMessage("you're not important enough.").queue();
                        return;
                    }
                    members = e.getMessage().getMentionedMembers();
                }
                // mute or unmute each one
                for (Member m : members) {
                    if (!m.isOwner()) {
                        if (m.getVoiceState().inVoiceChannel() && !m.getVoiceState().isMuted()) {
                            e.getGuild().getController().setMute(m, true).queue();
                        }
                        else if (m.getVoiceState().inVoiceChannel() && m.getVoiceState().isMuted()) {
                            e.getGuild().getController().setMute(m, false).queue();
                        }
                    }
                }
            }
        },

        help {
            // sends a link to the source code as "help"
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                e.getTextChannel().sendMessage("good luck: https://github.com/wenjalan/Starbot/blob/master/src/main/java/wenjalan/starbot/engines/CommandEngine.java").queue();
            }
        },

        echo {
            // echoes the message back at the user
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                e.getTextChannel().sendMessage(String.join(" ", args)).queue();
            }
        },

        someone {
            // mentions a random user, similar to how @someone worked during Discord's April Fools
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                List<Member> members = e.getGuild().getMembers();
                Member someone = members.get(new Random().nextInt(members.size()));
                e.getTextChannel().sendMessage(someone.getAsMention()).queue();
            }
        },

        invite {
            // sends the invite into the chat
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                e.getChannel().sendMessage("you make terrible decisions").complete();
                e.getChannel().sendMessage(e.getJDA().asBot().getInviteUrl()).complete();
            }
        },

        lofi {
            // makes the bot join and start playing lofi beats
            // https://www.twitch.tv/chillhopmusic
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // play the pillar man theme
                wakeUpAndPlay(e, "https://www.twitch.tv/chillhopmusic");
            }
        },

        awaken {
            // plays the Pillar Man Theme from JoJo's Bizarre Adventure
            // https://www.youtube.com/watch?v=XUhVCoTsBaM
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // play the pillar man theme
                wakeUpAndPlay(e, "https://www.youtube.com/watch?v=XUhVCoTsBaM");
            }
        },

        shinydays {
            // plays Shiny Days from Yuru Camp
            // https://www.youtube.com/watch?v=DCr-r0ZP9P8
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                wakeUpAndPlay(e, "https://www.youtube.com/watch?v=DCr-r0ZP9P8");
            }
        },

        monstercat {
            // plays the Monstercat Radio 24/7
            // https://www.twitch.tv/monstercat
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                wakeUpAndPlay(e, "https://www.twitch.tv/monstercat");
            }
        },

        joinup {
            // creates a new AudioEngine to play audio
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // check if the author is in a VoiceChannel
                VoiceChannel voiceChannel = e.getMember().getVoiceState().getChannel();

                // if none, quit
                if (voiceChannel == null) {
                    e.getChannel().sendMessage("fucking where").queue();
                    return;
                }

                // check if the bot's already in a voice channel on this server
                if (e.getGuild().getAudioManager().isConnected()) {
                    e.getChannel().sendMessage("I'm here you dipshit").queue();
                    return;
                }

                // create an AudioEngine for this guild
                createAudioEngine(e.getGuild(), voiceChannel);

                // sout
                System.out.println("connected to " + voiceChannel.getName() + " in " + e.getGuild().getName() + " for audio playback");
            }
        },

        queue {
            // pauses track playback
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // get the handler
                AudioEngine.SendHandler handler = getSendHandler(e.getGuild());

                // if none
                if (handler == null) {
                    e.getTextChannel().sendMessage("fucking how").queue();
                    return;
                }

                // if the args were empty, print the queue
                if (args.length == 0) {
                    // print the queue
                    e.getTextChannel().sendMessage(handler.getQueueToString()).queue();
                }
                else {
                    // queue the args
                    handler.queue(args[0]);
                }
            }
        },

        play {
            // pauses track playback
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // check if we're in a voice channel
                // get the handler
                AudioEngine.SendHandler handler = getSendHandler(e.getGuild());

                // if none
                if (handler == null) {
                    e.getTextChannel().sendMessage("fucking how").queue();
                    return;
                }

                // play the song
                handler.play(args[0]);
            }
        },

        pause {
            // pauses track playback
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // get the handler
                AudioEngine.SendHandler handler = getSendHandler(e.getGuild());

                // if none
                if (handler == null) {
                    e.getTextChannel().sendMessage("fucking how").queue();
                    return;
                }

                // pause
                handler.pause();
            }
        },

        resume {
            // pauses track playback
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // get the handler
                AudioEngine.SendHandler handler = getSendHandler(e.getGuild());

                // if none
                if (handler == null) {
                    e.getTextChannel().sendMessage("fucking how").queue();
                    return;
                }

                // resume
                handler.resume();
            }
        },

        clearqueue {
            // pauses track playback
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // get the handler
                AudioEngine.SendHandler handler = getSendHandler(e.getGuild());

                // if none
                if (handler == null) {
                    e.getTextChannel().sendMessage("fucking how").queue();
                    return;
                }

                // clear the queue
                handler.clearQueue();

                // send feedback
                e.getTextChannel().sendMessage("cleared.").queue();
            }
        },

        skip {
            // pauses track playback
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // get the handler
                AudioEngine.SendHandler handler = getSendHandler(e.getGuild());

                // if none
                if (handler == null) {
                    e.getTextChannel().sendMessage("fucking how").queue();
                    return;
                }

                // skip
                handler.skip();
            }
        },

        volume {
            // pauses track playback
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // check if we're in a voice channel
                if (!voiceChannelConnected(e.getGuild())) {
                    e.getChannel().sendMessage("fucking how").queue();
                    return;
                }

                // if no args were specified, quit
                if (args.length == 0) {
                    e.getChannel().sendMessage("to WHAT?").queue();
                    return;
                }

                // parse args
                try {
                    // get new volume
                    int volume = Integer.parseInt(args[0]);

                    // check if number is 0-100
                    if (volume > 100 || volume < 0) throw new IllegalArgumentException();

                    // get the SendHandler
                    AudioEngine.SendHandler handler = (AudioEngine.SendHandler) e.getGuild().getAudioManager().getSendingHandler();
                    handler.setVolume(volume);
                } catch (NumberFormatException ex) {
                    e.getTextChannel().sendMessage("what the fuck is " + args[0]).queue();
                } catch (IllegalArgumentException ex) {
                    e.getTextChannel().sendMessage("try again with a better number").queue();
                }

            }
        },

        playing {
            // pauses track playback
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // get the handler
                AudioEngine.SendHandler handler = getSendHandler(e.getGuild());

                // if none
                if (handler == null) {
                    e.getTextChannel().sendMessage("fucking how").queue();
                    return;
                }

                // send whatever's playing
                // get the SendHandler
                e.getTextChannel().sendMessage(handler.playing()).queue();
            }
        },

        getout {
            // disconnects the AudioEngine if connected
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // check if we're in a voice channel
                if (!voiceChannelConnected(e.getGuild())) {
                    e.getChannel().sendMessage("fucking how").queue();
                    return;
                }

                // get the voice channel
                VoiceChannel voiceChannel = e.getGuild().getMember(e.getJDA().getSelfUser()).getVoiceState().getChannel();

                // disconnect the AudioManager
                AudioManager audioManager = e.getGuild().getAudioManager();
                audioManager.closeAudioConnection();

                // sout
                System.out.println("disconnected from " + voiceChannel.getName() + " in " + e.getGuild().getName());
            }
        },

        commands {
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                String commands = "";
                for (Command c : Command.values()) {
                    commands += c.name() + "\n";
                }
                e.getChannel().sendMessage(commands).queue();
            }
        };

        // executes when the command is called
        public abstract void run(MessageReceivedEvent e, String[] args);

        ////////////////////
        // Shared Methods //
        ////////////////////

        // creates an AudioEngine for a VoiceChannel in a Guild
        public static AudioEngine createAudioEngine(Guild g, VoiceChannel channel) {
            AudioManager manager = g.getAudioManager();
            AudioEngine engine = new AudioEngine();
            manager.setSendingHandler(engine.sendHandler());
            manager.openAudioConnection(channel);
            return engine;
        }

        // returns the SendHandler of a Guild, null if none
        public static AudioEngine.SendHandler getSendHandler(Guild g) {
            if (voiceChannelConnected(g)) {
                return (AudioEngine.SendHandler) g.getAudioManager().getSendingHandler();
            }
            return null;
        }

        // checks if the bot is in a voice channel in this guild
        protected static boolean voiceChannelConnected(Guild g) {
            User starbot = g.getJDA().getSelfUser();
            return g.getMember(starbot).getVoiceState().getChannel() != null;
        }

        // makes the bot join and immediately start playing something
        // starts playing the query if the bot's already in
        protected void wakeUpAndPlay(MessageReceivedEvent e, String query) {
            // get the handler
            AudioEngine.SendHandler handler = getSendHandler(e.getGuild());

            // if none
            if (handler == null) {
                // connect a new AudioEngine
                // check if the author is in a VoiceChannel
                VoiceChannel voiceChannel = e.getMember().getVoiceState().getChannel();

                // if none, quit
                if (voiceChannel == null) {
                    e.getChannel().sendMessage("fucking where").queue();
                    return;
                }

                // create an AudioEngine for this guild
                AudioEngine engine = createAudioEngine(e.getGuild(), voiceChannel);

                // sout
                System.out.println("connected to " + voiceChannel.getName() + " in " + e.getGuild().getName() + " for audio playback");

                // retrieve the handler again
                handler = engine.sendHandler();

                // serr if still null
                if (handler == null) {
                    System.err.println("couldn't find the SendHandler to play lofi.");
                    return;
                }
            }

            // play lofi beats 24/7 to study and chill to
            handler.play(query);
        }

    }

}
