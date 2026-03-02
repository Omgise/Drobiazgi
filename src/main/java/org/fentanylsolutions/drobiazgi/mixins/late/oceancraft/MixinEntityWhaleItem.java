package org.fentanylsolutions.drobiazgi.mixins.late.Oceancraft;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import org.fentanylsolutions.drobiazgi.oceancraft.OceanCraftWhaleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.Oceancraft.common.EntityWhaleItem;

@Mixin(value = EntityWhaleItem.class, remap = false)
public class MixinEntityWhaleItem {

    // Thrown whale items create the generic whale entity directly, so swap in the configured fixed variant here.
    @Redirect(
        method = "onImpact",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z"))
    private boolean drobiazgi$spawnConfiguredWhaleVariant(World world, Entity entity) {
        return world.spawnEntityInWorld(OceanCraftWhaleManager.getConfiguredSpawnReplacement(world, entity));
    }
}
