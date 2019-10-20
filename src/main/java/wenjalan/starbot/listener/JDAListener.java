package wenjalan.starbot.listener;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

// listens for events regarding JDA
public class JDAListener extends ListenerAdapter {

    // on ready
    @Override
    public void onReady(ReadyEvent event) {
        // sout
        System.out.println("Starbot online!");
    }

    // on shutdown
    @Override
    public void onShutdown(ShutdownEvent event) {
        // sout
        System.out.println("Starbot shutting down...");
    }

}
