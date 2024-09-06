package com.mcmiddleearth.moderation.velocity.listener;

import com.mcmiddleearth.base.velocity.player.VelocityMcmePlayer;
import com.mcmiddleearth.moderation.core.McmeModeration;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;

public class WatchlistListener {


    @Subscribe
    public void onPlayerJoin(ServerPreConnectEvent event) {
McmeModeration.getPlugin().getMcmeLogger().warn("PlayerJoinEvent (MCME-Logger): "+event.getPlayer().getUsername());
//ModerationPluginVelocity.LOGGER.warn("PlayerJoinEvent (VelocityLogger): "+event.getPlayer().getUsername());
        if(event.getPreviousServer()==null) {
            McmeModeration.getWatchlistManager()
                    .processPlayerJoin(new VelocityMcmePlayer(event.getPlayer()));
        }
    }

    @Subscribe
    public void onCommand(CommandExecuteEvent event) {
        McmeModeration.getPlugin().getMcmeLogger().info(event.getCommand()+" "+event.getResult().isAllowed()+" "+event.getResult().isForwardToServer());
    }
}
