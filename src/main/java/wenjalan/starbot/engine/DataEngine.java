package wenjalan.starbot.engine;

import com.google.gson.Gson;

import java.io.*;
import java.net.URL;
import java.util.*;

// handles all persistent data processing
public class DataEngine {

    // some constants
    public static class Constants {

        public static final long OWNER_ID_LONG = 478706068223164416L; // wenton#8946

    }

    // the default filename for the responses
    public static final String DEFAULT_RESPONSES_FILE = "assets/responses.txt";

    // the default filename for the trigger phrase responses
    public static final String DEFAULT_TRIGGER_RESPONSES_FILE = "assets/phraseresponses.json";

    // the default filename for the radios phrase responses
    public static final String DEFAULT_RADIO_URLS_FILE = "assets/radios.json";

    // the name of the resources file
    public static final String PROPERTIES_FILEPATH = "starbot.properties";

    // the map of properties
    protected static Map<String, String> properties;

    // the random responses Starbot has when mentioned
    protected static List<String> responses = null;

    // the trigger responses Starbot has when people say dumb things
    protected static HashMap<String, String> triggerResponses = null;

    // the radio URLs mapped to their names
    protected static Map<String, String> radioUrls = null;

    // returns the value of a property
    // throws an exception if no property with the given name is found
    public static String getProperty(String name) {
        if (properties == null) {
            properties = loadProperties(PROPERTIES_FILEPATH);
        }
        if (!properties.containsKey(name)) {
            throw new IllegalArgumentException("no property found with the name " + name);
        }
        return properties.get(name);
    }

    // loads the properties into a map for use
    protected static Map<String, String> loadProperties(String filepath) {
        Map<String, String> properties = new TreeMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemClassLoader().getResource(filepath).openStream()))) {
            for (String line; (line = reader.readLine()) != null;) {
                String[] keyAndValue = line.split("=");
                properties.put(keyAndValue[0], keyAndValue[1]);
            }
        } catch (IOException e) {
            System.err.println("error loading properties file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return properties;
    }

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

            // if it's null (empty), complain and return an empty hashmap
            if (responses == null) {
                System.err.println("didn't find anything in the " + file + " file");
                return new HashMap<>();
            } else {
                return responses;
            }
        } catch (FileNotFoundException e) {
            System.err.println("encountered an error while loading trigger phrase responses");
            e.printStackTrace();
            return null;
        }
    }

    // returns whether or not a query contains a trigger phrase
    public static boolean hasTriggerPhrase(String query) {
        // ignore casing
        query = query.toLowerCase();

        // load responses if not loaded
        if (triggerResponses == null) {
            triggerResponses = loadTriggerResponses(DEFAULT_TRIGGER_RESPONSES_FILE);
        }

        // return whether or not the query contains a valid trigger phrase
        for (String phrase : triggerResponses.keySet()) {
            if (query.contains(phrase)) {
                return true;
            }
        }

        // return false
        return false;
    }

    // returns the response for a given trigger phrase
    // returns null if none was found
    public static String getTriggerResponseFor(String query) {
        // ignore casing
        query = query.toLowerCase();

        // load responses if not loaded
        if (triggerResponses == null) {
            triggerResponses = loadTriggerResponses(DEFAULT_TRIGGER_RESPONSES_FILE);
        }

        // return the proper response
        for (String phrase : triggerResponses.keySet()) {
            if (query.contains(phrase)) {
                return triggerResponses.get(phrase);
            }
        }

        // return null
        return null;
    }

    // saves the trigger phrase responses to the disk
    public static void saveTriggerResponses(String file) {
        // get the JSON String
        Gson g = new Gson();
        String json = g.toJson(triggerResponses);

        // write it
        writeJson(json, file);
    }

    // saves the trigger phrase responses to the disk
    public static void saveTriggerResponses() {
        saveTriggerResponses(DEFAULT_TRIGGER_RESPONSES_FILE);
    }

    // adds a response to the trigger phrase responses
    public static void addTriggerPhrase(String triggerPhrase, String response) {
        // load the triggerResponses if not already loaded
        if (triggerResponses == null) {
            triggerResponses = loadTriggerResponses();
        }

        // add an entry to the responses
        triggerResponses.put(triggerPhrase, response);

        // save the file
        saveTriggerResponses();
    }

    // removes a response from the trigger phrase responses
    public static void removeTriggerPhrase(String triggerPhrase) {
        // load the triggerResponses if not already loaded
        if (triggerResponses == null) {
            triggerResponses = loadTriggerResponses();
        }

        // add an entry to the responses
        triggerResponses.remove(triggerPhrase);

        // save the file
        saveTriggerResponses();
    }

    // returns the URL of a radio given its keyword
    // null if none was found
    public static String getRadioUrl(String name) {
        // check load
        if (radioUrls == null) {
            radioUrls = loadRadioUrls();
        }

        // return the given url
        return radioUrls.get(name);
    }

    // returns the list of radios available
    public static Set<String> getRadioNames() {
        // check load
        if (radioUrls == null) {
            radioUrls = loadRadioUrls();
        }

        // return the list of names
        return radioUrls.keySet();
    }

    // adds a radio to the map
    public static void addRadio(String name, String url) {
        // check load
        if (radioUrls == null) {
            radioUrls = loadRadioUrls();
        }

        // add
        radioUrls.put(name, url);

        // save
        saveRadioUrls();
    }

    // removes a radio from the map
    public static void removeRadio(String name) {
        // check load
        if (radioUrls == null) {
            radioUrls = loadRadioUrls();
        }

        // remove
        radioUrls.remove(name);

        // save
        saveRadioUrls();
    }

    // loads the radio URLs from the disk
    protected static Map<String, String> loadRadioUrls() {
        // new HashMap
        Map<String, String> hashMap = new HashMap<>();

        // try to find the file
        File urlFile = new File(DEFAULT_RADIO_URLS_FILE);

        // if it exists, read from it
        if (urlFile.exists()) {
            // new Gson to read in the data
            Gson g = new Gson();

            // read the data
            try {
                // read the data
                hashMap = g.fromJson(new FileReader(urlFile), HashMap.class);
                // check null
                if (hashMap == null) {
                    hashMap = new HashMap<>();
                }
            } catch (FileNotFoundException e) {
                System.err.println("error reading radio url file");
                e.printStackTrace();
            }
        }

        // return the map
        return hashMap;
    }

    // saves the radio URLs to the disk
    protected static void saveRadioUrls() {
        Gson g = new Gson();
        String data = g.toJson(radioUrls);

        // write it
        writeJson(data, DEFAULT_RADIO_URLS_FILE);
    }

    // writes JSON data to a file
    protected static void writeJson(String json, String filename) {
        // write it to the file
        try {
            // TODO: Reformat how it's printed to include line breaks to make direct editing easier
            FileWriter writer = new FileWriter(new File(filename));
            for (char c : json.toCharArray()) {
                // if the character is the ending bracket, insert a newline first
                if (c == '}') {
                    writer.write("\n");
                }
                writer.write(c);
                // if it was a bracket, write a newline and a tab
                if (c == '{') {
                    writer.write("\n\t");
                }
                // if it was a comma, write a newline and a tab
                else if (c == ',') {
                    writer.write("\n\t");
                }
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("encountered an error while saving trigger phrase responses");
            e.printStackTrace();
        }
    }

}