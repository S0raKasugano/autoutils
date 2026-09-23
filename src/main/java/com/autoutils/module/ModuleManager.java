package com.autoutils.module;

import com.autoutils.config.ModConfig;
import com.autoutils.module.automation.AntiAfkModule;
import com.autoutils.module.automation.AutoCommandModule;
import com.autoutils.module.automation.AutoRejoinModule;
import com.autoutils.module.general.AutoAttackModule;
import com.autoutils.module.general.AutoEatModule;
import com.autoutils.module.safety.AlertManager;
import com.autoutils.module.safety.AutoTotemModule;
import com.autoutils.module.safety.DamageKickModule;
import com.autoutils.module.safety.PlayerKickModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry and lifecycle dispatcher for all AutoUtils modules in Minecraft 26.2.
 */
public class ModuleManager {
    private static final ModuleManager INSTANCE = new ModuleManager();

    private final List<Module> modules = new ArrayList<>();

    public final AutoEatModule autoEat;
    public final AutoAttackModule autoAttack;
    public final AutoCommandModule autoCommands;
    public final AutoRejoinModule autoRejoin;
    public final AntiAfkModule antiAfk;
    public final PlayerKickModule playerKick;
    public final DamageKickModule damageKick;
    public final AutoTotemModule autoTotem;

    private ModuleManager() {
        modules.add(autoEat = new AutoEatModule());
        modules.add(autoAttack = new AutoAttackModule());
        modules.add(autoCommands = new AutoCommandModule());
        modules.add(autoRejoin = new AutoRejoinModule());
        modules.add(antiAfk = new AntiAfkModule());
        modules.add(playerKick = new PlayerKickModule());
        modules.add(damageKick = new DamageKickModule());
        modules.add(autoTotem = new AutoTotemModule());
    }

    public static ModuleManager get() {
        return INSTANCE;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void onTick(Minecraft client) {
        AlertManager.get().tick();

        if (client.player == null || client.level == null) {
            return;
        }

        if (ModConfig.get().globalKillswitch) {
            return;
        }

        for (Module module : modules) {
            if (module.isEnabled()) {
                try {
                    module.onTick(client);
                } catch (Exception e) {
                    com.autoutils.util.ChatUtils.LOGGER.error("[AutoUtils] Error in module tick: " + module.getName(), e);
                }
            }
        }
    }

    public void onJoin(String serverAddress) {
        for (Module module : modules) {
            module.onJoin(serverAddress);
        }
    }

    public void onDisconnect() {
        for (Module module : modules) {
            module.onDisconnect();
        }
    }

    public void onRenderHud(GuiGraphicsExtractor extractor, float tickDelta) {
        AlertManager.get().renderHud(extractor, tickDelta);

        if (ModConfig.get().globalKillswitch) {
            return;
        }

        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onRenderHud(extractor, tickDelta);
            }
        }
    }
}
