package services.openai;

import com.theokanning.openai.OpenAiService;

// handles all communication with the OpenAI API
public class OpenAIManager {

    // the instance of OpenAI service
    final private OpenAiService service;

    // the instance of the manager
    private static OpenAIManager instance;

    // private constructor
    private OpenAIManager() {
        throw new IllegalStateException("Tried to instantiate an instance of OpenAIManager without a token");
    }

    // private constructor: initializes OpenAI API client
    private OpenAIManager(String token) {
        service = new OpenAiService(token);
    }

    // returns the instance of the manager
    public static OpenAIManager get() {
        return instance == null ? init() : instance;
    }

    // initializes an instance of OpenAIManager
    public static OpenAIManager init() {
        // if there is already an instance, complain
        if (instance != null) {
            throw new IllegalStateException("Tried to instantiate an instance of OpenAIManager when one already existed");
        }

        // create a new instance of OpenAIManager
        // retrieve the token from the system environment
        final String OPENAI_API_TOKEN = System.getenv().get("OPENAI_API_KEY");
        instance = new OpenAIManager(OPENAI_API_TOKEN);

        // return the instance
        return instance;
    }

}
