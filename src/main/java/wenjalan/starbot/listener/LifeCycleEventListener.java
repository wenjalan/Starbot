package wenjalan.starbot.listener;

import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import wenjalan.starbot.Starbot;

import javax.annotation.Nonnull;

// listens for all life cycle related events
public class LifeCycleEventListener extends ListenerAdapter {

    // Logger
    Logger logger = LogManager.getLogger();

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        // report that Starbot started successfully
        logger.info("Starbot online!");

        // retrieve the bot's invite url and send it to the logger
        String inviteUrl = event.getJDA().getInviteUrl();
        logger.info("Invite: " + inviteUrl);
    }

    @Override
    public void onResume(@Nonnull ResumedEvent event) {
        super.onResume(event);
    }

    @Override
    public void onReconnect(@Nonnull ReconnectedEvent event) {
        super.onReconnect(event);
    }

    @Override
    public void onDisconnect(@Nonnull DisconnectEvent event) {
        super.onDisconnect(event);
    }

    @Override
    public void onShutdown(@Nonnull ShutdownEvent event) {
        super.onShutdown(event);
    }
}
