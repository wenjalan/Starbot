package wenjalan.starbot.engine.language;

import java.util.ArrayList;
import java.util.List;

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
            // todo: find a valid next word after the previousWord
            String nextWord = "";
            currentWords.add(nextWord);
            generateSentence(currentWords);
        }
    }

    // takes a list of words and puts them together with the right spacings
    private String formatSentence(List<String> tokens) {
        // todo: actually figure out how to do this
        return String.join(" ", tokens);
    }

}
