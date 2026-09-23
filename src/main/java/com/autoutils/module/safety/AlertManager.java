package com.autoutils.module.safety;

import com.autoutils.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages on-screen HUD alerts and audio cues for safety triggers in Minecraft 26.2.
 */
public class AlertManager {
    private static final AlertManager INSTANCE = new AlertManager();

    public static class ActiveAlert {
        public final String text;
        public final int color;
        public int remainingTicks;

        public ActiveAlert(String text, int color, int durationTicks) {
            this.text = text;
            this.color = color;
            this.remainingTicks = durationTicks;
        }
    }

    private final List<ActiveAlert> activeAlerts = new ArrayList<>();

    public static AlertManager get() {
        return INSTANCE;
    }

    /**
     * Adds an on-screen visual alert notification.
     */
    public synchronized void addAlert(String message, int color, int durationTicks) {
        activeAlerts.add(new ActiveAlert(message, color, durationTicks));
    }

    /**
     * Plays an audible warning ping to alert the user.
     */
    public void playWarningSound() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && ModConfig.get().playAlertSound) {
            float vol = ModConfig.get().alertSoundVolume;
            client.player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), vol, 1.2f);
        }
    }

    /**
     * Ticked every frame/tick to decay alert durations.
     */
    public synchronized void tick() {
        Iterator<ActiveAlert> iterator = activeAlerts.iterator();
        while (iterator.hasNext()) {
            ActiveAlert alert = iterator.next();
            alert.remainingTicks--;
            if (alert.remainingTicks <= 0) {
                iterator.remove();
            }
        }
    }

    /**
     * Renders active notifications and killswitch banner on the HUD.
     */
    public synchronized void renderHud(GuiGraphicsExtractor extractor, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (client.font == null || !ModConfig.get().renderHudAlerts) {
            return;
        }

        int y = 10;
        int screenWidth = client.getWindow().getGuiScaledWidth();

        // If Killswitch is active, show prominent status top-right
        if (ModConfig.get().globalKillswitch) {
            String killswitchText = "§c[AutoUtils KILLSWITCH ACTIVE]";
            int textWidth = client.font.width(killswitchText);
            extractor.text(client.font, killswitchText, screenWidth - textWidth - 10, y, 0xFF5555, true);
            y += 12;
        }

        // Render queued safety alerts
        for (ActiveAlert alert : activeAlerts) {
            int width = client.font.width(alert.text);
            int x = (screenWidth - width) / 2;
            extractor.text(client.font, alert.text, x, y, alert.color, true);
            y += 12;
        }
    }
}
