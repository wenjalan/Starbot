package wenjalan.starbot.engines;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

// has the responses to mentions and dms
public class ResponseEngine {

    // the list of responses
    protected LinkedList<String> responses;

    // the pool of responses to choose from
    protected List<String> pool;

    // random gen
    protected Random random;

    // the filepath to the responses txt file
    protected String filepath;

    // constructor
    // filepath: the path to the .txt doc with the responses
    public ResponseEngine(String filepath) throws IOException {
        this.responses = generateResponses(filepath);
        this.random = new Random();
        this.filepath = filepath;
    }

    // generates the responses based on a .txt file
    protected LinkedList<String> generateResponses(String filepath) throws IOException {
        // read the file and store it in a stack
        File responsesFile = new File(filepath);
        BufferedReader br = new BufferedReader(new FileReader(responsesFile));

        // parse into stack
        LinkedList<String> lines = new LinkedList<>();
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            // System.out.println("ResponseEngine: Parsed response " + line);
            lines.add(line);
        }

        // copy to pool for regeneration
        this.pool = new ArrayList<>();
        this.pool.addAll(lines);

        // return the stack
        return lines;
    }

    // regenerates the responses, should only be called after generateResponses() has been called at least once
    protected void regenerateResponses() {
        this.responses.addAll(pool);
        System.out.println("ResponseEngine: Refreshed response pool");
    }

    // returns a response off the stack
    public String getNextResponse() {
        // if the list is empty
        if (responses.size() == 0) {
            // generate a new one
            regenerateResponses();
        }
        return this.responses.remove(random.nextInt(responses.size()));
    }

}
