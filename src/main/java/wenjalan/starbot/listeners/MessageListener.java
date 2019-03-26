package wenjalan.starbot.listeners;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import wenjalan.starbot.Starbot;
import wenjalan.starbot.Users;
import wenjalan.starbot.engines.CommandEngine;
import wenjalan.starbot.engines.KeyPhraseEngine;
import wenjalan.starbot.engines.ResponseEngine;

import java.util.Arrays;

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
            // if the message was from me, check if it's a request to kill
            if (e.getAuthor().getIdLong() == Users.ALAN) {
                // if the message contains "die", die
                if (e.getMessage().getContentRaw().toLowerCase().contains("die")) {
                    // send a message
                    e.getChannel().sendMessage("committing self-isekai...").queue();
                    e.getJDA().shutdown();
                    return;
                }
            }

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
                // get the query
                String query = e.getMessage().getContentRaw().substring(1);
                String[] tokens = query.split("\\s+");

                // search for a matching command
                for (CommandEngine.Command command : CommandEngine.Command.values()) {
                    // if the command matches, run it
                    if (command.name().equalsIgnoreCase(tokens[0])) {
                        command.run(e, Arrays.copyOfRange(tokens, 1, tokens.length));
                        break;
                    }
                }
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
    public static boolean isCommand(String query) {
        return query.startsWith(COMMAND_PREFIX);
    }

    // returns true if the message has a keyphrase
    protected boolean hasKeyPhrase(String query) {
        return keyPhraseEngine.hasKeyPhrase(query.toLowerCase());
    }

    // accessors
    public ResponseEngine getResponseEngine() {
        return this.responseEngine;
    }

    public KeyPhraseEngine getKeyPhraseEngine() {
        return this.keyPhraseEngine;
    }

}
