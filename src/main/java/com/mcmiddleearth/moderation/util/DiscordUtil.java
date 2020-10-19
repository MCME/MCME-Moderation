package com.mcmiddleearth.moderation.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.moderation.ModerationPlugin;
import net.md_5.bungee.api.ProxyServer;

public class DiscordUtil {

    @SuppressWarnings("UnstableApiUsage")
    public static void sendDiscord(String message) {
        String discordChannel = ModerationPlugin.getConfig().getReportDiscordChannel();
        if(ModerationPlugin.getConfig().isReportPingModerators()) {
            String tag = "@"+ModerationPlugin.getConfig().getReportDiscordRole();
            message = tag+" "+message;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Discord");
        out.writeUTF(discordChannel);
        out.writeUTF(message);
        ProxyServer.getInstance().getPlayers().stream().findFirst()
                .ifPresent(other -> other.getServer().getInfo().sendData("mcme:connect", out.toByteArray(),false));
    }

}
