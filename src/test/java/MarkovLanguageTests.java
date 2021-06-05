import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import wenjalan.starbot.engine.language.MarkovLanguageEngine;
import wenjalan.starbot.engine.language.MarkovLanguageModel;
import wenjalan.starbot.engine.language.SentenceGenerator;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
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
        MarkovLanguageModel model = MarkovLanguageModel.fromSentences(shortCorpus);
    }

    @Test
    public void createSentenceGenerator() {
        MarkovLanguageModel model = MarkovLanguageModel.fromSentences(alternatingCorpus);
        SentenceGenerator generator = new SentenceGenerator(model);
        // try it ten times
        for (int i = 0; i < 10; i++) {
            System.out.println(generator.nextSentence());
        }
    }

    @Test
    public void createModelFromFile() throws IOException {
        MarkovLanguageModel model = MarkovLanguageModel.fromFiles(Arrays.asList(new File("assets/corpi/small.corpus"), new File("assets/corpi/0.corpus")));
    }

    @Test
    public void saveAndLoadModel() throws IOException {
        MarkovLanguageModel model = MarkovLanguageModel.fromSentences(alternatingCorpus);
        model.exportToJson(new File("test.mkv"));

        // load from file
        MarkovLanguageModel imported = MarkovLanguageModel.importFromJson(new File("test.mkv"));
        System.out.println(imported);
    }

    @Test
    public void modelFiftyShades() throws IOException {
        MarkovLanguageModel model = MarkovLanguageModel.fromFiles(Collections.singletonList(new File("assets/corpi/fiftyshades.txt")));
        model.exportToJson(new File("fiftyshades.mkv"));
        SentenceGenerator generator = new SentenceGenerator(model);
        for (int i = 0; i < 50; i++) {
            System.out.println(generator.nextSentence());
        }
    }

}
