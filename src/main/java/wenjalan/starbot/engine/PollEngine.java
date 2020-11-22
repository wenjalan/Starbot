package wenjalan.starbot.engine;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

// handles all poll related functions
public class PollEngine {

    // poll class
    public static class Poll {
        // emoji sets
        public enum EmojiSet {
            NUMBERS {
                @Override
                public List<String> getEmojis() {
                    return Arrays.asList(
                            "\u0031",
                            "\u0032",
                            "\u0033",
                            "\u0034",
                            "\u0035",
                            "\u0036",
                            "\u0037",
                            "\u0038"
                    );
                }
            },

            LETTERS {
                @Override
                public List<String> getEmojis() {
                    return Arrays.asList(
                            "U+1F1E6",
                            "U+1F1E7",
                            "U+1F1E8",
                            "U+1F1E9",
                            "U+1F1EA",
                            "U+1F1EB",
                            "U+1F1EC",
                            "U+1F1ED"
                    );
                }
            },

            COLORS {
                @Override
                public List<String> getEmojis() {
                    return Arrays.asList(
                            "U+1F534",
                            "U+1F7E0",
                            "U+1F7E1",
                            "U+1F7E2",
                            "U+1F535",
                            "U+1F7E3",
                            "U+1F7E4",
                            "\u26AB"
                    );
                }
            };

            // returns the 10 emojis associated with this EmojiSet
            public abstract List<String> getEmojis();
        }

        // the @Mention of the author
        public String authorMention;

        // the prompt of this Poll
        private String prompt;

        // a list of poll options
        private List<String> items;

        // the emoji set to use
        private EmojiSet emojiSet = EmojiSet.LETTERS;

        // constructor
        public Poll(String prompt, Member author) {
            this.prompt = prompt;
            this.authorMention = author.getAsMention();
        }

        // adds an item to the poll
        public void addItem(String item) {
            if (items.size() >= 8) {
                throw new IllegalStateException("Poll already has the maximum number of items (8)");
            }
            items.add(item);
        }

        // removes an item from the poll
        public void removeItem(String item) {
            if (!items.contains(item)) {
                throw new IllegalArgumentException("Poll does not contain item: " + item);
            }
            items.remove(item);
        }

        // sets the EmojiSet to use
        public void useEmojiSet(EmojiSet set) {
            this.emojiSet = set;
        }

        // returns a list of the poll's options
        public List<String> getItems() {
            return new LinkedList<>(items);
        }
    }

    // singleton
    private static PollEngine instance = null;

    // private constructor
    private PollEngine() {
        if (instance != null) {
            throw new IllegalStateException("An instance of PollEngine already exists");
        }
    }

    // creates a new Poll
    public void startPoll(TextChannel channel, Poll poll) {
        // get the poll's prompt, emojis and items
        String prompt = poll.prompt;
        String author = poll.authorMention;
        List<String> emojis = poll.emojiSet.getEmojis();
        List<String> items = poll.items;

        // create poll content
        String pollContent = items.stream()
                .map(item -> emojis.remove(0) + " " + item)
                .collect(Collectors.joining("\n"));

        // create an embed and send it
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.BLUE);
        embed.setTitle(prompt);
        embed.setDescription("Asked by " + author);
        embed.addField(null, pollContent, false);
        channel.sendMessage(embed.build()).queue(msg -> {
            // react to the message with each of the used emojis
            List<String> emojisAgain = poll.emojiSet.getEmojis();
            for (int i = 0; i < items.size(); i++) {
                msg.addReaction(emojisAgain.get(i)).queue();
            }
        });
    }

    // instance accessor
    public static PollEngine get() {
        if (instance == null) {
            instance = new PollEngine();
        }
        return instance;
    }

}
