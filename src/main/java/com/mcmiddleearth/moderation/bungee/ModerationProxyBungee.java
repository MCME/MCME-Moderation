package com.mcmiddleearth.moderation.bungee;

import com.mcmiddleearth.base.bungee.player.BungeeMcmePlayer;
import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.moderation.core.ModerationPlugin;
import com.mcmiddleearth.moderation.core.ModerationProxy;
import net.md_5.bungee.api.ProxyServer;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * deprecated
 * to be removed
 * features to be removed to Base plugins
 * */
public class ModerationProxyBungee extends ModerationProxy {

    @Override
    public Collection<McmeProxyPlayer> getPlayers() {
        return ProxyServer.getInstance().getPlayers().stream()
                .map(player -> new BungeeMcmePlayer(getPlugin(), player)).collect(Collectors.toList());
    }

    @Override
    public McmeProxyPlayer getPlayer(UUID uuid) {
        return new BungeeMcmePlayer(getPlugin(), ProxyServer.getInstance().getPlayer(uuid));
    }

    @Override
    public McmeProxyPlayer getPlayer(String playerName) {
        return new BungeeMcmePlayer(getPlugin(), ProxyServer.getInstance().getPlayer(playerName));
    }

    @Override
    public void schedule(ModerationPlugin plugin, Runnable task, int delay, TimeUnit timeUnit) {
        ProxyServer.getInstance().getScheduler().schedule((ModerationPluginBungee)plugin, task, delay, timeUnit);
    }
}
