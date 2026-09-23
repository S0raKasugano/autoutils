package com.autoutils.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility helper for container slot actions and food evaluation in Minecraft 26.2.
 */
public final class InventoryUtils {
    public static final int OFFHAND_CONTAINER_SLOT = 45;
    public static final int HOTBAR_START_CONTAINER_SLOT = 36;
    public static final int MAIN_INVENTORY_START_CONTAINER_SLOT = 9;

    private static final Set<Item> HARMFUL_FOODS = new HashSet<>();

    static {
        HARMFUL_FOODS.add(Items.ROTTEN_FLESH);
        HARMFUL_FOODS.add(Items.PUFFERFISH);
        HARMFUL_FOODS.add(Items.SPIDER_EYE);
        HARMFUL_FOODS.add(Items.POISONOUS_POTATO);
        HARMFUL_FOODS.add(Items.CHORUS_FRUIT); // Teleportation hazard
    }

    private InventoryUtils() {}

    /**
     * Checks whether an ItemStack is a valid edible food item.
     */
    public static boolean isEdibleFood(ItemStack stack, boolean allowHarmful) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) {
            return false;
        }

        if (!allowHarmful && HARMFUL_FOODS.contains(stack.getItem())) {
            return false;
        }

        return true;
    }

    /**
     * Determines whether the player can currently consume this food item according to hunger & component rules.
     */
    public static boolean canEatFood(net.minecraft.world.entity.player.Player player, ItemStack stack, boolean allowHarmful) {
        if (!isEdibleFood(stack, allowHarmful)) {
            return false;
        }
        FoodProperties food = stack.get(DataComponents.FOOD);
        net.minecraft.world.item.component.Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        if (consumable != null) {
            return consumable.canConsume(player, stack);
        }
        return player.canEat(food != null && food.canAlwaysEat());
    }

    /**
     * Calculates the nutritional score of a food item (nutrition + saturation).
     */
    public static float calculateFoodScore(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0.0f;
        }
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) {
            return 0.0f;
        }

        int nutrition = food.nutrition();
        float saturation = nutrition * food.saturation() * 2.0f;
        return nutrition + saturation;
    }

    /**
     * Swaps an inventory slot with the offhand slot using packet-safe ContainerInput.SWAP (button 40).
     */
    public static void swapSlotToOffhand(int containerSlotId) {
        Minecraft client = Minecraft.getInstance();
        if (client.gameMode == null || client.player == null) {
            return;
        }

        int containerId = client.player.inventoryMenu.containerId;
        // Button 40 with ContainerInput.SWAP swaps with the offhand slot in Minecraft inventory
        client.gameMode.handleContainerInput(containerId, containerSlotId, 40, ContainerInput.SWAP, client.player);
    }

    /**
     * Converts a player's hotbar index (0-8) to the inventory container slot index (36-44).
     */
    public static int hotbarToContainerSlot(int hotbarIndex) {
        return HOTBAR_START_CONTAINER_SLOT + hotbarIndex;
    }

    /**
     * Converts a player's main inventory slot (9-35) to container slot index.
     */
    public static int mainInventoryToContainerSlot(int invIndex) {
        return invIndex;
    }
}
