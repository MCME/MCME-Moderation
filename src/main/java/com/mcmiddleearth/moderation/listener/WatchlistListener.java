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

import com.mcmiddleearth.moderation.ModerationPlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * @author Eriol_Eandur
 */

public class WatchlistListener implements Listener {

    @EventHandler
    public void playerJoin(ServerConnectEvent event) {
        if(event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {

            ModerationPlugin.getWatchlistManager().addKnownPlayer(event.getPlayer());

            //handle name changes of players
            ModerationPlugin.getWatchlistManager().updateWatchlist(event.getPlayer());



            //TODO: Send notification to moderators
        }
    }
}
