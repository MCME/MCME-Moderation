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
package com.mcmiddleearth.moderation.listener;

import com.google.common.base.Joiner;
import com.mcmiddleearth.moderation.ModerationPlugin;
import com.mcmiddleearth.moderation.Permission;
import com.mcmiddleearth.moderation.Style;
import com.mcmiddleearth.moderation.util.DiscordUtil;
import com.mcmiddleearth.moderation.watchlist.WatchlistPlayerData;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @author Eriol_Eandur
 */

public class WatchlistListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerJoin(ServerConnectEvent event) {
        if(event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {

            ModerationPlugin.getWatchlistManager().addKnownPlayer(event.getPlayer());

            //handle name changes of players
            ModerationPlugin.getWatchlistManager().updateWatchlist(event.getPlayer());

            if(ModerationPlugin.getWatchlistManager().isOnWatchlist(event.getPlayer().getName())) {
                ProxyServer.getInstance().getScheduler().schedule(ModerationPlugin.getInstance(), () -> {
                    ComponentBuilder message = new ComponentBuilder(Style.INFO + "Watched player " + Style.INFO_STRESSED + event.getPlayer().getName()
                            + Style.INFO + " joined.");

                    if (ModerationPlugin.getConfig().isWatchlistPlayerJoinNotificationIngame()) {
                        ProxyServer.getInstance().getPlayers().stream()
                                .filter(moderator -> moderator.hasPermission(Permission.SEE_WATCHLIST))
                                .forEach(moderator -> ModerationPlugin.sendInfo(moderator, message));
                    }
                    if (ModerationPlugin.getConfig().isWatchlistPlayerJoinNotificationDiscord()) {
                        String discordChannel = ModerationPlugin.getConfig().getWatchlistDiscordChannel();
                        DiscordUtil.sendDiscord(discordChannel, "Watched player **" + event.getPlayer().getName() + "** joined the server.",
                                ModerationPlugin.getConfig().isWatchlistPingModerators());
                    }
                }, 5, TimeUnit.SECONDS);
            }  else if(ModerationPlugin.getWatchlistManager().hasWatchedIp(event.getPlayer())) {
                Collection<WatchlistPlayerData> aliases
                        = ModerationPlugin.getWatchlistManager().getWatchedAliases(event.getPlayer().getName());
                String reason = "Alt of "+ Joiner.on(", ").join(aliases.toArray(aliases.stream().map(alias -> {
                            if(alias.isNameUnknown()) {
                                return alias.getUuid();
                            } else {
                                return ModerationPlugin.getWatchlistManager().getName(alias);
                            }
                        }).toArray()));
                ModerationPlugin.getWatchlistManager().addWatchlist(event.getPlayer().getName(),
                        null,
                        reason);
                ProxyServer.getInstance().getScheduler().schedule(ModerationPlugin.getInstance(), () -> {
                    if (ModerationPlugin.getConfig().isWatchlistPlayerJoinNotificationIngame()) {
                        ComponentBuilder message = new ComponentBuilder(Style.INFO + "Player " + Style.INFO_STRESSED + event.getPlayer().getName()
                                + Style.INFO + " joined and was put on Watchlist because he's an "+reason);
                        ProxyServer.getInstance().getPlayers().stream()
                                .filter(moderator -> moderator.hasPermission(Permission.SEE_WATCHLIST))
                                .forEach(moderator -> ModerationPlugin.sendInfo(moderator, message));
                    }
                    if (ModerationPlugin.getConfig().isWatchlistPlayerJoinNotificationDiscord()) {
                        String discordChannel = ModerationPlugin.getConfig().getWatchlistDiscordChannel();
                        DiscordUtil.sendDiscord(discordChannel, "Player **" + event.getPlayer().getName()
                                                + "** joined the server and was put on Watchlist because he's an "+reason,
                                ModerationPlugin.getConfig().isWatchlistPingModerators());
                    }
                }, 5, TimeUnit.SECONDS);
            }
        }
    }
}
