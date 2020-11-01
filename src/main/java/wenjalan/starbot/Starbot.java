package wenjalan.starbot;

import net.dv8tion.jda.api.JDABuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wenjalan.starbot.listener.LifeCycleEventListener;
import wenjalan.starbot.listener.MessageListener;

import javax.security.auth.login.LoginException;
import java.util.Scanner;

// main program entry point
public class Starbot {

    // the instance of Starbot
    private static Starbot instance;

    // Logger
    public static final Logger logger = LogManager.getLogger();

    // program entry point
    // args[0]: the token for the bot account to run on
    public static void main(String[] args) {
        // grab token
        String token = null;
        if (args.length == 0) {
            logger.info("Enter bot token:");
            token = new Scanner(System.in).nextLine();
        }
        else {
            token = args[0];
        }

        // create a new Starbot
        instance = new Starbot(token);
    }

    // constructor
    public Starbot(String token) {
        // if an instance of Starbot already exists, complain
        if (instance != null) {
            logger.error("An instance of Starbot is already running");
            return;
        }

        // create the bot
        JDABuilder builder = JDABuilder.createDefault(token);

        // append all listeners
        builder.addEventListeners(new LifeCycleEventListener());
        builder.addEventListeners(new MessageListener());

        // initialize the bot
        try {
            builder.build().awaitReady();
        } catch (InterruptedException | LoginException e) {
            logger.error("Encountered an error during JDA initialization: " + e.getMessage());
        }
    }

    // instance accessor
    public static Starbot getInstance() {
        return instance;
    }

}
