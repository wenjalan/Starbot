package wenjalan.starbot.engine;

// handles all persistent data processing
public class DataEngine {

    // the default filename for the responses
    public static final String DEFAULT_RESPONSES_FILE = "assets/responses.txt";

    // the random responses Starbot has when mentioned
    protected String[] responses = null;

    // returns the String[] of random responses Starbot has when he's mentioned
    public static String[] getResponses() {
        // if the responses haven't been loaded, load them
        if (this.responses == null) {
            loadResponses(DEFAULT_RESPONSES_FILE);
        }

        // return the responses
        return this.responses;
    }

    // loads the Responses database for the ResponseEngine
    protected static void loadResponses(String filename) {
        try {
            // find the File to load the data from
            File f = new File(filename);
        } catch (IOException e) {
            System.err.println("error loading responses from file " + filename);
            e.printStackTrace();
        }

    }

}