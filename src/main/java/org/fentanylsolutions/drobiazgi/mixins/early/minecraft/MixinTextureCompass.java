package org.fentanylsolutions.drobiazgi.mixins.early.minecraft;

import net.minecraft.client.renderer.texture.TextureCompass;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

import org.fentanylsolutions.drobiazgi.compass.CompassRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TextureCompass.class)
public class MixinTextureCompass {

    @Redirect(
        method = "updateCompass",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getSpawnPoint()Lnet/minecraft/util/ChunkCoordinates;"))
    private ChunkCoordinates drobiazgi$getSpawnPointForNorth(World world, World currentWorld, double x, double z,
        double yaw, boolean forceRandom, boolean immediate) {
        if (!drobiazgi$useNorthCompass(currentWorld, forceRandom)) {
            return world.getSpawnPoint();
        }

        return new ChunkCoordinates(MathHelper.floor_double(x), 0, MathHelper.floor_double(z) - 1000000);
    }

    @Redirect(
        method = "updateCompass",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;isSurfaceWorld()Z"))
    private boolean drobiazgi$allowCompassInConfiguredDimensions(WorldProvider provider, World currentWorld, double x,
        double z, double yaw, boolean forceRandom, boolean immediate) {
        if (drobiazgi$useNorthCompass(currentWorld, forceRandom)) {
            return true;
        }

        return provider.isSurfaceWorld();
    }

    private static boolean drobiazgi$useNorthCompass(World world, boolean forceRandom) {
        return !forceRandom && world != null && CompassRules.isCompassEnabledForWorld(world);
    }
}
