package wenjalan.starbot.engine;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import wenjalan.starbot.engine.command.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// handles all command-related functionality
public class CommandEngine {

    // the help command
    // defined here because it needs access to all activated commands
    public class HelpCommand implements Command {
        @Override
        public String getName() {
            return "help";
        }

        @Override
        public String getDescription() {
            return "Provides information on commands and their usages";
        }

        @Override
        public String getUsage() {
            return "!help <command>";
        }

        @Override
        public boolean isGuildCommand() {
            return true;
        }

        @Override
        public boolean isDmCommand() {
            return true;
        }

        @Override
        public boolean isAdminCommand() {
            return false;
        }

        @Override
        public void run(Message msg) {
            // get args and channel
            String[] args = msg.getContentRaw().split("\\s+");
            MessageChannel channel = msg.getChannel();
            boolean isPrivate = msg.getChannelType().equals(ChannelType.PRIVATE);

            // if no args, send list of commands
            if (args.length <= 1) {
                // get commands that are either guild or dm commands
                String commandsList = commands.stream().filter(command -> {
                    if (isPrivate) {
                        return command.isDmCommand();
                    }
                    else {
                        return command.isGuildCommand();
                    }
                }).map(Command::getName).collect(Collectors.joining("\n"));
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.GREEN);
                embed.setTitle((isPrivate ? "DM" : "Server") + " Commands");
                embed.setDescription("Available commands for Starbot 5.0");
                embed.addField("Page 1 of 1", commandsList, false);
                embed.setFooter("For specific help, use !help <command>");
                channel.sendMessage(embed.build()).queue();
            }
            // otherwise, send information about the specific command
            else {
                // get the right command
                String query = args[1];
                for (Command command : commands) {
                    if (command.getName().equalsIgnoreCase(query)) {
                        // String commandInfo = "**!" + command.getName() + "**\n" + command.getDescription() + "\n`" + command.getUsage() + "`";
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setColor(Color.GREEN);
                        embed.setTitle("!" + command.getName());
                        embed.setDescription(command.getDescription());
                        embed.addField("Usage", command.getUsage(), true);
                        channel.sendMessage(embed.build()).queue();
                        return;
                    }
                }
                // report no command found
                channel.sendMessage(query + " is not a valid command").queue();
            }
        }
    }

    // singleton
    private static CommandEngine instance = null;

    // the command prefix for all commands
    public static final String COMMAND_PREFIX = "!";

    // a list of executable Commands
    private final List<Command> commands;

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
        commands.add(new HelpCommand());
        commands.add(new VersionCommand());
        commands.add(new InviteCommand());
        commands.add(new ReloadCommand());
        commands.add(new SaveCommand());
        commands.add(new PlayCommand());
        commands.add(new StopCommand());
        commands.add(new ClearQueueCommand());
        commands.add(new MarkovCommand());
        commands.add(new VolumeCommand());
        commands.add(new SeekCommand());
        // commands.add(new ShuffleCommand());
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
