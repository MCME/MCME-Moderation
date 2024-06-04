package com.mcmiddleearth.moderation.core;

import com.mcmiddleearth.moderation.core.configuration.ModerationConfig;
import com.mcmiddleearth.moderation.core.watchlist.WatchlistManager;
import net.kyori.adventure.audience.Audiences;
import net.kyori.adventure.platform.AudienceProvider;

public interface ModerationPlugin {

    WatchlistManager getWatchlistManager();

    ModerationConfig getConfig();

    AudienceProvider getAdventure();
}
