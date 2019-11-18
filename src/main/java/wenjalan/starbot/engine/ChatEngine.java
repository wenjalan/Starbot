package wenjalan.starbot.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;

// chat engine, handles all chat related functions
public class ChatEngine {

    // the Stack of responses that Starbot is using
    protected static Stack<String> responses = null;

    // the HashMap of responses for trigger phrases
    protected static HashMap<String, String> phraseResponses = null;

    // returns a random response for when Starbot is mentioned or dm'd
    public static String getRandomResponse() {
        // if responses is null, load them
        if (responses == null || responses.isEmpty()) {
            responses = generateResponses();
        }

        // return the next message
        return responses.pop();
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
    public static String getResponseForPhrase(String query) {
        // ignore casing
        query = query.toLowerCase();

        // if phraseResponses is null, load from disk
        if (phraseResponses == null) {
            phraseResponses = loadPhraseResponses();
        }

        // return the proper response
        for (String s : phraseResponses.keySet()) {
            if (query.contains(s)) {
                return phraseResponses.get(s);
            }
        }
        return null;
    }

    // returns whether or not a query contains a trigger phrase
    public static boolean containsTriggerPhrase(String query) {
        // ignore casing
        query = query.toLowerCase();

        // if not loaded, load
        if (phraseResponses == null) {
            phraseResponses = loadPhraseResponses();
        }

        // find out
        for (String s : phraseResponses.keySet()) {
            if (query.contains(s)) {
                return true;
            }
        }
        return false;
    }

    // loads the phrase responses from DataEngine
    protected static HashMap<String, String> loadPhraseResponses() {
        return DataEngine.loadTriggerResponses();
    }

}
