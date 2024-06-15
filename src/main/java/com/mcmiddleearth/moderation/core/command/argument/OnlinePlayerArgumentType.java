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
package com.mcmiddleearth.moderation.core.command.argument;

import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.moderation.core.ModerationProxy;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Eriol_Eandur
 */

public class OnlinePlayerArgumentType extends AbstractPlayerArgumentType {

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String o = reader.readUnquotedString();
        if (ModerationProxy.getInstance().getPlayers().stream().map(McmeProxyPlayer::getName).collect(Collectors.toSet()).contains(o)) {
            return o;
        }
        throw new CommandSyntaxException(new SimpleCommandExceptionType(new LiteralMessage("Failed parsing of OnlinePlayerArgument")),
                                         new LiteralMessage(String.format("Player not online: %s",o)));
    }

    @Override
    protected Collection<String> getPlayerSuggestions() {
        return ModerationProxy.getInstance().getPlayers().stream().map(McmeProxyPlayer::getName).collect(Collectors.toSet());
    }

}
