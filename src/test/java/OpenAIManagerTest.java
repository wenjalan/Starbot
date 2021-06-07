import org.junit.Test;
import services.openai.OpenAIManager;

public class OpenAIManagerTest {

    @Test
    public void initManager() {
        OpenAIManager manager = OpenAIManager.get();
    }

}
