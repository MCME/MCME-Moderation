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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Eriol_Eandur
 */

public class WatchlistReason {

    private final Date creationTime;

    private final String description;

    private final String initiator;

    private final boolean byModerator;

    private final String nameAtCreationTime;

    private final static String dateFormatPattern = "EEE, MMM d, yy, h:mm:ss a";

    /**
     * Constructor for new WatchlistReasons
     * @param creationTime
     * @param description
     * @param initiator
     * @param byModerator
     */
    public WatchlistReason(Date creationTime, String description, String initiator, String nameAtCreationTime, boolean byModerator) {
        this.creationTime = creationTime;
        this.description = description;
        this.initiator = initiator;
        this.byModerator = byModerator;
        this.nameAtCreationTime = nameAtCreationTime;
    }

    /**
     * Constructor to load data from watchlist.yml
     * @param data read from watchlist.yml by YamlBridge class
     * @throws ParseException thrown when date in watchlist.yml is not readable
     */
    public WatchlistReason(Map<String,Object> data) throws ParseException {
        this(DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.DEFAULT,Locale.US).parse((String) data.get("creationTime")),
                (String) data.get("description"), (String) data.get("initiator"), (String) data.get("nameAtCreationTime"),
                (boolean) data.get("byModerator"));
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public String getDescription() {
        return description;
    }

    public String getInitiator() {
        return initiator;
    }

    public String getNameAtCreationTime() { return nameAtCreationTime; }

    public boolean isByModerator() {
        return byModerator;
    }

    /**
     * Required to save data to watchlist.yml
     * @return
     */
    public Map<String,Object> serialize() {
        Map<String,Object> result = new HashMap<>();
        result.put("creationTime", DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.DEFAULT,Locale.US).format(creationTime));
        result.put("description", description);
        result.put("initiator",initiator);
        result.put("nameAtCreationTime",nameAtCreationTime);
        result.put("byModerator", byModerator);
        return result;
    }
}
