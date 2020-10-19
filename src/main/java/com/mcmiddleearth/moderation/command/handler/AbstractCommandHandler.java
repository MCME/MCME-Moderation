package com.mcmiddleearth.moderation.command.handler;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class AbstractCommandHandler {

    private final String command;

    public AbstractCommandHandler(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

}
