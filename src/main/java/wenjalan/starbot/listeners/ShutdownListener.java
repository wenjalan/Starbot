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
            try {
                g.getAudioManager().closeAudioConnection();
            } catch (Exception ex) {
                System.err.println("error closing one of the audio connections");
                ex.printStackTrace();
            }
        }

        // actually shut down?
        e.getJDA().shutdown();

        // announce
        System.out.println("Starbot shut down successfully.");
        System.exit(0);
    }

}
