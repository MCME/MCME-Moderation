package com.mcmiddleearth.moderation.velocity;

import com.mcmiddleearth.moderation.core.ModerationCommandSender;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;

public class ModerationCommandSenderVelocity implements ModerationCommandSender {

    private CommandSource source;

    @Override
    public boolean hasPermission(String permissionNode) {
        return source.hasPermission(permissionNode);
    }


    @Override
    public void sendInfo(Component message) {

    }

    @Override
    public void sendError(Component message) {

    }
}
