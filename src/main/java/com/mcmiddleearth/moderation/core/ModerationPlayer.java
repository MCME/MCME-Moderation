package com.mcmiddleearth.moderation.core;

import net.kyori.adventure.text.Component;

import java.net.SocketAddress;
import java.util.UUID;

public interface ModerationPlayer extends ModerationCommandSender {

    public void sendDataToBackend(String channel, byte[] data, boolean queue);

    String getName();

    boolean hasPermission(String permissionNode);

    UUID getUniqueId();

    SocketAddress getSocketAddress();

    void sendInfo(Component message);
    void sendError(Component message);


}
