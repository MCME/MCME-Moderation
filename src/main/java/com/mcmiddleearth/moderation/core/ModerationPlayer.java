package com.mcmiddleearth.moderation.core;

import java.net.SocketAddress;
import java.util.UUID;

public interface ModerationPlayer extends ModerationCommandSender {

    public void sendDataToBackend(String channel, byte[] data, boolean queue);

    UUID getUniqueId();

    SocketAddress getSocketAddress();

}
