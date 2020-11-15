import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import wenjalan.starbot.engine.language.MarkovLanguageModel;
import wenjalan.starbot.engine.language.SentenceGenerator;

import java.io.*;
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
        MarkovLanguageModel model = MarkovLanguageModel.fromSentences(shortCorpus);
        System.out.println(model.exportToJson());
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
        System.out.println(model.exportToJson());
    }

    @Test
    public void exportModelToFile() throws IOException {
        MarkovLanguageModel model = MarkovLanguageModel.fromSentences(shortCorpus);
        FileWriter writer = new FileWriter(new File("assets/models/testModel.mkv"));
        writer.write(model.exportToJson());
        writer.flush();
    }

    @Test
    public void importModelFromFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File("assets/models/testModel.mkv")));
        String json = "";
        for (String s = br.readLine(); s != null; s = br.readLine()) {
            json += s + "\n";
        }
        MarkovLanguageModel model = MarkovLanguageModel.importFromJson(json);
        System.out.println(model.exportToJson());
    }

    @Test
    public void largeCorpusComprehensive() throws IOException {
        MarkovLanguageModel model = MarkovLanguageModel.fromFiles(Arrays.asList(new File("assets/corpi/small.corpus"), new File("assets/corpi/0.corpus")));
        SentenceGenerator generator = new SentenceGenerator(model);
        for (int i = 0; i < 100; i++) {
            System.out.print(generator.nextSentence() + "\n");
        }
    }

}
