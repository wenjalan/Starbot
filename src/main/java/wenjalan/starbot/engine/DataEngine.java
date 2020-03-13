package wenjalan.starbot.engine;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

// handles all persistent data processing
public class DataEngine {

    // some constants
    public static class Constants {

        public static final long OWNER_ID_LONG = 478706068223164416L; // wenton#8946

    }

    // the wrapper class of a Guild Specific Keyphrases object, for JSON conversion
    public static class GuildKeyPhrases {

        // the id of the guild
        private long guildId;

        // the map of phrases to responses
        private Map<String, String> phrasesToResponses;

        // constructor
        public GuildKeyPhrases(long guildId, Map<String, String> phrasesToResponses) {
            this.guildId = guildId;
            this.phrasesToResponses = phrasesToResponses;
        }

        // accessors //
        public long getGuildId() {
            return guildId;
        }

        public Map<String, String> getPhrasesToResponses() {
            return phrasesToResponses;
        }

    }

    // the default filename for the responses
    public static final String DEFAULT_RESPONSES_FILE = "assets/responses.txt";

    // the default filename for the trigger phrase responses
    public static final String DEFAULT_TRIGGER_RESPONSES_FILE = "assets/phraseresponses.json";

    // the default filename for the radios phrase responses
    public static final String DEFAULT_RADIO_URLS_FILE = "assets/radios.json";

    // the name of the resources file
    public static final String PROPERTIES_FILEPATH = "assets/starbot.properties";

    // the map of properties
    protected static Map<String, String> properties;

    // the random responses Starbot has when mentioned
    protected static List<String> responses = null;

