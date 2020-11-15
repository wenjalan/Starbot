import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import wenjalan.starbot.engine.language.MarkovLanguageModel;
import wenjalan.starbot.engine.language.SentenceGenerator;

import java.util.Arrays;
import java.util.List;

public class MarkovLanguageTests {

    List<String> shortCorpus = Arrays.asList("hello world", "hello alan", "hello fran", "hello elbert");
    List<String> screamingCorpus = Arrays.asList("a a a a a");
    List<String> alternatingCorpus = Arrays.asList("a b", "a c");

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(MarkovLanguageTests.class);
        for (Failure fail : result.getFailures()) {
            System.out.println(fail.toString());
        }
        System.out.println(result.wasSuccessful());
    }

    @Test
    public void createLanguageModel() {
        MarkovLanguageModel model = MarkovLanguageModel.from(shortCorpus);
        System.out.println(model.exportToJson());
    }

    @Test
    public void createSentenceGenerator() {
        MarkovLanguageModel model = MarkovLanguageModel.from(alternatingCorpus);
        SentenceGenerator generator = new SentenceGenerator(model);
        // try it ten times
        for (int i = 0; i < 10; i++) {
            System.out.println(generator.nextSentence());
        }
    }

}
