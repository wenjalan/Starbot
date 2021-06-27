package wenjalan.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class VersionCommand implements SlashCommand {
    @Override
    public CommandData getData() {
        return new CommandData(getName(), getDescription());
    }

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public String getDescription() {
        return "Gives the current version of Starbot.";
    }

    @Override
    public void handle(SlashCommandEvent event) {
        // reply with version info
        event.reply("6.0.0")
                .setEphemeral(true)
                .queue();
    }
}