    // the trigger responses Starbot has when people say dumb things
    protected static Map<Long, Map<String, String>> triggerResponses = null;

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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)))) {
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

    // returns a HashMap of the trigger phrase responses for a guild
    public static Map<String, String> getTriggerResponses(long guildId) {
        // if null, load first
        if (triggerResponses == null) {
            triggerResponses = loadTriggerResponses();
        }

        // return a copy
        Map<String, String> gkf = triggerResponses.get(guildId);
        return gkf;
    }

    // loads the trigger phrase responses from the disk
    // returns a map of guild ids associated with their respective keys phrases and responses
    protected static Map<Long, Map<String, String>> loadTriggerResponses() {
        return loadTriggerResponses(DEFAULT_TRIGGER_RESPONSES_FILE);
    }

    // loads the trigger phrase responses from the disk
    // returns a map of guild ids associated with their respective keys phrases and responses
    protected static Map<Long, Map<String, String>> loadTriggerResponses(String file) {
        Gson g = new Gson();
        try {
            // fuck around with type stuff
            Type listOfGKF = new TypeToken<ArrayList<GuildKeyPhrases>>() {}.getType();
            List<GuildKeyPhrases> phrases = g.fromJson(new FileReader(file), listOfGKF);

            // if it's null (empty), complain and return an empty hashmap
            if (phrases == null) {
                System.err.println("didn't find anything in the " + file + " file");
                return new HashMap<>();
            } else {
                // announce what we've loaded
                System.out.println("loaded " + phrases.size() + " guild key phrase pairs from disk");
                // map to return
                Map<Long, Map<String, String>> map = new HashMap<>();
                // for every entry
                for (GuildKeyPhrases gkf : phrases) {
                    // add it to the overall map
                    map.put(gkf.getGuildId(), gkf.getPhrasesToResponses());
                    System.out.println("loaded phrases for guild id " + gkf.getGuildId());
                }
                // return the map
                return map;
            }
        } catch (FileNotFoundException e) {
            System.err.println("encountered an error while loading trigger phrase responses");
            e.printStackTrace();
            return null;
        }
    }

    // returns whether or not a query contains a trigger phrase
    public static boolean hasTriggerPhrase(long guildId, String query) {
        // ignore casing
        query = query.toLowerCase();

        // load responses if not loaded
        if (triggerResponses == null) {
            triggerResponses = loadTriggerResponses(DEFAULT_TRIGGER_RESPONSES_FILE);
        }

        // find the proper guild
        Map<String, String> gkf = triggerResponses.get(guildId);

        // if the gkf doesn't exist, return false
        if (gkf == null) {
            return false;
        }

        // return whether or not the query contains a valid trigger phrase
        for (String phrase : gkf.keySet()) {
            if (query.contains(phrase)) {
                return true;
            }
        }

        // return false
        return false;
    }

    // returns the response for a given trigger phrase
    // returns null if none was found
    public static String getTriggerResponseFor(long guildId, String query) {
        // ignore casing
        query = query.toLowerCase();

        // load responses if not loaded
        if (triggerResponses == null) {
            triggerResponses = loadTriggerResponses(DEFAULT_TRIGGER_RESPONSES_FILE);
        }

        // get the guild's map
        Map<String, String> gkf = triggerResponses.get(guildId);

        // if the gkf doesn't exist, return null
        if (gkf == null) {
            return null;
        }

        // return the proper response
        for (String phrase : gkf.keySet()) {
            if (query.contains(phrase)) {
                return gkf.get(phrase);
            }
        }

        // return null
        return null;
    }

    // saves the trigger phrase responses to the disk
    public static void saveTriggerResponses(String file) {
        // get the JSON String
        Gson g = new Gson();

        // make a whole bunch of Guild Key Phrase objects
        List<GuildKeyPhrases> guildKeyPhrases = new ArrayList<>();
        for (long id : triggerResponses.keySet()) {
            Map<String, String> responses = triggerResponses.get(id);
            guildKeyPhrases.add(new GuildKeyPhrases(id, responses));
        }

        // create a json string
        String json = g.toJson(guildKeyPhrases);

        // write it
        writeJson(json, file);
    }

    // saves the trigger phrase responses to the disk
    public static void saveTriggerResponses() {
        saveTriggerResponses(DEFAULT_TRIGGER_RESPONSES_FILE);
    }

    // adds a response to the trigger phrase responses
    public static void addTriggerPhrase(long guildId, String triggerPhrase, String response) {
        // ignore casing
        triggerPhrase = triggerPhrase.toLowerCase();

        // load the triggerResponses if not already loaded
        if (triggerResponses == null) {
            triggerResponses = loadTriggerResponses();
        }

        // find the guild's map
        Map<String, String> gkf = triggerResponses.get(guildId);

        // if no such map exists, create a new one
        if (gkf == null) {
            gkf = new HashMap<>();
            triggerResponses.put(guildId, gkf);
        }

        // add an entry to the responses
        gkf.put(triggerPhrase, response);

        // save the file
        saveTriggerResponses();
    }

    // removes a response from the trigger phrase responses
    public static void removeTriggerPhrase(long guildId, String triggerPhrase) {
        // load the triggerResponses if not already loaded
        if (triggerResponses == null) {
            triggerResponses = loadTriggerResponses();
        }

        // get the guild's map
        Map<String, String> gkf = triggerResponses.get(guildId);

        // remove an entry to the responses
        gkf.remove(triggerPhrase);

        // save the file
        saveTriggerResponses();
    }

    // saves a MarkovModel to the disk
    public static void saveModel(long guildId, MarkovEngine.MarkovModel model) {
        final String filename = "assets/markov/" + guildId + ".json";
        Gson gson = new Gson();
        final String json = gson.toJson(model);
        writeJson(json, filename);
        System.out.println("saved markov model for guild id " + guildId + " to " + filename);
    }

    // loads a MarkovModel from the disk
    public static MarkovEngine.MarkovModel loadModel(long guildId) {
        final String filename = "assets/markov/" + guildId + ".json";
        Gson gson = new Gson();
        try {
            MarkovEngine.MarkovModel model = gson.fromJson(new FileReader(filename), MarkovEngine.MarkovModel.class);
            if (model != null) System.out.println("loaded markov model for guild id " + guildId);
            else System.err.println("loaded null markov model for guild id " + guildId);
            return model;
        } catch (IOException e) {
            System.err.println("error loading markov model for guild id " + guildId + ": " + e.getMessage());
            return null;
        }
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
        // get the file
        File file = new File(filename);

        // create the file
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.err.println("error creating file " + filename + ": " + e.getMessage());
            e.printStackTrace();
            return;
        }

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
            return;
        }
    }

}