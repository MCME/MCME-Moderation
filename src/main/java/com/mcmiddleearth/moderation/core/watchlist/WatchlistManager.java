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
package com.mcmiddleearth.moderation.core.watchlist;

import com.google.common.base.Joiner;
import com.mcmiddleearth.base.core.command.McmeCommandSender;
import com.mcmiddleearth.base.core.configuration.YamlConfiguration;
import com.mcmiddleearth.base.core.player.McmeProxyPlayer;
import com.mcmiddleearth.base.net.kyori.adventure.text.Component;
import com.mcmiddleearth.moderation.core.McmeModeration;
import com.mcmiddleearth.moderation.core.Permission;
import com.mcmiddleearth.moderation.core.Style;
import com.mcmiddleearth.moderation.core.util.DiscordUtil;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Eriol_Eandur
 */

public class WatchlistManager {

    private final Map<String,WatchlistPlayerData> watchlist = new HashMap<>();

    private final File dataFile;

    private final Map<String,UUID> knownPlayers = new HashMap<>();

    /**
     * Constructor loads data from watchlist.yml
     */
    public WatchlistManager(File dataFolder) {
        dataFile = new File(dataFolder,"watchlist.yml");
        if(dataFile.exists()) {
            //YamlBridge yaml = new YamlBridge();
            //yaml.load(dataFile);
            YamlConfiguration yaml = new YamlConfiguration(dataFile);
            yaml.getMap().forEach((name, data) -> watchlist.put(name, new WatchlistPlayerData((Map<String, Object>) data)));
        }
    }

    /*public WatchlistPlayerData getPlayerData(String name) {
        return watchlist.get(name);
    }*/

    /**
     * This method is required for TabList feature in MCME-Connect plugin
     * @param name Player to check
     * @return if player is on watchlist
     */
    public boolean isOnWatchlist(String name) {
        return watchlist.keySet().stream().anyMatch(key -> key.equalsIgnoreCase(name));
    }

    public boolean hasWatchedIp(McmeProxyPlayer player) {
        return watchlist.values().stream().anyMatch(playerData -> !playerData.getIp().equals("unknown")
                                                                   && playerData.getIp().equals(getIp(player.getUniqueId())));
    }

    /**
     * This method is required for TabList feature in MCME-Connect plugin
     * @return Prefix to display for players on watchlist
     */
    public String watchlistPrefix() {
        return ""; //TODO
    }

    /**
     * Saves the watchlist to watchlist.yml. Should be called each time the watchlist is modified.
     */
    public void saveToFile() {
        //YamlBridge yaml = new YamlBridge();
        YamlConfiguration yaml = new YamlConfiguration();
        watchlist.forEach(((name, watchlistPlayerData) -> yaml.set(name,watchlistPlayerData.serialize())));
        yaml.save(dataFile);
    }

    public void updateWatchlist(McmeProxyPlayer player) {
        WatchlistPlayerData nameMatch = getWatchlistData(player.getName());
        if(nameMatch!=null) {

            // Set uuid for watchlist entries that were made without the player being online
            if (nameMatch.isUuidUnknown()) {
                nameMatch.setUuid(player.getUniqueId());

                // Set name to 'unknown##' for watchlist entries when a player with same name but other uuid joins
            } else if (!nameMatch.getUuid().equals(player.getUniqueId())) {
                watchlist.remove(player.getName());
                putWithUnknownName(nameMatch);
            }
        }

        //get a list of watchlist entries with same uuid as joining player
        List<Map.Entry<String,WatchlistPlayerData>> uuidMatches = watchlist.entrySet().stream()
                     .filter(entry -> !entry.getValue().isUuidUnknown() && entry.getValue().getUuid().equals(player.getUniqueId()))
                     .collect(Collectors.toList());
        if(uuidMatches.size()>0) {
            Map.Entry<String,WatchlistPlayerData> firstMatch = uuidMatches.get(0);

            // Set name for watchlist entry after player changed minecraft username
            if(!firstMatch.getKey().equalsIgnoreCase(player.getName())) {
                watchlist.remove(firstMatch.getKey());
                firstMatch.getValue().setNameUnknown(false);
                watchlist.put(player.getName(),firstMatch.getValue());
            }

            //merge entries with same uuid
            for(int i = 1; i< uuidMatches.size(); i++) {
                watchlist.remove(uuidMatches.get(i).getKey());
                firstMatch.getValue().getReasons().addAll(uuidMatches.get(i).getValue().getReasons());
            }
            firstMatch.getValue().getReasons().sort(Comparator.comparing(WatchlistReason::getCreationTime));
        }
        saveToFile();
    }

    private void putWithUnknownName(WatchlistPlayerData data) {
        int i = 0;
        while(watchlist.containsKey("unknownName"+i)) {
            i++;
        }
        data.setNameUnknown(true);
        watchlist.put("unknownName"+i,data);
    }

    public Map<String, WatchlistPlayerData> getWatchlist() {
        return watchlist;
    }

