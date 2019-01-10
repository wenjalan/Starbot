import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.ChannelManager;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.Presence;

import java.time.*;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;

// main Starbot class
public class Starbot {

    // the command prefix of the bot
    final static String COMMAND_PREFIX = "!";

    // main
    public static void main(String[] args) {
        new Starbot(args[0]);
    }

    private JDA jda;

    // constructor
    public Starbot(String botToken) {
        try {
            // start JDA and log in
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(botToken)
                    .addEventListener(new MessageListener())
                    // .addEventListener(new VoiceChannelEventListener())
                    .build();
            // print that we've started successfully
            System.out.println("Starbot online!");
            // print the invite
            System.out.printf("Invite: %s%n", jda.asBot().getInviteUrl());

            jda.awaitReady();

//            Thread t = new Thread(new SmashUltimateNicknameUpdater());
//            t.start();

        } catch (Exception e) {
            System.err.println("Failed to log in! Was the bot token correct?");
            e.printStackTrace();
            System.exit(1);
        }
    }

//    // updates my nickname based on how many hours are left until ultimate
//    public class SmashUltimateNicknameUpdater implements Runnable {
//
//        final LocalDateTime ULTIMATE_RELEASE = LocalDateTime.of(2018, 12, 6, 21, 0);
//
//        LocalDateTime previousUpdate = null;
//
//        @Override
//        public void run() {
//            try {
//                for (LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("PST"))); now.isBefore(ULTIMATE_RELEASE); now = LocalDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("PST")))) {
//                    if (previousUpdate == null || Math.abs(Duration.between(now, previousUpdate).toMinutes()) >= 1) {
//                        update(now);
//                        updateChannelName(now);
//                        previousUpdate = now;
//                    }
//                    Thread.sleep(10000);
//                }
//                // mention me, Alex and Shang
//                Guild dingusCrew = jda.getGuildById(175372417194000384L);
//                Member me = dingusCrew.getMemberById(478706068223164416L);
//                Member alex = dingusCrew.getMemberById(175049231709634560L);
//                Member shang = dingusCrew.getMemberById(210924920010571776L);
//                TextChannel whohastheanswers = dingusCrew.getTextChannelById(175372417194000384L);
//                for (int i = 0; i < 10; i++) {
//                    whohastheanswers.sendMessage(me.getAsMention() + alex.getAsMention() + shang.getAsMention() + "**SMASH ULTIMATE IS OUT**").queue();
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                // send me a message saying we fucked up
//                // get me
//                User me = jda.getUserById(478706068223164416L);
//                // send me a message
//                me.openPrivateChannel().complete().sendMessage("Hey we fucked up or ultimate is out").complete();
//            }
//        }

//        // updates my nickname
//        private void update(LocalDateTime now) {
//            // calculate the duration between now and the release of smash ultimate
//            Duration duration = Duration.between(now, ULTIMATE_RELEASE);
//            // get the days
//            long days = duration.toDays();
//            duration = duration.minusDays(days);
//            // get the hours
//            long hours = duration.toHours();
//            duration = duration.minusHours(hours);
//            // get the minutes
//            long minutes = duration.toMinutes();
//
//            // create the niokname
//            String nickname = days + "d " + hours + "h " + minutes + "m until Ultimate";
//
//            // set the nickname in The dingus crew
//            Guild dingusCrew = jda.getGuildById(175372417194000384L);
//            // get me
//            Member me = dingusCrew.getMemberById(478706068223164416L);
//            // set my nickname
//            dingusCrew.getController().setNickname(me, nickname).complete();
//
//            // send me a message
//            // me.getUser().openPrivateChannel().complete().sendMessage("Changed to " + nickname).complete();
//        }

//        // updates the channel name in Lakeside
//        private void updateChannelName(LocalDateTime now) {
//            // calculate the duration between now and the release of smash ultimate
//            Duration duration = Duration.between(now, ULTIMATE_RELEASE);
//            // get the days
//            long days = duration.toDays();
//            duration = duration.minusDays(days);
//            // get the hours
//            long hours = duration.toHours();
//            duration = duration.minusHours(hours);
//            // get the minutes
//            long minutes = duration.toMinutes();
//
//            // create the channel name
//            String channelName = "ultimate-" + days + "d-" + hours + "h-" + minutes + "m";
//
//            // set the channel name in Lakeside
//            Guild lakeSide = jda.getGuildById(419598386933530636L);
//            Channel ultimateChannel = lakeSide.getTextChannelById(518972817153458178L);
//            ChannelManager manager = ultimateChannel.getManager();
//            manager.setName(channelName).queue();
//        }
//
//    }

    // message listener
    public class MessageListener extends ListenerAdapter {
        // on message received
        @Override
        public void onMessageReceived(MessageReceivedEvent e) {

            // get the user
            User user = e.getAuthor();
            // get the user's JDA instance
            JDA userJda = user.getJDA();
            // get the Presence of this User
            Presence presence = userJda.getPresence();
            // get the game
            Game game = presence.getGame();

            // 喜欢路查

            // if the message came from a bot
            if (e.getAuthor().isBot()) {
                return;
            }
            // if the message came from a dm
            if (e.isFromType(ChannelType.PRIVATE)) {
                // ask to join a server of theirs
                e.getChannel().sendMessage("hey invite me to your server: " + e.getJDA().asBot().getInviteUrl()).queue();
            }
            // if the message came from a text channel
            else if (e.isFromType(ChannelType.TEXT)) {
                String msg = e.getMessage().getContentDisplay();
                // check if starts with the command prefix
                if (msg.startsWith(COMMAND_PREFIX)) {
                    // parse the command into a String[]
                    String[] commandQuery = msg.split("\\s+");
                    // compare the command query with all known commands
                    for (Command command : Command.values()) {
                        // if the query matches the name of the command
                        if (commandQuery[0].substring(1).equalsIgnoreCase(command.name())) {
                            // parse the command arguments
                            String[] args = Arrays.copyOfRange(commandQuery, 1, commandQuery.length);
                            // run the command
                            command.run(e, args);
                            break;
                        }
                    }
                }
                // check if the message contains a key phrase
                else if (KeyPhrase.containsKeyPhrase(msg.toLowerCase())) {
                    // send the corresponding response
                    e.getChannel().sendMessage(KeyPhrase.get(msg.toLowerCase())).queue();
                }
                // check if the message was directed towards @Starbot
                else if (e.getMessage().getMentionedUsers().contains(e.getJDA().getSelfUser())) {
                    // send them a random response
                    e.getChannel().sendMessage(Responses.next()).queue();
                }
            }
            // if it came from somewhere else
            else {
                // print it to the console, I guess
                String msg = e.getMessage().getContentDisplay();
                User author = e.getAuthor();
                System.out.printf("(%s) %s: %s%n", e.getChannel().getName(), author.getName(), msg);
            }
        }

    }

}
