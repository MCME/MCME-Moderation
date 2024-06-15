package com.mcmiddleearth.moderation.bungee;

import com.mcmiddleearth.moderation.core.ModerationCommandSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.CommandSender;

public class ModerationCommandSenderBungee implements ModerationCommandSender {

    private final CommandSender sender;

    public ModerationCommandSenderBungee(CommandSender commandSender) {
        sender = commandSender;
        commandSender.getName();
        /*if(commandSender instanceof ProxiedPlayer player) {
            audience = ((ModerationPluginBungee)ModerationProxy.getPlugin()).getAdventure().player(player);
        } else {
            audience = ((ModerationPluginBungee)ModerationProxy.getPlugin()).getAdventure().console();
        }*/
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public boolean hasPermission(String permissionNode) {
        return sender.hasPermission(permissionNode);
    }

    public CommandSender getBungeeCommandSender() {
        return sender;
    }

    @Override
    public void sendMessage(Component message) {
        sender.sendMessage(BungeeComponentSerializer.get().serialize(message));
    }

}
