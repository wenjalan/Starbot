package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.entities.Message;
import wenjalan.starbot.data.AssetManager;
import wenjalan.starbot.data.Users;

// saves assets
public class SaveCommand implements Command {
    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getDescription() {
        return "Saves assets";
    }

    @Override
    public String getUsage() {
        return "!save";
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

        // save assets
        AssetManager assets = AssetManager.get();
        assets.saveAssets();

        // report
        msg.getChannel().sendMessage("Save complete").queue();
    }
}
