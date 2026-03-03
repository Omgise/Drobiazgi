package org.fentanylsolutions.drobiazgi.mixins.late.lotr;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lotr.client.render.tileentity.LOTRTileEntityMobSpawnerRenderer;

@Mixin(value = LOTRTileEntityMobSpawnerRenderer.class, remap = false)
public class MixinLOTRTileEntityMobSpawnerRenderer {

    // The item preview cache only exists for inventory mob previews, so skip building hundreds of entities up front.
    @Inject(method = "func_147496_a", at = @At("HEAD"), cancellable = true)
    private void drobiazgi$skipInventoryMobCache(World world, CallbackInfo ci) {
        ci.cancel();
    }

    // Rendering a full mob inside every spawner item icon is the likely source of search-tab freezes.
    @Inject(method = "renderInvMobSpawner", at = @At("HEAD"), cancellable = true)
    private void drobiazgi$skipInventoryMobPreview(int entityId, CallbackInfo ci) {
        ci.cancel();
    }
}
