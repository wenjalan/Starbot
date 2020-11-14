package wenjalan.starbot.engine.language;

import java.util.Map;
import java.util.TreeMap;

// handles the Markov implementation of the Starbot Natural Language Initiative
public class MarkovLanguageEngine {

    // singleton
    private static MarkovLanguageEngine instance = null;

    // a map of guild ids to their sentence generators
    private Map<Long, SentenceGenerator> generators;

    // private constructor
    private MarkovLanguageEngine() {
        if (instance != null) {
            throw new IllegalStateException("An instance of MarkovLanguageEngine already exists");
        }
        this.generators = new TreeMap<>();
    }

    // returns a sentence generator given a guild
    public SentenceGenerator getSentenceGenerator(long guildId) {
        return generators.get(guildId);
    }

    // instance accessor
    public static MarkovLanguageEngine get() {
        if (instance == null) {
            instance = new MarkovLanguageEngine();
        }
        return instance;
    }

}
