package wenjalan.starbot.engine;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import wenjalan.starbot.guilds.WholesomeDegenerates;

import java.util.*;

import static wenjalan.starbot.engine.DataEngine.getTriggerResponses;

// handles the parsing and execution of commands
public class CommandEngine {

    // dm command enum
    protected enum DMCommand {

        // TODO: Figure out how to not show the Admin Commands in this
        // TODO: Until then, the !help command will stay unavailable in DM commands
//        // help command
//        help {
//            @Override
//            public void execute(PrivateMessageReceivedEvent event) {
//                // send a list of the commands
//                StringBuilder sb = new StringBuilder();
//                Arrays.stream(values()).forEach(s -> sb.append(s.toString() + "\n"));
//                event.getChannel().sendMessage(sb.toString()).queue();
//            }
//        },

        // reports the version of starbot
        version {
            @Override
            public void execute(PrivateMessageReceivedEvent event) {
                // send the current version
                event.getChannel().sendMessage("fuck if I know dude it's been like months since this command was updated").queue();
            }
        },

        // sends a link to invite Starbot
        invite {
            @Override
            public void execute(PrivateMessageReceivedEvent event) {
                // send an invite
                event.getChannel().sendMessage(event.getJDA().getInviteUrl()).queue();
            }
        },

        // shuts down starbot, only usable by me
        stop {
            @Override
            public void execute(PrivateMessageReceivedEvent event) {
                // check that the author is me
                long id = event.getAuthor().getIdLong();

                // if the author is me
                if (id == DataEngine.Constants.OWNER_ID_LONG) {
                    // shut down
                    event.getChannel().sendMessage("you got it boss").complete();

                    // log that we're shutting down because I said to
                    System.out.println("Shut down initiated by user " + event.getAuthor().getAsTag() +
                            " (ID: " + event.getAuthor().getIdLong() + ")");

                    // actually shut down
                    event.getJDA().shutdownNow();
                }
            }
        },

        // reloads the responses data, only usable by me
        reloadresponses {
            @Override
            public void execute(PrivateMessageReceivedEvent event) {
                // check that it's me
                long id = event.getAuthor().getIdLong();

                // if it was
                if (id == DataEngine.Constants.OWNER_ID_LONG) {
                    // reload the responses data
                    String[] args = parseArgs(event.getMessage());
                    if (args.length == 1) {
                        DataEngine.reloadResponses();
                    }
                    else { // I specified a specific file
                        DataEngine.reloadResponses(args[1]);
                    }

                    // send feedback
                    event.getChannel().sendMessage("done boss").queue();
                }
            }
        },

        // adds a new response to the bank of trigger phrases and responses
        addresponse {
            @Override
            public void execute(PrivateMessageReceivedEvent event) {
                // check that it's me
                long id = event.getAuthor().getIdLong();

                // if it was
                if (id == DataEngine.Constants.OWNER_ID_LONG) {
                    // get the phrase and the response
                    String query = event.getMessage().getContentRaw();

                    // find args in string
                    List<String> args = findArgsInString(query);

                    // find the phrase
                    String phrase = args.get(0);

                    // find the response
                    String response = args.get(1);

                    // add it to the list of recognized phrases
                    DataEngine.addTriggerPhrase(phrase, response);
                }
            }
        },

        // removes a trigger phrase
        removeresponse {
            @Override
            public void execute(PrivateMessageReceivedEvent event) {
                // check that it's me
                long id = event.getAuthor().getIdLong();

                // if it was
                if (id == DataEngine.Constants.OWNER_ID_LONG) {
                    // get the trigger phrase to remove
                    String phrase = findArgsInString(event.getMessage().getContentRaw()).get(0);

                    // if it's not a valid trigger phrase, say so
                    if (!ChatEngine.containsTriggerPhrase(phrase)) {
                        event.getChannel().sendMessage("trigger phrase not found").queue();
                        return;
                    }

                    // otherwise, remove it from the DataEngine
                    DataEngine.removeTriggerPhrase(phrase);
                }
            }
        },

        // lists the trigger phrases and their responses
        listresponses {
            @Override
            public void execute(PrivateMessageReceivedEvent event) {
                // check that it's me
                long id = event.getAuthor().getIdLong();

                // if it was
                if (id == DataEngine.Constants.OWNER_ID_LONG) {
                    // get the data from DataEngine
                    HashMap<String, String> map = DataEngine.getTriggerResponses();
                    // compile them together into a String
                    StringBuilder sb = new StringBuilder();
                    for (String phrase : map.keySet()) {
                        sb.append("\"" + phrase + "\" : \"" + map.get(phrase) + "\"\n");
                    }
                    // send that String to me
                    event.getChannel().sendMessage(sb.toString()).queue();
                }
            }
        },

        // lists the radios
        listradios {
            @Override
            public void execute(PrivateMessageReceivedEvent event) {
                // check me
                long id = event.getAuthor().getIdLong();
                if (id == DataEngine.Constants.OWNER_ID_LONG) {
                    Set<String> radios = DataEngine.getRadioNames();
                    StringBuilder sb = new StringBuilder();
                    for (String s : radios) {
                        sb.append(s + "\n");
                    }
                    event.getChannel().sendMessage(sb.toString()).queue();
                }
            }
        },

