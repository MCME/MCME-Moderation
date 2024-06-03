package com.mcmiddleearth.moderation.bungee;

import com.mcmiddleearth.moderation.core.ModerationCommandSender;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;

public class ModerationCommandSenderBungee implements ModerationCommandSender {

    private final CommandSender sender;

    public ModerationCommandSenderBungee(CommandSender commandSender) {
        sender = commandSender;
        commandSender.getName();
    }

    @Override
    public String getName() {
        return sender.getName();
    }


    @Override
    public boolean hasPermission(String permissionNode) {
        return sender.hasPermission(permissionNode);
    }

    @Override
    public void sendInfo(Component message) {
        Component result = Component.text("[Mod]").color(Style.MOD).append(" ").color(Style.INFO);
        result = result.append(message);
        sender.sendMessage(result);
    }

    @Override
    public void sendError(Component message) {
        Component result = Component.text("[Mod]").color(Style.MOD).append(" ").color(Style.ERROR);
        result = result.append(message);
        sender.sendMessage(result);
    }

    public CommandSender getBungeeCommandSender() {
        return sender;
    }
}
