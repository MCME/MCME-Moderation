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
package com.mcmiddleearth.moderation.watchlist;

import com.mcmiddleearth.moderation.ModerationPlugin;
import com.mcmiddleearth.moderation.Permission;
import com.mcmiddleearth.moderation.configuration.YamlBridge;
import com.sun.deploy.net.socket.UnixDomainSocket;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Eriol_Eandur
 */

public class WatchlistManager {

    private final Map<String,WatchlistPlayerData> watchlist = new HashMap<>();

    private final File dataFile = new File(ModerationPlugin.getInstance().getDataFolder(),"watchlist.yml");

    private final Map<String,UUID> knownPlayers = new HashMap<>();

    /**
     * Constructor loads data from watchlist.yml
     */
    public WatchlistManager() {
        if(dataFile.exists()) {
            YamlBridge yaml = new YamlBridge();
            yaml.load(dataFile);
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

    public boolean hasWatchedIp(ProxiedPlayer player) {
        return watchlist.values().stream().anyMatch(playerData -> playerData.getIp().equals(getIp(player.getUniqueId())));
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
        YamlBridge yaml = new YamlBridge();
        watchlist.forEach(((name, watchlistPlayerData) -> yaml.set(name,watchlistPlayerData.serialize())));
        yaml.save(dataFile);
    }

    public void updateWatchlist(ProxiedPlayer player) {
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

    public void addKnownPlayer(ProxiedPlayer player) {
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
        SocketAddress address =ProxyServer.getInstance().getPlayer(uuid).getSocketAddress();
        if(address instanceof InetSocketAddress) {
           InetAddress inetAddress = ((InetSocketAddress)address).getAddress();
           if(inetAddress!=null) {
               return inetAddress.getHostAddress();
           } else {
               return null;
           }

        } else {//if(address instanceof UnixDomainSocketAddress) {
            return null;
        }
    }

    public void addWatchlist(String addPlayer, CommandSender commandSender, String reason) {
        String initiator = (commandSender!=null?commandSender.getName():"plugin");
        boolean byModerator = commandSender == null || commandSender.hasPermission(Permission.ADD_WATCHLIST);
        WatchlistReason watchlistReason = new WatchlistReason(new Date(),reason,initiator,addPlayer,byModerator);
        WatchlistPlayerData data = watchlist.get(addPlayer);
        if(data != null) {
            data.addReason(watchlistReason);
        } else {
            UUID uuid = getUUID(addPlayer);
            String ip = getIp(uuid);
            data = new WatchlistPlayerData(uuid,ip,watchlistReason);
            watchlist.put(addPlayer,data);
        }
        saveToFile();
    }

    public void removeWatchlist(String removePlayer) {
        WatchlistPlayerData playerData = watchlist.get(removePlayer);
        watchlist.remove(removePlayer);
        getWatchedAliases(removePlayer).forEach(alias -> watchlist.remove(getName(alias)));
        saveToFile();
    }

    public void removeWatchlistReason(String player, int i) {
        WatchlistPlayerData data = watchlist.get(player);
        data.getReasons().remove(i);
    }

    public Collection<WatchlistPlayerData> getWatchedAliases(String player) {
        WatchlistPlayerData playerData = watchlist.get(player);
        return watchlist.values().stream().filter(watchlistPlayerData -> watchlistPlayerData.getIp().equals(playerData.getIp()))
                .collect(Collectors.toList());
    }

    public String getName(WatchlistPlayerData playerData) {
        return knownPlayers.entrySet().stream().filter(entry -> entry.getValue().equals(playerData.getUuid()))
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }
}
