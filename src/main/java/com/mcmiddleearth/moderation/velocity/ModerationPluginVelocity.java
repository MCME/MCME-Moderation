package com.mcmiddleearth.moderation.velocity;

import com.google.inject.Inject;
import com.mcmiddleearth.base.core.command.McmeCommandSender;
import com.mcmiddleearth.base.core.message.McmeColors;
import com.mcmiddleearth.base.core.message.Message;
import com.mcmiddleearth.base.velocity.AbstractVelocityPlugin;
import com.mcmiddleearth.moderation.core.McmeModeration;
import com.mcmiddleearth.moderation.core.McmeModerationConfig;
import com.mcmiddleearth.moderation.core.Permission;
import com.mcmiddleearth.moderation.core.command.handler.ReportCommandHandler;
import com.mcmiddleearth.moderation.velocity.command.ModerationPluginCommandVelocity;
import com.mcmiddleearth.moderation.velocity.listener.WatchlistListener;
import com.mojang.brigadier.CommandDispatcher;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

@Plugin(id = "mcmemoderation", name = "MCME-Moderation", version = "1.3.0",
        url = "https://github.com/MCME/MCME-Moderation", description = "Moderation plugin for MCME on Velocity proxy",
        authors = {"Eriol_Eandur"})

public class ModerationPluginVelocity extends AbstractVelocityPlugin {

    private CommandDispatcher<McmeCommandSender> dispatcher;

    @Inject
    public ModerationPluginVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        super(logger, server, dataDirectory);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        saveResourceToFile(McmeModerationConfig.FILE_NAME, (new File(getDataFolder(), McmeModerationConfig.FILE_NAME)));

        McmeModeration.enable(this);

        getProxyServer().getEventManager().register(this, new WatchlistListener());
        dispatcher = new CommandDispatcher<>();

        CommandManager commandManager = getProxyServer().getCommandManager();
        CommandMeta watchlistMeta = commandManager.metaBuilder("watchlist")
                .aliases("Watchlist", "wl")
                .plugin(this)
                .build();
        SimpleCommand watchlistCommand = new ModerationPluginCommandVelocity(dispatcher,
                new ReportCommandHandler("watchlist", Permission.WATCHLIST, dispatcher));
        commandManager.register(watchlistMeta, watchlistCommand);
        CommandMeta reportMeta = commandManager.metaBuilder("report")
                .aliases("Report", "rep")
                .plugin(this)
                .build();
        SimpleCommand reportCommand = new ModerationPluginCommandVelocity(dispatcher,
                new ReportCommandHandler("report", Permission.SEND_REPORT, dispatcher));
        commandManager.register(reportMeta, reportCommand);
        getMcmeProxy().getConsole().sendMessage(createMessage().add("Enabled on Velocity proxy!"));
    }

    @Override
    public Message getMessagePrefix() {
        return createMessage().add("[Mod] ", McmeColors.MOD);
    }



}
