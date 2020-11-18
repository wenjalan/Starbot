package wenjalan.starbot.engine.command;

import net.dv8tion.jda.api.entities.Message;

// Command interface, the basis for all Commands
public interface Command {

    // returns the name of this command
    String getName();

    // returns the description of this command
    String getDescription();

    // returns the usage template of this command
    String getUsage();

    // returns whether this Command can be run in a guild
    boolean isGuildCommand();

    // returns whether this Command can be run in a dm
    boolean isDmCommand();

    // returns whether this Command can only be used by me
    boolean isAdminCommand();

    // runs this command
    void run(Message msg);

}
