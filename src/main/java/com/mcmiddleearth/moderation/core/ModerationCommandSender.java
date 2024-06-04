package com.mcmiddleearth.moderation.core;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

public interface ModerationCommandSender {

    boolean hasPermission(String permissionNode);

    String getName();

    Audience getAudience();

    default void sendInfo(Component message) {
        Component result = Component.text("[Mod]").color(Style.MOD).append(Component.text(" ").color(Style.INFO));
        result = result.append(message);
        getAudience().sendMessage(result);
    }

    default void sendError(Component message) {
        Component result = Component.text("[Mod]").color(Style.MOD).append(Component.text(" ").color(Style.ERROR));
        result = result.append(message);
        getAudience().sendMessage(result);
    }


}
