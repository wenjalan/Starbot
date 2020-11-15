package wenjalan.starbot.engine.language;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
    public void createGuildModel(Guild g, TextChannel callbackChannel) {
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
                            .filter(msg -> !msg.getContentRaw().isEmpty())
                            // filter any bot-sent messages
                            .filter(msg -> !msg.getAuthor().isBot())
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

        // save the model to disk
        try {
            saveModel(g.getIdLong(), model);
        } catch (IOException e) {
            logger.error("Error saving Markov model for guild id " + g.getIdLong());
            logger.error(e.getMessage());
        }

        // respond
        callbackChannel.sendMessage("Markov Model initialized successfully").queue();
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
    public void loadModels() {
        // load all .mkv files
        File modelsDirectory = new File(MODELS_ASSET_DIRECTORY);
        List<File> modelFiles = Arrays.stream(modelsDirectory.list((dir, name) -> name.startsWith(".mkv")))
                .map(File::new)
                .collect(Collectors.toList());

        // create a model from each .mkv
        for (File f : modelFiles) {
            long guildId = Long.parseLong(f.getName().substring(0, f.getName().length() - 4));
            MarkovLanguageModel model = null;
            try {
                model = MarkovLanguageModel.importFromJson(f);
            } catch (IOException e) {
                logger.error("Couldn't read .mkv file for guild id " + guildId);
                logger.error(e.getMessage());
            }
            models.put(guildId, model);
        }

        // report done
        logger.info("Loaded " + modelFiles.size() + " Markov language models from disk");
    }

    // returns a sentence generator given a guild
    public SentenceGenerator getSentenceGenerator(long guildId) {
        // if this guild hadn't been modeled yet, complain
        if (!models.containsKey(guildId)) {
            throw new IllegalArgumentException("Guild id " + guildId + " has not been modeled");
        }

        // if no generator was created yet, create one
        if (!generators.containsKey(guildId)) {
            generators.put(guildId, new SentenceGenerator(models.get(guildId)));
        }

        // return the generator
        return generators.get(guildId);
    }

    // returns whether a guild has a model in the system
    public boolean hasModel(long guildId) {
        return models.containsKey(guildId);
    }

    // instance accessor
    public static MarkovLanguageEngine get() {
        if (instance == null) {
            instance = new MarkovLanguageEngine();
        }
        return instance;
    }

    // returns information about a guild's model
    public String getInfo(long guildId) {
        if (!hasModel(guildId)) {
            throw new IllegalArgumentException("Guild id " + guildId + " has not been modeled");
        }
        return models.get(guildId).getInfo();
    }
}
