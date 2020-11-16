package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import wenjalan.starbot.engine.ChatEngine;
import wenjalan.starbot.engine.language.MarkovLanguageEngine;

public class MarkovCommand implements Command {

    @Override
    public String getName() {
        return "markov";
    }

    @Override
    public String getDescription() {
        return "(Admin Only) Command for all markov-related features";
    }

    @Override
    public String getUsage() {
        return "!markov <on|off|info>";
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
        return true;
    }

    @Override
    public void run(Message msg) {
        // check if this person is an admin
        Member author = msg.getMember();
        if (!author.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        // get the argument
        String rawContent = msg.getContentRaw();
        TextChannel channel = msg.getTextChannel();
        if (rawContent.length() <= "!markov ".length()) {
            channel.sendMessage("Provide an argument (see !help markov)").queue();
            return;
        }
        String arg = msg.getContentRaw().substring("!markov ".length()).split("\\s+")[0];

        // parse commands
        MarkovLanguageEngine markov = MarkovLanguageEngine.get();
        long guildId = msg.getGuild().getIdLong();
        // info
        if (arg.equalsIgnoreCase("info")) {
            // send some information about the guild's markov model
            if (markov.hasModel(guildId)) {
                String info = markov.getInfo(guildId);
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("Markov Language Model");
                embed.addField("Guild ID " + guildId, info, false);
                channel.sendMessage(embed.build()).queue();
            } else {
                channel.sendMessage("This guild has not been modeled.").queue();
            }
        }

        // on
        else if (arg.equalsIgnoreCase("on")) {
            // enable markov
            ChatEngine.get().setNLIEnabled(guildId, true);
            channel.sendMessage("Enabled Markov").queue();
        }

        // off
        else if (arg.equalsIgnoreCase("off")) {
            // disable markov
            ChatEngine.get().setNLIEnabled(guildId, false);
            channel.sendMessage("Disabled Markov").queue();
        }

        // init
        else if (arg.equalsIgnoreCase("init")) {
            // create a markov model for this guild
            markov.createGuildModel(msg.getGuild(), channel);
            // enable markov
            ChatEngine.get().setNLIEnabled(guildId, true);
        }

        // reload
        else if (arg.equalsIgnoreCase("reload")) {
            // reload the models
            MarkovLanguageEngine.get().loadModels();
            channel.sendMessage("Reloaded Markov Models").queue();
        }

        // invalid
        else {
            channel.sendMessage("Invalid argument: " + arg).queue();
        }
    }
}
