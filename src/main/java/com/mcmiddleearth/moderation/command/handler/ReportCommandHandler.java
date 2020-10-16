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
package com.mcmiddleearth.moderation.command.handler;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcmiddleearth.moderation.ModerationPlugin;
import com.mcmiddleearth.moderation.Permission;
import com.mcmiddleearth.moderation.command.argument.PlayerArgumentType;
import com.mcmiddleearth.moderation.command.builder.HelpfulLiteralBuilder;
import com.mcmiddleearth.moderation.command.builder.HelpfulRequiredArgumentBuilder;
import com.mojang.brigadier.CommandDispatcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

/**
 * @author Eriol_Eandur
 */

public class ReportCommandHandler extends AbstractCommandHandler {

    public ReportCommandHandler(String name, CommandDispatcher<CommandSender> dispatcher) {
        super(name);
        dispatcher.register(HelpfulLiteralBuilder
            .literal(name)
                .withHelpText("Report inappropriate behaviour.")
                .withTooltip("Send a report to Moderators about inappropriate player behaviour.")
                .requires(commandSender -> commandSender.hasPermission(Permission.SEND_REPORT))
                .then(HelpfulRequiredArgumentBuilder.argument("player", new PlayerArgumentType())
                    .withTooltip("Name of the player who misbehaved.")
                    .then(HelpfulRequiredArgumentBuilder.argument("reason", greedyString())
                        .withTooltip("Reason of your report. Quickly explain the misbehaviour.")
                        .suggests((context,suggestionsBuilder) ->
                            suggestionsBuilder.suggest("Explain the inappropriate behaviour of "+context.getArgument("player",String.class)).buildFuture())
                        .executes(context -> sendReport(context.getSource(), context.getArgument("player",String.class),
                                                        context.getArgument("reason", String.class)))))
        );
    }

    private int sendReport(CommandSender commandSender, String player, String reason) {
        BaseComponent[] message = new ComponentBuilder(commandSender.getName()).color(ChatColor.YELLOW).bold(true).italic(true)
                .append(" reported player ").color(ChatColor.GOLD).bold(false).italic(false)
                .append(player).color(ChatColor.YELLOW).bold(true).italic(true)
                .append("\nReason: ").color(ChatColor.GOLD).bold(false).italic(false)
                .append(reason).color(ChatColor.YELLOW).bold(true).italic(true).create();
        if(ModerationPlugin.getConfig().isReportSendIngame()) {
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(moderator -> moderator.hasPermission(Permission.SEE_REPORT))
                    .forEach(moderator -> moderator.sendMessage(message));
        }
        if(ModerationPlugin.getConfig().isReportSendDiscord()) {
            sendDiscord("**"+commandSender.getName()+"** reported player **"+player+".**\nReason: **"+reason+"**");
        }
        commandSender.sendMessage(new ComponentBuilder("Your report has been sent to the moderation team.")
                                                       .color(ChatColor.GOLD).create());
        return 0;
    }

    @SuppressWarnings("UnstableApiUsage")
    private void sendDiscord(String message) {
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
