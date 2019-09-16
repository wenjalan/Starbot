package wenjalan.starbot.engine;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

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
                            "\nID: " + event.getAuthor().getIdLong());

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

    // returns the global command prefix
    public static String getGlobalCommandPrefix() {
        return globalCommandPrefix;
    }

    // sets the global command prefix
    public static void setGlobalCommandPrefix(String newPrefix) {
        globalCommandPrefix = newPrefix;
    }

}
