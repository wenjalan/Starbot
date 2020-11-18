package wenjalan.starbot.listener;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wenjalan.starbot.engine.ChatEngine;
import wenjalan.starbot.engine.CommandEngine;
import wenjalan.starbot.utils.ReactionControllerManager;

import javax.annotation.Nonnull;

// listens for all message related events
public class MessageListener extends ListenerAdapter {

    // Logger
    private Logger logger = LogManager.getLogger();

    // private message
    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        // if it was from a bot, ignore it
        if (event.getAuthor().isBot()) {
            return;
        }

        // grab some information about the event
        Message msg = event.getMessage();

        // retrieve some engines
        CommandEngine commandEngine = CommandEngine.get();
        ChatEngine chatEngine = ChatEngine.get();

        // if it was a command
        if (commandEngine.isCommand(msg)) {
            // run the associated command
            commandEngine.parseDmCommand(msg);
        }
        // otherwise send a generic response
        else {
            chatEngine.respondTo(msg);
        }
    }

    // guild message
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        // if it was from a bot
        if (event.getAuthor().isBot()) {
            // ignore it
            return;
        }

        // grab some information about the event
        Message msg = event.getMessage();

        // retrieve some engines
        CommandEngine commandEngine = CommandEngine.get();
        ChatEngine chatEngine = ChatEngine.get();

        // if it was a command
        if (commandEngine.isCommand(msg)) {
            // run the associated command
            commandEngine.parseGuildCommand(msg);
        }
        // if Starbot should respond
        else if (chatEngine.isChatPrompt(msg)) {
            // send an appropriate response
            chatEngine.respondTo(msg);
        }
    }

    // guild message react add
    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        // if it was from a bot ignore it
        if (event.getUser().isBot()) {
            return;
        }
        long msgId = event.getMessageIdLong();
        if (ReactionControllerManager.isController(msgId)) {
            // remove the reaction
            event.getReaction().removeReaction(event.getUser()).queue();
            String reaction = event.getReaction().getReactionEmote().getEmoji();
            ReactionControllerManager.handleReaction(msgId, reaction);
        }
    }
}
