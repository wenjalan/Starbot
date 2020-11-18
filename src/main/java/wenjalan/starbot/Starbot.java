package wenjalan.starbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wenjalan.starbot.listener.GuildEventListener;
import wenjalan.starbot.listener.LifeCycleEventListener;
import wenjalan.starbot.listener.MessageListener;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

// main program entry point
public class Starbot {

    // list of default permissions Starbot will ask for
    public static final List<Permission> DEFAULT_PERMISSIONS = Arrays.asList(
            Permission.NICKNAME_CHANGE,
            Permission.NICKNAME_MANAGE,
            Permission.VIEW_CHANNEL,

            Permission.MESSAGE_WRITE,
            Permission.MESSAGE_READ,
            Permission.MESSAGE_MANAGE,
            Permission.MESSAGE_EMBED_LINKS,
            // Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_HISTORY,
            Permission.MESSAGE_MENTION_EVERYONE,
            // Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_ADD_REACTION,

            Permission.VOICE_CONNECT,
            Permission.VOICE_SPEAK,
            Permission.VOICE_MUTE_OTHERS,
            Permission.VOICE_DEAF_OTHERS,
            Permission.VOICE_MOVE_OTHERS
    );

    // whether the program should be forced to ask for the token
    private final static boolean FORCE_ASK_TOKEN = true;

    // the held instance of JDA, use only if there's no other way to access JDA
    private static JDA jda;

    // the instance of Starbot
    private static Starbot instance;

    // Logger
    public static final Logger logger = LogManager.getLogger();

    // program entry point
    public static void main(String[] args) {
        // grab token
        String token = null;
        if (args.length == 0 || FORCE_ASK_TOKEN) {
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
        builder.addEventListeners(new GuildEventListener());

        // initialize the bot
        try {
            jda = builder.build().awaitReady();
        } catch (InterruptedException | LoginException e) {
            logger.error("Encountered an error during JDA initialization: " + e.getMessage());
        }
    }

    // instance accessor
    public static Starbot getInstance() {
        return instance;
    }

    // jda accessor
    public static JDA getJda() {
        return jda;
    }

}
