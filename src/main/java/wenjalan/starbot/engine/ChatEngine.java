package wenjalan.starbot.engine;

import net.dv8tion.jda.api.entities.Message;
import wenjalan.starbot.data.AssetManager;

import java.util.List;
import java.util.Random;

// handles all chatbot-related functions
// including interface with NLI, @Starbot, and keyword responses
public class ChatEngine {

    // a list of responses we're currently taking from
    private List<String> responsesBank;

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
    }

    // returns whether Starbot should respond to the message
    // msg: the Message object to check
    public boolean isChatPrompt(Message msg) {
        // return if the message mentions Starbot
        return msg.getMentionedUsers().contains(msg.getJDA().getSelfUser());
    }

    // responds to a message directed at Starbot
    public void respondTo(Message msg) {
        // if the responses bank isn't initialized or is empty, initialize it
        if (responsesBank == null || responsesBank.isEmpty()) {
            AssetManager assets = AssetManager.get();
            responsesBank = assets.getResponses();
        }

        // choose a random response from the bank
        Random r = new Random();
        String response = responsesBank.remove(r.nextInt(responsesBank.size()));
        msg.getChannel().sendMessage(response).queue();
    }
}
