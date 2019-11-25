package wenjalan.starbot.guilds;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import wenjalan.starbot.engine.CommandEngine;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;

// actions specific to The Dingus Crew
public class DingusCrew {

    // Constants
    public static class Constants {

        public static final long JUSTIN_ID_LONG = 175104183119249408L; // Jtango#9058

        public static final long JOSE_ID_LONG = 198889137988698113L; // LORD SHINE YOUR LIGHT ON ME#2430

        public static final long DINGUS_CREW_GUILD_ID_LONG = 175372417194000384L; // Dingus Crew Guild ID

        public static final long SERVER_ASSHOLE_ROLE_ID_LONG = 292906691425730560L; // server asshole

        public static final long GULLIBLE_RETARD_ID_LONG = 289563937299628032L; // gullible retard

    }

    // Fields
    // the number of times the !play command has been used since startup
    protected static int totalPlays = 0;

    // Command Enum
    public enum DingusCrewCommand {

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

        // prints out the number of times !play has been used since startup
        plays {
            @Override
            public void execute(GuildMessageReceivedEvent e) {
                e.getChannel().sendMessage("!play has been used " + totalPlays + " times").queue();
            }
        },

        // play: additional play command kicks people
        // - when its from 8 PM to 4 AM
        // - 1% chance
        play {
            @Override
            public void execute(GuildMessageReceivedEvent e) {
                // sout
                System.out.println("caught !play command in dingus crew");
                // increment totalPlays
                totalPlays++;
                // check the time
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("PST")));

                // if it's 8 to 4
                if (now.getHour() > 17 || now.getHour() < 3) {
                    // roll a die
                    int roll = new Random().nextInt(100);
                    // if the roll was 0, kick the author
                    if (roll == 0) {
                        // announce their kicking
                        e.getChannel().sendMessage(e.getMember().getAsMention() + " won the lottery on !play #" + totalPlays).queue();
                        kickAndReinvite(e.getGuild(), e.getMember(), "won the !play lottery");
                    }
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
            // kick them
            g.kick(m).reason(reason).queue();
            // reinvite them
            String invite = g.getDefaultChannel().createInvite().setMaxUses(1).complete().getUrl();
            m.getUser().openPrivateChannel().complete().sendMessage(invite).queue();
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

            // if the user was Justin, give him his role back
            if (e.getUser().getIdLong() == Constants.JUSTIN_ID_LONG) {
                Role serverAsshole = e.getGuild().getRoleById(Constants.SERVER_ASSHOLE_ROLE_ID_LONG);
                e.getGuild().addRoleToMember(e.getMember(), serverAsshole).queue();
            }
            // if the user was Jose, give him his role back
            else if (e.getUser().getIdLong() == Constants.JOSE_ID_LONG) {
                Role gullibleRetard = e.getGuild().getRoleById(Constants.GULLIBLE_RETARD_ID_LONG);
                e.getGuild().addRoleToMember(e.getMember(), gullibleRetard).queue();
            }
        }

    }

}
