package com.mcmiddleearth.moderation.core.command;

import com.google.common.base.Joiner;
import com.mcmiddleearth.base.core.command.McmeCommandSender;
import com.mcmiddleearth.moderation.bungee.ModerationPluginBungee;
import com.mcmiddleearth.moderation.core.Style;
import com.mcmiddleearth.moderation.core.command.node.HelpfulNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ModerationPluginCommand {

    private final CommandDispatcher<McmeCommandSender> commandDispatcher;

    public ModerationPluginCommand(CommandDispatcher<McmeCommandSender> commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    public void execute(McmeCommandSender sender, String name, String[] args) {
        try {
//CommandNode<ModerationCommandSender> node = dispatcher.getRoot();
//printTree(node);
            String message = String.format("%s %s", name, Joiner.on(' ').join(args)).trim();
            ParseResults<McmeCommandSender> result = commandDispatcher.parse(message, sender);
//Logger.getGlobal().info("nodes "+result.getContext().getNodes().size());
//Logger.getGlobal().info("Parsed");
            result.getExceptions().entrySet().stream()
                    .findFirst().ifPresent(error -> sender.sendError(Component.text(error.getValue().getMessage())
                            .color(Style.ERROR)));
            if(result.getExceptions().isEmpty()) {
                if(result.getContext().getNodes().size() > 0
                        && (result.getContext().getCommand()==null
                        || result.getContext().getRange().getEnd() < result.getReader().getString().length())) {
                    //check for possible child nodes to collect suggestions and bake better error message
                    TextComponent helpMessage;
                    boolean help = false;
                    String parsedCommand = "/" + result.getReader().getString()
                            .substring(0, result.getContext().getRange().getEnd());
                    if(result.getReader().getRemaining().trim().equals("help")){
                        helpMessage = Component.text("Help for command "+parsedCommand+":").color(Style.INFO);
                        help = true;
                    } else {
                        helpMessage = Component.text("Invalid command syntax.").color(Style.ERROR);
                    }
                    CommandNode<McmeCommandSender> parsedNode = result.getContext().getNodes().get(result.getContext().getNodes().size() - 1).getNode();
//Logger.getGlobal().info("Parsed Node:");
//printTree(parsedNode);
                    Collection<CommandNode<McmeCommandSender>> children = (result.getContext().getNodes().isEmpty()?new ArrayList<>(): parsedNode.getChildren()
                            .stream().filter(node -> node.canUse(result.getContext().getSource())).toList());
                    Map<CommandNode<McmeCommandSender>,String> use = commandDispatcher.getSmartUsage(parsedNode,result.getContext().getSource());
                    if (children.isEmpty()) {
                        if (result.getContext().getCommand() == null) {
                            helpMessage = helpMessage.append(Component.text(" Maybe you don't have permission."));
                        } else if(!help) {
                            helpMessage = helpMessage.append(Component.text(" Maybe you want to do:\n")).append(Component.text(parsedCommand).color(Style.INFO));
                        }
                    } else {
                        if(!help) {
                            helpMessage = helpMessage.append(Component.text(" Maybe you want to do:"));
                        }
                        for(Map.Entry<CommandNode<McmeCommandSender>,String> entry: use.entrySet()) {
                            String usageMessage = "";
                            helpMessage = helpMessage.append(Component.text("\n").color(Style.INFO));
                            String[] visitedNodes = parsedCommand.split(" ");
                            Iterator<ParsedCommandNode<McmeCommandSender>> iterator = result.getContext().getNodes().listIterator();
                            for (String visitedNode : visitedNodes) {
                                helpMessage = helpMessage.append(Component.text(" "+visitedNode));
                                ParsedCommandNode<McmeCommandSender> node = iterator.next();
//Logger.getGlobal().info("Visited Node:");
//printTree(node.getNode());
                                helpMessage = helpMessage.color((node.getNode() instanceof LiteralCommandNode ?Style.LITERAL:Style.ARGUMENT));
                                if ((node.getNode() instanceof HelpfulNode)) {
                                    helpMessage = helpMessage.hoverEvent(HoverEvent
                                            .showText(Component.text(((HelpfulNode) node.getNode()).getTooltip()).color(Style.TOOLTIP)));
                                    if(!((HelpfulNode) node.getNode()).getHelpText().equals("")) {
                                        usageMessage = ((HelpfulNode) node.getNode()).getHelpText();
                                    }
                                } else {
                                    helpMessage = helpMessage.hoverEvent(null);
                                }
                            }
                            String[] possibleNodes = entry.getValue().replace('|', ' ').split(" ");
                            CommandNode<McmeCommandSender> node = parsedNode;
                            CommandNode<McmeCommandSender> lastNode = parsedNode;
                            for(String possibleNode: possibleNodes) {
//Logger.getLogger(ModerationPluginCommandBungee.class.getSimpleName()).info("possible node "+possibleNode);
                                helpMessage = helpMessage.append(Component.text(" "+possibleNode));
                                CommandNode<McmeCommandSender> temp = node;
                                node = findDirectChild(node, possibleNode.replaceAll("[()\\[\\]<>]",""));
                                if(node==null) {
                                    node = findDirectChild(lastNode, possibleNode.replaceAll("[()\\[\\]<>]",""));
                                } else {
                                    lastNode = temp;
                                }
//Logger.getGlobal().info("possible Node:");
//printTree(node);
                                helpMessage = helpMessage.color((node instanceof LiteralCommandNode?Style.LITERAL:Style.ARGUMENT));
                                if ((node instanceof HelpfulNode)) {
                                    helpMessage = helpMessage.hoverEvent(HoverEvent
                                            .showText(Component.text(((HelpfulNode) node).getTooltip()).color(Style.TOOLTIP)));
                                    if(!((HelpfulNode) node).getHelpText().equals("")) {
                                        usageMessage = ((HelpfulNode) node).getHelpText();
                                    }
                                } else {
                                    helpMessage = helpMessage.hoverEvent(null);
                                }
                            }
                            if(!usageMessage.equals("")) {
                                helpMessage = helpMessage.append(Component.text(" : "+usageMessage).color(Style.HELP))
                                        .hoverEvent(HoverEvent.showText(Component.text(" ")));
                            }
                        }
                    }
                    sender.sendInfo(helpMessage);
                } else if(result.getContext().getCommand() == null) {
                    sender.sendError(Component.text("Invalid command. Maybe you don't have permission.")
                            .color(NamedTextColor.RED));
                } else {
                    commandDispatcher.execute(result);
                }
            }
        } catch (CommandSyntaxException e) {
            sender.sendError(Component.text("Internal command parser exception!")
                    .color(NamedTextColor.RED));
        }
    }

    public List<String> getSuggestions(McmeCommandSender sender, String cursor) {
        try {
            ParseResults<McmeCommandSender> result = commandDispatcher.parse(cursor,sender);
            if(result.getContext().getNodes().isEmpty()) {
                return Collections.emptyList();
            }
            return commandDispatcher.getCompletionSuggestions(result).get().getList().stream().map(Suggestion::getText).toList();
        } catch (InterruptedException | ExecutionException e) {
            Logger.getLogger(ModerationPluginBungee.class.getSimpleName()).log(Level.WARNING,"Command tab complete error.",e);
        }
        return Collections.emptyList();
    }

    private CommandNode<McmeCommandSender> findDirectChild(CommandNode<McmeCommandSender> root, String name) {
//Logger.getLogger(ModerationPluginCommandBungee.class.getSimpleName()).info("find node "+name);
        //if(root.getName().equals(name)) {
        //    return root;
        //} else {
        for(CommandNode<McmeCommandSender> node: root.getChildren()) {
            //CommandNode<ModerationCommandSender> found = findNode(node,name);
            if(node.getName().equals(name)) {//found != null) {
                return node;//found;
            }
        }
        //}
        return null;
    }

    private void printTree(CommandNode<McmeCommandSender> node) {
        Logger log = Logger.getLogger(ModerationPluginBungee.class.getSimpleName());
        log.info(printNode(node, "", "  "));
    }

    private String printNode(CommandNode<McmeCommandSender> node, String message, String indentation) {
        message = message + indentation+node.getClass().getSimpleName()+" "+node.getName()
                +"\n"+indentation+"-use: "+node.getUsageText();
        if(node instanceof HelpfulNode) {
            message = message
                    +"\n"+indentation+"-help: "+((HelpfulNode)node).getHelpText()
                    +"\n"+indentation+"-tool: "+((HelpfulNode)node).getTooltip();
        }
        for(CommandNode<McmeCommandSender> child: node.getChildren()) {
            message = printNode(child,message+"\n",indentation+"    ");
        }
        return message;
    }

}
