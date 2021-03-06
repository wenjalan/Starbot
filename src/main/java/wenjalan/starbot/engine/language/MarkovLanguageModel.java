package wenjalan.starbot.engine.language;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

// a language model based off of Markov Chains
public class MarkovLanguageModel {

    // sentence start and end sentinels
    public static final String END_SENTENCE_SENTINEL = "" + (char) 3;
    public static final String START_SENTENCE_SENTINEL = "" + (char) 2;

    // logger
    private final Logger logger = LogManager.getLogger();

    // the map of words to the words that follow it mapped to their occurrences
    private final Map<String, Map<String, Long>> model;

    // the tokenizer being used to tokenize sentences
    private Tokenizer tokenizer;

    // json import constructor
    private MarkovLanguageModel(Map<String, Map<String, Long>> map) {
        this.model = map;
    }

    // constructor
    private MarkovLanguageModel() {
        this.model = new TreeMap<>();
    }

    // factory: creates a model from a list of sentences
    public static MarkovLanguageModel fromSentences(List<String> sentences) {
        MarkovLanguageModel model = new MarkovLanguageModel();
        for (String sentence : sentences) {
            model.addSentenceToModel(sentence);
        }
        return model;
    }

    // factory: creates a model from a list of corpus files
    public static MarkovLanguageModel fromFiles(List<File> corpi) throws IOException {
        // list to store all the sentences
        List<String> sentences = new ArrayList<>();

        // read each file and add the lines to the sentences
        for (File f : corpi) {
            // if f doesn't exist skip it
            if (!f.exists()) {
                System.err.println("Corpus file " + f.getName() + " does not exist");
                continue;
            }

            // read the entire file and add each line to our sentences
            BufferedReader br = new BufferedReader(new FileReader(f));
            for (String s = br.readLine(); s != null; s = br.readLine()) {
                // todo: filter bad sentences
                if (s.isEmpty()) continue;
                sentences.add(s);
            }
        }

        // call fromSentences
        return fromSentences(sentences);
    }

    // reads a .mkv file and turns it into a model
    public static MarkovLanguageModel importFromJson(File f) throws IOException {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        InputStreamReader is = new InputStreamReader(new FileInputStream(f), MarkovLanguageEngine.JSON_CHARSET);
        TypeToken<Map<String, Map<String, Long>>> type = new TypeToken<Map<String, Map<String, Long>>>() {};
        Map<String, Map<String, Long>> map = gson.fromJson(is, type.getType());
        return new MarkovLanguageModel(map);
    }

    // processes a sentence and adds it to the model
    private void addSentenceToModel(String sentence) {
        // clean sentence for processing
        sentence = sentence.toLowerCase();

        // get the tokens
        String[] rawTokens = getTokens(sentence);

        // add two sentinels for processing convenience
        List<String> tokens = new ArrayList<>();
        tokens.add(START_SENTENCE_SENTINEL);
        tokens.addAll(Arrays.asList(rawTokens));
        tokens.add(END_SENTENCE_SENTINEL);

        // starting at the first word, and ending at the second-to-last one
        for (int i = 0; i < tokens.size() - 1; i++) {
            // get the word and the following one
            String word = tokens.get(i);
            String nextWord = tokens.get(i + 1);

            // add to map
            model.putIfAbsent(word, new TreeMap<>());
            Map<String, Long> followingWordsFrequencyMap = model.get(word);
            followingWordsFrequencyMap.putIfAbsent(nextWord, 0L);
            long currentValue = followingWordsFrequencyMap.get(nextWord);
            followingWordsFrequencyMap.put(nextWord, currentValue + 1);
        }

    }

    // tokenizes a sentence
    private String[] getTokens(String sentence) {
        // create a tokenizer to process the sentence
        try {
            // todo: find a solution that allows emojis to be encoded
             Tokenizer tokenizer = getTokenizer(false);
            // String[] rawTokens = tokenizer.tokenize(sentence);

            // todo: make less primitive
            String[] rawTokens = sentence.split("\\s+");
            return rawTokens;
        } catch (IOException e) {
            logger.error("Encountered an error while processing sentence");
            logger.error(e.getMessage());
            return null;
        }
    }

    // returns the tokenizer used to tokenize sentences
    private Tokenizer getTokenizer(boolean forceRecreate) throws IOException {
        if (this.tokenizer == null || forceRecreate) {
            InputStream inputStream = new FileInputStream("assets/language/en-token.bin");
            TokenizerModel model = new TokenizerModel(inputStream);
            this.tokenizer = new TokenizerME(model);
        }
        return this.tokenizer;
    }

    // returns a String of JSON representing this model for export
    public void exportToJson(File f) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(f), MarkovLanguageEngine.JSON_CHARSET);
        TypeToken<Map<String, Map<String, Long>>> type = new TypeToken<Map<String, Map<String, Long>>>() {};
        gson.toJson(model, type.getType(), ow);
        ow.flush();
    }

    // returns the map of next words for a given word
    public Map<String, Long> getNextWords(String word) {
        return model.get(word);
    }

    // returns some basic information about this model
    public String getInfo() {
        long vocabSize = model.keySet().size();
        long corpusSize = 0;
        for (Map<String, Long> nextWords : model.values()) {
            for (long l : nextWords.values()) {
                corpusSize += l;
            }
        }
        return "vocabSize=" + vocabSize + "\n" +
                "corpusSize=" + corpusSize;
    }

    // toString
    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        return gson.toJson(model);
    }
}
