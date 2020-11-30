package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import wenjalan.starbot.engine.CommandEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// clears a certain number of messages in chat sent from Starbot or are commands
public class ClearCommand implements Command {

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getDescription() {
        return "Deletes commands and other bot-sent messages in the chat";
    }

    @Override
    public String getUsage() {
        return "!clear <max number of messages to delete (max 100, default 10)>";
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
        // get the channel
        TextChannel channel = msg.getTextChannel();

        // check that this user has perms
        if (!msg.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("You don't have manage message permissions").queue();
            return;
        }

        // see how many they wanted to look through (max 100)
        int victimCount = 10;
        String rawContent = msg.getContentRaw();
        String[] tokens = rawContent.split("\\s+");
        if (tokens.length > 1) {
            victimCount = Integer.parseInt(tokens[1]);
            victimCount = Math.max(1, victimCount);
            victimCount = Math.min(100, victimCount);
        }

        // look through that many messages and delete ones
        // - mention Starbot
        // - start with Command Prefix
        // - are sent by Starbot
        List<Message> victims = new ArrayList<>();
        int finalVictimCount = victimCount;
        channel.getIterableHistory().forEachAsync((message) -> {
            // sent by Starbot
            if (message.getAuthor().getIdLong() == msg.getJDA().getSelfUser().getIdLong()) {
                victims.add(message);
            }
            // starts with command prefix
            else if (message.getContentRaw().startsWith(CommandEngine.COMMAND_PREFIX)) {
                victims.add(message);
            }
            // mentions Starbot
            else if (message.isMentioned(msg.getJDA().getSelfUser())) {
                victims.add(message);
            }
            return victims.size() < finalVictimCount;
        }).thenRun(() -> {
            // delete the messages
            channel.deleteMessages(victims).queue();
        });
    }
}
