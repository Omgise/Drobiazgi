package org.fentanylsolutions.drobiazgi.mixins.early.customnpcs;

import net.minecraft.world.WorldServer;

import org.fentanylsolutions.drobiazgi.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import noppes.npcs.NPCSpawning;

@Mixin(value = NPCSpawning.class, remap = false)
public class MixinNPCSpawning {

    // Replace CustomNPCs WIP natural spawning with Drobiazgi spawning when configured.
    @Inject(method = "findChunksForSpawning", at = @At("HEAD"), cancellable = true)
    private static void drobiazgi$cancelWipSpawnerIfReplacementEnabled(WorldServer world, CallbackInfo ci) {
        if (Config.isCustomNpcsSpawningEnabled() && Config.shouldReplaceCustomNpcsWipSpawner()) {
            ci.cancel();
        }
    }
}
