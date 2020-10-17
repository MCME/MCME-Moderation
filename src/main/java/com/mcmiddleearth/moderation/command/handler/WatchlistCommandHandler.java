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
import com.mcmiddleearth.moderation.command.argument.KnownPlayerArgumentType;
import com.mcmiddleearth.moderation.command.argument.OfflinePlayerArgumentType;
import com.mcmiddleearth.moderation.command.argument.PageArgumentType;
import com.mcmiddleearth.moderation.command.argument.ReasonArgumentType;
import com.mcmiddleearth.moderation.command.builder.HelpfulLiteralBuilder;
import com.mcmiddleearth.moderation.command.builder.HelpfulRequiredArgumentBuilder;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
                    .withTooltip("Returns details of which Mod added this player to the watchlist, the date he was added, and the reason.")
                    .requires(commandSender -> commandSender.hasPermission(Permission.SEE_WATCHLIST))
                    .executes(context -> viewDetails(context.getSource(), context.getArgument("player",String.class))))

                .then(HelpfulLiteralBuilder.literal("list")
                    .withHelpText("List players on watchlist")
                    .withTooltip("Get a list of all or a group of players on the watchlist")
                    .requires(commandSender -> commandSender.hasPermission(Permission.SEE_WATCHLIST))
                    .executes(context -> viewList(context.getSource(), "all", 1))

                    .then(HelpfulRequiredArgumentBuilder.argument(("page"),
                                         new PageArgumentType(context -> getWatchlistSelection("all")
                                                                            .stream().map(Map.Entry::getKey).collect(Collectors.toList())))
                            .executes(context -> viewList(context.getSource(), "all", context.getArgument("page", Integer.class))))

                    .then(HelpfulRequiredArgumentBuilder.argument("selection", word())
                        .withHelpText("Selection of player to display")
                        .withTooltip("Possible groups are 'all', 'online' or any string player names will be matched with.")
                        .suggests((context,suggestionsBuilder) ->
                                suggestionsBuilder.suggest("Possible groups are 'all', 'online' or any string player names will be matched with.").buildFuture())
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
                        .withHelpText("Remove a player from watchlist")
                        .withTooltip("Removes a player from watchlist entirely with all reasons.")
                        .requires(commandSender -> commandSender.hasPermission(Permission.REMOVE_WATCHLIST))

                        .then(HelpfulRequiredArgumentBuilder.argument("player", new KnownPlayerArgumentType())
                                .withTooltip("Name of player to remove from watchlist")
                                .executes(context -> removePlayer(context.getSource(),context.getArgument("player",String.class))))));
    }

    private int viewDetails(CommandSender commandSender, String showPlayer) {
        WatchlistPlayerData data = ModerationPlugin.getWatchlistManager().getWatchlistData(showPlayer);
        if(data != null) {
            StringBuilder message = new StringBuilder(ChatColor.GOLD+"Watchlist reasons for " + ChatColor.YELLOW + showPlayer + ChatColor.GOLD+":");
            for(WatchlistReason reason: data.getReasons()) {
                message.append("\n").append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US).format(reason.getCreationTime()))
                       .append(" (").append(reason.isByModerator() ? ChatColor.GOLD : ChatColor.GRAY).append(reason.getInitiator())
                       .append(ChatColor.YELLOW).append(") ").append(reason.getDescription());
            }
            commandSender.sendMessage(new ComponentBuilder(message.toString())
                    .color(ChatColor.YELLOW).create());
        } else {
            commandSender.sendMessage(new ComponentBuilder("Player not on watchlist!")
                    .color(ChatColor.RED).create());
        }
        return 0;
    }

    private int viewList(CommandSender commandSender, String group, Integer page) {
        List<Map.Entry<String,WatchlistPlayerData>> displayList = getWatchlistSelection(group);
        String message;
        if(group.equals("all")) {
            message = "All players on watchlist";
        } else {
            if(group.equals("online")) {
                message = "Online players on watchlist";
            } else {
                message = "Players on watchlist matching '"+group+"'";
            }
        }
        message = message +  " (page "+page+" of "+(displayList.size()/10+1)+")";
        ComponentBuilder builder = new ComponentBuilder(message).color(ChatColor.GOLD);
        if(displayList.size()>0) {
            for (int i = displayList.size() / 10; i < Math.min(displayList.size() / 10 + 10, displayList.size()); i++) {
                boolean unconfirmed = displayList.get(i).getValue().isUuidUnknown();
                String name = displayList.get(i).getKey();
                ChatColor color = ChatColor.GOLD;
                if (unconfirmed) {
                    name = name + "(unconfirmed)";
                    color = ChatColor.GRAY;
                }
                builder.append("\n[" + i + "] ").color(ChatColor.YELLOW)
                        .append(name).color(color).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/watchlist " + displayList.get(i).getKey()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder("Click for details.")
                                .color(ChatColor.YELLOW).create())));
            }
        } else {
            builder.append("- no Players - ").color(ChatColor.YELLOW);
        }
        commandSender.sendMessage(builder.create());
        return 0;
    }

    private List<Map.Entry<String, WatchlistPlayerData>> getWatchlistSelection(String selection) {
        Map<String,WatchlistPlayerData> watchlist = ModerationPlugin.getWatchlistManager().getWatchlist();
        List<Map.Entry<String,WatchlistPlayerData>> selectionList;
        if(selection.equals("all")) {
            selectionList = watchlist.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
        } else {
            if(selection.equals("online")) {
                selectionList = watchlist.entrySet().stream().filter(entry -> ProxyServer.getInstance().getPlayer(entry.getKey())!=null)
                        .sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
            } else {
                selectionList = watchlist.entrySet().stream().filter(entry -> entry.getKey().toLowerCase().contains(selection.toLowerCase()))
                        .sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());
            }
        }
        return selectionList;
    }

    private int addPlayer(CommandSender commandSender, String addPlayer, String reason) {
        ModerationPlugin.getWatchlistManager().addWatchlist(addPlayer, commandSender, reason);
        commandSender.sendMessage(new ComponentBuilder("Added "+addPlayer+" to watchlist for '"+reason+"'")
                .color(ChatColor.GOLD).create());
        return 0;
    }

    private int removePlayer(CommandSender commandSender, String removePlayer) {
        ModerationPlugin.getWatchlistManager().removeWatchlist(removePlayer);
        commandSender.sendMessage(new ComponentBuilder("Removed "+removePlayer+" from watchlist.")
                .color(ChatColor.GOLD).create());
        return 0;
    }

    /*private ProxiedPlayer getPlayer(CommandSender commandSender, String playerName) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
        if(player == null) {
            commandSender.sendMessage(new ComponentBuilder("*****Player not found!***should never get displayed**********")
                    .color(ChatColor.RED).create());
        }
        return player;
    }*/

}
