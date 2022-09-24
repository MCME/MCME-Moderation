/*
 * Copyright (C) 2020 MCME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.moderation.configuration;

import java.io.File;

/**
 * @author Eriol_Eandur
 */

public class ModerationConfig extends YamlBridge {

    public ModerationConfig(File configFile) {
        load(configFile);
    }

    public boolean isReportSendIngame() {
        return getBoolean("report.sendIngame", true);
    }
    public boolean isReportSendDiscord() {
        return getBoolean("report.sendDiscord", true);
    }
    public String getReportDiscordChannel() { return getString("report.discordChannel", "reports"); }
    public String getReportDiscordRole() { return getString("report.discordRole", "Moderator"); }
    public boolean isReportPingModerators() { return getBoolean("report.pingModerators", false); }
    public boolean isReportAddToWatchlist() { return getBoolean("report.addToWatchlist", true); }

    public boolean isWatchlistPlayerJoinNotificationIngame() { return getBoolean("watchlist.playerJoinNotification.sendIngame", true); }
    public boolean isWatchlistPlayerJoinNotificationDiscord() { return getBoolean("watchlist.playerJoinNotification.sendDiscord", true); }
    public boolean isWatchlistSendIngame() { return getBoolean("watchlist.sendIngame", true); }
    public boolean isWatchlistSendDiscord() { return getBoolean("watchlist.sendDiscord", true); }
    public String getWatchlistDiscordChannel() { return getString("watchlist.discordChannel", "reports"); }
    public boolean isWatchlistPingModerators() { return getBoolean("watchlist.pingModerators", false); }
    public String getWatchlistTablistPrefix() { return getString("watchlist.tabListPrefix", "#ff8866W"); }

}
