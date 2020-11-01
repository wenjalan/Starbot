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
    private Logger logger = LogManager.getLogger();

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
        // report that Starbot resumed
        logger.info("Starbot resumed!");
    }

    @Override
    public void onReconnect(@Nonnull ReconnectedEvent event) {
        // report that Starbot reconnected
        logger.info("Starbot reconnected!");
    }

    @Override
    public void onDisconnect(@Nonnull DisconnectEvent event) {
        // report
        logger.info("Starbot disconnected!");
    }

    @Override
    public void onShutdown(@Nonnull ShutdownEvent event) {
        // report
        logger.info("Starbot shutdown!");
    }
}
