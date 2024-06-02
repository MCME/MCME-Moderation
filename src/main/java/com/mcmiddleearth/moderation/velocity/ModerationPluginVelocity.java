package com.mcmiddleearth.moderation.velocity;

import com.google.inject.Inject;
import com.mcmiddleearth.moderation.velocity.listener.WatchlistListener;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(id = "mcmemoderation", name = "MCME-Moderation", version = "1.3.0",
        url = "https://github.com/MCME/MCME-Moderation", description = "Moderation plugin for MCME on Velocity proxy",
        authors = {"Eriol_Eandur"})
public class ModerationPluginVelocity {

    ProxyServer proxyServer;
    Logger logger;
    Path dataFolder;

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
        proxyServer.getEventManager().register(this, new WatchlistListener());
    }


}
