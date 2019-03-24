package wenjalan.starbot.servers;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import wenjalan.starbot.Starbot;
import wenjalan.starbot.Users;

import java.time.*;

// features specific to The Lakeside Lounge
public class LakesideLounge {

    // server id
    public static final long ID_LONG = 419598386933530636L;
    public static final String ID_STRING = "419598386933530636";

    // update the main channel description with a random response
    public static class ChannelDescriptionUpdater implements Runnable {

        // Starbot
        protected Starbot starbot;

        // constructor
        public ChannelDescriptionUpdater(Starbot starbot) {
            this.starbot = starbot;
        }

        @Override
        public void run() {
            try {
                // get the ZoneId for PST
                ZoneId zoneId = ZoneId.of(ZoneId.SHORT_IDS.get("PST"));
                // the last numerical hour we updated the desc
                int lastUpdateHour = -1;
                // now
                LocalDateTime now;
                // loop forever
                for (;;) {
                    // update now
                    now = LocalDateTime.now(zoneId);
                    // if the hour now is different from the last one
                    if (now.getHour() != lastUpdateHour) {
                        // update
                        update();
                        // update the last update
                        lastUpdateHour = now.getHour();
                    }
                    // sleep for 10 seconds
                    Thread.sleep(10000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                // tell me
                User me = starbot.jda().getUserById(Users.ALAN);
                me.openPrivateChannel().complete().sendMessage("LakesideLounge.ChannelDescriptionUpdater was interrupted, come check it out").queue();
            }

        }

        // updates the description of #lounge with a random response
        protected void update() {
            // get a description
            String description;
            do {
                description = starbot.getResponseEngine().getNextResponse();
            } while (description.length() >= 1000);

            // update it in Lakeside
            Guild lakeside = starbot.jda().getGuildById(ID_STRING);
            if (lakeside == null) {
                System.err.println("couldn't find lakeside lounge.");
                return;
            }
            TextChannel lounge = lakeside.getTextChannelById(419598386933530638L);
            lounge.getManager().setTopic(description).queue();
        }

    }

}
