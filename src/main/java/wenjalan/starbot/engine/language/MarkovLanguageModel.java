package wenjalan.starbot.engine.language;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

// a language model based off of Markov Chains
public class MarkovLanguageModel {

    // sentence start and end sentinels
    private static final String END_SENTENCE_SENTINEL = "" + (char) 3;
    private static final String START_SENTENCE_SENTINEL = "" + (char) 2;

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
    public static MarkovLanguageModel from(List<String> sentences) {
        MarkovLanguageModel model = new MarkovLanguageModel();
        for (String sentence : sentences) {
            model.addSentenceToModel(sentence);
        }
        return model;
    }

    // processes a sentence and adds it to the model
    private void addSentenceToModel(String sentence) {
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
            Tokenizer tokenizer = getTokenizer(false);
            return tokenizer.tokenize(sentence);
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
    public String exportToJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        TypeToken<Map<String, Map<String, Long>>> type = new TypeToken<Map<String, Map<String, Long>>>() {};
        String json = gson.toJson(model, type.getType());
        return json;
    }

    // imports a model from JSON
    public static MarkovLanguageModel importFromJson(String json) {
        Gson gson = new Gson();
        TypeToken<Map<String, Map<String, Long>>> type = new TypeToken<Map<String, Map<String, Long>>>() {};
        Map<String, Map<String, Long>> map = gson.fromJson(json, type.getType());
        return new MarkovLanguageModel(map);
    }

}
