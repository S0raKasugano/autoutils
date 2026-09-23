package com.autoutils.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Base abstraction for all client-side automation and safety modules in Minecraft 26.2.
 */
public abstract class Module {
    private final String name;

    public Module(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Checked on every tick. If false or global killswitch is active, module is skipped.
     */
    public abstract boolean isEnabled();

    /**
     * Invoked at the end of each client tick (20 times per second).
     */
    public void onTick(Minecraft client) {}

    /**
     * Invoked when client connects to a server or joins a singleplayer world.
     */
    public void onJoin(String serverAddress) {}

    /**
     * Invoked when client disconnects from the current server or leaves the world.
     */
    public void onDisconnect() {}

    /**
     * Invoked during in-game HUD rendering for overlay text and warnings.
     */
    public void onRenderHud(GuiGraphicsExtractor extractor, float tickDelta) {}
}
