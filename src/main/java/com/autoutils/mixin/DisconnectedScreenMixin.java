package com.autoutils.mixin;

import com.autoutils.config.ModConfig;
import com.autoutils.module.automation.AutoRejoinModule;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin injecting into DisconnectedScreen in Minecraft 26.2.
 */
@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {

    protected DisconnectedScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        AutoRejoinModule rejoin = AutoRejoinModule.get();
        rejoin.onDisconnectedScreenInit();

        if (rejoin.isCountdownActive()) {
            Button cancelButton = Button.builder(Component.literal("§cCancel Rejoin"), btn -> {
                rejoin.cancel();
                btn.active = false;
                btn.setMessage(Component.literal("§7Rejoin Cancelled"));
            }).bounds(this.width / 2 - 100, this.height - 30, 200, 20).build();

            this.addRenderableWidget(cancelButton);
        }
    }
}
