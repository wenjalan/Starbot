package wenjalan.starbot.engine;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import java.util.*;

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
        // find the guild's model
        MarkovModel model = models.get(g.getIdLong());

        // if we haven't learned anything, return that
        if (model == null) {
            return "haven't learned anything yet, use !learn first";
        }
        // otherwise return a generated sentence
        else {
            return model.generateSentence();
        }
    }

    // loads all messages ever sent in a Guild into the MarkovEngine
    public static void learn(Guild guild, TextChannel feedback) {
        // a list containing every message ever sent in all channels of this guild
        List<String> messages = new ArrayList<>();
        // for all channels
        feedback.sendMessage("learning from guild " + guild.getName() + " with " + guild.getTextChannels().size() + " channels").queue();
        for (TextChannel channel : guild.getTextChannels()) {
            feedback.sendMessage("learning from channel " + channel.getName()).queue();
            // load all messages ever sent in that channel
            List<String> channelMessages = loadMessageContent(channel, feedback);
            // add the channel's messages to the overall messages
            messages.addAll(channelMessages);
            feedback.sendMessage("finished learning from channel " + channel.getName() + ", learned " + channelMessages.size() + " messages").queue();
        }
        feedback.sendMessage("learning complete, found " + messages.size() + " messages to learn from").queue();
        // learn from all the messages gathered
        // if the model doesn't exist, create it, otherwise add the messages
        // add a new model if one doesn't exist for this guild
        if (!models.containsKey(guild.getIdLong())) {
            models.put(guild.getIdLong(), MarkovModel.from(messages));
        }
        else {
            models.get(guild.getIdLong()).learn(messages);
        }
    }

    // clears the model
    public static void clear(Guild g) {
        models.remove(g.getIdLong());
    }

    // loads all the messages ever sent in a TextChannel
    protected static List<String> loadMessageContent(TextChannel channel, TextChannel feedback) {
        // all the messages sent in this channel
        List<String> messages = new ArrayList<>();
        try {
            // retrieve all the messages
            channel.getIterableHistory().cache(false).forEach((message) -> {
                // filter: bot sent the message
                if (message.getAuthor().isBot()) {
                    return;
                }

                // get the content
                String content = message.getContentRaw();

                // filter: check if empty
                if (content.isEmpty()) {
                    return;
                }
                // filter: message was command
                if (content.startsWith("!")) {
                    return;
                }

                // add the content
                messages.add(content);
            });
        } catch (Exception e) {
            feedback.sendMessage("error: couldn't retrieve messages of channel " + channel.getName() + ": " + e.getMessage()).queue();
        }
        // return the messages
        return messages;
    }

}
