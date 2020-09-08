package com.mcmiddleearth.moderation.command;

import com.google.common.base.Joiner;
import com.mcmiddleearth.moderation.ModerationPlugin;
import com.mcmiddleearth.moderation.command.handler.AbstractCommandHandler;
import com.mcmiddleearth.moderation.command.node.HelpfulNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ModerationPluginCommand extends Command {

    private CommandDispatcher<CommandSender> commandDispatcher;

    public ModerationPluginCommand(CommandDispatcher<CommandSender> commandDispatcher, AbstractCommandHandler handler) {
        super(handler.getCommand());
        this.commandDispatcher = commandDispatcher;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            String message = String.format("%s %s", getName(), Joiner.on(' ').join(args)).trim();
//Logger.getGlobal().info(message);
            ParseResults<CommandSender> result = commandDispatcher.parse(message, sender);
            Logger.getGlobal().info("Parsed");
            result.getExceptions().entrySet().stream()
                    .findFirst().ifPresent(error -> sender.sendMessage(new ComponentBuilder(error.getValue().getMessage())
                    .color(ChatColor.RED).create()));
            if(result.getExceptions().isEmpty()) {
                if(result.getContext().getCommand()==null || result.getContext().getRange().getEnd() < result.getReader().getString().length()) {
                    //check for possible child nodes to collect suggestions and bake better error message
                    ComponentBuilder helpMessage;
                    boolean help = false;
                    String parsedCommand = "/" + result.getReader().getString()
                            .substring(0, result.getContext().getRange().getEnd());
                    if(result.getReader().getRemaining().trim().equals("help")){
                        helpMessage = new ComponentBuilder("Help for command "+parsedCommand+":").color(ChatColor.GREEN);
                        help = true;
                    } else {
                        helpMessage = new ComponentBuilder("Invalid command.").color(ChatColor.RED);
                    }
                    CommandNode<CommandSender> parsedNode = result.getContext().getNodes().get(result.getContext().getNodes().size() - 1).getNode();
                    Collection<CommandNode<CommandSender>> children = (result.getContext().getNodes().isEmpty()?new ArrayList<>():parsedNode.getChildren()
                            .stream().filter(node -> node.canUse(result.getContext().getSource())).collect(Collectors.toList()));
                    Map<CommandNode<CommandSender>,String> use = commandDispatcher.getSmartUsage(parsedNode,result.getContext().getSource());
                    if (children.isEmpty()) {
                        if (result.getContext().getCommand() == null) {
                            helpMessage.append(" Maybe you don't have permission.");
                        } else if(!help) {
                            helpMessage.append(" Maybe you want to do:\n" + parsedCommand);
                        }
                    } else {
                        if(!help) {
                            helpMessage.append(" Maybe you want to do:");
                        }
                        /*for (CommandNode<CommandSender> child : children) {
                            helpMessage.append("\n").append(parsedCommand).append(" ")
                                       .append(child.getUsageText())
                                       .append((child instanceof HelpfulNode?": "+((HelpfulNode)child).getHelpText():""));
                        }*/
                        for(Map.Entry<CommandNode<CommandSender>,String> entry: use.entrySet()) {
                            helpMessage.append("\n");//+parsedCommand+" "+entry.getValue());
//Logger.getGlobal().info("NOde: "+entry.getKey().getName()+" "+entry.getKey().getClass().getSimpleName() + " "
  //                          +((HelpfulNode)entry.getKey()).getHelpText());
                            String[] visitedNodes = parsedCommand.split(" ");
                            Iterator<ParsedCommandNode<CommandSender>> iterator = result.getContext().getNodes().listIterator();
                            for (String visitedNode : visitedNodes) {
                                helpMessage.append(visitedNode);
                                ParsedCommandNode<CommandSender> node = iterator.next();
                                if ((node.getNode() instanceof HelpfulNode) && !((HelpfulNode) node.getNode()).getTooltip().equals("")) {
                                    helpMessage.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new Text(new ComponentBuilder(((HelpfulNode) node.getNode()).getTooltip())
                                                    .color(ChatColor.YELLOW).create())));
                                }
                            }
                            String[] possibleNodes = entry.getValue().replace('|', ' ').split(" ");
                            for(String possibleNode: possibleNodes) {
                                helpMessage.append(possibleNode);
                                CommandNode<CommandSender> node = findNode(possibleNode.replace("\\(|\\)|\\[|\\]",""));
                                if ((node instanceof HelpfulNode) && !((HelpfulNode) node).getTooltip().equals("")) {
                                    helpMessage.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new Text(new ComponentBuilder(((HelpfulNode) node).getTooltip())
                                                    .color(ChatColor.YELLOW).create())));
                                }
                            }
                            if(entry.getKey() instanceof HelpfulNode) {
                                helpMessage.append(" : "+((HelpfulNode) entry.getKey()).getHelpText());
                                helpMessage.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new Text(new ComponentBuilder(((HelpfulNode) entry.getKey()).getHelpText())
                                                .color(ChatColor.YELLOW).create())));
                            }
                        }
                    }
                    sender.sendMessage(helpMessage.create());
                } else if(result.getContext().getCommand() == null) {
                    sender.sendMessage(new ComponentBuilder("Invalid command. Maybe you don't have permission.")
                            .color(ChatColor.RED).create());
                } else {
                    commandDispatcher.execute(result);
                }
            }
        } catch (CommandSyntaxException e) {
            sender.sendMessage(new ComponentBuilder("Internal command parser exception!")
                    .color(ChatColor.RED).create());
        }
    }

    public void onTabComplete(TabCompleteEvent event) {
        if (event.getSender() instanceof CommandSender) {
            try {
                CompletableFuture<Suggestions> completionSuggestions
                        = commandDispatcher.getCompletionSuggestions(commandDispatcher.parse(event.getCursor().substring(1), (ProxiedPlayer) event.getSender()));
                event.getSuggestions().addAll(completionSuggestions.get().getList().stream().map(Suggestion::getText).collect(Collectors.toList()));
            } catch (InterruptedException | ExecutionException e) {
                Logger.getLogger(ModerationPlugin.class.getSimpleName()).log(Level.WARNING,"Command tab complete error.",e);
            }
        }
    }

}
