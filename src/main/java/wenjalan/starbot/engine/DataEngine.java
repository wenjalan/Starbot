package wenjalan.starbot.engine;

import com.google.gson.Gson;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// handles all persistent data processing
public class DataEngine {

    // the default filename for the responses
    public static final String DEFAULT_RESPONSES_FILE = "assets/responses.txt";

    // the default filename for the trigger phrase responses
    public static final String DEFAULT_TRIGGER_RESPONSES_FILE = "assets/phraseresponses.json";

    // the random responses Starbot has when mentioned
    protected static List<String> responses = null;

    // the trigger responses Starbot has when people say dumb things
    protected static HashMap<String, String> triggerResponses = null;

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

    // returns a HashMap of the trigger phrase responses
    public static HashMap<String, String> getTriggerResponses() {
        // if null, load first
        if (triggerResponses == null) {
            triggerResponses = loadTriggerResponses();
        }

        // return a copy
        return new HashMap<>(triggerResponses);
    }

    // loads the trigger phrase responses from the disk
    protected static HashMap<String, String> loadTriggerResponses() {
        return loadTriggerResponses(DEFAULT_TRIGGER_RESPONSES_FILE);
    }

    // loads the trigger phrase responses from the disk
    protected static HashMap<String, String> loadTriggerResponses(String file) {
        Gson g = new Gson();
        try {
            // unchecked assignment
            // TODO: Figure out if this poses a security risk
            HashMap<String, String> responses = g.fromJson(new FileReader(file), HashMap.class);
            return responses;
        } catch (FileNotFoundException e) {
            System.err.println("encountered an error while loading trigger phrase responses");
            e.printStackTrace();
            return null;
        }
    }

}