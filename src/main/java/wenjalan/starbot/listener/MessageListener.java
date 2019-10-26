package wenjalan.starbot.listener;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import wenjalan.starbot.engine.ChatEngine;
import wenjalan.starbot.engine.CommandEngine;
import wenjalan.starbot.guilds.DingusCrew;

// listens for all Messages, sends data to corresponding engines
public class MessageListener extends ListenerAdapter {

    // private message listening method
    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        // if the message was sent from us, ignore it
        if (event.getAuthor().isBot()) {
            return;
        }

        // if it's a command, run it
        if (CommandEngine.isCommand(event.getMessage())) {
            CommandEngine.parseDMCommand(event);
        }
        // otherwise, send a random response
        else {
            event.getChannel().sendMessage(ChatEngine.getRandomResponse()).queue();
        }
    }

    // guild message received
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        // if the message was sent from us, ignore it
        if (event.getAuthor().isBot()) {
            return;
        }

        // if it's a command, run it
        if (CommandEngine.isCommand(event.getMessage())) {
            CommandEngine.parseGuildCommand(event);
        }
        // if the message mentions Starbot, send a random response
        else if (event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser())) {
            // send a random response
            event.getChannel().sendMessage(ChatEngine.getRandomResponse()).queue();
        }
        // if the message has a trigger phrase in it, send the corresponding response
        else if (ChatEngine.containsTriggerPhrase(event.getMessage().getContentRaw())) {
            // send the proper response
            event.getChannel().sendMessage(ChatEngine.getResponseForPhrase(event.getMessage().getContentRaw())).queue();
        }
    }

}
