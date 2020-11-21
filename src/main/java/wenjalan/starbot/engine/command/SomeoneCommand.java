package wenjalan.starbot.engine.command;

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
        // get the Guild to find members in
        Guild g = msg.getGuild();

        // get the TextChannel to mention in
        TextChannel channel = msg.getTextChannel();

        // for some reason TextChannel#getMembers() doesn't always return everyone who can read the channel
        // List<Member> members = channel.getMembers();
        List<Member> members = g.getMembers();

        // roll a die to choose a random member
        int roll = ThreadLocalRandom.current().nextInt(members.size());
        Member choice = members.get(roll);

        // reply to the original message with the random member mentioned
        msg.reply(choice.getAsMention()).queue();
    }
}
