package com.autoutils.module.automation;

import com.autoutils.config.ModConfig;
import com.autoutils.module.Module;
import com.autoutils.util.ChatUtils;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * Auto-Commands Module for Minecraft 26.2:
 * Dispatches configured commands with a delay upon joining a server/world.
 */
public class AutoCommandModule extends Module {
    private int ticksRemaining = -1;
    private String currentServer = "";
    private boolean executedForSession = false;

    public AutoCommandModule() {
        super("Auto-Commands");
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.get().autoCommandsEnabled;
    }

    @Override
    public void onJoin(String serverAddress) {
        this.currentServer = serverAddress != null ? serverAddress.toLowerCase().trim() : "singleplayer";
        this.ticksRemaining = ModConfig.get().commandDelayTicks;
        this.executedForSession = false;
        ChatUtils.LOGGER.info("[AutoUtils] Joined {}. Scheduled auto-commands in {} ticks.", currentServer, ticksRemaining);
    }

    @Override
    public void onTick(Minecraft client) {
        if (executedForSession || ticksRemaining < 0) {
            return;
        }

        if (client.player == null || client.getConnection() == null) {
            return;
        }

        if (ticksRemaining > 0) {
            ticksRemaining--;
            return;
        }

        executedForSession = true;
        ticksRemaining = -1;

        ModConfig config = ModConfig.get();

        // 1. Dispatch Global Commands
        if (config.globalCommands != null && !config.globalCommands.isEmpty()) {
            for (String cmd : config.globalCommands) {
                ChatUtils.sendCommandOrChat(cmd);
            }
        }

        // 2. Dispatch Server-Specific Profile Commands
        if (config.serverProfiles != null && !config.serverProfiles.isEmpty()) {
            for (var entry : config.serverProfiles.entrySet()) {
                String profileIp = entry.getKey().toLowerCase().trim();
                if (currentServer.contains(profileIp) || profileIp.equals("*")) {
                    List<String> commands = entry.getValue();
                    if (commands != null) {
                        for (String cmd : commands) {
                            ChatUtils.sendCommandOrChat(cmd);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisconnect() {
        ticksRemaining = -1;
        currentServer = "";
        executedForSession = false;
    }
}
