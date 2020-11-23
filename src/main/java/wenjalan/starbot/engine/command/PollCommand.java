package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import wenjalan.starbot.engine.PollEngine;

import java.util.ArrayList;
import java.util.List;

public class PollCommand implements Command {

    @Override
    public String getName() {
        return "poll";
    }

    @Override
    public String getDescription() {
        return "Creates a poll";
    }

    @Override
    public String getUsage() {
//        return "!poll <-n|-l|-c> \"<prompt>\" \"<option1>\" \"<option2>\" ... \"<option8>\"";
        return "!poll \"<prompt>\" \"<option1>\" \"<option2>\" ... \"<option8>\"";
    }

    @Override
    public boolean isGuildCommand() {
        return true;
    }

    @Override
    public boolean isDmCommand() {
        return false;
    }

    @Override
    public boolean isAdminCommand() {
        return false;
    }

    @Override
    public void run(Message msg) {
        // get the channel
        TextChannel channel = msg.getTextChannel();
        String query = msg.getContentRaw();

        // verify they specified at least one option
        if (query.length() <= "!poll ".length()) {
            channel.sendMessage("provide at least one option").queue();
            return;
        }
        query = query.substring("!poll".length()).trim();

        // if there was an emoji option specified, take it
        // todo: make the custom emojis thing work again
        PollEngine.Poll.EmojiSet emojiSet = PollEngine.Poll.EmojiSet.LETTERS;
//        if (query.startsWith("-")) {
//            // chop off the -
//            query = query.substring(1);
//
//            // if it was numbers
//            if (query.startsWith("n")) {
//                emojiSet = PollEngine.Poll.EmojiSet.NUMBERS;
//            }
//            // if it was letters
//            else if (query.startsWith("l")) {
//                emojiSet = PollEngine.Poll.EmojiSet.LETTERS;
//            }
//            // if it was colors
//            else if (query.startsWith("c")) {
//                emojiSet = PollEngine.Poll.EmojiSet.COLORS;
//            }
//            // if it was none
//            else {
//                channel.sendMessage(query.charAt(0) + " is not a valid emoji option").queue();
//                return;
//            }
//
//            // chop off the next letter
//            query = query.substring(1).trim();
//        }

        // get the poll strings
        if (query.length() <= 0) {
            channel.sendMessage("Specify a prompt").queue();
            return;
        }
        List<String> pollStrings = getTokensByQuotes(query);

        // make the poll
        String prompt = pollStrings.remove(0);
        PollEngine.Poll poll = new PollEngine.Poll(prompt, msg.getMember());
        poll.useEmojiSet(emojiSet);
        for (String option : pollStrings) {
            poll.addItem(option);
        }

        // if no items are in the poll, complain
        if (poll.getItems().isEmpty()) {
            channel.sendMessage("include an option").queue();
            return;
        }

        // start the poll
        PollEngine.get().startPoll(channel, poll);
    }

    // takes a series of Strings encapsulated by quotes and returns the elements within the quotes
    // ex: "\"hello\" \"world\"" returns ["hello", "world"]
    private List<String> getTokensByQuotes(String str) {
        // if the number of quotations isn't even, complain
        List<String> strings = new ArrayList<>();
        int quoteCount = 0;
        int quoteStart = -1;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '\"') {
                quoteCount++;
                if (quoteStart == -1) {
                    quoteStart = i;
                } else {
                    strings.add(str.substring(quoteStart + 1, i));
                    quoteStart = -1;
                }
            }
        }

        // if the wrong number of quotes
        if (quoteCount % 2 != 0) {
            throw new IllegalArgumentException("Syntax error: " + str);
        }

        // return the list
        return strings;
    }

}
