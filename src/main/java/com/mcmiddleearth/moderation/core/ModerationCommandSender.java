package com.mcmiddleearth.moderation.core;

import net.kyori.adventure.text.Component;

public interface ModerationCommandSender {

    boolean hasPermission(String permissionNode);

    String getName();

    default void sendInfo(Component message) {
        Component result = Component.text("[Mod]").color(Style.MOD).append(Component.text(" ").color(Style.INFO));
        result = result.append(message);
        sendMessage(result);
    }

    default void sendError(Component message) {
        Component result = Component.text("[Mod]").color(Style.MOD).append(Component.text(" ").color(Style.ERROR));
        result = result.append(message);
        sendMessage(result);
    }

    void sendMessage(Component message);


}
