package com.mcmiddleearth.moderation.bungee.command;

import com.mcmiddleearth.moderation.bungee.ModerationCommandSenderBungee;
import com.mcmiddleearth.moderation.core.ModerationCommandSender;
import com.mcmiddleearth.moderation.core.command.ModerationPluginCommand;
import com.mcmiddleearth.moderation.core.command.handler.AbstractCommandHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;

public class ModerationPluginCommandBungee extends Command {

    private final ModerationPluginCommand moderationCommand;

    private final String permission;

    public ModerationPluginCommandBungee(CommandDispatcher<ModerationCommandSender> commandDispatcher, AbstractCommandHandler handler) {
        super(handler.getCommand());
        moderationCommand = new ModerationPluginCommand(commandDispatcher);
        this.permission = handler.getPermission();
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        moderationCommand.execute(new ModerationCommandSenderBungee(commandSender),getName(), args);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(permission);
    }

    public void onTabComplete(TabCompleteEvent event) {
        if (event.getSender() instanceof CommandSender commandSender) {
            ModerationCommandSender sender = new ModerationCommandSenderBungee(commandSender);
            List<String> suggestions = moderationCommand.getSuggestions(sender, event.getCursor().substring(1));
            if (suggestions.isEmpty()) {
                event.setCancelled(true);
            } else {
                event.getSuggestions().addAll(suggestions);
            }
        }
    }

}
