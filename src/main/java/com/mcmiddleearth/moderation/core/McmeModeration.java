package com.mcmiddleearth.moderation.core;

import com.mcmiddleearth.base.core.plugin.McmeProxyPlugin;
import com.mcmiddleearth.moderation.core.watchlist.WatchlistManager;
import com.mcmiddleearth.base.net.kyori.adventure.text.Component;

public class McmeModeration {

    private static McmeProxyPlugin plugin;
    private static McmeModerationConfig config;

    private static WatchlistManager watchlistManager;

    public static void enable(McmeProxyPlugin plugin) {
        McmeModeration.plugin = plugin;
        config = new McmeModerationConfig(plugin.getDataFolder());
        watchlistManager = new WatchlistManager(plugin.getDataFolder());
    }

    public static void disable() {
        //nothing do to yet
    }

    public static McmeProxyPlugin getPlugin() {
        return plugin;
    }

    public static WatchlistManager getWatchlistManager() {
        return watchlistManager;
    }

    public static McmeModerationConfig getConfig() {
        return config;
    }

    public static Component getMessagePrefix() {
        return Component.text("[Mod]").color(Style.MOD);
    }

}
