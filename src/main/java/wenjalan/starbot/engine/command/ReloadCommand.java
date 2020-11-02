package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.entities.Message;
import wenjalan.starbot.data.AssetManager;
import wenjalan.starbot.data.Users;

// reloads assets
public class ReloadCommand implements Command {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reloads assets";
    }

    @Override
    public String getUsage() {
        return "!reload";
    }

    @Override
    public boolean isGuildCommand() {
        return false;
    }

    @Override
    public boolean isDmCommand() {
        return true;
    }

    @Override
    public boolean isAdminCommand() {
        return true;
    }

    @Override
    public void run(Message msg) {
        // check that it's Wenton
        if (msg.getAuthor().getIdLong() != Users.WENTON_ID_LONG) {
            return;
        }

        // reload all assets
        AssetManager assets = AssetManager.get();
        assets.loadAssets();

        // report
        msg.getChannel().sendMessage("Reload complete").queue();
    }

}
