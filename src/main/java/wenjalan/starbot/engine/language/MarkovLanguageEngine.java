package wenjalan.starbot.engine.language;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// handles the Markov implementation of the Starbot Natural Language Initiative
public class MarkovLanguageEngine {

    // the charset to encode JSON in
    public static final Charset JSON_CHARSET = StandardCharsets.UTF_16;

    // the directory to save models to
    public static final String MODELS_ASSET_DIRECTORY = "assets/models/";

    // logger
    Logger logger = LogManager.getLogger();

    // singleton
    private static MarkovLanguageEngine instance = null;

    // a map of guild ids to their markov models
    private final Map<Long, MarkovLanguageModel> models;

    // a map of guild ids to their sentence generators
    private final Map<Long, SentenceGenerator> generators;

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
        // report we're starting
        callbackChannel.sendMessage("Initializing Markov Model...").queue();

        // log
        logger.info("Creating new Markov Language Model for Guild ID " + g.getIdLong() + "...");

        // list of messages to model off of
        List<String> sentences = new ArrayList<>();

        // get a list of text channels
        List<TextChannel> channels = g.getTextChannels().parallelStream()
                // filter any we can't read or talk in
                .filter(TextChannel::canTalk)
                .collect(Collectors.toList());

        // todo: figure out if there's a better way to check if all channels have been read
        // retrieve all messages from each channel
        final int[] channelsRead = {0};

        // callback for message loads
        Consumer<List<String>> callback = strings -> {
            // add the messages to the sentences list
            sentences.addAll(strings);
            channelsRead[0]++;

            // if we've read all the channels
            if (channelsRead[0] == channels.size()) {
                // log
                logger.info("Finished reading channels, forming model...");

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
                logger.info("Markov Language Model for Guild ID " + g.getIdLong() + " created successfully");
            }
        };

        // load all messages
        for (TextChannel channel : channels) {
            loadMessagesAsync(channel, callback);
        }
    }

    // loads messages from a TextChannel asynchronously
    private void loadMessagesAsync(TextChannel channel, Consumer<List<String>> callback) {
        // log
        logger.info("> Reading channel " + channel.getName() + " (id " + channel.getId() + ")");
        // messages in this channel
        List<String> messages = new ArrayList<>();
        AtomicLong totalRead = new AtomicLong();
        try {
            // read the history
            channel.getIterableHistory()
                    .cache(false)
                    // for each message
                    .forEachAsync((msg) -> {
                        // make sure we know we're not in limbo
                        totalRead.getAndIncrement();
                        if (totalRead.get() % 1000 == 0) {
                            logger.info(">> Read " + totalRead.get() + " messages in " + channel.getName() + " so far...");
                        }

                        // do content checks to see if it's a valid sentence
                        String content = msg.getContentDisplay();
                        // is empty
                        if (content.isEmpty()) return true;
                        // is bot sent
                        if (msg.getAuthor().isBot()) return true;
                        // is a command
                        if (content.startsWith("!") || content.startsWith(".")) return true;
                        // is a link
                        if (content.startsWith("http")) return true;

                        // add the message and continue
                        messages.add(content);
                        return true;
                    })
                    .thenRun(() -> {
                        logger.info("> Finished reading channel " + channel.getName());
                        callback.accept(messages);
                    });
        } catch (Exception e) {
            callback.accept(Collections.emptyList());
            logger.error("Error while reading channel " + channel.getName() + " (id " + channel.getId() + ")");
            logger.error(e.getMessage());
        }
    }

    // saves a model to disk
    private void saveModel(Long guildId, MarkovLanguageModel model) throws IOException {
        File f = new File(MODELS_ASSET_DIRECTORY + guildId + ".mkv");
        model.exportToJson(f);
        logger.info("Saved model info for guild ID " + guildId);
    }

    // loads existing models from disk
    public void loadModels() {
        // load all .mkv files
        File modelsDirectory = new File(MODELS_ASSET_DIRECTORY);
        List<File> modelFiles = Arrays.stream(modelsDirectory.list((dir, name) -> name.endsWith(".mkv")))
                .map(name -> new File(MODELS_ASSET_DIRECTORY + name))
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
