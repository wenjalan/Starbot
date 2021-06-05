package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SomeoneCommand implements Command {
    @Override
    public String getName() {
        return "someone";
    }

    @Override
    public String getDescription() {
        return "Mentions a random member";
    }

    @Override
    public String getUsage() {
        return "!someone";
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
        // get the Guild and TextChannel to send to
        Guild g = msg.getGuild();
        TextChannel channel = msg.getTextChannel();

        // TextChannel#getMembers() only returns cached members
        // List<Member> members = channel.getMembers();

        // use Guild#findMembers() to filter to people in this TextChannel
        g.findMembers(member -> member.hasPermission(channel, Permission.MESSAGE_READ))
        .onSuccess(members -> {
            // roll a die to choose a random member
            int roll = ThreadLocalRandom.current().nextInt(members.size());
            Member choice = members.get(roll);

            // debug: reply with all the members we found
            // String memberNames = members.stream().map(Member::getEffectiveName).collect(Collectors.joining("\n"));
            // msg.reply("Found " + members.size() + " members in channel " + channel.getName() + ":\n" + memberNames).queue();

            // reply to the original message with the random member mentioned
            msg.reply(choice.getAsMention()).queue();
        });
    }
}
