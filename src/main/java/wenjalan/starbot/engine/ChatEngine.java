package wenjalan.starbot.engine;

import net.dv8tion.jda.api.entities.Guild;

import java.util.*;

// chat engine, handles all chat related functions
public class ChatEngine {

    // list of guild ids of which Markov responses are enabled
    private static List<String> markovGuilds = new ArrayList<>();

    // the Stack of responses that Starbot is using
    protected static Stack<String> responses = null;

    // returns a random response for when Starbot is mentioned or dm'd
    public static String getRandomResponse(Guild g) {
        // if this is a markov guild, send a markov response
        if (markovGuilds.contains(g.getId())) {
            // send a markov response
            return MarkovEngine.generate(g);
        }
        return getPresetResponse();
    }

    // returns a preset response
    public static String getPresetResponse() {
        // if responses is null, load them
        if (responses == null || responses.isEmpty()) {
            responses = generateResponses();
        }

        // return the next message
        return responses.pop();
    }

    // adds a guild to the list of markov guilds
    public static void enableMarkov(Guild g) {
        markovGuilds.add(g.getId());
    }

    // removes a guild from the list of markov guilds
    public static void disableMarkov(Guild g) {
        markovGuilds.remove(g.getId());
    }

    // returns whether a guild is a markov guild
    public static boolean isMarkov(Guild g) {
        return markovGuilds.contains(g.getId());
    }

    // generates a randomized Stack of responses
    protected static Stack<String> generateResponses() {
        // get the list of responses from DataEngine
        List<String> list = DataEngine.getResponses();

        // randomly remove things from the list while adding to the list
        Random r = new Random();
        Stack<String> s = new Stack<>();
        while (!list.isEmpty()) {
            int index = r.nextInt(list.size());
            s.push(list.remove(index));
        }

        // return the stack
        return s;
    }

    // returns a response for Starbot given a trigger phrase
    // returns null if none was found
    public static String getResponseForPhrase(long guildId, String query) {
        return DataEngine.getTriggerResponseFor(guildId, query);
    }

    // returns whether or not a query contains a trigger phrase
    public static boolean containsTriggerPhrase(long guildId, String query) {
        return DataEngine.hasTriggerPhrase(guildId, query);
    }

}
