import org.junit.Test;
import wenjalan.openai.OpenAIManager;

public class OpenAIManagerTest {

    @Test
    public void initManager() {
        OpenAIManager manager = OpenAIManager.get();
    }

}
