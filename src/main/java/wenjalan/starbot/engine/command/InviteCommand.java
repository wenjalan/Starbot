package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.entities.Message;

import static wenjalan.starbot.Starbot.DEFAULT_PERMISSIONS;

// responds with a link to invite Starbot to a server
public class InviteCommand implements Command {

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "Provides Starbot's invite url";
    }

    @Override
    public String getUsage() {
        return "!invite";
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
        // get and send the url
        String url = msg.getJDA().getInviteUrl(DEFAULT_PERMISSIONS);
        msg.getChannel().sendMessage(url).queue();
    }
}
