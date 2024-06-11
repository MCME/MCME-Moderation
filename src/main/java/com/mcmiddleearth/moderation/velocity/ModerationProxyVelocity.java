package com.mcmiddleearth.moderation.velocity;

import com.mcmiddleearth.moderation.core.ModerationPlayer;
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
    public Collection<ModerationPlayer> getPlayers() {
        return proxy.getAllPlayers().stream().map(ModerationPlayerVelocity::new).collect(Collectors.toList());
    }

    @Override
    public ModerationPlayer getPlayer(UUID uuid) {
        return proxy.getPlayer(uuid).map(ModerationPlayerVelocity::new).orElse(null);
    }

    @Override
    public ModerationPlayer getPlayer(String playerName) {
        return proxy.getPlayer(playerName).map(ModerationPlayerVelocity::new).orElse(null);
    }

    @Override
    public void schedule(ModerationPlugin plugin, Runnable task, int delay, TimeUnit timeUnit) {
        proxy.getScheduler().buildTask(plugin, task)
                .delay(delay, timeUnit)
                .schedule();
    }
}
