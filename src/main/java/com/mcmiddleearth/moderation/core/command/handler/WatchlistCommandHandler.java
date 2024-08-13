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


import com.mcmiddleearth.base.core.command.McmeCommandSender;
import com.mcmiddleearth.base.core.message.*;
import com.mcmiddleearth.moderation.core.McmeModeration;
import com.mcmiddleearth.moderation.core.Permission;
import com.mcmiddleearth.moderation.core.command.argument.KnownPlayerArgumentType;
import com.mcmiddleearth.moderation.core.command.argument.OfflinePlayerArgumentType;
import com.mcmiddleearth.moderation.core.command.argument.PageArgumentType;
import com.mcmiddleearth.moderation.core.command.argument.ReasonArgumentType;
import com.mcmiddleearth.moderation.core.command.builder.HelpfulLiteralBuilder;
import com.mcmiddleearth.moderation.core.command.builder.HelpfulRequiredArgumentBuilder;
import com.mcmiddleearth.moderation.core.util.DiscordUtil;
import com.mcmiddleearth.moderation.core.watchlist.WatchlistPlayerData;
import com.mcmiddleearth.moderation.core.watchlist.WatchlistReason;
import com.mojang.brigadier.CommandDispatcher;

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

    public WatchlistCommandHandler(String name, String permission, CommandDispatcher<McmeCommandSender> dispatcher) {
        super(name, permission);
        dispatcher
            .register(HelpfulLiteralBuilder.literal(name)
                .withHelpText("Manage Moderation Watchlist.")
                .withTooltip("Manage list of players who are being watched for possible moderation action.")
                .requires(ModerationCommandSender -> ModerationCommandSender.hasPermission(permission))

                .then(HelpfulLiteralBuilder.literal("details")
                    .withHelpText("Watchlist details about a player.")
                    .withTooltip("See all reported reasons for a player together with report dates.")
                    .requires(ModerationCommandSender -> ModerationCommandSender.hasPermission(Permission.SEE_WATCHLIST))
                    .then(HelpfulRequiredArgumentBuilder.argument("player", new KnownPlayerArgumentType())
                        .withTooltip("Name of player to see details about.")
                        .executes(context -> viewDetails(context.getSource(), context.getArgument("player",String.class)))))

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
                                         new PageArgumentType(context -> getWatchlistSelection( (String) context.getArgument("selection", String.class))
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
                                    .withTooltip("Number of Reason to remove.")
                                    .executes(context -> removeReason(context.getSource(),context.getArgument("player",String.class),
                                                                                          context.getArgument("reason",Integer.class)))))));
    }

    private int viewDetails(McmeCommandSender commandSender, String showPlayer) {
        WatchlistPlayerData data = McmeModeration.getWatchlistManager().getWatchlistData(showPlayer);
        if(data != null) {
            Message message = McmeModeration.infoMessage("Watchlist reasons for ")
                    .add(showPlayer, McmeColors.INFO_STRESSED)
                    .add(":");
            for(int i = 0; i < data.getReasons().size(); i++) {
                WatchlistReason reason = data.getReasons().get(i);
                message.add("\n[" + (i+1) + "] ")
                        .add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US)
                                .format(reason.getCreationTime()),McmeColors.INFO_LIGHT)
                        .add(" (")
                        .add("by "+reason.getInitiator(), (reason.isByModerator() ? McmeColors.MOD : McmeColors.UNCONFIRMED))
                        .add(") "+reason.getDescription());
                if (!reason.getNameAtCreationTime().equals(showPlayer)) {
                    message.add(" (player name at time of report: "+reason.getNameAtCreationTime()+")",McmeColors.WARNING);
                }
            }
            commandSender.sendMessage(message);
        } else {
            commandSender.sendMessage(McmeModeration.errorMessage("Player not on watchlist!"));
        }
        return 0;
    }

    private int viewList(McmeCommandSender commandSender, String group, Integer page) {
        List<Map.Entry<String,WatchlistPlayerData>> displayList = getWatchlistSelection(group);
        Message message;
        if(group.equals("all")) {
            message = McmeModeration.infoMessage()
                    .add("All ",McmeColors.INFO_STRESSED, MessageDecoration.BOLD)
                    .add("players on watchlist");
        } else {
            if(group.equals("online")) {
                message =McmeModeration.infoMessage()
                        .add("Online ",McmeColors.INFO_STRESSED, MessageDecoration.BOLD)
                        .add("players on watchlist");
            } else {
                message = McmeModeration.infoMessage("Players on watchlist matching '")
                        .add(group,McmeColors.INFO_STRESSED,MessageDecoration.BOLD)
                        .add("'");
            }
        }
        //message = message +  " (page "+Style.INFO_STRESSED+page+Style.INFO+" of "+(displayList.size()/10+1)+")";
        int maxPage = displayList.size()/10 +1;
        if((page) > maxPage) {
            page = maxPage;
        }
        message.add(" (page ");
        if(page > 1) {
            Message clickMessage = McmeModeration.message("<", McmeColors.INFO_STRESSED, MessageDecoration.BOLD)
                   .addClick(new MessageClickEvent(MessageClickEvent.Action.RUN_COMMAND,
                                                   "/watchlist list " + (page - 1)))
                   .addHover(new MessageHoverEvent(MessageHoverEvent.Action.TEXT,
                             McmeModeration.getPlugin().createMessage().add("Click for previous page.", McmeColors.TOOLTIP)));
            message.add(clickMessage);
        }
        message = message.add(""+page, MessageDecoration.BOLD);
        if(page < maxPage) {
            Message clickMessage = McmeModeration.message(">",McmeColors.INFO_STRESSED, MessageDecoration.BOLD)
                    .addClick(new MessageClickEvent(MessageClickEvent.Action.RUN_COMMAND,
                                            "/watchlist list " + (page + 1)))
                    .addHover(new MessageHoverEvent(MessageHoverEvent.Action.TEXT,
                            McmeModeration.getPlugin().createMessage().add("Click for next page.",McmeColors.TOOLTIP)));
            message.add(clickMessage);
        }
        message.add(" of "+maxPage+")", MessageDecoration.BOLD);
        if(displayList.size()>0) {
//Logger.getGlobal().info("all: "+ModerationPluginBungee.getWatchlistManager().getWatchlist().size()+" Size: "+displayList.size());
            for (int i = (page-1) * 10; i < Math.min((page-1) * 10 + 10, displayList.size()); i++) {
//Logger.getGlobal().info("Count: "+i);
                String name = displayList.get(i).getKey();
                UUID uuid = displayList.get(i).getValue().getUuid();
                MessageColor color = McmeColors.MOD;
                if (displayList.get(i).getValue().isUuidUnknown()) {
                    name = name + " (unconfirmed)";
                    color = McmeColors.UNCONFIRMED;
                } else if(displayList.get(i).getValue().isNameUnknown()) {
                    color = McmeColors.WARNING;
                }
                message.add("\n- ");
                Message clickMessage = McmeModeration.message(name,color)
                        .add(" "+(uuid!=null?uuid.toString():"unknown UUID"))
                        .addClick(new MessageClickEvent(MessageClickEvent.Action.RUN_COMMAND,
                                                        "/watchlist details " + displayList.get(i).getKey()))
                        .addHover(new MessageHoverEvent(MessageHoverEvent.Action.TEXT,
                                McmeModeration.getPlugin().createMessage().add("Click for details.",McmeColors.TOOLTIP)));
                message.add(clickMessage);
            }
        } else {
            message = message.add("\n- no Players - ");
        }
        commandSender.sendMessage(message);
        return 0;
    }

    private List<Map.Entry<String, WatchlistPlayerData>> getWatchlistSelection(String selection) {
        Map<String,WatchlistPlayerData> watchlist = McmeModeration.getWatchlistManager().getWatchlist();
        List<Map.Entry<String,WatchlistPlayerData>> selectionList;
        if(selection.equals("all")) {
            selectionList = watchlist.entrySet().stream().sorted(Comparator.comparing(entry -> entry.getKey().toLowerCase())).collect(Collectors.toList());
        } else {
            if(selection.equals("online")) {
                selectionList = watchlist.entrySet().stream().filter(entry -> McmeModeration.getProxy().getPlayer(entry.getKey())!=null)
                        .sorted(Comparator.comparing(entry -> entry.getKey().toLowerCase())).collect(Collectors.toList());
            } else {
                selectionList = watchlist.entrySet().stream().filter(entry -> entry.getKey().toLowerCase().contains(selection.toLowerCase()))
                        .sorted(Comparator.comparing(entry -> entry.getKey().toLowerCase())).collect(Collectors.toList());
            }
        }
        return selectionList;
    }

    private int addPlayer(McmeCommandSender commandSender, String addPlayer, String reason) {
        McmeModeration.getWatchlistManager().addWatchlist(addPlayer, commandSender, reason);
        commandSender.sendMessage(McmeModeration.infoMessage("Added ")
                .add(addPlayer, McmeColors.INFO_STRESSED)
                .add(" to watchlist for '"+reason+"'"));
        Message message = McmeModeration.infoMessage(commandSender.getName()+" added ")
                .add(addPlayer, McmeColors.INFO_STRESSED)
                .add(" to watchlist for '"+reason+"'");
        if(McmeModeration.getConfig().isWatchlistSendIngame()) {
            McmeModeration.getProxy().getPlayers().stream()
                    .filter(moderator -> moderator.hasPermission(Permission.SEE_WATCHLIST) && !moderator.equals(commandSender))
                    .forEach(moderator -> moderator.sendMessage(message));
        }
        if(McmeModeration.getConfig().isWatchlistSendDiscord()) {
            String discordChannel = McmeModeration.getConfig().getWatchlistDiscordChannel();
            DiscordUtil.sendDiscord(discordChannel,"**"+commandSender.getName()+"** reported player **"+addPlayer+".**\nReason: **"+reason+"**",
                    McmeModeration.getConfig().isWatchlistPingModerators());
        }
        return 0;
    }

    private int removePlayer(McmeCommandSender commandSender, String removePlayer) {
        WatchlistPlayerData data = McmeModeration.getWatchlistManager().getWatchlistData(removePlayer);
        if(data != null) {
            McmeModeration.getWatchlistManager().removeWatchlist(removePlayer);
            commandSender.sendMessage(McmeModeration.infoMessage("Removed ")
                    .add(removePlayer, McmeColors.INFO_STRESSED)
                    .add(" from watchlist."));
        } else {
            commandSender.sendMessage(McmeModeration.errorMessage("Player not on watchlist!"));
        }
        return 0;
    }

    private int removeReason(McmeCommandSender commandSender, String player, Integer reason) {
        WatchlistPlayerData data = McmeModeration.getWatchlistManager().getWatchlistData(player);
        if(data != null) {
            if (reason > data.getReasons().size()) {
                commandSender.sendMessage(McmeModeration.errorMessage("Player does not have that many reasons."));
            } else {
                if(data.getReasons().size()>1) {
                    McmeModeration.getWatchlistManager().removeWatchlistReason(player, reason - 1);
                    commandSender.sendMessage(McmeModeration.infoMessage("Watchlist reason removed from player '" + player + "'."));
                } else {
                    McmeModeration.getWatchlistManager().removeWatchlist(player);
                    commandSender.sendMessage(McmeModeration.infoMessage("Removed ")
                                                    .add(player, McmeColors.INFO_STRESSED)
                                                    .add(" from watchlist as you removed the last reason for him to be there."));
                }
            }
        } else {
            commandSender.sendMessage(McmeModeration.errorMessage("Player not on watchlist!"));
        }
        return 0;
    }



}
