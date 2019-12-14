package wenjalan.starbot.guilds;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import wenjalan.starbot.engine.CommandEngine;
import wenjalan.starbot.engine.DataEngine;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// actions specific to The Dingus Crew
public class DingusCrew {

    // Constants
    public static class Constants {

        public static final long JUSTIN_ID_LONG = 175104183119249408L; // Jtango#9058

        public static final long JOSE_ID_LONG = 198889137988698113L; // LORD SHINE YOUR LIGHT ON ME#2430

        public static final long JARED_ID_LONG = 168485668694130688L; // Emuex#5154

        public static final long DINGUS_CREW_GUILD_ID_LONG = 175372417194000384L; // Dingus Crew Guild ID

        public static Map<Long, Long> memberToId = new HashMap<>(); // the map of member ids to their roles, stores those of kicked members

    }

    // Fields
    // the number of times the !play command has been used since startup
    protected static int totalPlays = 0;

    // the last person to use the !play command
    protected static Member lastPlayer = null;

    // Command Enum
    public enum DingusCrewCommand {

        // prints out the available Dingus Crew Commands
        // runs in parallel to the regular help command
        help {
            @Override
            public void execute(GuildMessageReceivedEvent e) {
                // send a list of the commands
                StringBuilder sb = new StringBuilder();
                sb.append("dingus crew commands:\n" );
                Arrays.stream(values()).forEach(s -> sb.append(s.toString() + "\n"));
                e.getChannel().sendMessage(sb.toString()).queue();
            }
        },

        // nword: kicks justin and sends him an invite
        nword {
            @Override
            public void execute(GuildMessageReceivedEvent e) {
                // check our perms
                if (!kickPermissionCheck(e)) {
                    return;
                }

                // if both check out, kick and reinvite justin
                Member justin = e.getGuild().getMemberById(Constants.JUSTIN_ID_LONG);
                if (justin == null) {
                    e.getChannel().sendMessage("couldn't find justin").queue();
                    return;
                }

                // kick him
                kickAndReinvite(e.getGuild(), justin, "you don't have the n word pass");
            }
        },

        // wife: kick jose and reinvites him
        wife {
            @Override
            public void execute(GuildMessageReceivedEvent e) {
                // check our perms
                if (!kickPermissionCheck(e)) {
                    return;
                }

                // if both check out, kick and reinvite jose
                Member jose = e.getGuild().getMemberById(Constants.JOSE_ID_LONG);
                if (jose == null) {
                    e.getChannel().sendMessage("couldn't find jose").queue();
                    return;
                }

                // kick and reinvite him
                DingusCrewCommand.kickAndReinvite(e.getGuild(), jose, "you don't have the w word pass");
            }
        },

        // loli: kicks alan (for testing purposes)
        loli {
            @Override
            public void execute(GuildMessageReceivedEvent e) {
                // check perms
                if (!kickPermissionCheck(e)) {
                    return;
                }

                // get me
                Member alan = e.getGuild().getMemberById(DataEngine.Constants.OWNER_ID_LONG);
                // if null complain
                if (alan == null) {
                    e.getChannel().sendMessage("couldn't find alan").queue();
                    return;
                }

                // kick me
                DingusCrewCommand.kickAndReinvite(e.getGuild(), alan, "sentenced to ten hours soft loli breathing");
            }
        },

        // kicks jared
        jared {
            @Override
            public void execute(GuildMessageReceivedEvent e) {
                // check perms
                if (!kickPermissionCheck(e)) {
                    return;
                }

                // find jared
                Member jared = e.getGuild().getMemberById(Constants.JARED_ID_LONG);
                if (jared == null) {
                    e.getChannel().sendMessage("couldn't find jared").queue();
                    return;
                }

                // kick
                kickAndReinvite(e.getGuild(), jared, "jared");
            }
        },

        // prints out the number of times !play has been used since startup
        plays {
            @Override
            public void execute(GuildMessageReceivedEvent e) {
                e.getChannel().sendMessage("!play has been used " + totalPlays + " times").queue();
            }
        },

        // play: additional play command kicks people
        // - when its from 8 PM to 4 AM
        // - 0.5% chance
        play {
            @Override
            public void execute(GuildMessageReceivedEvent e) {
                // save the user that last played something
                lastPlayer = e.getMember();

                // increment totalPlays
                totalPlays++;
                // check the time
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("PST")));

