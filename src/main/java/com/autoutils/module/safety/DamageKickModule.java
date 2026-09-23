package com.autoutils.module.safety;

import com.autoutils.config.ModConfig;
import com.autoutils.module.Module;
import com.autoutils.util.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Damage Disconnect Safety Module for Minecraft 26.2:
 * Disconnects the player immediately when health falls below a configured threshold or when damage is sustained.
 */
public class DamageKickModule extends Module {
    private float lastHealth = -1.0f;

    public DamageKickModule() {
        super("Damage Kick");
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.get().damageKickEnabled;
    }

    @Override
    public void onTick(Minecraft client) {
        if (client.player == null || client.getConnection() == null) {
            lastHealth = -1.0f;
            return;
        }

        float currentHealth = client.player.getHealth();

        if (lastHealth < 0) {
            lastHealth = currentHealth;
            return;
        }

        ModConfig config = ModConfig.get();

        if (currentHealth < lastHealth) {
            boolean isCombat = client.player.getLastAttacker() != null || client.player.getLastHurtByMob() != null;

            boolean allowTrigger = false;
            if (isCombat && config.triggerOnCombatDamage) {
                allowTrigger = true;
            } else if (!isCombat && config.triggerOnEnvironmentDamage) {
                allowTrigger = true;
            }

            if (allowTrigger && currentHealth <= config.healthThreshold) {
                if (config.playAlertSound) {
                    AlertManager.get().playWarningSound();
                }

                String cause = isCombat ? "Combat/Mob Attack" : "Environmental Hazard";
                ChatUtils.LOGGER.warn("[AutoUtils] Critical Health Disconnect! Health: {:.1f}/20.0, Cause: {}",
                        currentHealth, cause);

                AlertManager.get().addAlert("§c[SAFETY] Low Health Disconnect!", 0xFF3333, 100);

                client.getConnection().getConnection().disconnect(
                        Component.literal(String.format("§c[AutoUtils Safety] Disconnected: Low Health (%.1f HP, %s)",
                                currentHealth, cause))
                );
                lastHealth = -1.0f;
                return;
            }
        }

        lastHealth = currentHealth;
    }

    @Override
    public void onDisconnect() {
        lastHealth = -1.0f;
    }
}
