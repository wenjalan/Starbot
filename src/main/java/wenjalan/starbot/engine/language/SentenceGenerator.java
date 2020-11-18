package wenjalan.starbot.engine.language;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static wenjalan.starbot.engine.language.MarkovLanguageModel.END_SENTENCE_SENTINEL;
import static wenjalan.starbot.engine.language.MarkovLanguageModel.START_SENTENCE_SENTINEL;

// generates sentences based on a corpus
public class SentenceGenerator {

    // the model to use to create sentences
    private final MarkovLanguageModel model;

    // private constructor
    public SentenceGenerator(MarkovLanguageModel model) {
        this.model = model;
    }

    // generates a new sentence
    public String nextSentence() {
        // generate a list of tokens that the sentence should contain
        List<String> tokens = new ArrayList<>();
        tokens.add(START_SENTENCE_SENTINEL);
        generateSentence(tokens);

        // format the sentence into something coherent
        String finalSentence = formatSentence(tokens);
        return finalSentence;
    }

    // gets the next word in a sentence
    private void generateSentence(List<String> currentWords) {
        // if it's empty, throw an exception
        if (currentWords.isEmpty()) {
            throw new IllegalArgumentException("currentWords must contain at least the START_SENTENCE_SENTINEL");
        }

        // get the last word generated so far
        String previousWord = currentWords.get(currentWords.size() - 1);

        // if the previous word was the end sentence sentinel, do nothing
        if (previousWord.equalsIgnoreCase(END_SENTENCE_SENTINEL)) {
            return;
        }

        // if not, find the next word and keep finding the next word
        else {
            Map<String, Long> nextWords = model.getNextWords(previousWord);
            String nextWord = getRandomWord(nextWords);
            currentWords.add(nextWord);
            generateSentence(currentWords);
        }
    }

    // chooses a word to use as the next word
    private String getRandomWord(Map<String, Long> nextWords) {
        // get the total weight of the map
        long totalWeight = nextWords.values().stream().mapToLong(Long::intValue).sum();

        // roll a die between 0 and the total weight
        long roll = ThreadLocalRandom.current().nextLong(totalWeight);

        // for each pair in the map
        for (Map.Entry<String, Long> entry : nextWords.entrySet()) {
            // subtract the weight from the roll
            roll -= entry.getValue();

            // if the roll is now less than 0, return the word, otherwise keep going
            if (roll < 0) {
                return entry.getKey();
            }
        }

        // if we get here we fucked up
        throw new IllegalStateException("Error rolling a random word");
    }

    // takes a list of words and puts them together with the right spacings
    private String formatSentence(List<String> tokens) {
        return tokens.stream()
                // filter sentinels
                .filter(token -> !token.equalsIgnoreCase(START_SENTENCE_SENTINEL) && !token.equalsIgnoreCase(END_SENTENCE_SENTINEL))
                .collect(Collectors.joining(" "));
    }

}
