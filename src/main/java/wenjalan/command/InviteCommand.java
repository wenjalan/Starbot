package wenjalan.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class InviteCommand implements SlashCommand {
    @Override
    public CommandData getData() {
        return new CommandData(getName(), getDescription());
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "Generates an invite for Starbot to join your server.";
    }

    @Override
    public void handle(SlashCommandEvent event) {
        String invite = event.getJDA().getInviteUrl();
        event.reply(invite).setEphemeral(false).queue();
    }
}
