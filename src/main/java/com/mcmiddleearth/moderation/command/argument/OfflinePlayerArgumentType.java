/*
 * Copyright (C) 2020 MCME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.moderation.command.argument;

import com.mcmiddleearth.moderation.ModerationPlugin;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Eriol_Eandur
 */

public class OfflinePlayerArgumentType extends AbstractPlayerArgumentType {

    protected Collection<String> getPlayerSuggestions() {
        Collection<String> result = ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toSet());
        result.addAll(ModerationPlugin.getWatchlistManager().getKnownPlayers().keySet());
        result.addAll(ModerationPlugin.getWatchlistManager().getWatchlist().keySet());
        return result.stream().sorted().collect(Collectors.toList());
    }

}
