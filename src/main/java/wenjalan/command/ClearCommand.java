package wenjalan.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.concurrent.atomic.AtomicInteger;

public class ClearCommand implements SlashCommand {

    // the default number of messages to look through
    public static final int DEFAULT_HISTORY_PEEK = 30;

    // the maximum number of messages to look through
    public static final int MAX_HISTORY_PEEK = 300;

    @Override
    public CommandData getData() {
        return new CommandData(getName(), getDescription())
                .addOption(OptionType.INTEGER, "amount", "The number of messages to look back through.", false);
    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getDescription() {
        return "Clears bot-authored messages in a channel.";
    }

    @Override
    public void handle(SlashCommandEvent event) {
        // check if the user has the right permissions
        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.reply("You don't have permission.").setEphemeral(true).queue();
            return;
        }

        // check if we have the right permissions
        TextChannel channel = event.getTextChannel();
        Member self = event.getGuild().getSelfMember();
        if (!self.hasPermission(Permission.MESSAGE_MANAGE)) {
            event.reply("I don't have permission to do that.").queue();
            return;
        }

        // paw through the history
        int toRetrieve = event.getOptions().size() == 0 ? DEFAULT_HISTORY_PEEK : (int) event.getOption("amount").getAsLong();
        toRetrieve = Math.min(toRetrieve, MAX_HISTORY_PEEK);
        AtomicInteger deleted = new AtomicInteger();
        channel.getHistory().retrievePast(toRetrieve).queue(msgs -> {
            msgs.forEach(msg -> {
                // if the message was sent by a bot delete it
                if (msg.getAuthor().isBot()) {
                    msg.delete().queue();
                    deleted.getAndIncrement();
                }
            });
            // acknowledge command
            event.reply("Deleted " + deleted + " messages from bots.").setEphemeral(true).queue();
        });
    }
}