        // adds a radio
        addradio {
            @Override
            public void execute(PrivateMessageReceivedEvent event) {
                // check me
                long id = event.getAuthor().getIdLong();
                if (id == DataEngine.Constants.OWNER_ID_LONG) {
                    List<String> args = findArgsInString(event.getMessage().getContentRaw());
                    DataEngine.addRadio(args.get(0), args.get(1));
                }
            }
        },

        // removes a radio
        removeradio {
            @Override
            public void execute(PrivateMessageReceivedEvent event) {
                // check me
                long id = event.getAuthor().getIdLong();
                if (id == DataEngine.Constants.OWNER_ID_LONG) {
                    List<String> args = findArgsInString(event.getMessage().getContentRaw());
                    DataEngine.removeRadio(args.get(0));
                }
            }
        },

        // sets the banned letter in the banned letter channel
        setbannedletter {
            @Override
            public void execute(PrivateMessageReceivedEvent event) {
                // check the author is me
                long id = event.getAuthor().getIdLong();
                if (id == DataEngine.Constants.OWNER_ID_LONG) {
                    char c = event.getMessage().getContentRaw().split(" ")[1].charAt(0);
                    WholesomeDegenerates.WDListener.setBannedLetterChannelLetter(c);
                }
            }
        };

        public abstract void execute(PrivateMessageReceivedEvent event);

    }

    // guild command enum
    protected enum GuildCommand {

        // help command
        help {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // if they were asking for audio commands
                String[] args = parseArgs(event.getMessage());
                if (args.length > 1) {
                    // audio
                    if (args[1].equalsIgnoreCase("audio")) {
                        // send the audio commands
                        StringBuilder sb = new StringBuilder();
                        sb.append("audio commands:\n");
                        Arrays.stream(AudioEngine.AudioCommand.values()).forEach(s -> sb.append(s.toString() + "\n"));
                        event.getChannel().sendMessage(sb.toString()).queue();
                    }
                }
                // send them a list of general commands
                else {
                    // send a list of the commands
                    StringBuilder sb = new StringBuilder();
                    sb.append("available commands:\n" );
                    Arrays.stream(values()).forEach(s -> sb.append(s.toString() + "\n"));
                    sb.append("for audio commands do !help audio");
                    event.getChannel().sendMessage(sb.toString()).queue();
                }

            }
        },

