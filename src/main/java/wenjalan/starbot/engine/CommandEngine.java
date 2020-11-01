package wenjalan.starbot.engine;

import net.dv8tion.jda.api.entities.Message;
import wenjalan.starbot.engine.command.Command;
import wenjalan.starbot.engine.command.Version;

import java.util.ArrayList;
import java.util.List;

// handles all command-related functionality
public class CommandEngine {

    // singleton
    private static CommandEngine instance = null;

    // the command prefix for all commands
    public static final String COMMAND_PREFIX = "!";

    // a list of executable Commands
    private List<Command> commands;

    // singleton constructor
    private CommandEngine() {
        if (instance != null) {
            throw new IllegalStateException("New instances of CommandEngine cannot be instantiated");
        }
        this.commands = loadCommands();
    }

    // loads all available commands
    private List<Command> loadCommands() {
        // add all commands to the list
        // ! this is where all commands must be registered !
        ArrayList<Command> commands = new ArrayList<>();
        commands.add(new Version());
        return commands;
    }

    // returns the instance of the CommandEngine
    public static CommandEngine get() {
        if (instance == null) {
            instance = new CommandEngine();
        }
        return instance;
    }

    // returns whether the message is a command
    // message: the Message object to check for a command
    public boolean isCommand(Message msg) {
        // strip message content
        String rawContent = msg.getContentRaw().trim();

        // if it starts with the command prefix
        if (rawContent.startsWith(COMMAND_PREFIX)) {
            // split the content into argument tokens
            String[] args = rawContent.substring(1).split("\\s+");

            // look for a command that matches the first token
            for (Command command : commands) {
                // if one is found, return true
                if (args[0].equalsIgnoreCase(command.getName())) {
                    return true;
                }
            }
        }

        // otherwise return false
        return false;
    }

    // parses a guild command
    // msg: the Message object associated with the command, verified that it is
    //      a valid command either manually or through #isCommand()
    public void parseGuildCommand(Message msg) {
        // strip message content
        String rawContent = msg.getContentRaw().trim();
        String[] args = rawContent.substring(1).split("\\s+");

        // find the associated command and run it
        for (Command command : commands) {
            if (command.isGuildCommand() && args[0].equalsIgnoreCase(command.getName())) {
                command.run(msg);
                return;
            }
        }

        // if we get here, no command was found
        throw new IllegalArgumentException("A guild command failed to execute: " + rawContent);
    }

    // parses a dm command
    // msg: the Message object associated with the command, verified that it is
    //      a valid command either manually or through #isCommand()
    public void parseDmCommand(Message msg) {
        // strip message content
        String rawContent = msg.getContentRaw().trim();
        String[] args = rawContent.substring(1).split("\\s+");

        // find the associated command and run it
        for (Command command : commands) {
            if (command.isDmCommand() && args[0].equalsIgnoreCase(command.getName())) {
                command.run(msg);
                return;
            }
        }

        // if we get here, no command was found
        throw new IllegalArgumentException("A dm command failed to execute: " + rawContent);
    }

}
