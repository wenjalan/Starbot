package wenjalan.starbot.engine;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

// functionality related to the !poll command
public class PollEngine {

    // the emojis to use for various options
    public static final String[] EMOJI_OPTION = {
            ":regional_indicator_a:",
            ":regional_indicator_b:",
            ":regional_indicator_c:",
            ":regional_indicator_d:",
    };

    // the emoji unicodes to use for reactions
    public static final String[] EMOJI_UNICODE = {
        "U+1F1E6", // regional_indicator_a
        "U+1F1E7", // regional_indicator_b
        "U+1F1E8", // regional_indicator_c
        "U+1F1E9", // regional_indicator_d

    };

    // poll class
    public static class Poll {

        // the poll question
        private final String pollQuestion;

        // the options to choose from
        private final List<String> options;

        // constructor
        public Poll(String question, List<String> options) {
            this.pollQuestion = question;
            this.options = options;
        }

        // returns the poll message
        public String getMessage() {
            String message = "**" + pollQuestion + "**";
            for (int i = 0; i < options.size(); i++) {
                String optionEmoji = EMOJI_OPTION[i];
                message += "\n" + optionEmoji + " " + options.get(i);
            }
            return message;
        }

        // prints the poll to a textchannel
        public void sendPoll(TextChannel channel) {
            // send the message and add reactions
            String pollText = getMessage();
            channel.sendMessage(pollText).queue((message) -> {
                for (int i = 0; i < options.size(); i++) {
                    message.addReaction(EMOJI_UNICODE[i]).queue();
                }
            });
        }

    }

}
