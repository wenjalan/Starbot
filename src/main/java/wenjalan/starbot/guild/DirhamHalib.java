package wenjalan.starbot.guild;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class DirhamHalib {

    public static final long GUILD_ID = 687152843559272511L;

    public static class DirhamHalibEventListener extends ListenerAdapter {

        @Override
        public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
            if (event.getGuild().getIdLong() != GUILD_ID) {
                return;
            }
        }

    }

}
