package wenjalan.starbot.engines;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import wenjalan.starbot.listeners.MessageListener;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static wenjalan.starbot.listeners.MessageListener.COMMAND_PREFIX;

// contains all the commands and their implementations
public class CommandEngine {

    // the MessageListener this CommandEngine belongs to
    protected MessageListener messageListener;

    // constructor
    public CommandEngine(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

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
                    victims = Integer.parseInt(args[0]);
                } catch (NumberFormatException ex) {
                    // do nothing
                }

                // get the channel
                TextChannel channel = e.getTextChannel();

                // announce
                channel.sendMessage("clearing " + victims + " messages...").queue();

                // get messages
                List<Message> history = channel.getHistory().getRetrievedHistory();

                // iterate
                Iterator<Message> iter = history.iterator();
                int deleted = 0;
                Message msg;
                while (iter.hasNext() && deleted <= victims) {
                    // get the next message
                    msg = iter.next();

                    // if message came from Starbot
                    if (msg.getAuthor().getIdLong() == e.getJDA().getSelfUser().getIdLong()) {
                        // delete it
                        msg.delete().queue();
                    }
                    // if the message is a command
                    else if (MessageListener.isCommand(msg.getContentRaw())) {
                        // delete it
                        msg.delete().queue();
                    }
                    // if the message mentions Starbot
                    else if (msg.getMentionedUsers().contains(e.getJDA().getSelfUser())) {
                        // delete it
                        msg.delete().queue();
                    }
                }
                // send a done message
                channel.sendMessage("done").queue();
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

    }

}
