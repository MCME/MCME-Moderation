package com.mcmiddleearth.moderation.core.command.handler;

public abstract class AbstractCommandHandler {

    private final String command;

    private final String permission;

    public AbstractCommandHandler(String command, String permission) {
        this.command = command;
        this.permission = permission;
    }

    public String getCommand() {
        return command;
    }

    public String getPermission() {
        return permission;
    }

}
