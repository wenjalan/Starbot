package wenjalan.starbot.guilds;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;

// custom guild actions for The Wholesome Degenerates
public class WholesomeDegenerates {

    // constants
    public static final long ID = 503787981090586639L;
    public static final long BANNED_LETTER_CHANNEL_ID = 682677511158890578L;

    // listener
    public static class WDListener extends ListenerAdapter {

        // fields
        public static char bannedLetterChannelLetter = getRandomChar();

        // a JDA instance to manage stuff with
        private static JDA jda;

        // onReady
        @Override
        public void onReady(ReadyEvent e) {
            jda = e.getJDA();

            // deprecated: banned letter channel
//            // set channel desc
//            updateChannelLetter(bannedLetterChannelLetter);
//
//            // start update thread for banned letter channel
//            new Thread() {
//                @Override
//                public void run() {
//                    // coding genius
//                    final boolean EVER = true;
//                    int lastUpdate = 0; // last day we updated
//                    for (;EVER;) {
//                        // check if it's time to update
//                        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("PST")));
//                        // time is 6 AM and we haven't updated today
//                        if (now.getHour() == 6 && lastUpdate != now.getDayOfMonth()) {
//                            // change the letter
//                            bannedLetterChannelLetter = getRandomChar();
//                            lastUpdate = now.getDayOfMonth();
//
//                            // change the channel desc
//                            updateChannelLetter(bannedLetterChannelLetter);
//                        }
//                        // sleep for 30 minutes
//                        try {
//                            Thread.sleep(30 * 60 * 60);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }.start();
        }

        // message received
        @Override
        public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
            // if the guild wasn't WD or the message was sent by a bot, quit
            if (e.getGuild().getIdLong() != ID || e.getAuthor().isBot()) {
                return;
            }
            // if the channel was the banned letter channel
            if (e.getChannel().getIdLong() == BANNED_LETTER_CHANNEL_ID) {
                // if the message contained the banned letter, delete it
                if (e.getMessage().getContentDisplay().toLowerCase().contains("" + bannedLetterChannelLetter)) {
                    e.getMessage().delete().queue();
                }
            }
        }

        // updates the letter displayed in the channel description
        private static void updateChannelLetter(char c) {
//            // get the channel
//            TextChannel channel = jda.getTextChannelById(BANNED_LETTER_CHANNEL_ID);
//
//            // set the description
//            channel.getManager().setTopic("today's banned letter is " + c).queue();
            // send the banned letter into the chat
            TextChannel channel = jda.getTextChannelById(BANNED_LETTER_CHANNEL_ID);
            channel.sendMessage("today's banned letter is " + c).queue();
        }

        // sets the banned letter channel letter
        public static void setBannedLetterChannelLetter(char c) {
            bannedLetterChannelLetter = c;
            updateChannelLetter(c);
        }

    }

    // returns a random letter, a-z (ascii index 97 to 122)
    public static char getRandomChar() {
        return (char) (new Random().nextInt(27) + 97);
    }

}
