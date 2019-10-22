package wenjalan.starbot.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// handles all persistent data processing
public class DataEngine {

    // the default filename for the responses
    public static final String DEFAULT_RESPONSES_FILE = "assets/responses.txt";

    // the random responses Starbot has when mentioned
    protected static List<String> responses = null;

    // returns the a copy of the list of responses
    public static List<String> getResponses() {
        // if not loaded, load
        if (responses == null) {
            responses = loadResponses(DEFAULT_RESPONSES_FILE);
        }

        // return a copy of the responses
        return new ArrayList<>(responses);
    }

    // loads the responses from the disk
    protected static List<String> loadResponses(String file) {
        try {
            // new BufferedReader to read the file
            BufferedReader br = new BufferedReader(new FileReader(new File(file)));

            // keep reading while there's another line
            ArrayList<String> list = new ArrayList<>();
            String line = br.readLine();
            while (line != null) {
                list.add(line);
                line = br.readLine();
            }

            // return the list
            return list;
        } catch (IOException e) {
            System.err.println("error occurred while loading responses from " + file);
            e.printStackTrace();
            return null;
        }
    }

    // reloads the responses from a given file
    public static void reloadResponses(String file) {
        responses = loadResponses(file);
    }

    // overloading method
    public static void reloadResponses() {
        reloadResponses(DEFAULT_RESPONSES_FILE);
    }

}