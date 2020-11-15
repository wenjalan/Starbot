package wenjalan.starbot.engine;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import wenjalan.starbot.data.AssetManager;
import wenjalan.starbot.engine.language.MarkovLanguageEngine;
import wenjalan.starbot.engine.language.SentenceGenerator;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

// handles all chatbot-related functions
// including interface with NLI, @Starbot, and keyword responses
public class ChatEngine {

    // a list of responses we're currently taking from
    private List<String> responsesBank;

    // a set of NLI-enabled guilds
    private Set<Long> nliEnabledGuilds;

    // singleton
    private static ChatEngine instance = null;

    // returns the instance of ChatEngine
    public static ChatEngine get() {
        if (instance == null) {
            instance = new ChatEngine();
        }
        return instance;
    }

    // singleton constructor
    private ChatEngine() {
        if (instance != null) {
            throw new IllegalStateException("New instances of ChatEngine cannot be instantiated");
        }
        nliEnabledGuilds = new TreeSet<>();
    }

    // returns whether Starbot should respond to the message
    // msg: the Message object to check
    public boolean isChatPrompt(Message msg) {
        // return if the message mentions Starbot
        return msg.getMentionedUsers().contains(msg.getJDA().getSelfUser());
    }

    // responds to a message directed at Starbot
    public void respondTo(Message msg) {
        long guildId = msg.getGuild().getIdLong();
        TextChannel channel = msg.getTextChannel();

        // if this is a nli-enabled guild, send them a nli response
        if (nliEnabledGuilds.contains(guildId)) {
            SentenceGenerator generator = MarkovLanguageEngine.get().getSentenceGenerator(guildId);
            if (generator == null) {
                channel.sendMessage("Markov has not been initialized for this guild").queue();
                return;
            }
            channel.sendMessage(generator.nextSentence()).queue();
            return;
        }

        // if the responses bank isn't initialized or is empty, initialize it
        if (responsesBank == null || responsesBank.isEmpty()) {
            AssetManager assets = AssetManager.get();
            responsesBank = assets.getResponses();
        }

        // choose a random response from the bank
        Random r = new Random();
        String response = responsesBank.remove(r.nextInt(responsesBank.size()));
        channel.sendMessage(response).queue();
    }

    // sets this guild to be NLI enabled or not
    public void setNLIEnabled(long guildId, boolean enabled) {
        if (enabled) {
            nliEnabledGuilds.add(guildId);
        } else {
            nliEnabledGuilds.remove(guildId);
        }
    }
}
