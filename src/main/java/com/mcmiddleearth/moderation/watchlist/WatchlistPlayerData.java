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

import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Eriol_Eandur
 */

public class WatchlistPlayerData {

    private static final UUID unknownUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private boolean nameUnknown = false;

    private UUID uuid;

    private List<WatchlistReason> reasons = new ArrayList<>();

    public WatchlistPlayerData(UUID uuid, WatchlistReason reason) {
        this.uuid = (uuid != null ? uuid : unknownUuid);
        reasons.add(reason);
    }

    /**
     * Constructor to read data from watchlist.yml
     *
     * @param data from the watchlist.yml
     */
    public WatchlistPlayerData(Map<String, Object> data) {
        uuid = UUID.fromString((String)data.get("uuid"));
        nameUnknown = (boolean) data.get("nameUnknown");
        List<Map<String,Object>> reasonData = (List<Map<String,Object>>) data.get("reasons");
        reasonData.forEach(reason -> {
            try {
                reasons.add(new WatchlistReason(reason));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    public List<WatchlistReason> getReasons() {
        return reasons;
    }

    public UUID getUuid() {
        return (uuid.equals(unknownUuid) ? null : uuid);
    }

    public boolean isUuidUnknown() {
        return uuid.equals(unknownUuid);
    }

    public void setUuid(UUID uuid) {
        if (this.uuid.equals(unknownUuid) && uuid != null) {
            this.uuid = uuid;
        }
    }

    public boolean isNameUnknown() { return nameUnknown; }

    public void setNameUnknown(boolean nameUnknown) {
        this.nameUnknown = nameUnknown;
    }

    /**
     * Required to save data to watchlist.yml
     *
     * @return
     */
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("uuid", uuid.toString());
        List<Map<String, Object>> reasonData = new ArrayList<>();
        reasons.forEach(reason -> reasonData.add(reason.serialize()));
        result.put("reasons", reasonData);
        result.put("nameUnknown",nameUnknown);
        return result;
    }

    public void addReason(WatchlistReason watchlistReason) {
        reasons.add(watchlistReason);
    }
}
