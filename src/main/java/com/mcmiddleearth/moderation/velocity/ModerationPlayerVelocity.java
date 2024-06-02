package com.mcmiddleearth.moderation.velocity;

import com.mcmiddleearth.moderation.core.ModerationPlayer;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.net.SocketAddress;
import java.util.UUID;

public class ModerationPlayerVelocity implements ModerationPlayer {

    private final Player player;

    public ModerationPlayerVelocity(Player player) {
        this.player = player;
    }

    @Override
    public void sendDataToBackend(String channel, byte[] data, boolean queue) {

    }

    @Override
    public String getName() {
        return player.getUsername();
    }

    @Override
    public boolean hasPermission(String permissionNode) {
        return player.hasPermission(permissionNode);
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public SocketAddress getSocketAddress() {
        return player.getRemoteAddress();
    }

    @Override
    public void sendInfo(Component message) {

    }

    @Override
    public void sendError(Component message) {

    }
}
