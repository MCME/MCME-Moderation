package com.mcmiddleearth.moderation.command.node;

import com.mcmiddleearth.moderation.command.argument.HelpfulArgument;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.md_5.bungee.api.CommandSender;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class HelpfulArgumentNode extends ArgumentCommandNode<CommandSender, String> implements HelpfulNode {

    private String helpText;
    private String tooltip;

    public HelpfulArgumentNode(String name, ArgumentType<String> type, Command<CommandSender> command, Predicate<CommandSender> requirement,
                               CommandNode<CommandSender> redirect, RedirectModifier<CommandSender> modifier, boolean forks,
                               SuggestionProvider<CommandSender> customSuggestions, String helpText, String tooltip) {
        super(name, type, command, requirement, redirect, modifier, forks, customSuggestions);
        this.helpText = helpText;
    }

    @Override
    public String getTooltip() {
        return tooltip;
    }

    @Override
    public String getHelpText() {
        return helpText;
    }

    @Override
    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(final CommandContext<CommandSender> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        if(canUse(context.getSource())) {
            if (getCustomSuggestions() == null) {
                if (getType() instanceof HelpfulArgument) {
                    return ((HelpfulArgument) getType()).listSuggestions(context, builder, helpText);
                } else {
                    return getType().listSuggestions(context, builder);
                }
            } else {
                return getCustomSuggestions().getSuggestions(context, builder);
            }
        }
        return Suggestions.empty();
    }

    @Override
    public void addChild(CommandNode<CommandSender> node) {
        super.addChild(node);
        CommandNode<CommandSender> child = getChildren().stream().filter(search -> search.getName().equals(node.getName()))
                .findFirst().orElse(null);
        if(child instanceof HelpfulNode) {
            ((HelpfulNode)child).setHelpText(helpText);
        }
    }

}
