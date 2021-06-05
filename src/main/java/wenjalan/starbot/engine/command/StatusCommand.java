package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.Presence;

public class StatusCommand implements Command {

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getDescription() {
        return "(Dev-Only) Sets Starbot's global status";
    }

    @Override
    public String getUsage() {
        return "!status <-p|-w|-s|-l> <string>";
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
        JDA jda = msg.getJDA();
        PrivateChannel channel = msg.getPrivateChannel();
        Presence presence = jda.getPresence();
        String rawContent = msg.getContentRaw();
        try {
            String query = rawContent.substring("!status -".length());
            String activityQuery = "" + query.charAt(0);
            String activityString = query.substring(2);
            // playing, watching, streaming, listening
            if (activityQuery.equalsIgnoreCase("p")) {
                presence.setActivity(Activity.playing(activityString));
            }
            else if (activityQuery.equalsIgnoreCase("w")) {
                presence.setActivity(Activity.watching(activityString));
            }
            else if (activityQuery.equalsIgnoreCase("s")) {
                // note: the streaming endpoint still ends up as playing, idk if it's supported properly
                presence.setActivity(Activity.streaming(activityString, "https://ko-fi.com/wenton"));
            }
            else if (activityQuery.equalsIgnoreCase("l")) {
                presence.setActivity(Activity.listening(activityString));
            }
            else {
                throw new IllegalArgumentException("Invalid Activity option: " + activityQuery);
            }
        } catch (Exception e) {
            channel.sendMessage(e.getMessage()).queue();
        }
    }
}
