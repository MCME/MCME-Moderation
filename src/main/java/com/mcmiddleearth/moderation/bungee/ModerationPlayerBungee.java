package com.mcmiddleearth.moderation.bungee;

import com.mcmiddleearth.moderation.core.ModerationPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.SocketAddress;
import java.util.UUID;

public class ModerationPlayerBungee extends ModerationCommandSenderBungee implements ModerationPlayer {

    public ModerationPlayerBungee(ProxiedPlayer player) {
        super(player);
    }

    @Override
    public void sendDataToBackend(String channel, byte[] data, boolean queue) {
        getProxiedPlayer().getServer().getInfo().sendData(channel, data,queue);
    }

    @Override
    public UUID getUniqueId() {
        return getProxiedPlayer().getUniqueId();
    }

    @Override
    public SocketAddress getSocketAddress() {
        return getProxiedPlayer().getSocketAddress();
    }

    public ProxiedPlayer getProxiedPlayer() {
        return (ProxiedPlayer) getBungeeCommandSender();
    }
}
