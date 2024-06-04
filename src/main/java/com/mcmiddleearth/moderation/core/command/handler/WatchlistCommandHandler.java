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
package com.mcmiddleearth.moderation.core.command.handler;


import com.mcmiddleearth.moderation.core.Style;
import com.mcmiddleearth.moderation.core.command.argument.KnownPlayerArgumentType;
import com.mcmiddleearth.moderation.core.command.argument.OfflinePlayerArgumentType;
import com.mcmiddleearth.moderation.core.command.argument.PageArgumentType;
import com.mcmiddleearth.moderation.core.command.argument.ReasonArgumentType;
import com.mcmiddleearth.moderation.core.command.builder.HelpfulLiteralBuilder;
import com.mcmiddleearth.moderation.core.command.builder.HelpfulRequiredArgumentBuilder;
import com.mcmiddleearth.moderation.core.ModerationCommandSender;
import com.mcmiddleearth.moderation.core.ModerationProxy;
import com.mcmiddleearth.moderation.core.Permission;
import com.mcmiddleearth.moderation.core.util.DiscordUtil;
import com.mcmiddleearth.moderation.core.watchlist.WatchlistPlayerData;
import com.mcmiddleearth.moderation.core.watchlist.WatchlistReason;
import com.mojang.brigadier.CommandDispatcher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

/**
 *
 * @author Eriol_Eandur
 */

public class WatchlistCommandHandler extends AbstractCommandHandler {

    public WatchlistCommandHandler(String name, String permission, CommandDispatcher<ModerationCommandSender> dispatcher) {
        super(name, permission);
        dispatcher
            .register(HelpfulLiteralBuilder.literal(name)
                .withHelpText("Manage Moderation Watchlist.")
                .withTooltip("Manage list of players who are being watched for possible moderation action.")
                .requires(ModerationCommandSender -> ModerationCommandSender.hasPermission(permission))

                .then(HelpfulRequiredArgumentBuilder.argument("player", new KnownPlayerArgumentType())
                    .withHelpText("Watchlist details about a player.")
                    .withTooltip("Name of player to see details about.")
                    .requires(ModerationCommandSender -> ModerationCommandSender.hasPermission(Permission.SEE_WATCHLIST))
                    .executes(context -> viewDetails(context.getSource(), context.getArgument("player",String.class))))

                .then(HelpfulLiteralBuilder.literal("list")
                    .withHelpText("See players on watchlist")
                    .withTooltip("Get a list of all or a group of players on the watchlist")
                    .requires(ModerationCommandSender -> ModerationCommandSender.hasPermission(Permission.SEE_WATCHLIST))
                    .executes(context -> viewList(context.getSource(), "all", 1))

                    .then(HelpfulRequiredArgumentBuilder.argument(("page"),
                                         new PageArgumentType(context -> getWatchlistSelection("all")
                                                                            .stream().map(Map.Entry::getKey).collect(Collectors.toList())))
                            .executes(context -> viewList(context.getSource(), "all", context.getArgument("page", Integer.class))))

                    .then(HelpfulRequiredArgumentBuilder.argument("selection", word())
                        .withTooltip("Possible groups are 'all', 'online' or any string player names will be matched with.")
                        .suggests((context,suggestionsBuilder) ->
                                suggestionsBuilder.suggest("all").suggest("online").suggest("<selection>").buildFuture())
                        .executes(context -> viewList(context.getSource(), context.getArgument("selection", String.class), 1))

                        .then(HelpfulRequiredArgumentBuilder.argument(("page"),
                                         new PageArgumentType(context -> getWatchlistSelection((String) context.getArgument("selection",String.class))
                                                                                            .stream().map(Map.Entry::getKey).collect(Collectors.toList())))
                            .executes(context -> viewList(context.getSource(), context.getArgument("selection", String.class),
                                                                               context.getArgument("page", Integer.class))))))

                .then(HelpfulLiteralBuilder.literal("add")
                        .withHelpText("Add a player to watchlist")
                        .withTooltip("Add a player to watchlist and give a reason why he should be watched. You can also add more reasons to a player already on the list.")
                        .requires(ModerationCommandSender -> ModerationCommandSender.hasPermission(Permission.ADD_WATCHLIST))

                        .then(HelpfulRequiredArgumentBuilder.argument("player", new OfflinePlayerArgumentType())
                                .withTooltip("Name of player to add to watchlist")

                                .then(HelpfulRequiredArgumentBuilder.argument("reason", new ReasonArgumentType())
                                        .executes(context -> addPlayer(context.getSource(),context.getArgument("player",String.class),
                                                context.getArgument("reason", String.class))))))

                .then(HelpfulLiteralBuilder.literal("remove")
                        .withHelpText("Remove from watchlist")
                        .withTooltip("Removes a reason from a player or a player entirely from watchlist.")
                        .requires(ModerationCommandSender -> ModerationCommandSender.hasPermission(Permission.REMOVE_WATCHLIST))

                        .then(HelpfulRequiredArgumentBuilder.argument("player", new KnownPlayerArgumentType())
                                .withTooltip("Name of player to remove from watchlist")
                                .executes(context -> removePlayer(context.getSource(),context.getArgument("player",String.class)))

                                .then(HelpfulRequiredArgumentBuilder.argument("reason",integer(1))
                                    .withTooltip("No. of Reason to remove.")
                                    .executes(context -> removeReason(context.getSource(),context.getArgument("player",String.class),
                                                                                          context.getArgument("reason",Integer.class)))))));
    }

