package wenjalan.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.utils.EncodingUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PollCommand implements SlashCommand {

    // list of emojis, in order, to use as indexes
    List<String> indexEmojis = Arrays.asList(
            "U+1F1E6",
            "U+1F1E7",
            "U+1F1E8",
            "U+1F1E9",
            "U+1F1EA",
            "U+1F1EB",
            "U+1F1EC",
            "U+1F1ED"
    );

    @Override
    public CommandData getData() {
        return new CommandData(getName(), getDescription())
            .addOption(OptionType.STRING, "prompt", "The prompt of the poll.", true)
            .addOption(OptionType.STRING, "choice1", "The first option in the poll.", true)
            .addOption(OptionType.STRING, "choice2", "The second option in the poll.", false)
            .addOption(OptionType.STRING, "choice3", "The third option in the poll.", false)
            .addOption(OptionType.STRING, "choice4", "The fourth option in the poll.", false)
            .addOption(OptionType.STRING, "choice5", "The fifth option in the poll.", false)
            .addOption(OptionType.STRING, "choice6", "The sixth option in the poll.", false)
            .addOption(OptionType.STRING, "choice7", "The seventh option in the poll.", false)
            .addOption(OptionType.STRING, "choice8", "The eighth option in the poll.", false);

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
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(prompt);
        String optionsText = "";
        for (int i = 0; i < choices.size(); i++) {
            String option = choices.get(i);
            String index = EncodingUtil.decodeCodepoint(indexEmojis.get(i));
            optionsText += index + " " + option + "\n";
        }
        embedBuilder.setDescription("Asked by " + event.getMember().getAsMention() + "\n" + optionsText);
        embedBuilder.setColor(Color.CYAN);

        // handle and add reactions
        event.replyEmbeds(embedBuilder.build()).setEphemeral(false)
                .queue(e -> e.getInteraction().getHook().retrieveOriginal().queue(msg -> {
                    for (int i = 0; i < choices.size(); i++) {
                        msg.addReaction(indexEmojis.get(i)).queue();
                    }
                }));
    }
}
