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
package com.mcmiddleearth.moderation.core.command.handler;

import com.mcmiddleearth.base.core.command.McmeCommandSender;
import com.mcmiddleearth.base.core.message.McmeColors;
import com.mcmiddleearth.base.core.message.Message;
import com.mcmiddleearth.base.core.message.MessageDecoration;
import com.mcmiddleearth.moderation.core.McmeModeration;
import com.mcmiddleearth.moderation.core.Permission;
import com.mcmiddleearth.moderation.core.command.argument.OfflinePlayerArgumentType;
import com.mcmiddleearth.moderation.core.command.argument.ReasonArgumentType;
import com.mcmiddleearth.moderation.core.command.builder.HelpfulLiteralBuilder;
import com.mcmiddleearth.moderation.core.command.builder.HelpfulRequiredArgumentBuilder;
import com.mcmiddleearth.moderation.core.util.DiscordUtil;
import com.mojang.brigadier.CommandDispatcher;

/**
 * @author Eriol_Eandur
 */

public class ReportCommandHandler extends AbstractCommandHandler {

    public ReportCommandHandler(String name, String permission, CommandDispatcher<McmeCommandSender> dispatcher) {
        super(name, permission);
        dispatcher
            .register(HelpfulLiteralBuilder.literal(name)
                .withHelpText("Report inappropriate behaviour!")
                .withTooltip("Send a report to Moderators about inappropriate player behaviour.")
                .requires(ModerationCommandSender -> ModerationCommandSender.hasPermission(permission))

                .then(HelpfulRequiredArgumentBuilder.argument("player", new OfflinePlayerArgumentType())
                    .withTooltip("Name of the player who misbehaved.")

                    .then(HelpfulRequiredArgumentBuilder.argument("reason", new ReasonArgumentType())
                        //.withTooltip("Reason of your report. Quickly explain the misbehaviour.")
                        //.suggests((context,suggestionsBuilder) ->
                        //    suggestionsBuilder.suggest("Explain the inappropriate behaviour of "+context.getArgument("player",String.class)).buildFuture())
                        .executes(context -> sendReport(context.getSource(), context.getArgument("player",String.class),
                                                        context.getArgument("reason", String.class))))));
    }

    private int sendReport(McmeCommandSender commandSender, String player, String reason) {
        Message message = McmeModeration.infoMessage()
                .add(commandSender.getName(), McmeColors.INFO_STRESSED)//.bold(true).italic(true)
                .add(" reported player ") //McmeColors.INFO))//.bold(false).italic(false)
                .add(player, McmeColors.INFO_STRESSED, MessageDecoration.BOLD, MessageDecoration.ITALIC)
                .add("\nReason: ")//, MessageDecoration.BOLD, MessageDecoration.ITALIC)
                .add(reason, McmeColors.HELP);//.bold(true).italic(true);
        if(McmeModeration.getConfig().isReportSendIngame()) {
            McmeModeration.getProxy().getPlayers().stream()
                    .filter(moderator -> moderator.hasPermission(Permission.SEE_REPORT))
                    .forEach(moderator -> moderator.sendMessage(message));
        }
        if(McmeModeration.getConfig().isReportAddToWatchlist()) {
            McmeModeration.getWatchlistManager().addWatchlist(player, commandSender, reason);
        }
        if(McmeModeration.getConfig().isReportSendDiscord()) {
            String discordChannel = McmeModeration.getConfig().getReportDiscordChannel();
            DiscordUtil.sendDiscord(discordChannel,"**"+commandSender.getName()+"** reported player **"+player+".**\nReason: **"+reason+"**",
                    McmeModeration.getConfig().isReportPingModerators());
        }
        commandSender.sendMessage(McmeModeration.infoMessage("Your report has been sent to the moderation team."));
        return 0;
    }


}
