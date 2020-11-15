package wenjalan.starbot.engine.language;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// handles the Markov implementation of the Starbot Natural Language Initiative
public class MarkovLanguageEngine {

    // the directory to save models to
    public static final String MODELS_ASSET_DIRECTORY = "assets/models/";

    // logger
    Logger logger = LogManager.getLogger();

    // singleton
    private static MarkovLanguageEngine instance = null;

    // a map of guild ids to their markov models
    private Map<Long, MarkovLanguageModel> models;

    // a map of guild ids to their sentence generators
    private Map<Long, SentenceGenerator> generators;

    // private constructor
    private MarkovLanguageEngine() {
        if (instance != null) {
            throw new IllegalStateException("An instance of MarkovLanguageEngine already exists");
        }
        this.generators = new TreeMap<>();
        this.models = new TreeMap<>();
    }

    // creates a language model for a guild
    public void createGuildModel(Guild g) {
        // list of messages to model off of
        List<String> sentences = new ArrayList<>();
        // get a list of text channels
        g.getTextChannels().stream()
                // filter any we can't read or talk in
                .filter(TextChannel::canTalk)
                // collect messages from each channel
                .forEach(channel -> {
                    // for all the messages
                    channel.getIterableHistory().stream()
                            // filter any empty messages
                            .filter(msg -> msg.getContentRaw().isEmpty())
                            // modify and add to sentences
                            .forEach(msg -> {
                                String content = msg.getContentDisplay();
                                // convert to lowercase to conserve on vocab space
                                content = content.toLowerCase();
                                // add to sentences
                                sentences.add(content);
                            });
                });

        // create a new model and generator
        MarkovLanguageModel model = MarkovLanguageModel.fromSentences(sentences);
        models.put(g.getIdLong(), model);
        SentenceGenerator generator = new SentenceGenerator(model);
        generators.put(g.getIdLong(), generator);

        // save the model to disk
        try {
            saveModel(g.getIdLong(), model);
        } catch (IOException e) {
            logger.error("Error saving Markov model for guild id " + g.getIdLong());
            logger.error(e.getMessage());
        }
    }

    // saves a model to disk
    private void saveModel(Long guildId, MarkovLanguageModel model) throws IOException {
        File f = new File(guildId + ".mkv");
        FileWriter writer = new FileWriter(f);
        writer.write(model.exportToJson());
        writer.flush();
        writer.close();
    }

    // loads existing models from disk
    private void loadModels() {
        
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
