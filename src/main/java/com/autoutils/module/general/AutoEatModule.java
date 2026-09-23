package com.autoutils.module.general;

import com.autoutils.config.ModConfig;
import com.autoutils.module.Module;
import com.autoutils.util.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

/**
 * Auto-Eat Module for Minecraft 26.2:
 * Consumes the best food available when hunger or saturation falls below configured thresholds.
 */
public class AutoEatModule extends Module {
    private boolean isEating = false;
    private int eatingTicks = 0;
    private int originalSlot = -1;
    private int eatDelayTimer = 0;
    private boolean swappedFromInventory = false;
    private int swappedContainerSlot = -1;

    public AutoEatModule() {
        super("Auto-Eat");
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.get().autoEatEnabled;
    }

    @Override
    public void onTick(Minecraft client) {
        if (client.player == null || client.level == null || client.gameMode == null) {
            stopEating(client);
            return;
        }

        if (client.player.isCreative() || client.player.isSpectator()) {
            stopEating(client);
            return;
        }

        // Handle active eating cycle
        if (isEating) {
            eatingTicks++;

            if (client.player.isUsingItem()) {
                // Actively consuming, hold down use key
                client.options.keyUse.setDown(true);
            } else {
                // If past grace period (5 ticks) and not using item, food consumption is done or canceled
                if (eatingTicks > 5) {
                    stopEating(client);
                    eatDelayTimer = ModConfig.get().eatDelayTicks;
                    return;
                } else {
                    client.options.keyUse.setDown(true);
                }
            }

            // Safety timeout: 50 ticks (2.5 seconds) maximum per eat cycle
            if (eatingTicks > 50) {
                stopEating(client);
                eatDelayTimer = ModConfig.get().eatDelayTicks;
            }
            return;
        }

        if (eatDelayTimer > 0) {
            eatDelayTimer--;
            return;
        }

        // If player is manually using another item (shield, bow, trident), don't disrupt
        if (client.player.isUsingItem()) {
            return;
        }

        ModConfig config = ModConfig.get();
        FoodData foodData = client.player.getFoodData();
        int hunger = foodData.getFoodLevel();
        float saturation = foodData.getSaturationLevel();

        // Player needs food if hunger dropped below threshold, or saturation dropped and hunger is not full
        boolean needsFood = hunger <= config.hungerThreshold 
                || (hunger < 20 && saturation <= config.saturationThreshold);

        if (!needsFood) {
            return;
        }

        // 1. Check Offhand Food
        if (config.preferOffhandFood) {
            ItemStack offhandStack = client.player.getOffhandItem();
            if (InventoryUtils.canEatFood(client.player, offhandStack, config.allowHarmfulFood)) {
                startEating(client, -1, false, -1);
                return;
            }
        }

        // 2. Scan Hotbar for Best Food
        Inventory inv = client.player.getInventory();
        int bestHotbarSlot = -1;
        float bestHotbarScore = -1.0f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (InventoryUtils.canEatFood(client.player, stack, config.allowHarmfulFood)) {
                float score = InventoryUtils.calculateFoodScore(stack);
                if (score > bestHotbarScore) {
                    bestHotbarScore = score;
                    bestHotbarSlot = i;
                }
            }
        }

        if (bestHotbarSlot != -1) {
            startEating(client, bestHotbarSlot, false, -1);
            return;
        }

        // 3. Scan Main Inventory if not found in Hotbar
        int bestInvSlot = -1;
        float bestInvScore = -1.0f;

        for (int i = 9; i < 36; i++) {
            ItemStack stack = inv.getItem(i);
            if (InventoryUtils.canEatFood(client.player, stack, config.allowHarmfulFood)) {
                float score = InventoryUtils.calculateFoodScore(stack);
                if (score > bestInvScore) {
                    bestInvScore = score;
                    bestInvSlot = i;
                }
            }
        }

        if (bestInvSlot != -1) {
            int currentHotbar = inv.getSelectedSlot();
            int containerSlot = InventoryUtils.mainInventoryToContainerSlot(bestInvSlot);

            client.gameMode.handleContainerInput(
                    client.player.inventoryMenu.containerId,
                    containerSlot,
                    currentHotbar,
                    ContainerInput.SWAP,
                    client.player
            );

            startEating(client, currentHotbar, true, containerSlot);
        }
    }

    private void startEating(Minecraft client, int hotbarSlot, boolean swapped, int containerSlot) {
        this.isEating = true;
        this.eatingTicks = 0;
        this.swappedFromInventory = swapped;
        this.swappedContainerSlot = containerSlot;

        InteractionHand hand;
        if (hotbarSlot != -1) {
            this.originalSlot = client.player.getInventory().getSelectedSlot();
            client.player.getInventory().setSelectedSlot(hotbarSlot);
            hand = InteractionHand.MAIN_HAND;
        } else {
            this.originalSlot = -1;
            hand = InteractionHand.OFF_HAND;
        }

        // Initiate consumption packet and hold down keyUse
        client.gameMode.useItem(client.player, hand);
        client.options.keyUse.setDown(true);
    }

    private void stopEating(Minecraft client) {
        if (!isEating) {
            return;
        }
        isEating = false;
        eatingTicks = 0;

        if (client.options != null && client.options.keyUse != null) {
            client.options.keyUse.setDown(false);
        }

        if (originalSlot != -1 && client.player != null) {
            client.player.getInventory().setSelectedSlot(originalSlot);
            originalSlot = -1;
        }

        if (swappedFromInventory && client.player != null && client.gameMode != null) {
            int currentHotbar = client.player.getInventory().getSelectedSlot();
            client.gameMode.handleContainerInput(
                    client.player.inventoryMenu.containerId,
                    swappedContainerSlot,
                    currentHotbar,
                    ContainerInput.SWAP,
                    client.player
            );
            swappedFromInventory = false;
            swappedContainerSlot = -1;
        }
    }

    @Override
    public void onDisconnect() {
        isEating = false;
        eatingTicks = 0;
        originalSlot = -1;
        swappedFromInventory = false;
        swappedContainerSlot = -1;
    }
}
