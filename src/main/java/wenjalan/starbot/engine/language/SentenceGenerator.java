package wenjalan.starbot.engine.language;

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
        // todo: generate a sentence
        return null;
    }

}
