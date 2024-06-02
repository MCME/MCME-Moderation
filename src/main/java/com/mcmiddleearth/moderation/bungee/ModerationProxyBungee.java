package com.mcmiddleearth.moderation.bungee;

import com.mcmiddleearth.moderation.core.ModerationPlayer;
import com.mcmiddleearth.moderation.core.ModerationPlugin;
import com.mcmiddleearth.moderation.core.ModerationProxy;
import net.md_5.bungee.api.ProxyServer;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ModerationProxyBungee extends ModerationProxy {

    @Override
    public Collection<ModerationPlayer> getPlayers() {
        return ProxyServer.getInstance().getPlayers().stream().map(ModerationPlayerBungee::new).collect(Collectors.toList());
    }

    @Override
    public ModerationPlayer getPlayer(UUID uuid) {
        return new ModerationPlayerBungee(ProxyServer.getInstance().getPlayer(uuid));
    }

    @Override
    public ModerationPlayer getPlayer(String playerName) {
        return new ModerationPlayerBungee(ProxyServer.getInstance().getPlayer(playerName));
    }

    @Override
    public void schedule(ModerationPlugin plugin, Runnable task, int delay, TimeUnit timeUnit) {
        ProxyServer.getInstance().getScheduler().schedule((ModerationPluginBungee)plugin, task, delay, timeUnit);
    }
}
