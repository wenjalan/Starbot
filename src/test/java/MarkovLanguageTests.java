import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import wenjalan.starbot.engine.language.MarkovLanguageModel;

import java.util.Arrays;
import java.util.List;

public class MarkovLanguageTests {

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(MarkovLanguageTests.class);
        for (Failure fail : result.getFailures()) {
            System.out.println(fail.toString());
        }
        System.out.println(result.wasSuccessful());
    }

    @Test
    public void createLanguageModel() {
        List<String> sentence = Arrays.asList("hello world");
        MarkovLanguageModel model = MarkovLanguageModel.from(sentence);
    }

}
