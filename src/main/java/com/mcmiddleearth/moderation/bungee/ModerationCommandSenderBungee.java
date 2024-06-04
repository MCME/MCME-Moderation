package com.mcmiddleearth.moderation.bungee;

import com.mcmiddleearth.moderation.core.ModerationCommandSender;
import com.mcmiddleearth.moderation.core.ModerationProxy;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ModerationCommandSenderBungee implements ModerationCommandSender {

    private final CommandSender sender;

    private final Audience audience;

    public ModerationCommandSenderBungee(CommandSender commandSender) {
        sender = commandSender;
        commandSender.getName();
        if(commandSender instanceof ProxiedPlayer player) {
            audience = ((BungeeAudiences)ModerationProxy.getPlugin().getAdventure()).player(player);
        } else {
            audience = ModerationProxy.getPlugin().getAdventure().console();
        }
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public Audience getAudience() {
        return audience;
    }

    @Override
    public boolean hasPermission(String permissionNode) {
        return sender.hasPermission(permissionNode);
    }

    public CommandSender getBungeeCommandSender() {
        return sender;
    }
}
