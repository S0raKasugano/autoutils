package com.autoutils.module.automation;

import com.autoutils.config.ModConfig;
import com.autoutils.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

/**
 * Auto-Rejoin Module for Minecraft 26.2:
 * Coordinates reconnection attempts after disconnects or kicks.
 */
public class AutoRejoinModule extends Module {
    private static AutoRejoinModule INSTANCE;

    private ServerData lastServerData = null;
    private int attemptCount = 0;
    private boolean isCountdownActive = false;
    private int countdownTicksRemaining = 0;
    private boolean cancelled = false;

    public AutoRejoinModule() {
        super("Auto-Rejoin");
        INSTANCE = this;
    }

    public static AutoRejoinModule get() {
        return INSTANCE;
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.get().autoRejoinEnabled;
    }

    @Override
    public void onJoin(String serverAddress) {
        Minecraft client = Minecraft.getInstance();
        if (client.getCurrentServer() != null) {
            this.lastServerData = client.getCurrentServer();
        }
        this.attemptCount = 0;
        this.cancelled = false;
        this.isCountdownActive = false;
    }

    public void onDisconnectedScreenInit() {
        ModConfig config = ModConfig.get();
        if (!isEnabled() || config.globalKillswitch || lastServerData == null || cancelled) {
            isCountdownActive = false;
            return;
        }

        if (attemptCount >= config.maxRejoinAttempts) {
            isCountdownActive = false;
            return;
        }

        isCountdownActive = true;
        countdownTicksRemaining = config.rejoinDelaySeconds * 20;
    }

    public void tickCountdown(Screen parentScreen) {
        if (!isCountdownActive || cancelled) {
            return;
        }

        if (countdownTicksRemaining > 0) {
            countdownTicksRemaining--;
            return;
        }

        isCountdownActive = false;
        attemptCount++;

        Minecraft client = Minecraft.getInstance();
        if (lastServerData != null && client != null) {
            ServerAddress address = ServerAddress.parseString(lastServerData.ip);
            ConnectScreen.startConnecting(parentScreen, client, address, lastServerData, false, null);
        }
    }

    public void cancel() {
        this.cancelled = true;
        this.isCountdownActive = false;
    }

    public boolean isCountdownActive() {
        return isCountdownActive && !cancelled;
    }

    public int getSecondsRemaining() {
        return (int) Math.ceil(countdownTicksRemaining / 20.0);
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public ServerData getLastServerData() {
        return lastServerData;
    }
}
