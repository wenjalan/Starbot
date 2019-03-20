package wenjalan.starbot.servers;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.GuildController;
import wenjalan.starbot.Starbot;
import wenjalan.starbot.Users;
import wenjalan.starbot.engines.AudioEngine;
import wenjalan.starbot.listeners.MessageListener;
import wenjalan.starbot.listeners.ServerEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

// features specific to the Dingus Crew Server
public class DingusCrew {

    // the server ID
    public static final long ID_LONG = 175372417194000384L;

    // the number of times Justin has been kicked
    public static int justin_kicks = 0;

    // MessageListener
    public static class DingusCrewMessageListener extends MessageListener {

        // constructor
        public DingusCrewMessageListener(Starbot starbot) {
            super(starbot);
        }

        // onMessageReceived
        @Override
        public void onMessageReceived(MessageReceivedEvent e) {
            // if the message wasn't sent from a guild, return
            if (!e.isFromType(ChannelType.TEXT)) return;

            // check if this is The Dingus Crew
            if (e.getGuild().getIdLong() != DingusCrew.ID_LONG) {
                return;
            }

            // do the normal things
            // ! if the original listener is already listening, there's no need
            // super.onMessageReceived(e);

            // check if this is a server-specific command
            if (isCommand(e.getMessage().getContentRaw())) {
                // check if the command is available in the Commands enum
                String query = e.getMessage().getContentRaw().substring(1);
                String[] tokens = query.split("\\s+");
                for (DingusCommands c : DingusCommands.values()) {
                    // if the command matches
                    if (tokens[0].equalsIgnoreCase(c.name())) {
                        // run it
                        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);
                        c.run(e, args);
                    }
                }
            }
        }
    }

    // Commands
    public enum DingusCommands {

        nword {
            // kicks Justin from the server, sends him an invite, and increments his name by 1
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // check for perms
                if (!e.getGuild().getMember(e.getJDA().getSelfUser()).hasPermission(
                        Permission.KICK_MEMBERS,
                        Permission.NICKNAME_MANAGE
                )) {
                    e.getChannel().sendMessage("give me perms").queue();
                    return;
                }

                // check if the author has perms to kick
                if (!e.getGuild().getMember(e.getAuthor()).hasPermission(
                        Permission.KICK_MEMBERS
                )) {
                    e.getChannel().sendMessage("you don't have perms").queue();
                    return;
                }

                // record the number of times Justin's been kicked
                Member justin = e.getGuild().getMemberById(Users.JUSTIN);
                try {
                    justin_kicks = Integer.parseInt(justin.getNickname()) + 1;
                } catch (NumberFormatException ex) {
                    e.getChannel().sendMessage("you gotta change his nickname yourself").queue();
                }

                // kick Justin
                e.getGuild().getController().kick(justin).reason("insufficient permissions: you don't have the nword pass" + justin_kicks).queue();

                // send him an invite to the server
                String invite = e.getTextChannel().createInvite()
                        .setMaxAge(1000)
                        .setMaxUses(1)
                        .complete()
                        .getURL();
                justin.getUser().openPrivateChannel().complete().sendMessage(invite).queue();
            }
        },

        play {
            // extra functionality of the !play command for The Dingus Crew
            // kicks the author if it's 8:00 PM - 8:10 PM
            // kicks the author if the number of times music has been played (50, 100, 150) is a multiple of 50
            @Override
            public void run(MessageReceivedEvent e, String[] args) {
                // code runs in conjunction with code in CommandEngine
                playCount++;

                // if this is a multiple of 50, or it's 8:00 to 8:15 PM, kick the author
                LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("PST")));
                int hour = now.getHour();
                int minute = now.getMinute();
                if (playCount % 50 == 0 || (hour == 20 && minute <= 15)) {
                    try {
                        // kick the author
                        Member author = e.getMember();
                        GuildController controller = e.getGuild().getController();
                        controller.kick(author, "no.").queue();

                        // pause playback if playing
                        AudioEngine.SendHandler handler = (AudioEngine.SendHandler) e.getGuild().getAudioManager().getSendingHandler();
                        if (handler.isPlaying()) {
                            handler.pause();
                        }
                        handler.pause();
                    } catch (InsufficientPermissionException ex) {
                        e.getTextChannel().sendMessage("no.").queue();
                    }
                }
            }
        };

        protected int playCount = 0;

        public abstract void run(MessageReceivedEvent e, String[] args);

    }

    // ServerEventListener
    public static class DingusCrewEventListener extends ServerEventListener {

        // constructor
        public DingusCrewEventListener(Starbot starbot) {
            super(starbot);
        }

        @Override
        public void onGuildMemberJoin(GuildMemberJoinEvent e) {
            // check if we're in The Dingus Crew
            if (e.getGuild().getIdLong() != DingusCrew.ID_LONG) {
                return;
            }

            // if the other listener is adapted, we don't need to call super
            // super.onGuildMemberJoin(event);

            // if the Member is Justin
            if (e.getUser().getIdLong() == Users.JUSTIN) {
                // update his nickname with the recent most count
                e.getGuild().getController().setNickname(e.getMember(), "" + justin_kicks).queue();

                // give him his role back
                e.getGuild().getController().addSingleRoleToMember(e.getMember(), e.getGuild().getRoleById(292906691425730560L)).queue();
            }
        }

    }

}
