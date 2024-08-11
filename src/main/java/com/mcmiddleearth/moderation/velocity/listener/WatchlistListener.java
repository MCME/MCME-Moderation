package com.mcmiddleearth.moderation.velocity.listener;

import com.mcmiddleearth.base.velocity.player.VelocityMcmePlayer;
import com.mcmiddleearth.moderation.core.McmeModeration;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;

public class WatchlistListener {


    @Subscribe
    public void onPlayerJoin(ServerPreConnectEvent event) {
        if(event.getPreviousServer()==null) {
            McmeModeration.getWatchlistManager()
                    .processPlayerJoin(new VelocityMcmePlayer(McmeModeration.getPlugin(), event.getPlayer()));
        }
    }
}