    private int viewDetails(ModerationCommandSender commandSender, String showPlayer) {
        WatchlistPlayerData data = ModerationProxy.getPlugin().getWatchlistManager().getWatchlistData(showPlayer);
        if(data != null) {
            Component message = Component.text("Watchlist reasons for ")
                    .append(Component.text(showPlayer,Style.INFO_STRESSED))
                    .append(Component.text(":",Style.INFO));
            for(int i = 0; i < data.getReasons().size(); i++) {
                WatchlistReason reason = data.getReasons().get(i);
                message = message.append(Component.text("\n[" + (i+1) + "] "))
                        .append(Component.text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US)
                                .format(reason.getCreationTime()),Style.INFO_LIGHT))
                        .append(Component.text(" (",Style.INFO))
                        .append(Component.text("by "+reason.getInitiator(), (reason.isByModerator() ? Style.MOD : Style.UNCONFIRMED)))
                        .append(Component.text(") "+reason.getDescription(),Style.INFO));
                if (!reason.getNameAtCreationTime().equals(showPlayer)) {
                    message = message.append(Component.text(" (player name at time of report: "+reason.getNameAtCreationTime()+")",Style.WARNING));
                }
            }
            commandSender.sendInfo(message);
        } else {
            commandSender.sendError(Component.text("Player not on watchlist!"));
        }
        return 0;
    }

    private int viewList(ModerationCommandSender commandSender, String group, Integer page) {
        List<Map.Entry<String,WatchlistPlayerData>> displayList = getWatchlistSelection(group);
        Component message;
        if(group.equals("all")) {
            message = Component.text("All ",Style.INFO_STRESSED,TextDecoration.BOLD)
                    .append(Component.text("players on watchlist",Style.INFO));
        } else {
            if(group.equals("online")) {
                message =Component.text("Online ",Style.INFO_STRESSED, TextDecoration.BOLD)
                        .append(Component.text("players on watchlist",Style.INFO));
            } else {
                message = Component.text("Players on watchlist matching '",Style.INFO)
                        .append(Component.text(group,Style.INFO_STRESSED,TextDecoration.BOLD))
                        .append(Component.text("'",Style.INFO));
            }
        }
        //message = message +  " (page "+Style.INFO_STRESSED+page+Style.INFO+" of "+(displayList.size()/10+1)+")";
        int maxPage = displayList.size()/10 +1;
        if((page) > maxPage) {
            page = maxPage;
        }
        message = message.append(Component.text(" (page "));
        if(page > 1) {
            message = message.append(Component.text("<", Style.INFO_STRESSED, TextDecoration.BOLD))
                   .clickEvent(ClickEvent.runCommand("/watchlist list " + (page - 1)))
                   .hoverEvent(HoverEvent.showText(Component.text("Click for previous page.").color(Style.TOOLTIP)));
        }
        message = message.append(Component.text(""+page, Style.INFO, TextDecoration.BOLD));
        if(page < maxPage) {
            message = message.append(Component.text(">",Style.INFO_STRESSED, TextDecoration.BOLD))
                    .clickEvent(ClickEvent.runCommand("/watchlist list " + (page + 1)))
                    .hoverEvent(HoverEvent.showText(Component.text("Click for next page.",Style.TOOLTIP)));
        }
        message = message.append(Component.text(" of "+maxPage+")",Style.INFO, TextDecoration.BOLD));
        if(displayList.size()>0) {
//Logger.getGlobal().info("all: "+ModerationPluginBungee.getWatchlistManager().getWatchlist().size()+" Size: "+displayList.size());
            for (int i = (page-1) * 10; i < Math.min((page-1) * 10 + 10, displayList.size()); i++) {
//Logger.getGlobal().info("Count: "+i);
                String name = displayList.get(i).getKey();
                UUID uuid = displayList.get(i).getValue().getUuid();
                TextColor color = Style.MOD;
                if (displayList.get(i).getValue().isUuidUnknown()) {
                    name = name + " (unconfirmed)";
                    color = Style.UNCONFIRMED;
                } else if(displayList.get(i).getValue().isNameUnknown()) {
                    color = Style.WARNING;
                }
                message = message.append(Component.text("\n- ",Style.INFO)
                        .append(Component.text(name,color).clickEvent(ClickEvent.runCommand("/watchlist " + displayList.get(i).getKey())))
                        .hoverEvent(HoverEvent.showText(Component.text("Click for details.",Style.TOOLTIP)))
                        .append(Component.text(" "+(uuid!=null?uuid.toString():"unknown UUID"),Style.INFO)));
            }
        } else {
            message = message.append(Component.text("\n- no Players - ",Style.INFO));
        }
        commandSender.sendInfo(message);
        return 0;
    }

    private List<Map.Entry<String, WatchlistPlayerData>> getWatchlistSelection(String selection) {
        Map<String,WatchlistPlayerData> watchlist = ModerationProxy.getPlugin().getWatchlistManager().getWatchlist();
        List<Map.Entry<String,WatchlistPlayerData>> selectionList;
        if(selection.equals("all")) {
            selectionList = watchlist.entrySet().stream().sorted(Comparator.comparing(entry -> entry.getKey().toLowerCase())).collect(Collectors.toList());
        } else {
            if(selection.equals("online")) {
                selectionList = watchlist.entrySet().stream().filter(entry -> ModerationProxy.getInstance().getPlayer(entry.getKey())!=null)
                        .sorted(Comparator.comparing(entry -> entry.getKey().toLowerCase())).collect(Collectors.toList());
            } else {
                selectionList = watchlist.entrySet().stream().filter(entry -> entry.getKey().toLowerCase().contains(selection.toLowerCase()))
                        .sorted(Comparator.comparing(entry -> entry.getKey().toLowerCase())).collect(Collectors.toList());
            }
        }
        return selectionList;
    }

    private int addPlayer(ModerationCommandSender commandSender, String addPlayer, String reason) {
        ModerationProxy.getPlugin().getWatchlistManager().addWatchlist(addPlayer, commandSender, reason);
        commandSender.sendInfo(Component.text("Added ")
                .append(Component.text(addPlayer).color(Style.INFO_STRESSED))
                .append(Component.text(" to watchlist for '"+reason+"'").color(Style.INFO)));
        Component message = Component.text(commandSender.getName()+" added ")
                .append(Component.text(addPlayer).color(Style.INFO_STRESSED))
                .append(Component.text(" to watchlist for '"+reason+"'").color(Style.INFO));
        if(ModerationProxy.getPlugin().getConfig().isWatchlistSendIngame()) {
            ModerationProxy.getInstance().getPlayers().stream()
                    .filter(moderator -> moderator.hasPermission(Permission.SEE_WATCHLIST) && !moderator.equals(commandSender))
                    .forEach(moderator -> moderator.sendInfo(message));
        }
        if(ModerationProxy.getPlugin().getConfig().isWatchlistSendDiscord()) {
            String discordChannel = ModerationProxy.getPlugin().getConfig().getWatchlistDiscordChannel();
            DiscordUtil.sendDiscord(discordChannel,"**"+commandSender.getName()+"** reported player **"+addPlayer+".**\nReason: **"+reason+"**",
                    ModerationProxy.getPlugin().getConfig().isWatchlistPingModerators());
        }
        return 0;
    }

    private int removePlayer(ModerationCommandSender commandSender, String removePlayer) {
        WatchlistPlayerData data = ModerationProxy.getPlugin().getWatchlistManager().getWatchlistData(removePlayer);
        if(data != null) {
            ModerationProxy.getPlugin().getWatchlistManager().removeWatchlist(removePlayer);
            commandSender.sendInfo(Component.text("Removed ")
                    .append(Component.text(removePlayer).color(Style.INFO_STRESSED))
                    .append(Component.text(" from watchlist.").color(Style.INFO)));
        } else {
            commandSender.sendError(Component.text("Player not on watchlist!"));
        }
        return 0;
    }

    private int removeReason(ModerationCommandSender commandSender, String player, Integer reason) {
        WatchlistPlayerData data = ModerationProxy.getPlugin().getWatchlistManager().getWatchlistData(player);
        if(data != null) {
            if (reason > data.getReasons().size()) {
                commandSender.sendError(Component.text("Player does not have that many reasons."));
            } else {
                if(data.getReasons().size()>1) {
                    ModerationProxy.getPlugin().getWatchlistManager().removeWatchlistReason(player, reason - 1);
                    commandSender.sendInfo(Component.text("Watchlist reason removed from player '" + player + "'."));
                } else {
                    ModerationProxy.getPlugin().getWatchlistManager().removeWatchlist(player);
                    commandSender.sendInfo(Component.text("Removed ")
                                                    .append(Component.text(player).color(Style.INFO_STRESSED))
                                                    .append(Component.text(" from watchlist as you removed the last reason for him to be there.").color(Style.INFO)));
                }
            }
        } else {
            commandSender.sendError(Component.text("Player not on watchlist!"));
        }
        return 0;
    }



}
