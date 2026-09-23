package com.autoutils.module.safety;

import com.autoutils.config.ModConfig;
import com.autoutils.module.Module;
import com.autoutils.util.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Auto-Totem Module for Minecraft 26.2:
 * Equips a Totem of Undying to the offhand slot from inventory using single-tick ContainerInput.SWAP.
 */
public class AutoTotemModule extends Module {
    private int delayTimer = 0;

    public AutoTotemModule() {
        super("Auto-Totem");
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.get().autoTotemEnabled;
    }

    @Override
    public void onTick(Minecraft client) {
        if (client.player == null || client.gameMode == null) {
            return;
        }

        if (delayTimer > 0) {
            delayTimer--;
            return;
        }

        ItemStack offhandStack = client.player.getOffhandItem();
        if (offhandStack.is(Items.TOTEM_OF_UNDYING)) {
            return;
        }

        Inventory inv = client.player.getInventory();
        int totemSlot = -1;

        // Search hotbar (0-8)
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                totemSlot = InventoryUtils.hotbarToContainerSlot(i);
                break;
            }
        }

        // If not in hotbar, search main inventory (9-35)
        if (totemSlot == -1) {
            for (int i = 9; i < 36; i++) {
                if (inv.getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                    totemSlot = InventoryUtils.mainInventoryToContainerSlot(i);
                    break;
                }
            }
        }

        if (totemSlot != -1) {
            int containerId = client.player.inventoryMenu.containerId;
            // Button 40 specifies the offhand slot in Minecraft's SWAP container action
            client.gameMode.handleContainerInput(containerId, totemSlot, 40, ContainerInput.SWAP, client.player);

            AlertManager.get().addAlert("§e[Auto-Totem] Totem equipped to offhand", 0xFFFF55, 30);
            delayTimer = ModConfig.get().totemSwapDelayTicks;
        }
    }
}
