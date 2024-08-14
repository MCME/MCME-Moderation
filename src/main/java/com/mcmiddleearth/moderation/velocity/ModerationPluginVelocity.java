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
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Plugin(id = "mcme-moderation", name = "MCME-Moderation", version = "1.3.0",
        url = "https://github.com/MCME/MCME-Moderation", description = "Moderation plugin for MCME on Velocity proxy",
        authors = {"Eriol_Eandur"})

public class ModerationPluginVelocity extends AbstractVelocityPlugin {

    private final Set<ModerationPluginCommandVelocity> commands = new HashSet<>();

    @Inject
    public ModerationPluginVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        super(logger, server, dataDirectory);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        File configFile = new File(getDataFolder(), McmeModerationConfig.FILE_NAME);
        saveResourceToFile(McmeModerationConfig.FILE_NAME, configFile);

        McmeModeration.enable(this);

        CommandDispatcher<McmeCommandSender> dispatcher = new CommandDispatcher<>();

        CommandManager commandManager = getProxyServer().getCommandManager();
        CommandMeta watchlistMeta = commandManager.metaBuilder("watchlist")
                .aliases("Watchlist", "wl")
                .plugin(this)
                .build();
        ModerationPluginCommandVelocity watchlistCommand = new ModerationPluginCommandVelocity(dispatcher,
                new ReportCommandHandler("watchlist", Permission.WATCHLIST, dispatcher));
        commands.add(watchlistCommand);
        commandManager.register(watchlistMeta, watchlistCommand);

        CommandMeta reportMeta = commandManager.metaBuilder("report")
                .aliases("Report", "rep")
                .plugin(this)
                .build();
        ModerationPluginCommandVelocity reportCommand = new ModerationPluginCommandVelocity(dispatcher,
                new ReportCommandHandler("report", Permission.SEND_REPORT, dispatcher));
        commands.add(reportCommand);
        commandManager.register(reportMeta, reportCommand);

        getProxyServer().getEventManager().register(this, new WatchlistListener());

        getMcmeProxy().getConsole().sendMessage(createMessage().add("Enabled on Velocity proxy!"));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        McmeModeration.disable();
    }

    @Subscribe
    public void onTabComplete(TabCompleteEvent event) {
        for (ModerationPluginCommandVelocity command : commands) {
            /*if (event.getPartialMessage().startsWith("/"+command.getName())) {
                command.onTabComplete(event);
                return;
            }*/
        }
    }

    @Override
    public Message getMessagePrefix() {
        return createMessage().add("[Mod] ", McmeColors.MOD);
    }



}
