package com.autoutils.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility helper for chat notifications, logger output, and dispatching client/server commands.
 */
public final class ChatUtils {
    public static final Logger LOGGER = LoggerFactory.getLogger("AutoUtils");
    public static final String PREFIX = "§8[§bAutoUtils§8]§r ";

    private ChatUtils() {}

    /**
     * Prints an in-game chat message visible only to the local client.
     */
    public static void message(String text) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal(PREFIX + text));
        }
    }

    /**
     * Prints an error/warning message in red.
     */
    public static void warn(String text) {
        message("§c" + text);
    }

    /**
     * Prints a success message in green.
     */
    public static void success(String text) {
        message("§a" + text);
    }

    /**
     * Prints an informational message in aqua/white.
     */
    public static void info(String text) {
        message("§b" + text);
    }

    /**
     * Dispatches a command or chat message.
     * If the string starts with '/', it sends a command packet without the slash.
     * Otherwise, sends a standard chat message.
     */
    public static void sendCommandOrChat(String commandOrMessage) {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null || client.player == null) {
            return;
        }

        String trimmed = commandOrMessage.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        if (trimmed.startsWith("/")) {
            String cmd = trimmed.substring(1);
            LOGGER.info("[AutoUtils] Dispatching command: /{}", cmd);
            client.getConnection().sendCommand(cmd);
        } else {
            LOGGER.info("[AutoUtils] Dispatching chat message: {}", trimmed);
            client.getConnection().sendChat(trimmed);
        }
    }
}