        // reports the version of starbot
        version {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                event.getChannel().sendMessage("heh").queue();
            }
        },

        // sends a link to Starbot's GitHub
        github {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                event.getChannel().sendMessage("https://github.com/wenjalan/Starbot").queue();
            }
        },

        // starts a new poll
        poll {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // get the options
                List<String> args = findArgsInString(event.getMessage().getContentRaw());
                String question = args.get(0);
                List<String> options = args.subList(1, args.size());

                PollEngine.Poll poll = new PollEngine.Poll(question, options);
                poll.sendPoll(event.getChannel());
            }
        },

        // defines a query on urban dictionary
        define {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                if (event.getMessage().getContentRaw().length() > 7) {
                    String query = event.getMessage().getContentRaw().substring(8);
                    // replace any spaces with +
                    query = query.replaceAll(" ", "+");
                    List<String> definitions = UrbanDictionaryEngine.define(query);
                    if (definitions.isEmpty()) {
                        event.getChannel().sendMessage("nothing found").queue();
                    }
                    else {
                        String def = definitions.get(0);
                        if (def.length() > 2000) {
                            def = def.substring(0, 2000);
                        }
                        event.getChannel().sendMessage(def).queue();
                    }
                }
            }
        },

        // @'s a random user in the server
        someone {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // get a random member of the guild
                List<Member> members = event.getGuild().getMembers();
                Member poorSoul = members.get(new Random().nextInt(members.size()));
                event.getChannel().sendMessage(poorSoul.getAsMention()).queue();
            }
        },

        // sends a link to invite Starbot
        invite {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // send an invite
                event.getChannel().sendMessage(event.getJDA().getInviteUrl()).queue();
            }
        },

        // makes Starbot learn the speaking patterns of all the channels in a guild
        // usable only by me
        learn {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // learn from the guild
                event.getChannel().sendMessage("learning your ways...").queue();
                MarkovEngine.learn(event.getGuild());
                event.getChannel().sendMessage("done.").queue();
            }
        },

        // toggles the sending of markov or preset responses when mentioning Starbot
        // only if !learn was used before in the server
        markov {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // if it is a markov guild, turn it back to normal
                if (ChatEngine.isMarkov(event.getGuild())) {
                    ChatEngine.disableMarkov(event.getGuild());
                }
                // otherwise, enable it
                else {
                    ChatEngine.enableMarkov(event.getGuild());
                }
            }
        },

        // clears a number of Starbot-sent messages and commands from the chat, default 10
        clear {
            @Override
            public void execute(GuildMessageReceivedEvent event) {
                // check for permissions
                User self = event.getJDA().getSelfUser();
                if (!event.getGuild().getMember(self).hasPermission(
                        Permission.MESSAGE_HISTORY,
                        Permission.MESSAGE_MANAGE
                )) {
                    // if not, complain and return
                    event.getChannel().sendMessage("gimme the perms first").queue();
                    return;
                }

                // find out how many messages to clear
                int victims = 10;
                String[] args = parseArgs(event.getMessage());
                try {
                    // if there is a number there
                    if (args.length > 1) {
                        // get the number
                        int requestedVictims = Integer.parseInt(args[1]);
                        // if it's less than 1, say no
                        if (requestedVictims < 1) {
                            throw new IllegalArgumentException("too small, at least 1");
                        }
                        // otherwise, set the victims and get on with life
                        victims = requestedVictims;
                    }
                } catch (IllegalArgumentException e) {
                    // do nothing
                }

                // actually clear the messages
                event.getChannel().sendMessage("alright one sec").queue();
                List<Message> messages = event.getChannel().getHistory().retrievePast(victims).complete();
                long selfId = event.getJDA().getSelfUser().getIdLong();
                int messagesCleared = 0;
                for (Message msg : messages) {
                    // if it was sent by Starbot, delete it
                    if (msg.getAuthor().getIdLong() == selfId) {
                        msg.delete().queue();
                        messagesCleared++;
                    }
                    // if the message was a command, delete it
                    else if (CommandEngine.isCommand(msg)) {
                        msg.delete().queue();
                        messagesCleared++;
                    }
                    // if the message mentions Starbot, delete it
                    else if (msg.getMentionedUsers().contains(event.getJDA().getSelfUser())) {
                        msg.delete();
                        messagesCleared++;
                    }
                }

                // send that we're done
                event.getChannel().sendMessage("deleted " + messagesCleared + " messages").queue();
            }
        };

        public abstract void execute(GuildMessageReceivedEvent event);

    }

    // the global command prefix, used to indicate whether a message is a command
    // can be changed via admin controls
    protected static String globalCommandPrefix = "!";

    // helper method to recognize if a message is a command
    public static boolean isCommand(Message msg) {
        // get the content
        String content = msg.getContentRaw();

        // return if the message starts with the command prefix
        return content.startsWith(globalCommandPrefix);
    }

    // parses a DM-specific command
    public static void parseDMCommand(PrivateMessageReceivedEvent event) {
        // get the command they wanted
        String commandKeyword = event.getMessage().getContentRaw().split("\\s+")[0].substring(1);

        // find out if it's a valid command
        for (DMCommand c : DMCommand.values()) {
            // if the name of the command matches the keyword
            if (commandKeyword.equalsIgnoreCase(c.name())) {
                // run that command
                c.execute(event);
                // stop executing commands
                return;
            }
        }
        // do nothing if no commands matched the keyword
    }

    // parses a Guild-specific command
    public static void parseGuildCommand(GuildMessageReceivedEvent event) {
        // get the command they wanted
        String commandKeyword = event.getMessage().getContentRaw().split("\\s+")[0].substring(1);

        // find out if it's a valid command
        for (GuildCommand c : GuildCommand.values()) {
            // if the name of the command matches the keyword
            if (commandKeyword.equalsIgnoreCase(c.name())) {
                // run that command
                c.execute(event);
                // stop executing commands
                return;
            }
        }
        // find out if it's an AudioCommand
        for (AudioEngine.AudioCommand c : AudioEngine.AudioCommand.values()) {
            // if there's a command that matches, run it
            if (commandKeyword.equalsIgnoreCase(c.name())) {
                c.execute(event);
                return;
            }
        }
        // do nothing if no commands matched the keyword
    }

    // parses the arguments of a command into Strings, includes the command itself as an arg
    protected static String[] parseArgs(Message m) {
        return m.getContentRaw().split("\\s+");
    }

    // returns the global command prefix
    public static String getGlobalCommandPrefix() {
        return globalCommandPrefix;
    }

    // sets the global command prefix
    public static void setGlobalCommandPrefix(String newPrefix) {
        globalCommandPrefix = newPrefix;
    }

    // returns a List of Strings found enclosed within quotes in a given String
    public static List<String> findArgsInString(String query) {
        // the list of Strings we found
        List<String> strings = new ArrayList<>();

        // StringBuilder to build Strings
        StringBuilder sb = new StringBuilder();

        // whether or not we're currently in a quotation
        boolean isInside = false;

        // for the entire string
        for (int x = 0; x < query.length(); x++) {
            // get the current char
            char c = query.charAt(x);

            // if it's an ending quote, build and add the string to the list
            if (c == '"' && isInside) {
                strings.add(sb.toString());
                sb = new StringBuilder();
                isInside = false;
            }
            // if it's an opening quote, set isInside to true
            else if (c == '"' && !isInside) {
                isInside = true;
            }
            // if it's a character and we're inside a quote, add it to the sb
            else if (isInside) { // isInside == true
                sb.append(c);
            }
        }

        // return
        return strings;
    }

}
