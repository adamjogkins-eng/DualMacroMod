package com.dualmacro.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

public class DualMacroClient implements ClientModInitializer {
    private static KeyMapping zKeyBinding;
    private static KeyMapping xKeyBinding;

    private boolean bowCartMode = false;
    private boolean xbowCartMode = false;

    private int sequenceStep = 0;
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        zKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.dualmacro.bowcart",
                GLFW.GLFW_KEY_Z,
                "category.dualmacro.binds"
        ));

        xKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.dualmacro.xbowcart",
                GLFW.GLFW_KEY_X,
                "category.dualmacro.binds"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (zKeyBinding.consumeClick()) {
                bowCartMode = !bowCartMode;
                xbowCartMode = false;
                client.player.displayClientMessage(
                        Component.literal("Bow Cart Mode: " + (bowCartMode ? "ENABLED" : "DISABLED")),
                        true
                );
            }

            while (xKeyBinding.consumeClick()) {
                xbowCartMode = !xbowCartMode;
                bowCartMode = false;
                client.player.displayClientMessage(
                        Component.literal("XBOW Cart Mode: " + (xbowCartMode ? "ENABLED" : "DISABLED")),
                        true
                );
            }

            handleMacroTick(client);
        });
    }

    private void handleMacroTick(Minecraft client) {
        if (!xbowCartMode && !bowCartMode) return;

        if (xbowCartMode) {
            runXbowSequence(client);
        }
    }

    private void runXbowSequence(Minecraft client) {
        if (sequenceStep == 0) return;

        tickCounter++;
        if (tickCounter < 2) return; 
        tickCounter = 0;

        Inventory inv = client.player.getInventory();

        switch (sequenceStep) {
            case 1: // Place Cart on Rail
                switchToItem(inv, Items.MINECART, Items.TNT_MINECART);
                simulateRightClick(client);
                sequenceStep = 2;
                break;
            case 2: // Ignite Flint and Steel toward player
                switchToItem(inv, Items.FLINT_AND_STEEL);
                simulateRightClick(client);
                sequenceStep = 3;
                break;
            case 3: // Sneak
                client.options.keyShift.setDown(true);
                sequenceStep = 4;
                break;
            case 4: // Switch to Crossbow and Shoot
                switchToItem(inv, Items.CROSSBOW);
                simulateRightClick(client);
                client.options.keyShift.setDown(false);
                sequenceStep = 0; // Sequence finish
                break;
        }
    }

    public void triggerXbowSequence() {
        if (xbowCartMode) {
            this.sequenceStep = 1;
            this.tickCounter = 0;
        }
    }

    private void switchToItem(Inventory inv, net.minecraft.world.item.Item... targetItems) {
        for (int i = 0; i < 9; i++) {
            for (net.minecraft.world.item.Item target : targetItems) {
                if (inv.getItem(i).is(target)) {
                    inv.selected = i;
                    return;
                }
            }
        }
    }

    private void simulateRightClick(Minecraft client) {
        if (client.gameMode != null && client.hitResult != null && client.hitResult.getType() == HitResult.Type.BLOCK) {
            client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, (BlockHitResult) client.hitResult);
        } else if (client.gameMode != null) {
            client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
        }
    }
}
