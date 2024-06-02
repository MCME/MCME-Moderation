package com.mcmiddleearth.moderation.core;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class ModerationProxy {

    private static ModerationPlugin moderationPlugin;
    private static ModerationProxy instance;

    public static ModerationPlugin getPlugin() {
        return moderationPlugin;
    }

    public static void setPlugin(ModerationPlugin plugin) {
        moderationPlugin = plugin;
    }

    public static ModerationProxy getInstance() {
        return instance;
    }

    public static void setInstance(ModerationProxy proxy)  {
        instance = proxy;
    }

    public abstract Collection<ModerationPlayer> getPlayers();

    public abstract ModerationPlayer getPlayer(UUID uuid);

    public abstract ModerationPlayer getPlayer(String playerName);

    public abstract void schedule(ModerationPlugin plugin, Runnable task, int delay, TimeUnit seconds);
}
