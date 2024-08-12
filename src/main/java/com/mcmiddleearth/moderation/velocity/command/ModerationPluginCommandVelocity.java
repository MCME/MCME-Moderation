package com.mcmiddleearth.moderation.velocity.command;

import com.google.common.base.Joiner;
import com.mcmiddleearth.base.core.command.McmeCommandSender;
import com.mcmiddleearth.base.velocity.command.VelocityMcmeCommandSender;
import com.mcmiddleearth.moderation.core.command.ModerationPluginCommand;
import com.mcmiddleearth.moderation.core.command.handler.AbstractCommandHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModerationPluginCommandVelocity implements SimpleCommand {

    private final ModerationPluginCommand moderationCommand;

    private final String name, permission;

    public ModerationPluginCommandVelocity(CommandDispatcher<McmeCommandSender> commandDispatcher, AbstractCommandHandler handler) {
        moderationCommand = new ModerationPluginCommand(commandDispatcher);
        this.permission = handler.getPermission();
        this.name = handler.getCommand();
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        moderationCommand.execute(new VelocityMcmeCommandSender(source),name, args);
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission(permission);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
        String cursor = Joiner.on(" ").join(name, invocation.arguments());
        return CompletableFuture.completedFuture(moderationCommand
                .getSuggestions(new VelocityMcmeCommandSender(invocation.source()),cursor));
    }

}
