package com.autoutils.module.automation;

import com.autoutils.config.ModConfig;
import com.autoutils.module.Module;
import com.autoutils.util.FreeCamUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;

import java.util.Random;

/**
 * Anti-AFK Module for Minecraft 26.3:
 * Executes randomized non-intrusive actions to prevent inactivity kicks.
 */
public class AntiAfkModule extends Module {
    private final Random random = new Random();
    private int nextActionTicks = 100;

    public AntiAfkModule() {
        super("Anti-AFK");
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.get().antiAfkEnabled;
    }

    @Override
    public void onTick(Minecraft client) {
        if (client.player == null || client.level == null || FreeCamUtils.isFreeCamActive(client)) {
            return;
        }

        ModConfig config = ModConfig.get();

        if (nextActionTicks > 0) {
            nextActionTicks--;
            return;
        }

        int min = Math.max(20, config.antiAfkMinDelay);
        int max = Math.max(min + 1, config.antiAfkMaxDelay);
        nextActionTicks = min + random.nextInt(max - min);

        int action = random.nextInt(3);

        switch (action) {
            case 0 -> {
                // Subtle Look Nudge
                if (config.antiAfkRandomLook) {
                    float yawOffset = (random.nextFloat() * 5.0f) - 2.5f;
                    float pitchOffset = (random.nextFloat() * 2.0f) - 1.0f;
                    client.player.setYRot(client.player.getYRot() + yawOffset);
                    client.player.setXRot(Math.max(-90.0f, Math.min(90.0f, client.player.getXRot() + pitchOffset)));
                }
            }
            case 1 -> {
                // Subtle Jump
                if (config.antiAfkJump && client.player.onGround() && !client.player.getAbilities().flying) {
                    client.player.jumpFromGround();
                }
            }
            case 2 -> {
                // Subtle Hand Swing
                if (config.antiAfkSwing) {
                    client.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
                }
            }
        }
    }
}
