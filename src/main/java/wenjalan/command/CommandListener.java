package wenjalan.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.*;

// handles the listening and delegates handling of commands
public class CommandListener extends ListenerAdapter {

    // a list of names to their Slash Commands
    private Map<String, SlashCommand> slashCommands;

    // constructor
    public CommandListener() {
        this(Collections.EMPTY_LIST);
    }

    // constructor: with a list of commands
    public CommandListener(List<SlashCommand> commands) {
        this.slashCommands = new TreeMap<>(Comparator.naturalOrder());
        for (SlashCommand c : commands) {
            addCommand(c);
        }
    }

    // add a new Slash Command to listen for
    public void addCommand(SlashCommand command) {
        this.slashCommands.put(command.getName(), command);
    }

    // removes a Slash Command to listen for
    public SlashCommand removeCommand(SlashCommand command) {
        return this.slashCommands.remove(command);
    }

    // returns a list of currently registered Slash Commands
    public List<SlashCommand> getRegisteredCommands() {
        List<SlashCommand> list = new ArrayList<>(slashCommands.values().size());
        list.addAll(slashCommands.values());
        return list;
    }

    // on Slash Command
    @Override
    public void onSlashCommand(@Nonnull SlashCommandEvent event) {
        // find the command associated with that name and handle it
        String name = event.getName();
        SlashCommand command = slashCommands.get(name);

        // if we can't find that command, complain
        if (command == null) {
            event.reply("Error: Unknown Command '" + name + "'")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // handle the event
        command.handle(event);
    }

}
