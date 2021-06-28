package wenjalan.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.concurrent.ThreadLocalRandom;

public class SomeoneCommand implements SlashCommand {
    @Override
    public CommandData getData() {
        return new CommandData(getName(), getDescription())
                .addOption(OptionType.ROLE, "role", "A specific role to choose within", false);
    }

    @Override
    public String getName() {
        return "someone";
    }

    @Override
    public String getDescription() {
        return "Mentions a random user in a text channel.";
    }

    @Override
    public void handle(SlashCommandEvent event) {
        // find members who can see this channel and have the role if specified
        TextChannel channel = event.getTextChannel();
        Guild g = channel.getGuild();
        g.findMembers(m ->
                event.getOption("role") == null ?
                        m.hasPermission(channel, Permission.MESSAGE_READ) :
                        m.hasPermission(channel, Permission.MESSAGE_READ) &&
                                m.getRoles().contains(event.getOption("role").getAsRole())
        ).onSuccess(members -> {
            // mention a random one of them
            int roll = ThreadLocalRandom.current().nextInt(members.size());
            Member target = members.get(roll);
            event.reply(target.getAsMention()).setEphemeral(false).queue();
        });
    }
}
