package com.mcmiddleearth.moderation.core.command;

import com.google.common.base.Joiner;
import com.mcmiddleearth.base.core.command.McmeCommandSender;
import com.mcmiddleearth.base.core.logger.McmeLogger;
import com.mcmiddleearth.base.core.message.McmeColors;
import com.mcmiddleearth.base.core.message.Message;
import com.mcmiddleearth.base.core.message.MessageHoverEvent;
import com.mcmiddleearth.moderation.core.McmeModeration;
import com.mcmiddleearth.moderation.core.command.node.HelpfulNode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class ModerationPluginCommand {

    private final CommandDispatcher<McmeCommandSender> commandDispatcher;

    public ModerationPluginCommand(CommandDispatcher<McmeCommandSender> commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    public void execute(McmeCommandSender sender, String name, String[] args) {
        try {
            String message = String.format("%s %s", name, Joiner.on(' ').join(args)).trim();
            ParseResults<McmeCommandSender> result = commandDispatcher.parse(message, sender);
            result.getExceptions().entrySet().stream()
                    .findFirst().ifPresent(error -> sender.sendMessage(McmeModeration.errorMessage()
                                                          .add(error.getValue().getMessage())));
            if(result.getExceptions().isEmpty()) {
                if(result.getContext().getNodes().size() > 0
                        && (result.getContext().getCommand()==null
                        || result.getContext().getRange().getEnd() < result.getReader().getString().length())) {
                    //check for possible child nodes to collect suggestions and bake better error message
                    Message helpMessage;
                    boolean help = false;
                    String parsedCommand = "/" + result.getReader().getString()
                            .substring(0, result.getContext().getRange().getEnd());
                    if(result.getReader().getRemaining().trim().equals("help")){
                        helpMessage = McmeModeration.infoMessage().add("Help for command "+parsedCommand+":");
                        help = true;
                    } else {
                        helpMessage = McmeModeration.infoMessage().add("Invalid command syntax.", McmeColors.ERROR);
                    }
                    CommandNode<McmeCommandSender> parsedNode = result.getContext().getNodes().get(result.getContext().getNodes().size() - 1).getNode();
                    Collection<CommandNode<McmeCommandSender>> children = (result.getContext().getNodes().isEmpty()?new ArrayList<>(): parsedNode.getChildren()
                            .stream().filter(node -> node.canUse(result.getContext().getSource())).toList());
                    Map<CommandNode<McmeCommandSender>,String> use = commandDispatcher.getSmartUsage(parsedNode,result.getContext().getSource());
                    if (children.isEmpty()) {
                        if (result.getContext().getCommand() == null) {
                            helpMessage.add(" Maybe you don't have permission.");
                        } else if(!help) {
                            helpMessage.add(" Maybe you want to do:"+"\n")
                                       .add(parsedCommand);
                        }
                    } else {
                        if(!help) {
                            helpMessage.add(" Maybe you want to do:");
                        }
                        for(Map.Entry<CommandNode<McmeCommandSender>,String> entry: use.entrySet()) {
                            String usageMessage = "";
                            helpMessage.add("\n");
                            String[] visitedNodes = parsedCommand.split(" ");
                            Iterator<ParsedCommandNode<McmeCommandSender>> iterator = result.getContext().getNodes().listIterator();
                            for (String visitedNode : visitedNodes) {

                                ParsedCommandNode<McmeCommandSender> node = iterator.next();
                                Message nodeMessage = McmeModeration.getPlugin().createMessage().add(" "+visitedNode,
                                                (node.getNode() instanceof LiteralCommandNode ?McmeColors.LITERAL:McmeColors.ARGUMENT));
                                if ((node.getNode() instanceof HelpfulNode)) {
                                    nodeMessage = nodeMessage.addHover(new MessageHoverEvent(MessageHoverEvent.Action.TEXT,
                                            McmeModeration.getPlugin().createMessage()
                                            .add(((HelpfulNode) node.getNode()).getTooltip(), McmeColors.TOOLTIP)));
                                    if(!((HelpfulNode) node.getNode()).getHelpText().equals("")) {
                                        usageMessage = ((HelpfulNode) node.getNode()).getHelpText();
                                    }
                                }/* else {
                                    helpMessage = helpMessage.hoverEvent(null);
                                }*/
                                helpMessage.add(nodeMessage);
                            }
                            String[] possibleNodes = entry.getValue().replace('|', ' ').split(" ");
                            CommandNode<McmeCommandSender> node = parsedNode;
                            CommandNode<McmeCommandSender> lastNode = parsedNode;
                            for(String possibleNode: possibleNodes) {
//Logger.getLogger(ModerationPluginCommandBungee.class.getSimpleName()).info("possible node "+possibleNode);
                                CommandNode<McmeCommandSender> temp = node;
                                node = findDirectChild(node, possibleNode.replaceAll("[()\\[\\]<>]",""));
                                if(node==null) {
                                    node = findDirectChild(lastNode, possibleNode.replaceAll("[()\\[\\]<>]",""));
                                } else {
                                    lastNode = temp;
                                }
//Logger.getGlobal().info("possible Node:");
                                Message nodeMessage = McmeModeration.getPlugin().createMessage().add(" "+possibleNode,
                                                (node instanceof LiteralCommandNode?McmeColors.LITERAL:McmeColors.ARGUMENT));
                                if ((node instanceof HelpfulNode)) {
                                    nodeMessage.addHover(new MessageHoverEvent(MessageHoverEvent.Action.TEXT,
                                            McmeModeration.getPlugin().createMessage()
                                                    .add(((HelpfulNode) node).getTooltip(), McmeColors.TOOLTIP)));
                                    if(!((HelpfulNode) node).getHelpText().equals("")) {
                                        usageMessage = ((HelpfulNode) node).getHelpText();
                                    }
                                }/* else {
                                    helpMessage = helpMessage.hoverEvent(null);
                                }*/
                                helpMessage.add(nodeMessage);
                            }
                            if(!usageMessage.equals("")) {
                                Message hoverMessage = McmeModeration.getPlugin().createMessage()
                                        .add(" : "+usageMessage, McmeColors.HELP);
                                        /*.addHover(new MessageHoverEvent(MessageHoverEvent.Action.TEXT,
                                                                        McmeModeration.getPlugin().createMessage()
                                                                        .add(usageTooltip, McmeColors.TOOLTIP)));*/
                                helpMessage.add(hoverMessage);
                            }
                        }
                    }
                    sender.sendMessage(helpMessage);
                } else if(result.getContext().getCommand() == null) {
                    sender.sendMessage(McmeModeration.errorMessage().add("Invalid command. Maybe you don't have permission."));
                } else {
                    commandDispatcher.execute(result);
                }
            }
        } catch (CommandSyntaxException e) {
            sender.sendMessage(McmeModeration.errorMessage().add("Internal command parser exception!"));
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
            McmeModeration.getPlugin().getMcmeLogger().error("Command tab complete error.",e);
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
        McmeLogger log = McmeModeration.getPlugin().getMcmeLogger();
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
