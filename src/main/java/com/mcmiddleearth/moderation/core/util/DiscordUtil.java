package com.mcmiddleearth.moderation.core.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.moderation.core.ModerationProxy;

public class DiscordUtil {

    @SuppressWarnings("UnstableApiUsage")
    public static void sendDiscord(String discordChannel, String message, boolean pingModerator) {
        if(pingModerator) {
            String tag = "@"+ ModerationProxy.getPlugin().getConfig().getReportDiscordRole();
            message = tag+" "+message;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Discord");
        out.writeUTF(discordChannel);
        out.writeUTF(message);
        ModerationProxy.getInstance().getPlayers().stream().findFirst()
                .ifPresent(other -> other.sendDataToBackend("mcme:connect", out.toByteArray(),true));
    }

}
