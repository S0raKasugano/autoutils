package com.autoutils;

import com.autoutils.config.ModConfig;
import com.autoutils.module.ModuleManager;
import com.autoutils.module.safety.AlertManager;
import com.autoutils.util.ChatUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;

/**
 * Main client-side entrypoint for AutoUtils in Minecraft 26.3.
 */
public class AutoUtilsClient implements ClientModInitializer {
    public static final String MOD_ID = "autoutils";

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "general")
    );

    private static KeyMapping killswitchKey;
    private static KeyMapping configKey;
    private static KeyMapping autoAttackKey;
    private static KeyMapping autoEatKey;
    private static KeyMapping antiAfkKey;

    @Override
    public void onInitializeClient() {
        ChatUtils.LOGGER.info("[AutoUtils] Initializing client mod for Minecraft 26.3...");

        // Load persisted configuration
        ModConfig.get();

        // 1. Register Keybindings in dedicated AutoUtils Controls Category
        killswitchKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.autoutils.killswitch",
                InputConstants.UNKNOWN.getValue(),
                CATEGORY
        ));

        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.autoutils.config",
                InputConstants.KEY_O,
                CATEGORY
        ));

        autoAttackKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.autoutils.autoAttack",
                InputConstants.KEY_V,
                CATEGORY
        ));

        autoEatKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.autoutils.autoEat",
                InputConstants.UNKNOWN.getValue(),
                CATEGORY
        ));

        antiAfkKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.autoutils.antiAfk",
                InputConstants.UNKNOWN.getValue(),
                CATEGORY
        ));

        // 2. Client Tick Event
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);

        // 3. Network Connection Events
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String serverIp = "singleplayer";
            if (client.getCurrentServer() != null) {
                serverIp = client.getCurrentServer().ip;
            }
            ModuleManager.get().onJoin(serverIp);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ModuleManager.get().onDisconnect();
        });

        // 4. In-Game HUD Render Callback (Fabric 26.2 HudElementRegistry)
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "hud_overlay"),
                (graphicsExtractor, deltaTracker) -> {
                    ModuleManager.get().onRenderHud(graphicsExtractor, deltaTracker.getGameTimeDeltaPartialTick(true));
                }
        );

        ChatUtils.LOGGER.info("[AutoUtils] Client mod initialized successfully for Minecraft 26.2.");
    }

    private void onEndClientTick(Minecraft client) {
        // Handle Keybind: Global Kill-Switch
        while (killswitchKey.consumeClick()) {
            ModConfig config = ModConfig.get();
            config.globalKillswitch = !config.globalKillswitch;
            config.save();

            if (config.globalKillswitch) {
                ChatUtils.warn("Kill-Switch ACTIVATED! All automated utilities suspended.");
                AlertManager.get().addAlert("§c[KILLSWITCH ENGAGED]", 0xFF5555, 60);
            } else {
                ChatUtils.success("Kill-Switch DEACTIVATED! Automated utilities resumed.");
                AlertManager.get().addAlert("§a[KILLSWITCH DISENGAGED]", 0x55FF55, 60);
            }
        }

        // Handle Keybind: Open Cloth Config GUI
        while (configKey.consumeClick()) {
            if (client.gui != null) {
                client.gui.setScreen(ModConfig.createConfigScreen(client.gui.screen()));
            }
        }

        // Handle Keybind: Toggle Auto-Attack
        while (autoAttackKey.consumeClick()) {
            ModConfig config = ModConfig.get();
            config.autoAttackEnabled = !config.autoAttackEnabled;
            if (config.autoAttackEnabled && config.globalKillswitch) {
                config.globalKillswitch = false;
                ChatUtils.warn("Global Kill-Switch automatically DEACTIVATED.");
            }
            config.save();

            if (config.autoAttackEnabled) {
                ChatUtils.info("Auto-Attack: §aENABLED");
                AlertManager.get().addAlert("§a[Auto-Attack ON]", 0x55FF55, 40);
            } else {
                ChatUtils.info("Auto-Attack: §cDISABLED");
                AlertManager.get().addAlert("§c[Auto-Attack OFF]", 0xFF5555, 40);
            }
        }

        // Handle Keybind: Toggle Auto-Eat
        while (autoEatKey.consumeClick()) {
            ModConfig config = ModConfig.get();
            config.autoEatEnabled = !config.autoEatEnabled;
            if (config.autoEatEnabled && config.globalKillswitch) {
                config.globalKillswitch = false;
                ChatUtils.warn("Global Kill-Switch automatically DEACTIVATED.");
            }
            config.save();

            if (config.autoEatEnabled) {
                ChatUtils.info("Auto-Eat: §aENABLED");
                AlertManager.get().addAlert("§a[Auto-Eat ON]", 0x55FF55, 40);
            } else {
                ChatUtils.info("Auto-Eat: §cDISABLED");
                AlertManager.get().addAlert("§c[Auto-Eat OFF]", 0xFF5555, 40);
            }
        }

        // Handle Keybind: Toggle Anti-AFK
        while (antiAfkKey.consumeClick()) {
            ModConfig config = ModConfig.get();
            config.antiAfkEnabled = !config.antiAfkEnabled;
            config.save();

            if (config.antiAfkEnabled) {
                ChatUtils.info("Anti-AFK: §aENABLED");
                AlertManager.get().addAlert("§a[Anti-AFK ON]", 0x55FF55, 40);
            } else {
                ChatUtils.info("Anti-AFK: §cDISABLED");
                AlertManager.get().addAlert("§c[Anti-AFK OFF]", 0xFF5555, 40);
            }
        }

        // Tick all module routines
        ModuleManager.get().onTick(client);

        // Tick auto-rejoin countdown if disconnected screen is active
        if (client.gui != null && client.gui.screen() instanceof DisconnectedScreen screen) {
            com.autoutils.module.automation.AutoRejoinModule.get().tickCountdown(screen);
        }
    }
}
