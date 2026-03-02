package org.fentanylsolutions.drobiazgi.mixins.late.Oceancraft;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;

import org.fentanylsolutions.drobiazgi.oceancraft.OceanCraftWhaleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Oceancraft.common.EntityRegisterOceancraft;
import com.Oceancraft.common.EntityWhale;

@Mixin(value = EntityRegisterOceancraft.class, remap = false)
public class MixinEntityRegisterOceancraft {

    // OceanCraft registers its whales during init, so this is the right point to add our extra entity classes.
    @Inject(method = "registerMobs", at = @At("TAIL"))
    private static void drobiazgi$registerSplitWhaleEntities(CallbackInfo ci) {
        OceanCraftWhaleManager.registerCustomEntities();
    }

    // Replace the single whale spawn entry with three fixed variants when the feature is enabled.
    @Redirect(
        method = "addSpawns",
        at = @At(
            value = "INVOKE",
            target = "Lcpw/mods/fml/common/registry/EntityRegistry;addSpawn(Ljava/lang/Class;IIILnet/minecraft/entity/EnumCreatureType;[Lnet/minecraft/world/biome/BiomeGenBase;)V",
            ordinal = 5))
    private static void drobiazgi$splitNaturalWhaleSpawns(Class<? extends EntityWhale> entityClass, int weightedProb,
        int minGroupCount, int maxGroupCount, EnumCreatureType creatureType, BiomeGenBase... biomes) {
        OceanCraftWhaleManager.addNaturalWhaleSpawns(weightedProb, minGroupCount, maxGroupCount, creatureType, biomes);
    }
}
