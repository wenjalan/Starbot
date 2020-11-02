package wenjalan.starbot.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// handles the loading and preprocessing of data assets on disk
public class AssetManager {

    // logger
    private static Logger logger = LogManager.getLogger();

    // default directory to look in for assets
    public static final String DEFAULT_ASSETS_DIRECTORY = "assets/";

    // singleton instance
    private static AssetManager instance = null;

    // the list of default responses
    private List<String> responses = null;

    // private constructor
    private AssetManager() {
        if (instance != null) {
            throw new IllegalStateException("An instance of AssetLoader already exists");
        }
        loadAssets();
    }

    // loads all hot loaded assets from disk
    public void loadAssets() {
        try {
            responses = loadResponses();
        } catch (IOException e) {
            logger.error("Encountered an error hot loading assets: " + e.getMessage());
        }
        logger.info("Hot loaded assets from disk");
    }

    // saves all hot loaded assets to disk
    public void saveAssets() {

    }

    // loads the responses from disk
    private List<String> loadResponses() throws FileNotFoundException {
        // initialize and read from GSON
        FileReader fr = new FileReader(new File(DEFAULT_ASSETS_DIRECTORY + "responses.json"));
        Gson gson = new Gson();
        TypeToken<List<String>> type = new TypeToken<List<String>>() {};
        List<String> responses = gson.fromJson(fr, type.getType());
        return responses;
    }

    // accessors //
    // returns the instance of AssetLoader
    public static AssetManager get() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    // loads Starbot's default responses from the disk
    public List<String> getResponses() {
        return new ArrayList<>(responses);
    }

}