    public WatchlistPlayerData getWatchlistData(String player) {
        return watchlist.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(player))
                                            .map(Map.Entry::getValue).findFirst().orElse(null);
    }

    public void addKnownPlayer(McmeProxyPlayer player) {
        knownPlayers.put(player.getName(),player.getUniqueId());
//for(String name: knownPlayers.keySet()) {
//    Logger.getGlobal().info("Known: "+name+" "+knownPlayers.get(name));
//}
    }

    public boolean isKnown(String name) {
        return knownPlayers.keySet().stream().anyMatch(key -> key.equalsIgnoreCase(name));
    }

    public Map<String, UUID> getKnownPlayers() {
        return knownPlayers;
    }

    public UUID getUUID(String name) {
        return knownPlayers.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(name))
                                               .map(Map.Entry::getValue).findFirst().orElse(null);
    }

    public String getIp(UUID uuid) {
        McmeProxyPlayer player = McmeModeration.getPlugin().getPlayer(uuid);
        if(player != null) {
            SocketAddress address =player.getSocketAddress();
            if(address instanceof InetSocketAddress) {
               InetAddress inetAddress = ((InetSocketAddress)address).getAddress();
               if(inetAddress!=null) {
                   return inetAddress.getHostAddress();
               }
            } //if(address instanceof UnixDomainSocketAddress) {
        }
        return null;
    }

    public void addWatchlist(String addPlayer, McmeCommandSender sender, String reason) {
        String initiator = (sender!=null?sender.getName():"plugin");
        boolean byModerator = sender == null || sender.hasPermission(Permission.ADD_WATCHLIST);
        WatchlistReason watchlistReason = new WatchlistReason(new Date(),reason,initiator,addPlayer,byModerator);
        WatchlistPlayerData data = watchlist.get(addPlayer);
        UUID uuid = getUUID(addPlayer);
        String ip = getIp(uuid);
        if(data != null) {
            data.addReason(watchlistReason);
            data.setIp(ip);
        } else {
            data = new WatchlistPlayerData(uuid,ip,watchlistReason);
            watchlist.put(addPlayer,data);
        }
        saveToFile();
    }

    public void removeWatchlist(String removePlayer) {
        //WatchlistPlayerData playerData = watchlist.get(removePlayer);
        watchlist.remove(removePlayer);
        getWatchedAliases(removePlayer).forEach(alias -> watchlist.remove(getName(alias)));
        saveToFile();
    }

    public void removeWatchlistReason(String player, int i) {
        WatchlistPlayerData data = watchlist.get(player);
        data.getReasons().remove(i);
    }

    public Collection<WatchlistPlayerData> getWatchedAliases(String playerName) {
        //WatchlistPlayerData playerData = watchlist.get(player);
        McmeProxyPlayer player = McmeModeration.getPlugin().getPlayer(playerName);
        if(player!=null) {
            return watchlist.values().stream().filter(watchlistPlayerData -> !watchlistPlayerData.getIp().equals("unknown")
                            && watchlistPlayerData.getIp().equals(getIp(player.getUniqueId())))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public String getName(WatchlistPlayerData playerData) {
        return watchlist.entrySet().stream().filter(entry -> entry.getValue().getUuid().equals(playerData.getUuid()))
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }

    public void processPlayerJoin(McmeProxyPlayer player) {
        McmeModeration.getWatchlistManager().addKnownPlayer(player);

        //handle name changes of players
        McmeModeration.getWatchlistManager().updateWatchlist(player);

        if(McmeModeration.getWatchlistManager().isOnWatchlist(player.getName())) {
            McmeModeration.getPlugin().getTask( () -> {
                Component message = Component.text("Watched player ").color(Style.INFO)
                        .append(Component.text(player.getName()).color(Style.INFO_STRESSED))
                                .append(Component.text(" joined.").color(Style.INFO));

                if (McmeModeration.getConfig().isWatchlistPlayerJoinNotificationIngame()) {
                    McmeModeration.getPlugin().getPlayers().stream()
                            .filter(moderator -> moderator.hasPermission(Permission.SEE_WATCHLIST))
                            .forEach(moderator -> moderator.sendInfo(message));
                }
                if (McmeModeration.getConfig().isWatchlistPlayerJoinNotificationDiscord()) {
                    String discordChannel = McmeModeration.getConfig().getWatchlistDiscordChannel();
                    DiscordUtil.sendDiscord(discordChannel, "Watched player **" + player.getName() + "** joined the server.",
                            McmeModeration.getConfig().isWatchlistPingModerators());
                }
            }).schedule(5, TimeUnit.SECONDS);
        }  else if(McmeModeration.getWatchlistManager().hasWatchedIp(player)) {
            Collection<WatchlistPlayerData> aliases
                    = McmeModeration.getWatchlistManager().getWatchedAliases(player.getName());
            String reason = "Alt of "+ Joiner.on(", ").join(aliases.stream().map(alias -> {
                if(alias.isNameUnknown()) {
                    return alias.getUuid().toString();
                } else {
                    return McmeModeration.getWatchlistManager().getName(alias)+" ("+alias.getUuid().toString()+")";
                }
            }).toArray());
            McmeModeration.getWatchlistManager().addWatchlist(player.getName(),
                    null,
                    reason);
            McmeModeration.getPlugin().getTask( () -> {
                if (McmeModeration.getConfig().isWatchlistPlayerJoinNotificationIngame()) {
                    Component message = Component.text("Player ").color(Style.INFO)
                                .append(Component.text(player.getName()).color(Style.INFO_STRESSED))
                                .append(Component.text(" joined and was put on Watchlist because he's an "+reason).color(Style.INFO));
                    McmeModeration.getPlugin().getPlayers().stream()
                            .filter(moderator -> moderator.hasPermission(Permission.SEE_WATCHLIST))
                            .forEach(moderator -> moderator.sendInfo(message));
                }
                if (McmeModeration.getConfig().isWatchlistPlayerJoinNotificationDiscord()) {
                    String discordChannel = McmeModeration.getConfig().getWatchlistDiscordChannel();
                    DiscordUtil.sendDiscord(discordChannel, "Player **" + player.getName()
                                    + "** joined the server and was put on Watchlist because he's an "+reason,
                            McmeModeration.getConfig().isWatchlistPingModerators());
                }
            }).schedule(5, TimeUnit.SECONDS);
        }
    }
}
