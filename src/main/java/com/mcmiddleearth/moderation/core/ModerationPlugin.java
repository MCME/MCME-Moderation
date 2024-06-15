package com.mcmiddleearth.moderation.core;

import com.mcmiddleearth.base.core.command.McmePlugin;
import com.mcmiddleearth.moderation.core.configuration.ModerationConfig;
import com.mcmiddleearth.moderation.core.watchlist.WatchlistManager;
import net.kyori.adventure.text.Component;

public interface ModerationPlugin extends McmePlugin {

    WatchlistManager getWatchlistManager();

    ModerationConfig getConfig();

    @Override
    default public Component getMessagePrefix() {
        return Component.text("[Mod]").color(Style.MOD);
    }

}
