package wenjalan.starbot.guild;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wenjalan.starbot.engine.command.Command;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// all server-specific functionality related to The Dingus Crew
public class DingusCrew {

    // Logger
    private static Logger logger = LogManager.getLogger();

    // Guild ID
    public static final long GUILD_ID = 175372417194000384L;

    // the Dingus Crew command prefix
    public static final String COMMAND_PREFIX = "!";

    // Map of User IDs to their associated Role IDs
    private static final Map<Long, List<Long>> roles = new HashMap<>();

    // Map of Server-Specific command names to their Commands
    private static final Map<String, Command> commands = new HashMap<>();

    // ids
    public static class MEMBERS {
        public static final long WENTON_ID_LONG = 478706068223164416L;
        public static final long JAY_ID_LONG = 198889137988698113L;
        public static final long EMUEX_ID_LONG = 168485668694130688L;
        public static final long JTANGO_ID_LONG = 175104183119249408L;
    }

    // event listener
    public static class DingusCrewEventListener extends ListenerAdapter {
        @Override
        public void onGuildReady(@Nonnull GuildReadyEvent event) {
            if (event.getGuild().getIdLong() != GUILD_ID) return;
            saveRoles(event.getGuild());

            // register commands
            Guild g = event.getGuild();
            JDA jda = event.getJDA();
            commands.put("loli", new DingusCommand.KickCommand("loli", MEMBERS.WENTON_ID_LONG));
            commands.put("wife", new DingusCommand.KickCommand("wife", MEMBERS.JAY_ID_LONG));
            commands.put("nword", new DingusCommand.KickCommand("nword", MEMBERS.JTANGO_ID_LONG));
            commands.put("fart", new DingusCommand.KickCommand("fart", MEMBERS.EMUEX_ID_LONG));
            commands.put("help", new Command() {
                @Override
                public String getName() {
                    return "help";
                }

                @Override
                public String getDescription() {
                    return "Provides help for Dingus Crew specific commands";
                }

                @Override
                public String getUsage() {
                    return "!help <command>";
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
                    String commandInfo = commands.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue().getDescription()).collect(Collectors.joining("\n"));
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("Dingus Crew Commands");
                    embed.setColor(Color.ORANGE);
                    embed.setDescription("Commands available specifically in The Dingus Crew");
                    embed.addField("Page (1/1)", commandInfo, false);
                    msg.getChannel().sendMessage(embed.build()).queue();
                }
            });

            // announce
            logger.info("Loaded DingusCrew module");
        }

        @Override
        public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
            if (event.getGuild().getIdLong() != GUILD_ID) return;

            // parse guild-specific commands
            Message msg = event.getMessage();
            String rawContent = msg.getContentRaw();
            if (rawContent.startsWith(COMMAND_PREFIX)) {
                String query = rawContent.substring(1);
                for (String commandName : commands.keySet()) {
                    if (query.startsWith(commandName)) {
                        commands.get(commandName).run(msg);
                    }
                }
            }
        }

        @Override
        public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
            if (event.getGuild().getIdLong() != GUILD_ID) return;
            saveRoles(event.getGuild());
        }

        @Override
        public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
            if (event.getGuild().getIdLong() != GUILD_ID) return;
            saveRoles(event.getGuild());
        }

        @Override
        public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
            if (event.getGuild().getIdLong() != GUILD_ID) return;

            // if this user has roles saved
            Member member = event.getMember();
            Guild g = event.getGuild();
            if (roles.containsKey(member.getIdLong())) {
                // assign them their roles
                roles.get(member.getIdLong()).forEach(roleId -> {
                    // get the role
                    Role r = g.getRoleById(roleId);
                    // move it to the third-highest role in the server
                    if (r != null) {
                        g.modifyRolePositions(false).selectPosition(r).moveTo(2).queue();
                        g.addRoleToMember(member, r).queue();
                    }
                });
            }
        }

        // saves the roles to the role map
        private void saveRoles(Guild dingusCrew) {
            // save the user roles in the map
            for (Member m : dingusCrew.getMembers()) {
                List<Role> memberRoles = m.getRoles();
                roles.put(m.getIdLong(), memberRoles.stream().map(ISnowflake::getIdLong).collect(Collectors.toList()));
            }
        }
    }

    // commands
    public static class DingusCommand {

        // kick command base
        public static class KickCommand implements Command {

            private final String commandName;
            private final long userId;

            public KickCommand(String commandName, long userId) {
                this.commandName = commandName;
                this.userId = userId;
            }

            @Override
            public String getName() {
                return commandName;
            }

            @Override
            public String getDescription() {
                return "Kicks the associated member";
            }

            @Override
            public String getUsage() {
                return "!" + commandName;
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
                // check the author has kick permissions
                Guild g = msg.getGuild();
                Member author = msg.getMember();
                TextChannel channel = msg.getTextChannel();
                if (!author.hasPermission(Permission.KICK_MEMBERS)) {
                    channel.sendMessage("You don't have kick permissions").queue();
                    return;
                }

                // send the victim an invite to the server
                Member victim = g.getMemberById(userId);
                if (victim == null) {
                    channel.sendMessage("Can't find that member").queue();
                    return;
                }
                PrivateChannel privateChannel = victim.getUser().openPrivateChannel().complete();
                String inviteUrl = g.getDefaultChannel().createInvite().setMaxUses(1).complete().getUrl();
                privateChannel.sendMessage(inviteUrl).complete();

                // kick the victim
                victim.kick(author.getUser().getName() + " used !" + commandName).queue();
            }
        }

    }

}
