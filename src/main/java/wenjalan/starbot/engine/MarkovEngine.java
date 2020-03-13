package wenjalan.starbot.engine;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

// handles the generation of responses based on Markov chains
public class MarkovEngine {

    // a list of Engines associated with guild ids
    private static Map<Long, MarkovModel> models = new TreeMap<>();

    // represents a Markov Model used to generate sentences
    public static class MarkovModel {

        // represents a Node in the Markov Model
        public static class MarkovNode {

            // specialized node for beginning of sentence
            public static class StartNode extends MarkovNode {

                // constructor
                public StartNode() {
                    super(null);
                }

                // return [ as the word of this node
                @Override
                public String getWord() {
                    return "[";
                }

            }

            // specialized node for end of sentence
            public static class EndNode extends MarkovNode {

                // constructor
                public EndNode() {
                    super(null);
                }

                // end node should not be able to add or get next words
                @Override
                public void addNextWord(String word) {
                    throw new IllegalStateException("Cannot add next word to end of sentence");
                }

                @Override
                public Map<String, Integer> getNextWords() {
                    throw new IllegalStateException("Cannot get next words of the end of a sentence");
                }

                // return ] as the word
                @Override
                public String getWord() {
                    return "]";
                }

            }

            // Random object for generating random numbers
            private static Random random = new Random();

            // the word this node represents
            private final String word;

            // the words that can follow this word, mapped to their probabilities
            private TreeMap<String, Integer> nextWords;

            // the possibility board of words that could follow this word
            // each word in nextWords is given n number of spaces, where n is their value in the map
            private String[] wordBoard = null;

            // constructor
            public MarkovNode(String word) {
                this.word = word;
                this.nextWords = new TreeMap<>();
            }

            // adds a next possible word to this node
            // increases the weight if the word was already added
            public void addNextWord(String word) {
                // increment map
                this.nextWords.putIfAbsent(word, 0);
                this.nextWords.put(word, this.nextWords.get(word) + 1);
            }

            // returns the word this Node is associated with
            public String getWord() {
                return this.word;
            }

            // returns the map of possible next words of this node
            public Map<String, Integer> getNextWords() {
                return this.nextWords;
            }

            // creates the word board from the map of words
            private void generateBoard() {
                // find the total number of options this board should have
                int totalOptions = 0;
                for (String v : this.nextWords.keySet()) {
                    totalOptions += this.nextWords.get(v);
                }

                // create the board and populate it
                this.wordBoard = new String[totalOptions];
                int i = 0;
                for (String v : this.nextWords.keySet()) {
                    for (int j = 0; j < this.nextWords.get(v); j++) {
                        this.wordBoard[i] = v;
                        i++;
                    }
                }
            }

            // returns a weight random next word of this node
            public String getNextWord() {
                // regenerate the board
                generateBoard();
                // next int
                int roll = random.nextInt(wordBoard.length);
                return wordBoard[roll];
            }

        }

        // the end of sentence sequence, a supposedly unique string of chars that shouldn't appear elsewhere
        // currently the "end of sequence" character
        public static String EOS_SEQUENCE = "" + (char) 4;

        // the beginning of sentence node, not contained in the words map
        private final MarkovNode.StartNode startNode;

        // the end of sentence node, contained in the words map
        private final MarkovNode.EndNode endNode;

        // a map of words to their nodes
        private Map<String, MarkovNode> words;

        // constructor
        private MarkovModel() {
            this.startNode = new MarkovNode.StartNode();
            this.endNode = new MarkovNode.EndNode();
            this.words = new TreeMap<>();
            words.put(EOS_SEQUENCE, this.endNode);
        }

        // main factory method
        // sentences: a list of sentences to generate the model from
        public static MarkovModel from(List<String> sentences) {
            // create a new model
            MarkovModel model = new MarkovModel();

            // learn the words from the sentences
            model.learn(sentences);

            // return the model
            return model;
        }

        // adds more messages to this model
        public void learn(List<String> sentences) {
            // for each sentence, get the words and the words that follow
            for (String sentence : sentences) {
                // get the words
                String[] words = sentence.split("\\s+");

                // for the beginning of sentence node, add the first word
                this.startNode.addNextWord(words[0]);

                // for each word in the sentence
                for (int i = 0; i < words.length - 1; i++) {
                    String word = words[i];
                    this.words.putIfAbsent(word, new MarkovNode(word));
                    this.words.get(word).addNextWord(words[i + 1]);
                }

                // for the last word, add the EOS sequence "" as the next word
                String lastWord = words[words.length - 1];
                this.words.putIfAbsent(lastWord, new MarkovNode(lastWord));
                this.words.get(lastWord).addNextWord(EOS_SEQUENCE);
            }
        }

