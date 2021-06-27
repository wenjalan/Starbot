package wenjalan.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

// represents a Slash Command
public interface SlashCommand {

    // returns a CommandData object pertaining to this command
    CommandData getData();

    // returns the name of this command
    String getName();

    // returns the description of this command
    String getDescription();

    // handles an event
    void handle(SlashCommandEvent event);

}