                // if it's 8 to 4
                if (now.getHour() > 17 || now.getHour() < 3) {
                    // roll a die
                    int roll = new Random().nextInt(200);
                    // if the roll was 0, kick the author
                    if (roll == 0) {
                        // announce their kicking
                        e.getChannel().sendMessage(e.getMember().getAsMention() + " won the lottery on !play #" + totalPlays).queue();
                        kickAndReinvite(e.getGuild(), e.getMember(), "won the !play lottery");
                    }
                }
            }
        },

        // seek: additional seek command kicks people
        // same conditions as the !play kick command
        seek {
            @Override
            public void execute(GuildMessageReceivedEvent e) {
                // check the time
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("PST")));

                // if it's 8 to 4
                if (now.getHour() > 17 || now.getHour() < 3) {
                    // roll a die
                    int roll = new Random().nextInt(200);
                    // if the roll was 0, kick the author
                    if (roll == 0) {
                        // announce their kicking
                        e.getChannel().sendMessage(e.getMember().getAsMention() + " won the lottery for !seek").queue();
                        kickAndReinvite(e.getGuild(), e.getMember(), "won the !seek lottery");
                    }
                }
            }
        },

        // fuck: kicks me, jose, justin and jared
        fuck {
            @Override
            public void execute(GuildMessageReceivedEvent e) {
                nword.execute(e);
                loli.execute(e);
                wife.execute(e);
                jared.execute(e);
            }
        },

        // no: kicks the last person that played something
        no {
            @Override
            public void execute(GuildMessageReceivedEvent e) {
                // kick the last player
                if (lastPlayer != null) {
                    kickAndReinvite(e.getGuild(), lastPlayer, "no");
                }
            }
        };

        public abstract void execute(GuildMessageReceivedEvent e);

        // returns whether the author and the bot has permission to kick and reinvite a user
        public static boolean kickPermissionCheck(GuildMessageReceivedEvent e) {
            // check permissions for us
            MessageChannel channel = e.getChannel();
            Member starbot = e.getGuild().getSelfMember();
            if (!starbot.hasPermission(Permission.KICK_MEMBERS, Permission.CREATE_INSTANT_INVITE)) {
                // complain and reutrn
                channel.sendMessage("I need kick and invite perms").queue();
                return false;
            }

            // check permissions for the author
            Member author = e.getMember();
            if (!author.hasPermission(Permission.KICK_MEMBERS)) {
                // complain and reutrn
                channel.sendMessage("you don't have perms").queue();
                return false;
            }

            // otherwise, return true
            return true;
        }

        // kicks and reinvites a user
        protected static void kickAndReinvite(Guild g, Member m, String reason) {
            // get their ID and their role to store in the map
            long id = m.getIdLong();
            long roleId = m.getRoles().get(0).getIdLong();
            // put it in the map
            Constants.memberToId.put(id, roleId);

            // note: bot should reinvite then kick to make sure we still have a mutual server

            // reinvite them
            String invite = g.getDefaultChannel().createInvite().setMaxAge(1L, TimeUnit.HOURS).complete().getUrl();
            m.getUser().openPrivateChannel().complete().sendMessage(invite).complete();

            // kick them
            g.kick(m).reason(reason).queue();
        }

    }

    // Listener
    public static class DingusCrewListener extends ListenerAdapter {

        // public message listening method
        @Override
        public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
            // if the guild wasn't Dingus Crew, return
            if (e.getGuild().getIdLong() != Constants.DINGUS_CREW_GUILD_ID_LONG) {
                return;
            }

            // if it was a bot, ignore it
            if (e.getAuthor().isBot()) {
                return;
            }

            // if it was a command, run it
            if (CommandEngine.isCommand(e.getMessage())) {
                // find out which command and run it
                String command = e.getMessage().getContentRaw().split("\\s+")[0].substring(1);

                // DingusCrewCommand
                for (DingusCrewCommand c : DingusCrewCommand.values()) {
                    if (command.startsWith(c.name())) {
                        c.execute(e);
                        return;
                    }
                }
                // do nothing otherwise
            }
        }

        // guild event
        @Override
        public void onGuildMemberJoin(GuildMemberJoinEvent e) {
            // if the guild wasn't Dingus Crew, return
            if (e.getGuild().getIdLong() != Constants.DINGUS_CREW_GUILD_ID_LONG) {
                return;
            }

            // if the user has an entry in the roles map, give them their role
            long memberId = e.getMember().getIdLong();
            if (Constants.memberToId.containsKey(memberId)) {
                // assign role
                Role r = e.getGuild().getRoleById(Constants.memberToId.get(memberId));
                e.getGuild().addRoleToMember(e.getMember(), r).queue();
            }
        }

    }

}