        // returns a new sentence given a random int seed
        public String generateSentence() {
            String sentence = generateSentence(this.startNode);
            return sentence;
        }

        private String generateSentence(MarkovNode node) {
            // if the node is the end of sentence node, return nothing
            if (node instanceof MarkovNode.EndNode) {
                return "";
            }
            // otherwise, return this word plus whatever the next word is
            else {
                String word = "";
                if (!(node instanceof MarkovNode.StartNode)) {
                    word = node.getWord() + " ";
                }
                String nextWord = node.getNextWord();
                MarkovNode nextNode = words.get(nextWord);
                return word + generateSentence(nextNode);
            }
        }

    }

    // returns a random response generated from a MarkovModel
    public static String generate(Guild g) {
        long guildId = g.getIdLong();
        // find the guild's model
        MarkovModel model = models.get(guildId);

        // if we haven't learned anything, attempt to load from disk
        if (model == null) {
//            model = DataEngine.loadModel(guildId);
//            models.put(guildId, model);
            // if still null
            if (model == null) {
                return "haven't learned anything yet, use !learn first";
            }
        }
        // return a generated sentence
        return model.generateSentence();
    }

    // loads all messages ever sent in a Guild into the MarkovEngine
    public static void learn(Guild guild, TextChannel feedback, boolean verbose) {
        // announce learning
        final int totalChannels = guild.getTextChannels().size();
        feedback.sendMessage("learning from guild " + guild.getName() + " with " + totalChannels + " channels...").queue();

        // create a new model for this guild
        final MarkovModel model = new MarkovModel();
        models.put(guild.getIdLong(), model);

        // for all channels
        final AtomicLong messageCount = new AtomicLong(0L);
        final AtomicInteger channelsLearned = new AtomicInteger(0);
        for (int i = 0; i < totalChannels; i++) {
            // get a channel
            TextChannel channel = guild.getTextChannels().get(i);

            // load all messages ever sent in that channel
            Consumer<List<String>> callback = (response) -> {
                // learn all the messages sent
                model.learn(response);

                // count the messages
                messageCount.addAndGet(response.size());

                // count the learned channels
                channelsLearned.getAndIncrement();

                // logging
                if (verbose) {
                    feedback.sendMessage("finished learning from channel " + channel.getName() + ", learned " + response.size() + " messages").queue();
                }

                // if this was the last channel, say so
                if (channelsLearned.get() == totalChannels) {
                    feedback.sendMessage("learning complete! found " + messageCount.get() + " total messages").queue();
                    // save the model to disk
                    // DataEngine.saveModel(guild.getIdLong(), model);
                }
            };

            // load the content
            loadMessageContent(channel, feedback, callback);
        }
    }

    // clears the model
    public static void clear(Guild g) {
        models.remove(g.getIdLong());
    }

    // loads all the messages ever sent in a TextChannel
    protected static void loadMessageContent(TextChannel channel, TextChannel feedback, Consumer<List<String>> callback) {
        // all the messages sent in this channel
        List<String> messages = new ArrayList<>();
        try {
            // retrieve all the messages
            channel.getIterableHistory().cache(false).forEachAsync((message) -> {
                // pre-content checks:
                // 1. Author is Bot
                // 2. Message pings a bot
                if (message.getAuthor().isBot()) return true;
                List<User> mentionedUsers = message.getMentionedUsers();
                for (User u : mentionedUsers) {
                    if (u.isBot()) return true;
                }

                // content checks:
                // 1. Empty message
                // 2. Message was command
                // 3. Mention is only a ping (@User)
                String contentDisplay = message.getContentDisplay();
                if (contentDisplay.isEmpty()) return true;
                if (contentDisplay.startsWith(CommandEngine.getGlobalCommandPrefix())) return true;
                if (contentDisplay.startsWith("@") && !contentDisplay.contains(" ")) return true;

                // add the message after all the filters
                messages.add(contentDisplay);

                // continue iterating
                return true;
            }).thenRun(() -> {
                callback.accept(messages);
            });
        } catch (Exception e) {
            callback.accept(Collections.emptyList());
            feedback.sendMessage("error: couldn't retrieve messages of channel " + channel.getName() + ": " + e.getMessage()).queue();
        }
    }

}
