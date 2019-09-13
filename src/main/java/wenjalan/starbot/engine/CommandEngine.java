package wenjalan.starbot.engine;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

// handles the parsing and execution of commands
public class CommandEngine {

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

    // parses a command sent from a direct message
    public static void parseCommand(MessageReceivedEvent event) {
        // check what kind of channel we're in
        // private
        if (event.isFromType(ChannelType.PRIVATE)) {
            // handle it
            parseDMCommand(event);
        }
        // guild
        else if (event.isFromType(ChannelType.TEXT) && event.isFromGuild()) {
            parseGuildCommand(event);
        }
        // something else
        else {
            System.err.println("Command sent from an unknown source: " + event.getChannelType() + ":" +
                    event.getMessage().getContentRaw());
        }
    }

    // parses a DM-specific command
    protected static void parseDMCommand(MessageReceivedEvent event) {

    }

    // parses a Guild-specific command
    protected static void parseGuildCommand(MessageReceivedEvent event) {

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
