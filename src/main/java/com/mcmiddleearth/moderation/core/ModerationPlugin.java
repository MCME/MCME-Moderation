package com.mcmiddleearth.moderation.core;

import com.mcmiddleearth.moderation.core.configuration.ModerationConfig;
import com.mcmiddleearth.moderation.core.watchlist.WatchlistManager;

public interface ModerationPlugin {

    WatchlistManager getWatchlistManager();

    ModerationConfig getConfig();

}
