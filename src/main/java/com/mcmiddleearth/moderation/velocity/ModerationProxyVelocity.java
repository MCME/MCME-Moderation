package com.mcmiddleearth.moderation.velocity;

import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.base.velocity.player.VelocityMcmePlayer;
import com.mcmiddleearth.moderation.core.ModerationPlugin;
import com.mcmiddleearth.moderation.core.ModerationProxy;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ModerationProxyVelocity extends ModerationProxy {

    ProxyServer proxy;

    public ModerationProxyVelocity(ProxyServer proxy) {
        this.proxy = proxy;
    }

    @Override
    public Collection<McmeProxyPlayer> getPlayers() {
        return proxy.getAllPlayers().stream()
                .map(player -> new VelocityMcmePlayer(ModerationProxy.getPlugin(), player)).collect(Collectors.toList());
    }

    @Override
    public McmeProxyPlayer getPlayer(UUID uuid) {
        return proxy.getPlayer(uuid)
                .map(player -> new VelocityMcmePlayer(ModerationProxy.getPlugin(), player)).orElse(null);
    }

    @Override
    public McmeProxyPlayer getPlayer(String playerName) {
        return proxy.getPlayer(playerName)
                .map(player -> new VelocityMcmePlayer(ModerationProxy.getPlugin(), player)).orElse(null);
    }

    @Override
    public void schedule(ModerationPlugin plugin, Runnable task, int delay, TimeUnit timeUnit) {
        proxy.getScheduler().buildTask(plugin, task)
                .delay(delay, timeUnit)
                .schedule();
    }
}
