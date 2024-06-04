package com.mcmiddleearth.moderation.velocity;

import com.mcmiddleearth.moderation.core.ModerationCommandSender;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;

public class ModerationCommandSenderVelocity implements ModerationCommandSender {

    private final CommandSource source;

    public ModerationCommandSenderVelocity(CommandSource source) {
        this.source = source;
    }

    @Override
    public boolean hasPermission(String permissionNode) {
        return source.hasPermission(permissionNode);
    }

    @Override
    public String getName() {
        return source.pointers().getOrDefault(Identity.NAME,"Console");
    }

    @Override
    public Audience getAudience() {
        return source;
    }
}
