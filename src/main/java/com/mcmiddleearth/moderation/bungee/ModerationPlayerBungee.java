package com.mcmiddleearth.moderation.bungee;

import com.mcmiddleearth.moderation.core.ModerationPlayer;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.SocketAddress;
import java.util.UUID;

public class ModerationPlayerBungee implements ModerationPlayer {

    private final ProxiedPlayer player;

    public ModerationPlayerBungee(ProxiedPlayer player) {
        this.player = player;
    }

    @Override
    public void sendDataToBackend(String channel, byte[] data, boolean queue) {
        player.getServer().getInfo().sendData(channel, data,queue);
    }

    @Override
    public String getName() {
        return player.getName();
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
        return player.getSocketAddress();
    }

    @Override
    public void sendInfo(Component message) {
        Component result = Component.text("[Mod]").color(Style.MOD).append(" ").color(Style.INFO);
        result = result.append(message);
        player.sendMessage(result);
    }

    @Override
    public void sendError(Component message) {
        Component result = Component.text("[Mod]").color(Style.MOD).append(" ").color(Style.ERROR);
        result = result.append(message);
        player.sendMessage(result);
    }


}
