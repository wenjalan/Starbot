package wenjalan.starbot.engine;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.util.List;

// handles the parsing and execution of commands
public class CommandEngine {

    // dm command enum
    protected enum DMCommand {

        // reports the version of starbot
        version {
            @Override
            public void execute(PrivateMessageReceivedEvent event) {
                // send the current version
                event.getChannel().sendMessage("4.0 prerelease").queue();
            }
        },

        // sends a link to invite Starbot
        invite {
            @Override
            public void execute(PrivateMessageReceivedEvent event) {
                // send an invite
                event.getChannel().sendMessage(event.getJDA().getInviteUrl()).queue();
            }
        },

        // shuts down starbot, only usable by me
        stop {
            @Override
            public void execute(PrivateMessageReceivedEvent event) {
                // check that the author is me
                long id = event.getAuthor().getIdLong();

                // if the author is me
                if (id == 478706068223164416L) {
                    // shut down
                    event.getChannel().sendMessage("you got it boss").complete();

                    // log that we're shutting down because I said to
                    System.out.println("Shut down initiated by user " + event.getAuthor().getAsTag() +
                            " (ID: " + event.getAuthor().getIdLong() + ")");

                    // actually shut down
                    event.getJDA().shutdownNow();
                }
            }
        };

        public abstract void execute(PrivateMessageReceivedEvent event);

    }

    // guild command enum
    protected enum GuildCommand {

        // reports the version of starbot
        version {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                event.getChannel().sendMessage("4.0 prerelease").queue();
            }
        },

        // sends a link to invite Starbot
        invite {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // send an invite
                event.getChannel().sendMessage(event.getJDA().getInviteUrl()).queue();
            }
        },

        // clears a number of Starbot-sent messages and commands from the chat, default 10
        clear {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // check for permissions
                User self = event.getJDA().getSelfUser();
                if (!event.getGuild().getMember(self).hasPermission(
                        Permission.MESSAGE_HISTORY,
                        Permission.MESSAGE_MANAGE
                )) {
                    // if not, complain and return
                    event.getChannel().sendMessage("gimme the perms first").queue();
                    return;
                }

                // find out how many messages to clear
                int victims = 10;
                String[] args = parseArgs(event.getMessage());
                try {
                    // if there is a number there
                    if (args.length > 1) {
                        // get the number
                        int requestedVictims = Integer.parseInt(args[1]);
                        // if it's less than 1, say no
                        if (requestedVictims < 1) {
                            throw new IllegalArgumentException("too small, at least 1");
                        }
                        // otherwise, set the victims and get on with life
                        victims = requestedVictims;
                    }
                } catch (IllegalArgumentException e) {
                    // do nothing
                }

                // actually clear the messages
                event.getChannel().sendMessage("alright one sec").queue();
                List<Message> messages = event.getChannel().getHistory().retrievePast(victims).complete();
                long selfId = event.getJDA().getSelfUser().getIdLong();
                int messagesCleared = 0;
                for (Message msg : messages) {
                    // if it was sent by Starbot, delete it
                    if (msg.getAuthor().getIdLong() == selfId) {
                        msg.delete().queue();
                        messagesCleared++;
                    }
                    // if the message was a command, delete it
                    else if (CommandEngine.isCommand(msg)) {
                        msg.delete().queue();
                        messagesCleared++;
                    }
                    // if the message mentions Starbot, delete it
                    else if (msg.getMentionedUsers().contains(event.getJDA().getSelfUser())) {
                        msg.delete();
                        messagesCleared++;
                    }
                }

                // send that we're done
                event.getChannel().sendMessage("deleted " + messagesCleared + " messages").queue();
            }
        };

        public abstract void execute(GuildMessageReceivedEvent event);

    }

    // the global command prefix, used to indicate whether a message is a command
    // can be changed via admin controls
    protected static String globalCommandPrefix = "!";

    // helper method to recognize if a message is a command
    public static boolean isCommand(Message msg) {
        // get the content
        String content = msg.getContentRaw();

        // return if the message starts with the command prefix
        return content.startsWith(globalCommandPrefix);
    }

    // parses a DM-specific command
    public static void parseDMCommand(PrivateMessageReceivedEvent event) {
        // get the command they wanted
        String commandKeyword = event.getMessage().getContentRaw().split("\\s+")[0].substring(1);

        // find out if it's a valid command
        for (DMCommand c : DMCommand.values()) {
            // if the name of the command matches the keyword
            if (commandKeyword.equalsIgnoreCase(c.name())) {
                // run that command
                c.execute(event);
                // stop executing commands
                return;
            }
        }
        // do nothing if no commands matched the keyword
    }

    // parses a Guild-specific command
    public static void parseGuildCommand(GuildMessageReceivedEvent event) {
        // get the command they wanted
        String commandKeyword = event.getMessage().getContentRaw().split("\\s+")[0].substring(1);

        // find out if it's a valid command
        for (GuildCommand c : GuildCommand.values()) {
            // if the name of the command matches the keyword
            if (commandKeyword.equalsIgnoreCase(c.name())) {
                // run that command
                c.execute(event);
                // stop executing commands
                return;
            }
        }
        // do nothing if no commands matched the keyword
    }

    // parses the arguments of a command into Strings
    protected static String[] parseArgs(Message m) {
        return m.getContentRaw().split("\\s+");
    }

    // returns the global command prefix
    public static String getGlobalCommandPrefix() {
        return globalCommandPrefix;
    }

    // sets the global command prefix
    public static void setGlobalCommandPrefix(String newPrefix) {
        globalCommandPrefix = newPrefix;
    }

}
