package com.autoutils.module.safety;

import com.autoutils.config.ModConfig;
import com.autoutils.module.Module;
import com.autoutils.util.ChatUtils;
import com.autoutils.util.FreeCamUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;

/**
 * Proximity Disconnect Safety Module for Minecraft 26.2:
 * Disconnects player immediately when an unwhitelisted player enters the configured radius.
 */
public class PlayerKickModule extends Module {

    public PlayerKickModule() {
        super("Player Proximity Kick");
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.get().playerKickEnabled;
    }

    @Override
    public void onTick(Minecraft client) {
        if (client.player == null || client.level == null || client.getConnection() == null) {
            return;
        }

        ModConfig config = ModConfig.get();
        double radius = config.playerRadius;
        double radiusSq = radius * radius;

        for (AbstractClientPlayer other : client.level.players()) {
            if (other == null || !other.isAlive() || FreeCamUtils.isFreeCamOrSelf(other, client)) {
                continue;
            }

            String otherName = other.getGameProfile().name();

            boolean isWhitelisted = config.playerWhitelist.stream()
                    .anyMatch(name -> name.equalsIgnoreCase(otherName));

            if (isWhitelisted) {
                continue;
            }

            double distSq = client.player.distanceToSqr(other);
            if (distSq <= radiusSq) {
                double dist = Math.sqrt(distSq);

                if (config.playAlertSound) {
                    AlertManager.get().playWarningSound();
                }

                if (config.logPlayerName) {
                    ChatUtils.LOGGER.warn("[AutoUtils] Proximity Safety Triggered! Player '{}' detected at {:.1f} blocks. Disconnecting...",
                            otherName, dist);
                }

                AlertManager.get().addAlert("§c[SAFETY] Player nearby: " + otherName, 0xFF5555, 100);

                client.getConnection().getConnection().disconnect(
                        Component.literal(String.format("§c[AutoUtils Safety] Disconnected: Player nearby (%s, %.1fm)", otherName, dist))
                );
                return;
            }
        }
    }
}
