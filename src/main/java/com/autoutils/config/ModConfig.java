package com.autoutils.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main configuration container for AutoUtils in Minecraft 26.2.
 * Persists to config/autoutils.json via Gson and provides Cloth Config integration.
 */
public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("AutoUtils/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("autoutils.json");

    private static ModConfig INSTANCE;

    // --- Global Controls ---
    public boolean globalKillswitch = false;
    public boolean renderHudAlerts = true;
    public float alertSoundVolume = 1.0f;

    // --- 1. Auto-Eat ---
    public boolean autoEatEnabled = true;
    public int hungerThreshold = 14;
    public float saturationThreshold = 6.0f;
    public boolean allowHarmfulFood = false;
    public boolean preferOffhandFood = true;
    public int eatDelayTicks = 4;

    // --- 2. Auto-Attack ---
    public boolean autoAttackEnabled = true;
    public boolean attackOnlyFullCooldown = true;
    public double attackRange = 3.0;
    public boolean targetHostile = true;
    public boolean targetPassive = false;
    public boolean targetBosses = true;

    // --- 3. Auto-Commands on Join ---
    public boolean autoCommandsEnabled = true;
    public int commandDelayTicks = 40;
    public List<String> globalCommands = new ArrayList<>();
    public Map<String, List<String>> serverProfiles = new HashMap<>();

    // --- 4. Auto-Rejoin ---
    public boolean autoRejoinEnabled = true;
    public int rejoinDelaySeconds = 5;
    public int maxRejoinAttempts = 5;

    // --- 5. Proximity Safety Kick ---
    public boolean playerKickEnabled = true;
    public double playerRadius = 50.0;
    public List<String> playerWhitelist = new ArrayList<>();
    public boolean playAlertSound = true;
    public boolean logPlayerName = true;

    // --- 6. Damage Safety Kick ---
    public boolean damageKickEnabled = true;
    public float healthThreshold = 8.0f; // 4 hearts
    public boolean triggerOnCombatDamage = true;
    public boolean triggerOnEnvironmentDamage = true;

    // --- 7. Auto-Totem ---
    public boolean autoTotemEnabled = true;
    public int totemSwapDelayTicks = 2;

    // --- 8. Anti-AFK ---
    public boolean antiAfkEnabled = false;
    public int antiAfkMinDelay = 100; // 5 seconds
    public int antiAfkMaxDelay = 300; // 15 seconds
    public boolean antiAfkRandomLook = true;
    public boolean antiAfkJump = true;
    public boolean antiAfkSwing = true;

    public ModConfig() {
        List<String> defaultCmds = new ArrayList<>();
        defaultCmds.add("/gamma 1000");
        serverProfiles.put("example.hypixel.net", defaultCmds);
    }

    public static ModConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static ModConfig load() {
        File file = CONFIG_PATH.toFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                ModConfig config = GSON.fromJson(reader, ModConfig.class);
                if (config != null) {
                    config.globalKillswitch = false; // Always reset killswitch on startup
                    LOGGER.info("[AutoUtils] Successfully loaded config from {}", file.getName());
                    return config;
                }
            } catch (Exception e) {
                LOGGER.error("[AutoUtils] Failed to load config, falling back to defaults", e);
            }
        }
        ModConfig newConfig = new ModConfig();
        newConfig.save();
        return newConfig;
    }

    public void save() {
        File file = CONFIG_PATH.toFile();
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(this, writer);
                LOGGER.info("[AutoUtils] Config saved successfully.");
            }
        } catch (IOException e) {
            LOGGER.error("[AutoUtils] Error writing config to disk", e);
        }
    }

    public List<String> getServerCommandsAsList() {
        List<String> list = new ArrayList<>();
        if (serverProfiles != null) {
            for (Map.Entry<String, List<String>> entry : serverProfiles.entrySet()) {
                for (String cmd : entry.getValue()) {
                    list.add(entry.getKey() + " -> " + cmd);
                }
            }
        }
        return list;
    }

    public void setServerCommandsFromList(List<String> list) {
        if (serverProfiles == null) {
            serverProfiles = new HashMap<>();
        }
        serverProfiles.clear();
        for (String line : list) {
            if (line == null) continue;
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.contains("->")) {
                String[] parts = trimmed.split("->", 2);
                String server = parts[0].trim().toLowerCase();
                String cmd = parts[1].trim();
                serverProfiles.computeIfAbsent(server, k -> new ArrayList<>()).add(cmd);
            } else {
                if (!globalCommands.contains(trimmed)) {
                    globalCommands.add(trimmed);
                }
            }
        }
    }

    /**
     * Builds the Cloth Config screen for Minecraft 26.2.
     */
    public static Screen createConfigScreen(Screen parent) {
        ModConfig config = get();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.autoutils.config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // ==========================================
        // 1. COMBAT CATEGORY
        // ==========================================
        ConfigCategory combatCat = builder.getOrCreateCategory(Component.translatable("category.autoutils.combat"));

        combatCat.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.autoAttack"), config.autoAttackEnabled)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.autoutils.autoAttack.tooltip"))
                .setSaveConsumer(val -> config.autoAttackEnabled = val)
                .build());

        combatCat.addEntry(entryBuilder.startDoubleField(Component.translatable("option.autoutils.attackRange"), config.attackRange)
                .setDefaultValue(3.0)
                .setMin(1.0).setMax(6.0)
                .setSaveConsumer(val -> config.attackRange = val)
                .build());

        combatCat.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.attackOnlyFullCooldown"), config.attackOnlyFullCooldown)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.attackOnlyFullCooldown = val)
                .build());

        // Target Entity Filtering SubCategory
        SubCategoryBuilder targetFilterSub = entryBuilder.startSubCategory(Component.translatable("subcategory.autoutils.targetFiltering"));
        targetFilterSub.setExpanded(true);
        targetFilterSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.targetHostile"), config.targetHostile)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.targetHostile = val)
                .build());
        targetFilterSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.targetPassive"), config.targetPassive)
                .setDefaultValue(false)
                .setSaveConsumer(val -> config.targetPassive = val)
                .build());
        targetFilterSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.targetBosses"), config.targetBosses)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.targetBosses = val)
                .build());
        combatCat.addEntry(targetFilterSub.build());

        // Auto-Totem SubCategory
        SubCategoryBuilder totemSub = entryBuilder.startSubCategory(Component.translatable("subcategory.autoutils.autoTotem"));
        totemSub.setExpanded(true);
        totemSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.autoTotem"), config.autoTotemEnabled)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.autoutils.autoTotem.tooltip"))
                .setSaveConsumer(val -> config.autoTotemEnabled = val)
                .build());
        totemSub.add(entryBuilder.startIntSlider(Component.translatable("option.autoutils.totemSwapDelayTicks"), config.totemSwapDelayTicks, 0, 20)
                .setDefaultValue(2)
                .setSaveConsumer(val -> config.totemSwapDelayTicks = val)
                .build());
        combatCat.addEntry(totemSub.build());

        // ==========================================
        // 2. SURVIVAL CATEGORY
        // ==========================================
        ConfigCategory survivalCat = builder.getOrCreateCategory(Component.translatable("category.autoutils.survival"));

        survivalCat.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.autoEat"), config.autoEatEnabled)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.autoutils.autoEat.tooltip"))
                .setSaveConsumer(val -> config.autoEatEnabled = val)
                .build());

        survivalCat.addEntry(entryBuilder.startIntSlider(Component.translatable("option.autoutils.hungerThreshold"), config.hungerThreshold, 1, 20)
                .setDefaultValue(14)
                .setSaveConsumer(val -> config.hungerThreshold = val)
                .build());

        survivalCat.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.preferOffhandFood"), config.preferOffhandFood)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.preferOffhandFood = val)
                .build());

        survivalCat.addEntry(entryBuilder.startIntSlider(Component.translatable("option.autoutils.eatDelayTicks"), config.eatDelayTicks, 0, 20)
                .setDefaultValue(4)
                .setSaveConsumer(val -> config.eatDelayTicks = val)
                .build());

        // Advanced Eating Options SubCategory
        SubCategoryBuilder advancedFoodSub = entryBuilder.startSubCategory(Component.translatable("subcategory.autoutils.advancedFood"));
        advancedFoodSub.setExpanded(false);
        advancedFoodSub.add(entryBuilder.startFloatField(Component.translatable("option.autoutils.saturationThreshold"), config.saturationThreshold)
                .setDefaultValue(6.0f)
                .setMin(0.0f).setMax(20.0f)
                .setSaveConsumer(val -> config.saturationThreshold = val)
                .build());
        advancedFoodSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.allowHarmfulFood"), config.allowHarmfulFood)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("option.autoutils.allowHarmfulFood.tooltip"))
                .setSaveConsumer(val -> config.allowHarmfulFood = val)
                .build());
        survivalCat.addEntry(advancedFoodSub.build());

        // ==========================================
        // 3. AUTOMATION CATEGORY
        // ==========================================
        ConfigCategory autoCat = builder.getOrCreateCategory(Component.translatable("category.autoutils.automation"));

        // Join Commands SubCategory
        SubCategoryBuilder autoCommandsSub = entryBuilder.startSubCategory(Component.translatable("subcategory.autoutils.autoCommands"));
        autoCommandsSub.setExpanded(true);
        autoCommandsSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.autoCommands"), config.autoCommandsEnabled)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.autoutils.autoCommands.tooltip"))
                .setSaveConsumer(val -> config.autoCommandsEnabled = val)
                .build());
        autoCommandsSub.add(entryBuilder.startIntField(Component.translatable("option.autoutils.commandDelayTicks"), config.commandDelayTicks)
                .setDefaultValue(40)
                .setMin(0).setMax(1200)
                .setTooltip(Component.translatable("option.autoutils.commandDelayTicks.tooltip"))
                .setSaveConsumer(val -> config.commandDelayTicks = val)
                .build());
        autoCommandsSub.add(entryBuilder.startStrList(Component.translatable("option.autoutils.globalCommands"), config.globalCommands)
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Component.translatable("option.autoutils.globalCommands.tooltip"))
                .setExpanded(true)
                .setSaveConsumer(val -> config.globalCommands = new ArrayList<>(val))
                .build());
        autoCommandsSub.add(entryBuilder.startStrList(Component.translatable("option.autoutils.serverCommands"), config.getServerCommandsAsList())
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Component.translatable("option.autoutils.serverCommands.tooltip"))
                .setExpanded(true)
                .setSaveConsumer(config::setServerCommandsFromList)
                .build());
        autoCat.addEntry(autoCommandsSub.build());

        // Auto-Rejoin SubCategory
        SubCategoryBuilder autoRejoinSub = entryBuilder.startSubCategory(Component.translatable("subcategory.autoutils.autoRejoin"));
        autoRejoinSub.setExpanded(false);
        autoRejoinSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.autoRejoin"), config.autoRejoinEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.autoRejoinEnabled = val)
                .build());
        autoRejoinSub.add(entryBuilder.startIntSlider(Component.translatable("option.autoutils.rejoinDelaySeconds"), config.rejoinDelaySeconds, 1, 60)
                .setDefaultValue(5)
                .setSaveConsumer(val -> config.rejoinDelaySeconds = val)
                .build());
        autoRejoinSub.add(entryBuilder.startIntField(Component.translatable("option.autoutils.maxRejoinAttempts"), config.maxRejoinAttempts)
                .setDefaultValue(5)
                .setMin(1).setMax(100)
                .setSaveConsumer(val -> config.maxRejoinAttempts = val)
                .build());
        autoCat.addEntry(autoRejoinSub.build());

        // Anti-AFK SubCategory
        SubCategoryBuilder antiAfkSub = entryBuilder.startSubCategory(Component.translatable("subcategory.autoutils.antiAfk"));
        antiAfkSub.setExpanded(false);
        antiAfkSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.antiAfk"), config.antiAfkEnabled)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("option.autoutils.antiAfk.tooltip"))
                .setSaveConsumer(val -> config.antiAfkEnabled = val)
                .build());
        antiAfkSub.add(entryBuilder.startIntField(Component.translatable("option.autoutils.antiAfkMinDelay"), config.antiAfkMinDelay)
                .setDefaultValue(100)
                .setSaveConsumer(val -> config.antiAfkMinDelay = val)
                .build());
        antiAfkSub.add(entryBuilder.startIntField(Component.translatable("option.autoutils.antiAfkMaxDelay"), config.antiAfkMaxDelay)
                .setDefaultValue(300)
                .setSaveConsumer(val -> config.antiAfkMaxDelay = val)
                .build());
        antiAfkSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.antiAfkRandomLook"), config.antiAfkRandomLook)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.antiAfkRandomLook = val)
                .build());
        antiAfkSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.antiAfkJump"), config.antiAfkJump)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.antiAfkJump = val)
                .build());
        antiAfkSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.antiAfkSwing"), config.antiAfkSwing)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.antiAfkSwing = val)
                .build());
        autoCat.addEntry(antiAfkSub.build());

        // ==========================================
        // 4. SAFETY & ALERTS CATEGORY
        // ==========================================
        ConfigCategory safetyCat = builder.getOrCreateCategory(Component.translatable("category.autoutils.safety"));

        safetyCat.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.killswitch"), config.globalKillswitch)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("option.autoutils.killswitch.tooltip"))
                .setSaveConsumer(val -> config.globalKillswitch = val)
                .build());

        safetyCat.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.renderHudAlerts"), config.renderHudAlerts)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.autoutils.renderHudAlerts.tooltip"))
                .setSaveConsumer(val -> config.renderHudAlerts = val)
                .build());

        safetyCat.addEntry(entryBuilder.startFloatField(Component.translatable("option.autoutils.alertSoundVolume"), config.alertSoundVolume)
                .setDefaultValue(1.0f)
                .setMin(0.0f).setMax(1.0f)
                .setTooltip(Component.translatable("option.autoutils.alertSoundVolume.tooltip"))
                .setSaveConsumer(val -> config.alertSoundVolume = val)
                .build());

        // Proximity Disconnect SubCategory
        SubCategoryBuilder playerKickSub = entryBuilder.startSubCategory(Component.translatable("subcategory.autoutils.playerKick"));
        playerKickSub.setExpanded(true);
        playerKickSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.playerKick"), config.playerKickEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.playerKickEnabled = val)
                .build());
        playerKickSub.add(entryBuilder.startDoubleField(Component.translatable("option.autoutils.playerRadius"), config.playerRadius)
                .setDefaultValue(50.0)
                .setMin(5.0).setMax(256.0)
                .setSaveConsumer(val -> config.playerRadius = val)
                .build());
        playerKickSub.add(entryBuilder.startStrList(Component.translatable("option.autoutils.playerWhitelist"), config.playerWhitelist)
                .setDefaultValue(new ArrayList<>())
                .setSaveConsumer(val -> config.playerWhitelist = val)
                .build());
        playerKickSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.playAlertSound"), config.playAlertSound)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.playAlertSound = val)
                .build());
        playerKickSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.logPlayerName"), config.logPlayerName)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.logPlayerName = val)
                .build());
        safetyCat.addEntry(playerKickSub.build());

        // Low Health Disconnect SubCategory
        SubCategoryBuilder damageKickSub = entryBuilder.startSubCategory(Component.translatable("subcategory.autoutils.damageKick"));
        damageKickSub.setExpanded(false);
        damageKickSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.damageKick"), config.damageKickEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.damageKickEnabled = val)
                .build());
        damageKickSub.add(entryBuilder.startFloatField(Component.translatable("option.autoutils.healthThreshold"), config.healthThreshold)
                .setDefaultValue(8.0f)
                .setMin(1.0f).setMax(20.0f)
                .setSaveConsumer(val -> config.healthThreshold = val)
                .build());
        damageKickSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.triggerOnCombatDamage"), config.triggerOnCombatDamage)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.triggerOnCombatDamage = val)
                .build());
        damageKickSub.add(entryBuilder.startBooleanToggle(Component.translatable("option.autoutils.triggerOnEnvironmentDamage"), config.triggerOnEnvironmentDamage)
                .setDefaultValue(true)
                .setSaveConsumer(val -> config.triggerOnEnvironmentDamage = val)
                .build());
        safetyCat.addEntry(damageKickSub.build());

        builder.setSavingRunnable(config::save);
        return builder.build();
    }
}
