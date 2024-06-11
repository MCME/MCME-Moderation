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
package com.mcmiddleearth.moderation.bungee;

import com.mcmiddleearth.moderation.bungee.command.ModerationPluginCommandBungee;
import com.mcmiddleearth.moderation.bungee.listener.WatchlistListener;
import com.mcmiddleearth.moderation.core.ModerationCommandSender;
import com.mcmiddleearth.moderation.core.ModerationPlugin;
import com.mcmiddleearth.moderation.core.ModerationProxy;
import com.mcmiddleearth.moderation.core.Permission;
import com.mcmiddleearth.moderation.core.command.handler.ReportCommandHandler;
import com.mcmiddleearth.moderation.core.command.handler.WatchlistCommandHandler;
import com.mcmiddleearth.moderation.core.configuration.ModerationConfig;
import com.mcmiddleearth.moderation.core.watchlist.WatchlistManager;
import com.mojang.brigadier.CommandDispatcher;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Eriol_Eandur
 */


public class ModerationPluginBungee extends Plugin implements ModerationPlugin, Listener {
    
    private static ModerationConfig config;

    BungeeAudiences adventure = null;

    private final CommandDispatcher<ModerationCommandSender> commandDispatcher = new CommandDispatcher<>();
    private final Set<ModerationPluginCommandBungee> commands = new HashSet<>();

    private static WatchlistManager watchlistManager;

    @Override
    public void onEnable() {
        adventure = BungeeAudiences.create(this);
        ModerationProxy.setPlugin(this);
        ModerationProxy.setInstance(new ModerationProxyBungee());

        ModerationConfig.saveDefaultConfig(getDataFolder());
        config = new ModerationConfig(ModerationConfig.getConfigFile(getDataFolder()));

        commands.add(new ModerationPluginCommandBungee(commandDispatcher,
                new WatchlistCommandHandler("watchlist", Permission.WATCHLIST, commandDispatcher)));
        commands.add(new ModerationPluginCommandBungee(commandDispatcher,
                new ReportCommandHandler("report", Permission.SEND_REPORT, commandDispatcher)));

        commands.forEach(command -> ProxyServer.getInstance().getPluginManager()
                .registerCommand(this, command));

        //Listener for tab complete
        ProxyServer.getInstance().getPluginManager().registerListener(this,this);
        ProxyServer.getInstance().getPluginManager().registerListener(this, new WatchlistListener());
        watchlistManager = new WatchlistManager(getDataFolder());
    }

    @Override
    public void onDisable() {
        //maybe TODO: e.g. cancel scheduled tasks.
        adventure.close();
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        for (ModerationPluginCommandBungee command : commands) {
            if (event.getCursor().startsWith("/"+command.getName())) {
                command.onTabComplete(event);
                return;
            }
        }
    }

    /*public static void sendInfo(CommandSender recipient, ComponentBuilder message) {
        ComponentBuilder result = new ComponentBuilder("[Mod]").color(Style.MOD).append(" ").color(Style.INFO);
        result.append(message.create());
        recipient.sendMessage(result.create());
    }
    public static void sendError(CommandSender recipient, ComponentBuilder message) {
        ComponentBuilder result = new ComponentBuilder("[Mod]").color(Style.MOD).append(" ").color(Style.ERROR);
        result.append(message.create());
        recipient.sendMessage(result.create());
    }*/

    @Override
    public ModerationConfig getConfig() {
        return config;
    }

    @Override
    public WatchlistManager getWatchlistManager() {
        return watchlistManager;
    }

    public boolean isOnWatchlist(ProxiedPlayer player) {
        return getWatchlistManager().isOnWatchlist(player.getName());
    }

    public String getTablistPrefix() {
        return getConfig().getWatchlistTablistPrefix();
    }

    public BungeeAudiences getAdventure() {
        return adventure;
    }
}