package com.mcmiddleearth.moderation.core;

import com.mcmiddleearth.base.core.message.McmeColors;
import com.mcmiddleearth.base.core.message.Message;
import com.mcmiddleearth.base.core.plugin.McmeProxyPlugin;
import com.mcmiddleearth.base.core.server.McmeProxy;
import com.mcmiddleearth.moderation.core.watchlist.WatchlistManager;

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

    public static McmeProxy getProxy() {
        return plugin.getMcmeProxy();
    }

    public static WatchlistManager getWatchlistManager() {
        return watchlistManager;
    }

    public static McmeModerationConfig getConfig() {
        return config;
    }

    public static Message getMessagePrefix() {
        return plugin.createMessage().add("[Mod] ", McmeColors.MOD);
    }

    public static Message infoMessage() {
        return getPlugin().createInfoMessage();
    }

    public static  Message errorMessage() {
        return getPlugin().createErrorMessage();
    }
    public static Message infoMessage(String message) {
        return getPlugin().createInfoMessage().add(message);
    }

    public static  Message errorMessage(String message) {
        return getPlugin().createErrorMessage().add(message);
    }
}
