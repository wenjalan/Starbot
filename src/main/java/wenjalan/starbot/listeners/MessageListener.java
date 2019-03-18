package wenjalan.starbot.listeners;

import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import wenjalan.starbot.Starbot;
import wenjalan.starbot.engines.KeyPhraseEngine;
import wenjalan.starbot.engines.ResponseEngine;

// listens to messages sent in guilds
public class MessageListener extends ListenerAdapter {

    // the command prefix
    public static final String COMMAND_PREFIX = "!";

    // the instance of Starbot that this MessageListener is listening for
    protected Starbot starbot;

    // the KeyPhraseEngine
    protected KeyPhraseEngine keyPhraseEngine;

    // the ResponseEngine
    protected ResponseEngine responseEngine;

    // constructor
    public MessageListener(Starbot starbot) {
        this.starbot = starbot;
        this.keyPhraseEngine = starbot.getKeyPhraseEngine();
        this.responseEngine = starbot.getResponseEngine();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        // if the message was sent by a bot, return
        if (e.getAuthor().isBot()) return;

        // if the message is from a dm
        if (e.isFromType(ChannelType.PRIVATE)) {
            // respond to it with a response
            MessageChannel channel = e.getChannel();
            String response = responseEngine.getNextResponse();
            channel.sendMessage(response).queue();
        }

        // if the message is from a guild
        else if (e.isFromType(ChannelType.TEXT)) {
            // if the message mentioned Starbot
            if (e.getMessage().getMentionedUsers().contains(e.getJDA().getSelfUser())) {
                // respond to it with a response
                MessageChannel channel = e.getChannel();
                String response = responseEngine.getNextResponse();
                channel.sendMessage(response).queue();
            }
            // if the message is a command
            else if (isCommand(e.getMessage().getContentRaw())) {
                // handle the command
                MessageChannel channel = e.getChannel();
                String response = "ooh that's a command";
                channel.sendMessage(response).queue();
            }
            // if the message contains a keyphrase
            else if (hasKeyPhrase(e.getMessage().getContentRaw())) {
                // send the proper response
                MessageChannel channel = e.getChannel();
                String response = this.keyPhraseEngine.getResponse(e.getMessage().getContentRaw());
                channel.sendMessage(response).queue();
            }
        }
    }

    // returns true if the message starts with the command prefix
    protected boolean isCommand(String query) {
        return query.startsWith(COMMAND_PREFIX);
    }

    // returns true if the message has a keyphrase
    protected boolean hasKeyPhrase(String query) {
        return keyPhraseEngine.hasKeyPhrase(query.toLowerCase());
    }

}
