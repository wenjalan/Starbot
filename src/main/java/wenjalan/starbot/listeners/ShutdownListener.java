package wenjalan.starbot.listeners;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.List;

public class ShutdownListener extends ListenerAdapter {

    @Override
    public void onShutdown(ShutdownEvent e) {
        // disconnect all audio adapters
        List<Guild> guilds = e.getJDA().getGuilds();
        for (Guild g : guilds) {
            g.getAudioManager().closeAudioConnection();
        }

        // announce
        System.out.println("Starbot shut down successfully.");
    }

}
