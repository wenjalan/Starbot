package wenjalan.starbot;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import wenjalan.starbot.engines.KeyPhraseEngine;
import wenjalan.starbot.engines.ResponseEngine;
import wenjalan.starbot.listeners.MessageListener;

import java.util.Scanner;

// the main Starbot class, contains main() and the JDA setup
public class Starbot {

    // the filepath to the responses
    public static final String RESPONSES_FILEPATH = "responses.txt";

    // the JDA object
    protected JDA jda;

    // the KeyPhraseEngine, for responding to funny things with other funny things
    protected KeyPhraseEngine keyPhraseEngine;

    // the ResponseEngine, for responding to mentions with witty comments
    protected ResponseEngine responseEngine;

    // main
    public static void main(String[] args) {
        // the bot token
        String token;

        // if args is empty
        if (args.length == 0) {
            // ask the user for the token
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter bot token: ");
            token = scanner.next();
            scanner.close();
        }
        else {
            // get the key from args
            token = args[0];
        }

        // check if a valid key is there
        if (token == null || token.isEmpty()) {
            System.out.println("Error Initializing Starbot: Token not specified");
        }
        else {
            try {
                new Starbot(token);
            } catch (Exception e) {
                System.err.println("Error Starting Starbot: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // constructor
    protected Starbot(String token) throws Exception {
        // get a builder
        JDABuilder builder = new JDABuilder(token);

        // initialize engines
        this.keyPhraseEngine = new KeyPhraseEngine();
        this.responseEngine = new ResponseEngine(RESPONSES_FILEPATH);

        // build the JDA with our listeners
        this.jda = builder
                .addEventListener(new MessageListener(this))
                .build()
                .awaitReady();

        // announce that Starbot is ready and print an invite
        System.out.println("Starbot started successfully!");
        System.out.println("Invite: " + jda.asBot().getInviteUrl());
    }

    // accessors
    public KeyPhraseEngine getKeyPhraseEngine() {
        return this.keyPhraseEngine;
    }

    public ResponseEngine getResponseEngine() {
        return this.responseEngine;
    }

}
