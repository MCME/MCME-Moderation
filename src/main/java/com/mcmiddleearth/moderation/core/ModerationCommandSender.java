package com.mcmiddleearth.moderation.core;

import net.kyori.adventure.text.Component;

public interface ModerationCommandSender {

    boolean hasPermission(String permissionNode);

    String getName();

    void sendInfo(Component message);
    void sendError(Component message);


}
