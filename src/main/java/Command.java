//import com.google.cloud.translate.Translate;
//import com.google.cloud.translate.Translate.TranslateOption;
//import com.google.cloud.translate.TranslateOptions;
//import com.google.cloud.translate.Translation;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.AudioManager;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

// contains all executable commands
public enum Command {
    clear {
        // clears
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            // number of messages to look through to clear
            int clearUpTo;

            // parse the argument
            if (args.length == 0) {
                clearUpTo = 100;
            }
            else {
                try {
                    clearUpTo = Integer.parseInt(args[0]);
                    if (clearUpTo < 1) {
                        throw new IllegalArgumentException("clearUpTo less than 1");
                    }
                }
                catch (NumberFormatException ex) {
                    e.getTextChannel().sendMessage("!clear <amount>").queue();
                    return;
                }
                catch (IllegalArgumentException ex) {
                    e.getTextChannel().sendMessage("Amount must be greater than 0").queue();
                    return;
                }
            }

            // clear the messages
            try {
                MessageChannel channel = e.getTextChannel();
                Iterator<Message> iterator = channel.getIterableHistory().iterator();
                int n = 0;
                while (n <= clearUpTo && iterator.hasNext()) {
                    Message m = iterator.next();
                    String msg = m.getContentDisplay();
                    // if the message starts with a command prefix or was sent by a bot
                    if (msg.startsWith(Starbot.COMMAND_PREFIX) || m.getAuthor().isBot() || m.getMentionedUsers().contains(e.getJDA().getSelfUser())) {
                        // delete it
                        m.delete().queue();
                    }
                    n++;
                }
            } catch (InsufficientPermissionException ex) {
                e.getChannel().sendMessage("I don't have permission to do that.").queue();
            }

            // send message that we're clearing
            e.getTextChannel().sendMessage("Cleared the last " + clearUpTo + " message(s).").queue();
        }
    },

    mute {
        // mutes the mentioned users
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            // get the mentioned members
            List<Member> members;
            // if @everyone was mentioned
            if (args[0].equalsIgnoreCase("all")) {
                // if the user doesn't have admin, return
                if (!e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                    e.getTextChannel().sendMessage("You don't have admin.").queue();
                    return;
                }
                members = e.getMember().getVoiceState().getChannel().getMembers();
            }
            // otherwise
            else {
                // if the user doesn't have mute permissions, return
                if (!e.getMember().hasPermission(Permission.VOICE_MUTE_OTHERS)) {
                    e.getTextChannel().sendMessage("You don't have permission.").queue();
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
            e.getTextChannel().sendMessage("good luck: https://pastebin.com/VCGG9vqK").queue();
        }
    },

    echo {
        // echoes the message back at the user
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            e.getTextChannel().sendMessage(Arrays.stream(args).collect(Collectors.joining(" "))).queue();
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

    ramen {
        // prints the Ramen King rap lyrics
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            e.getChannel().sendMessage("Yo, I still masturbate while eating top ramen\n" +
                    "At a faster rate in the bigger quantities\n" +
                    "It counts as rape when I'm slurping at this unbelievable pace\n" +
                    "I turn the temperature up all the way to sweat up on my face\n" +
                    "If you give no effort, if you got no money\n" +
                    "Then I got a cheap method\n" +
                    "Crack it open throw it in a pan and let it cook bitch\n" +
                    "Now that's a real education fuck books\n" +
                    "If you wanna make it in college acknowledge\n" +
                    "All the flavors that be dropping\n" +
                    "Mad knowledge on these pussy ass canned goods\n" +
                    "We got chicken and beef to boost the manhood\n" +
                    "Anybody want a piece of me will have to get this ramen first\n" +
                    "Start with the shrimp and then the fire\n" +
                    "If you're fully blazed then this shit'll get you higher, hah\n" +
                    "Thirty-five cents a pack, three for a dollar\n" +
                    "Unbelievable pricing that's the future of a blue collar worker\n" +
                    "And I'm talkin' bout ramen\n" +
                    "This shit'll fill you up when you're feeling like an African\n" +
                    "Come back when you're in the state that I'm in\n" +
                    "And say \"Hi\" to my homeboy top ramen").complete();
        }
    },

    bop {
        // provides a link to the Krabs Bop Channel
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            e.getChannel().sendMessage("https://www.youtube.com/watch?time_continue=1&v=CTJ4P1EmiD4").complete();
        }
    },

    vaporwave {
        // provides a link to macintosh plus
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            e.getChannel().sendMessage("https://www.youtube.com/watch?v=aQkPcPqTq4M").complete();
        }
    },

    invite {
        // sends the invite into the chat
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            e.getChannel().sendMessage("You make terrible decisions: " + e.getJDA().asBot().getInviteUrl()).complete();
        }
    },

    grade {
        // gives a grade based on an integer
        // method provided by Fran
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            TextChannel channel = e.getTextChannel();

            double grade = -1.0;
            try {
                grade = Double.parseDouble(args[0]);
            } catch (NumberFormatException ex) {
                // do nothing, lmao
            }

            if(grade > 100.0) {
                channel.sendMessage("IB doesn't give extra credit. You're a cheater.").queue();
            } else if(grade >= 92.0) {
                channel.sendMessage("Nice! That's an A!").queue();
            } else if(grade >= 88.0) {
                channel.sendMessage("Good stuff, that's an A-!").queue();
            } else if(grade >= 84.0) {
                channel.sendMessage("YOU'RE A FAILURE. IB STUDENTS ONLY GET A'S AND THAT'S A B+!").queue();
            } else if(grade >= 80.0) {
                channel.sendMessage("Don't let the haters get you down, that B is solid!").queue();
            } else if(grade >= 76.0) {
                channel.sendMessage("Hey, B- is B range and B range is best range.").queue();
            } else if(grade >= 72.0) {
                channel.sendMessage("Things may be looking rough, but ain't no C+ ever held you down!").queue();
            } else if(grade >= 68.0) {
                channel.sendMessage("You've got some work to do, but a C isn't the end of the world.").queue();
            } else if(grade >= 64.0) {
                channel.sendMessage("Yikes, that C- might sting!").queue();
            } else if(grade >= 60.0) {
                channel.sendMessage("A D+. Are you even trying?").queue();
            } else if(grade >= 52.0) {
                channel.sendMessage("Hang in there, even a D is redeemable!").queue();
            } else if(grade >= 0) {
                channel.sendMessage("Oh. An F. I'm sorry friend.").queue();
            } else {
                channel.sendMessage("Literally how...?").queue();
            }
        }
    },

    r {
        // sends a starbot response into the chat
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            // attempt to delete the message
            try {
                e.getMessage().delete().complete();
            } catch (InsufficientPermissionException ex) {
                // do nothing
            }
            // send a response
            e.getChannel().sendMessage(Responses.next()).complete();
        }
    },

    joinup {
        // joins the sender's voice call, if the sender is in a voice call
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            // get the voice channel the sender is in
            VoiceChannel channel = e.getMember().getVoiceState().getChannel();

            // if the author is connected to a voice channel in this guild, join it
            if (channel != null) {
                AudioManager audioManager = e.getGuild().getAudioManager();
                audioManager.setSendingHandler(new Audio().new AudioPlayerSendHandler());
                audioManager.openAudioConnection(channel);
            }
            else {
                e.getChannel().sendMessage("fucking how").queue();
            }
        }
    },

    play {
        // plays the song in the queue
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            // get the time
            // if it's 8:00 PM
            // System.out.println("Got hour of " + LocalTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("PST"))).getHour());
            if (LocalTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("PST"))).getHour() == 20) {
                // kick the user who wanted to play something from the server
                Guild guild = e.getGuild();
                Member member = e.getMember();

                // generate an invite to send
                Invite invite = e.getGuild().getDefaultChannel()
                        .createInvite()
                        .setMaxUses(1)
                        .setMaxAge(1000)
                        .complete();

                // send the user an invite
                member.getUser().openPrivateChannel().complete().sendMessage(invite.getURL()).queue();

                // kick the author of the message
                guild.getController().kick(member).reason("no.").queue();
            }
            else {
                Audio.AudioPlayerSendHandler handler = (Audio.AudioPlayerSendHandler) e.getGuild().getAudioManager().getSendingHandler();
                if (handler == null) {
                    e.getChannel().sendMessage("how").queue();
                }
                if (args.length == 0) {
                    handler.aPlay();
                }
                else {
                    handler.aPlay(args[0]);
                }
            }
        }
    },

    pleaseplay {
        // plays the song, even if it's 8:00 PM, and the user has proper perms
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            Member member = e.getMember();
            if (member.getPermissions().contains(Permission.ADMINISTRATOR) || member.getUser().getIdLong() == 478706068223164416L) {
                Audio.AudioPlayerSendHandler handler = (Audio.AudioPlayerSendHandler) e.getGuild().getAudioManager().getSendingHandler();
                if (handler == null) {
                    e.getChannel().sendMessage("how").queue();
                }
                if (args.length == 0) {
                    handler.aPlay();
                }
                else {
                    handler.aPlay(args[0]);
                }
            }
            else {
                e.getChannel().sendMessage("you're not important enough.").queue();
            }
        }
    },

    queue {
        // adds a song to the queue
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            Audio.AudioPlayerSendHandler handler = (Audio.AudioPlayerSendHandler) e.getGuild().getAudioManager().getSendingHandler();
            if (handler == null) {
                e.getChannel().sendMessage("how").queue();
            }
            if (args.length == 0) {
                String queued = handler.aGetQueueString();
                e.getChannel().sendMessage("queued songs:\n" + queued).queue();
            }
            else {
                handler.aQueue(args[0]);
            }
        }
    },

    skip {
        // skips a song
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            Audio.AudioPlayerSendHandler handler = (Audio.AudioPlayerSendHandler) e.getGuild().getAudioManager().getSendingHandler();
            if (handler == null) {
                e.getChannel().sendMessage("how").queue();
            }
            handler.aPlay();
        }
    },

    getout {
        // leaves the voice call
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            AudioManager audioManager = e.getGuild().getAudioManager();
            audioManager.closeAudioConnection();
        }
    },

    nword {
        // kicks justin from the dingus crew
        @Override
        public void run(MessageReceivedEvent e, String[] args) {
            // check if we're in the dingus crew
            if (e.getGuild().getIdLong() == 175372417194000384L) {
                // check if the person has the power to kick
                if (e.getMember().hasPermission(Permission.KICK_MEMBERS)) {
                    // kick justin
                    Member justin = e.getGuild().getMemberById(175104183119249408L);
                    // get his nick name
                    String nickname = justin.getNickname();
                    // if his name is a number, increment it by 1 and store it
                    try {
                        Starbot.justinBans = Integer.parseInt(nickname);
                    } catch (NumberFormatException ex) {
                        // do nothing I guess
                    }
                    e.getGuild().getController().kick(justin).reason("" + Starbot.justinBans).complete();
                    Starbot.justinBans++;

                    // send justin an invite
                    String invite = e.getTextChannel().createInvite().setMaxUses(1).complete().getURL();
                    justin.getUser().openPrivateChannel().complete().sendMessage(invite).queue();
                }
            }
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

//    jp {
//        // translates the comment into japanese
//        @Override
//        public void run(MessageReceivedEvent e, String[] args) {
//            String query = Arrays.stream(args).collect(Collectors.joining(" "));
//            if (query.length() < 300) {
//                // System.out.println(query);
//                Translate translate = TranslateOptions.getDefaultInstance().getService();
//                Translation translation = translate.translate(
//                        query,
//                        Translate.TranslateOption.sourceLanguage("en"),
//                        Translate.TranslateOption.targetLanguage("ja"));
//                query = translation.getTranslatedText();
//                // System.out.println(query);
//                e.getChannel().sendMessage(query).queue();
//            }
//        }
//    };

    // executes when the command is called
    public abstract void run(MessageReceivedEvent e, String[] args);

}
