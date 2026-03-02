package org.fentanylsolutions.drobiazgi.mixins.early.minecraftforge;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

import org.fentanylsolutions.drobiazgi.biometags.BiomeDictionaryCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeDictionary.class)
public abstract class MixinBiomeDictionary {

    // Unsafe mod biomes like LOTR use local IDs, so store their tags by biome object instead.
    @Inject(method = "registerBiomeType", at = @At("HEAD"), cancellable = true, remap = false)
    private static void drobiazgi$registerCompatBiomeType(BiomeGenBase biome, BiomeDictionary.Type[] types,
        CallbackInfoReturnable<Boolean> cir) {
        if (BiomeDictionaryCompat.shouldUseCompatStorage(biome)) {
            cir.setReturnValue(BiomeDictionaryCompat.registerCompatBiomeType(biome, types));
        }
    }

    // For unsafe biomes, return the compat tags instead of whatever the colliding vanilla ID points at.
    @Inject(method = "getTypesForBiome", at = @At("HEAD"), cancellable = true, remap = false)
    private static void drobiazgi$getCompatTypesForBiome(BiomeGenBase biome,
        CallbackInfoReturnable<BiomeDictionary.Type[]> cir) {
        if (BiomeDictionaryCompat.shouldUseCompatStorage(biome)) {
            cir.setReturnValue(BiomeDictionaryCompat.getCompatTypesForBiome(biome));
        }
    }

    // Route unsafe biome type checks through the compat storage so tag lookups stay correct.
    @Inject(method = "isBiomeOfType", at = @At("HEAD"), cancellable = true, remap = false)
    private static void drobiazgi$isCompatBiomeOfType(BiomeGenBase biome, BiomeDictionary.Type type,
        CallbackInfoReturnable<Boolean> cir) {
        if (BiomeDictionaryCompat.shouldUseCompatStorage(biome)) {
            cir.setReturnValue(BiomeDictionaryCompat.isCompatBiomeOfType(biome, type));
        }
    }

    // This keeps getBiomesForType useful for mods that build spawn lists from BiomeDictionary types.
    @Inject(method = "getBiomesForType", at = @At("RETURN"), cancellable = true, remap = false)
    private static void drobiazgi$appendCompatBiomesForType(BiomeDictionary.Type type,
        CallbackInfoReturnable<BiomeGenBase[]> cir) {
        cir.setReturnValue(BiomeDictionaryCompat.appendCompatBiomesForType(type, cir.getReturnValue()));
    }

    // isBiomeRegistered is wrong for LOTR because its local IDs collide with vanilla IDs.
    @Inject(
        method = "isBiomeRegistered(Lnet/minecraft/world/biome/BiomeGenBase;)Z",
        at = @At("HEAD"),
        cancellable = true,
        remap = false)
    private static void drobiazgi$isCompatBiomeRegistered(BiomeGenBase biome, CallbackInfoReturnable<Boolean> cir) {
        if (BiomeDictionaryCompat.shouldUseCompatStorage(biome)) {
            cir.setReturnValue(BiomeDictionaryCompat.isCompatBiomeRegistered(biome));
        }
    }

    // Equivalence needs the same compat path or unsafe biomes compare against the wrong vanilla tags.
    @Inject(method = "areBiomesEquivalent", at = @At("HEAD"), cancellable = true, remap = false)
    private static void drobiazgi$areCompatBiomesEquivalent(BiomeGenBase biomeA, BiomeGenBase biomeB,
        CallbackInfoReturnable<Boolean> cir) {
        if (BiomeDictionaryCompat.shouldUseCompatStorage(biomeA)
            || BiomeDictionaryCompat.shouldUseCompatStorage(biomeB)) {
            cir.setReturnValue(BiomeDictionaryCompat.areBiomesEquivalent(biomeA, biomeB));
        }
    }
}
