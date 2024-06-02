package com.mcmiddleearth.moderation.command.argument;

public interface HelpfulArgumentType {

    //<S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder, String tooltip);
    void setTooltip(String tooltip);

    default String getTooltip() {
        return null;
    }
}
