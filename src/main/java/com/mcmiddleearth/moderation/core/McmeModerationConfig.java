package com.mcmiddleearth.moderation.core;

import com.mcmiddleearth.base.core.configuration.YamlConfiguration;

import java.io.File;

public class McmeModerationConfig {

    public static final String FILE_NAME = "config.yml";

    private final YamlConfiguration config;

    public McmeModerationConfig(File dataFolder) {
        this.config = new YamlConfiguration(new File(dataFolder, FILE_NAME));

    }

    public YamlConfiguration getRawConfig() {
        return config;
    }

    public boolean isReportSendIngame() {
        return config.getBoolean("report.sendIngame", true);
    }
    public boolean isReportSendDiscord() {
        return config.getBoolean("report.sendDiscord", true);
    }
    public String getReportDiscordChannel() { return config.getString("report.discordChannel", "reports"); }
    public String getReportDiscordRole() { return config.getString("report.discordRole", "Moderator"); }
    public boolean isReportPingModerators() { return config.getBoolean("report.pingModerators", false); }
    public boolean isReportAddToWatchlist() { return config.getBoolean("report.addToWatchlist", true); }

    public boolean isWatchlistPlayerJoinNotificationIngame() { return config.getBoolean("watchlist.playerJoinNotification.sendIngame", true); }
    public boolean isWatchlistPlayerJoinNotificationDiscord() { return config.getBoolean("watchlist.playerJoinNotification.sendDiscord", true); }
    public boolean isWatchlistSendIngame() { return config.getBoolean("watchlist.sendIngame", true); }
    public boolean isWatchlistSendDiscord() { return config.getBoolean("watchlist.sendDiscord", true); }
    public String getWatchlistDiscordChannel() { return config.getString("watchlist.discordChannel", "reports"); }
    public boolean isWatchlistPingModerators() { return config.getBoolean("watchlist.pingModerators", false); }
    public String getWatchlistTablistPrefix() { return config.getString("watchlist.tabListPrefix", "#ff8866W"); }
}
