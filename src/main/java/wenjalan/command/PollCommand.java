package wenjalan.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PollCommand implements SlashCommand {
    @Override
    public CommandData getData() {
        return new CommandData(getName(), getDescription())
            .addOption(OptionType.STRING, "prompt", "The prompt of the poll.", true)
            .addOption(OptionType.STRING, "choice 1", "The first option in the poll.", true)
            .addOption(OptionType.STRING, "choice 2", "The second option in the poll.", false)
            .addOption(OptionType.STRING, "choice 3", "The third option in the poll.", false)
            .addOption(OptionType.STRING, "choice 4", "The fourth option in the poll.", false);
    }

    @Override
    public String getName() {
        return "poll";
    }

    @Override
    public String getDescription() {
        return "Creates a new poll.";
    }

    @Override
    public void handle(SlashCommandEvent event) {
        // grab prompt and choices
        String prompt = event.getOption("prompt").getAsString();
        List<String> choices = new ArrayList<>();
        for (OptionMapping option : event.getOptions()) {
            if (option.getName().equalsIgnoreCase("prompt")) continue;
            String choice = option.getAsString();
            choices.add(choice);
        }

        // create embed

    }
}
