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

import com.mcmiddleearth.base.bungee.AbstractBungeePlugin;
import com.mcmiddleearth.base.core.command.McmeCommandSender;
import com.mcmiddleearth.base.net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import com.mcmiddleearth.base.net.kyori.adventure.text.Component;
import com.mcmiddleearth.moderation.bungee.command.ModerationPluginCommandBungee;
import com.mcmiddleearth.moderation.bungee.listener.WatchlistListener;
import com.mcmiddleearth.moderation.core.McmeModeration;
import com.mcmiddleearth.moderation.core.McmeModerationConfig;
import com.mcmiddleearth.moderation.core.Permission;
import com.mcmiddleearth.moderation.core.command.handler.ReportCommandHandler;
import com.mcmiddleearth.moderation.core.command.handler.WatchlistCommandHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Eriol_Eandur
 */


public class ModerationPluginBungee extends AbstractBungeePlugin implements Listener {
    
    BungeeAudiences adventure = null;

    private final CommandDispatcher<McmeCommandSender> commandDispatcher = new CommandDispatcher<>();
    private final Set<ModerationPluginCommandBungee> commands = new HashSet<>();

    @Override
    public void onEnable() {
        //todo: replace adventure by Base plugin adventure
        adventure = BungeeAudiences.create(this);
        File configFile = new File(getDataFolder(), McmeModerationConfig.FILE_NAME);
        saveResourceToFile(McmeModerationConfig.FILE_NAME, configFile);

        McmeModeration.enable(this);

        commands.add(new ModerationPluginCommandBungee(commandDispatcher,
                new WatchlistCommandHandler("watchlist", Permission.WATCHLIST, commandDispatcher)));
        commands.add(new ModerationPluginCommandBungee(commandDispatcher,
                new ReportCommandHandler("report", Permission.SEND_REPORT, commandDispatcher)));

        commands.forEach(command -> ProxyServer.getInstance().getPluginManager()
                .registerCommand(this, command));

        //Listener for tab complete
        ProxyServer.getInstance().getPluginManager().registerListener(this,this);
        ProxyServer.getInstance().getPluginManager().registerListener(this, new WatchlistListener());
        Logger.getGlobal().info("Enabled Moderation plugin! sent to global logger.");
        adventure.console().sendMessage(Component.text("Enabled Moderation plugin! Sent to audience.console"));
    }

    @Override
    public void onDisable() {
        //maybe TODO: e.g. cancel scheduled tasks.
        McmeModeration.disable();
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

    public boolean isOnWatchlist(ProxiedPlayer player) {
        return McmeModeration.getWatchlistManager().isOnWatchlist(player.getName());
    }

    public String getTablistPrefix() {
        return McmeModeration.getConfig().getWatchlistTablistPrefix();
    }

    @Override
    public Component getMessagePrefix() {
        return McmeModeration.getMessagePrefix();
    }
}