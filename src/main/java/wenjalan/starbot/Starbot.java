package wenjalan.starbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import wenjalan.starbot.engine.CommandEngine;
import wenjalan.starbot.guilds.DingusCrew;
import wenjalan.starbot.listener.JDAListener;
import wenjalan.starbot.listener.MessageListener;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Scanner;

// the main Starbot class, the entry point for the program
public class Starbot {

    // program entry point, we require a bot token to start
    public static void main(String[] args) {
        // run tests
        // test();

        // announce that we're starting
        System.out.println("Starting Starbot 4...");

        // find the discord bot token from args or ask for one
        String token = null;
        if (args.length > 0) {
            token = args[0];
            System.out.println("Found token from args!");
        }
        else {
            System.out.println("Please enter a bot token:");
            token = new Scanner(System.in).nextLine();
        }

        // create a new Starbot
        new Starbot(token);
    }

    // Starbot constructor, call with a botToken to create a new Starbot
    protected Starbot(String botToken) {
        // initialize JDA
        JDA jda = getJDA(botToken);

        // print invite
        System.out.println("Invite: " + jda.getInviteUrl());
    }

    // test method, used for when we're trying to do a specific thing with a specific part
    // after this method finishes, the program will end
    private static void test() {
        List<String> strings = CommandEngine.findArgsInString("\"foo\" \"bar\"");
        System.out.println(strings);

        // end program execution
        System.exit(0);
    }

    // returns an instance of the JDA given a bot token
    // returns null if an error occurred
    protected JDA getJDA(String token) {
        // create a Builder
        JDABuilder builder = new JDABuilder(token);

        // attach listeners
        builder.addEventListeners(new MessageListener());
        builder.addEventListeners(new JDAListener());

        // server-specific listeners
        builder.addEventListeners(new DingusCrew.DingusCrewListener()); // Dingus Crew

        // build
        JDA instance = null;
        try {
            instance = builder.build().awaitReady();
        } catch (LoginException | InterruptedException e) {
            // if an error occurs, stop the program
            System.err.println("Encountered an error while trying to build JDA!");
            e.printStackTrace();
            System.exit(1);
        }

        // return the instance
        return instance;
    }

}
