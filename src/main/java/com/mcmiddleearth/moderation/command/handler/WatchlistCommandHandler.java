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
package com.mcmiddleearth.moderation.command.handler;


import com.mcmiddleearth.moderation.ModerationPlugin;
import com.mcmiddleearth.moderation.Permission;
import com.mcmiddleearth.moderation.Style;
import com.mcmiddleearth.moderation.command.argument.KnownPlayerArgumentType;
import com.mcmiddleearth.moderation.command.argument.OfflinePlayerArgumentType;
import com.mcmiddleearth.moderation.command.argument.PageArgumentType;
import com.mcmiddleearth.moderation.command.argument.ReasonArgumentType;
import com.mcmiddleearth.moderation.command.builder.HelpfulLiteralBuilder;
import com.mcmiddleearth.moderation.command.builder.HelpfulRequiredArgumentBuilder;
import com.mcmiddleearth.moderation.util.DiscordUtil;
import com.mcmiddleearth.moderation.watchlist.WatchlistPlayerData;
import com.mcmiddleearth.moderation.watchlist.WatchlistReason;
import com.mojang.brigadier.CommandDispatcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

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

    public WatchlistCommandHandler(String name, CommandDispatcher<CommandSender> dispatcher) {
        super(name);
        dispatcher
            .register(HelpfulLiteralBuilder.literal(name)
                .withHelpText("Manage Moderation Watchlist.")
                .withTooltip("Manage list of players who are being watched for possible moderation action.")
                .requires(commandSender -> commandSender.hasPermission(Permission.WATCHLIST))

                .then(HelpfulRequiredArgumentBuilder.argument("player", new KnownPlayerArgumentType())
                    .withHelpText("Watchlist details about a player.")
                    .withTooltip("Name of player to see details about.")
                    .requires(commandSender -> commandSender.hasPermission(Permission.SEE_WATCHLIST))
                    .executes(context -> viewDetails(context.getSource(), context.getArgument("player",String.class))))

                .then(HelpfulLiteralBuilder.literal("list")
                    .withHelpText("See players on watchlist")
                    .withTooltip("Get a list of all or a group of players on the watchlist")
                    .requires(commandSender -> commandSender.hasPermission(Permission.SEE_WATCHLIST))
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
                        .requires(commandSender -> commandSender.hasPermission(Permission.ADD_WATCHLIST))

                        .then(HelpfulRequiredArgumentBuilder.argument("player", new OfflinePlayerArgumentType())
                                .withTooltip("Name of player to add to watchlist")

                                .then(HelpfulRequiredArgumentBuilder.argument("reason", new ReasonArgumentType())
                                        .executes(context -> addPlayer(context.getSource(),context.getArgument("player",String.class),
                                                context.getArgument("reason", String.class))))))

                .then(HelpfulLiteralBuilder.literal("remove")
                        .withHelpText("Remove from watchlist")
                        .withTooltip("Removes a reason from a player or a player entirely from watchlist.")
                        .requires(commandSender -> commandSender.hasPermission(Permission.REMOVE_WATCHLIST))

                        .then(HelpfulRequiredArgumentBuilder.argument("player", new KnownPlayerArgumentType())
                                .withTooltip("Name of player to remove from watchlist")
                                .executes(context -> removePlayer(context.getSource(),context.getArgument("player",String.class)))

                                .then(HelpfulRequiredArgumentBuilder.argument("reason",integer(1))
                                    .withTooltip("No. of Reason to remove.")
                                    .executes(context -> removeReason(context.getSource(),context.getArgument("player",String.class),
                                                                                          context.getArgument("reason",Integer.class)))))));
    }

    private int viewDetails(CommandSender commandSender, String showPlayer) {
        WatchlistPlayerData data = ModerationPlugin.getWatchlistManager().getWatchlistData(showPlayer);
        if(data != null) {
            StringBuilder message = new StringBuilder("Watchlist reasons for " + Style.INFO_STRESSED + showPlayer + Style.INFO+":");
            for(int i = 0; i < data.getReasons().size(); i++) {
                WatchlistReason reason = data.getReasons().get(i);
                message.append("\n[" + (i+1) + "] " + Style.INFO_LIGHT + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US).format(reason.getCreationTime()))
                       .append(Style.INFO + " (" + (reason.isByModerator() ? Style.MOD : Style.UNCONFIRMED) + "by "+reason.getInitiator())
                       .append(Style.INFO + ") " + reason.getDescription());
                if (!reason.getNameAtCreationTime().equals(showPlayer)) {
                    message.append(Style.WARNING+" (player name at time of report: "+reason.getNameAtCreationTime()+")");
                }
            }
            ModerationPlugin.sendInfo(commandSender,new ComponentBuilder(message.toString()));
        } else {
            ModerationPlugin.sendError(commandSender,new ComponentBuilder("Player not on watchlist!"));
        }
        return 0;
    }

    private int viewList(CommandSender commandSender, String group, Integer page) {
        List<Map.Entry<String,WatchlistPlayerData>> displayList = getWatchlistSelection(group);
        String message;
        if(group.equals("all")) {
            message = ""+Style.INFO_STRESSED+ChatColor.BOLD+"All "+Style.INFO+"players on watchlist";
        } else {
            if(group.equals("online")) {
                message = ""+Style.INFO_STRESSED+ChatColor.BOLD+"Online "+Style.INFO+"players on watchlist";
            } else {
                message = "Players on watchlist matching '"+Style.INFO_STRESSED+ChatColor.BOLD+group+Style.INFO+"'";
            }
        }
        //message = message +  " (page "+Style.INFO_STRESSED+page+Style.INFO+" of "+(displayList.size()/10+1)+")";
        int maxPage = displayList.size()/10 +1;
        if((page) > maxPage) {
            page = maxPage;
        }
        ComponentBuilder builder = new ComponentBuilder(message).append(" (page ");
        if(page > 1) {
            builder.append("<").color(Style.INFO_STRESSED).bold(true)
                   .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/watchlist list " + (page -1)))
                   .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Click for previous page.")
                            .color(Style.TOOLTIP).create())));
        }
        builder.append(""+page).color(Style.INFO).bold(false);
        if(page < maxPage) {
            builder.append(">").color(Style.INFO_STRESSED).bold(true)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/watchlist list " + (page + 1)))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Click for next page.")
                            .color(Style.TOOLTIP).create())));
        }
        builder.append(" of "+maxPage+")").color(Style.INFO).bold(false);
        if(displayList.size()>0) {
//Logger.getGlobal().info("all: "+ModerationPlugin.getWatchlistManager().getWatchlist().size()+" Size: "+displayList.size());
            for (int i = (page-1) * 10; i < Math.min((page-1) * 10 + 10, displayList.size()); i++) {
//Logger.getGlobal().info("Count: "+i);
                String name = displayList.get(i).getKey();
                UUID uuid = displayList.get(i).getValue().getUuid();
                ChatColor color = Style.MOD;
                if (displayList.get(i).getValue().isUuidUnknown()) {
                    name = name + " (unconfirmed)";
                    color = Style.UNCONFIRMED;
                } else if(displayList.get(i).getValue().isNameUnknown()) {
                    color = Style.WARNING;
                }
                builder.append("\n- ").color(Style.INFO)
                        .append(name).color(color).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/watchlist " + displayList.get(i).getKey()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Click for details.")
                                .color(Style.TOOLTIP).create())))
                        .append(" "+(uuid!=null?uuid.toString():"unknown UUID")).color(Style.INFO);
            }
        } else {
            builder.append("\n- no Players - ").color(Style.INFO);
        }
        ModerationPlugin.sendInfo(commandSender,builder);
        return 0;
    }

    private List<Map.Entry<String, WatchlistPlayerData>> getWatchlistSelection(String selection) {
        Map<String,WatchlistPlayerData> watchlist = ModerationPlugin.getWatchlistManager().getWatchlist();
        List<Map.Entry<String,WatchlistPlayerData>> selectionList;
        if(selection.equals("all")) {
            selectionList = watchlist.entrySet().stream().sorted(Comparator.comparing(entry -> entry.getKey().toLowerCase())).collect(Collectors.toList());
        } else {
            if(selection.equals("online")) {
                selectionList = watchlist.entrySet().stream().filter(entry -> ProxyServer.getInstance().getPlayer(entry.getKey())!=null)
                        .sorted(Comparator.comparing(entry -> entry.getKey().toLowerCase())).collect(Collectors.toList());
            } else {
                selectionList = watchlist.entrySet().stream().filter(entry -> entry.getKey().toLowerCase().contains(selection.toLowerCase()))
                        .sorted(Comparator.comparing(entry -> entry.getKey().toLowerCase())).collect(Collectors.toList());
            }
        }
        return selectionList;
    }

    private int addPlayer(CommandSender commandSender, String addPlayer, String reason) {
        ModerationPlugin.getWatchlistManager().addWatchlist(addPlayer, commandSender, reason);
        ModerationPlugin.sendInfo(commandSender,new ComponentBuilder("Added "+Style.INFO_STRESSED+addPlayer
                                                                     +Style.INFO+" to watchlist for '"+reason+"'"));
        ComponentBuilder message = new ComponentBuilder(commandSender.getName()+" added "+Style.INFO_STRESSED+addPlayer
                                                                     +Style.INFO+" to watchlist for '"+reason+"'");
        if(ModerationPlugin.getConfig().isWatchlistSendIngame()) {
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(moderator -> moderator.hasPermission(Permission.SEE_WATCHLIST) && !moderator.equals(commandSender))
                    .forEach(moderator -> ModerationPlugin.sendInfo(moderator,message));
        }
        if(ModerationPlugin.getConfig().isWatchlistSendDiscord()) {
            String discordChannel = ModerationPlugin.getConfig().getWatchlistDiscordChannel();
            DiscordUtil.sendDiscord(discordChannel,"**"+commandSender.getName()+"** reported player **"+addPlayer+".**\nReason: **"+reason+"**",
                    ModerationPlugin.getConfig().isWatchlistPingModerators());
        }
        return 0;
    }

    private int removePlayer(CommandSender commandSender, String removePlayer) {
        WatchlistPlayerData data = ModerationPlugin.getWatchlistManager().getWatchlistData(removePlayer);
        if(data != null) {
            ModerationPlugin.getWatchlistManager().removeWatchlist(removePlayer);
            ModerationPlugin.sendInfo(commandSender, new ComponentBuilder("Removed " + Style.INFO_STRESSED + removePlayer + Style.INFO + " from watchlist."));
        } else {
            ModerationPlugin.sendError(commandSender,new ComponentBuilder("Player not on watchlist!"));
        }
        return 0;
    }

    private int removeReason(CommandSender commandSender, String player, Integer reason) {
        WatchlistPlayerData data = ModerationPlugin.getWatchlistManager().getWatchlistData(player);
        if(data != null) {
            if (reason > data.getReasons().size()) {
                ModerationPlugin.sendError(commandSender, new ComponentBuilder("Player does not have that many reasons."));
            } else {
                if(data.getReasons().size()>1) {
                    ModerationPlugin.getWatchlistManager().removeWatchlistReason(player, reason - 1);
                    ModerationPlugin.sendInfo(commandSender, new ComponentBuilder("Watchlist reason removed from player '" + player + "'."));
                } else {
                    ModerationPlugin.getWatchlistManager().removeWatchlist(player);
                    ModerationPlugin.sendInfo(commandSender,new ComponentBuilder("Removed "+Style.INFO_STRESSED+player+Style.INFO+" from watchlist as you removed the last reason for him to be there."));
                }
            }
        } else {
            ModerationPlugin.sendError(commandSender,new ComponentBuilder("Player not on watchlist!"));
        }
        return 0;
    }



}
