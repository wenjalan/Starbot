package wenjalan.starbot.utils;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// allows the event catching of reaction buttons for a message
public class ReactionControllerManager {

    // a map of message ids to their listeners
    private static final Map<Long, ReactionListener> listeners = new TreeMap<>();

    // reaction listener class
    public interface ReactionListener {
        void onButtonClicked(String emojiRegex);
    }

    // msg: the Message object to add this controller to
    public static void addController(Message msg, List<String> emojiCodes, ReactionListener listener) {
        // add each emoji regex to the message
        for (String emoji : emojiCodes) {
            msg.addReaction(emoji).queue();
        }

        // add to map
        listeners.put(msg.getIdLong(), listener);
    }

    // msgId: the id of the Message to remove
    public static void removeController(long msgId) {
        listeners.remove(msgId);
    }

    // on reaction
    public static void handleReaction(long messageId, String reaction) {
        if (!isController(messageId)) {
            throw new IllegalArgumentException("Message id " + messageId + " is not a controller");
        }
        listeners.get(messageId).onButtonClicked(reaction);
    }

    // returns whether the message is a controller
    public static boolean isController(long msgId) {
        return listeners.containsKey(msgId);
    }

}
