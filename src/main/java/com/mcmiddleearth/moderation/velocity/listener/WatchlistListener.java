package com.mcmiddleearth.moderation.velocity.listener;

import com.mcmiddleearth.base.velocity.player.VelocityMcmePlayer;
import com.mcmiddleearth.moderation.core.ModerationProxy;
import com.mcmiddleearth.moderation.velocity.ModerationPlayerVelocity;
import com.mcmiddleearth.moderation.velocity.ModerationPluginVelocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;

public class WatchlistListener {


    @Subscribe
    public void onPlayerJoin(ServerPreConnectEvent event) {
        if(event.getPreviousServer()==null) {
            ModerationProxy.getPlugin().getWatchlistManager()
                    .processPlayerJoin(new VelocityMcmePlayer(ModerationProxy.getPlugin(), event.getPlayer()));
        }
    }
}
