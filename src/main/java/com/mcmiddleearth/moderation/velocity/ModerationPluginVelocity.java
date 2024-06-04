package com.mcmiddleearth.moderation.velocity;

import com.google.inject.Inject;
import com.mcmiddleearth.moderation.bungee.ModerationProxyBungee;
import com.mcmiddleearth.moderation.core.ModerationCommandSender;
import com.mcmiddleearth.moderation.core.ModerationPlugin;
import com.mcmiddleearth.moderation.core.ModerationProxy;
import com.mcmiddleearth.moderation.core.Permission;
import com.mcmiddleearth.moderation.core.command.handler.ReportCommandHandler;
import com.mcmiddleearth.moderation.core.configuration.ModerationConfig;
import com.mcmiddleearth.moderation.core.watchlist.WatchlistManager;
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
import net.kyori.adventure.platform.AudienceProvider;

import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(id = "mcmemoderation", name = "MCME-Moderation", version = "1.3.0",
        url = "https://github.com/MCME/MCME-Moderation", description = "Moderation plugin for MCME on Velocity proxy",
        authors = {"Eriol_Eandur"})
public class ModerationPluginVelocity implements ModerationPlugin {

    ProxyServer proxyServer;
    Logger logger;
    Path dataFolder;
    CommandDispatcher<ModerationCommandSender> dispatcher;

    @Inject
    public ModerationPluginVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = server;
        this.logger = logger;
        this.dataFolder = dataDirectory;

    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Do some operation demanding access to the Velocity API here.
        // For instance, we could register an event:
        ModerationProxy.setPlugin(this);
        ModerationProxy.setInstance(new ModerationProxyVelocity(proxyServer));
        proxyServer.getEventManager().register(this, new WatchlistListener());
        dispatcher = new CommandDispatcher<>();
        CommandManager commandManager = proxyServer.getCommandManager();
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
    }


    @Override
    public WatchlistManager getWatchlistManager() {
        return null;
    }

    @Override
    public ModerationConfig getConfig() {
        return null;
    }

    @Override
    public AudienceProvider getAdventure() {
        return null;
    }
}
