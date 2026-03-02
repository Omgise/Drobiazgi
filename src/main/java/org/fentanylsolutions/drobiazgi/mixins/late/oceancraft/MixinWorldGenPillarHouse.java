package org.fentanylsolutions.drobiazgi.mixins.late.Oceancraft;

import net.minecraft.block.Block;

import org.fentanylsolutions.drobiazgi.oceancraft.OceanCraftWorldgenCompat;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.Oceancraft.common.WorldGenPillarHouse;

@Mixin(value = WorldGenPillarHouse.class, remap = false)
public class MixinWorldGenPillarHouse {

    // Pillar house generation is infrequent, but this keeps it on the same safe path as the other generators.
    @Redirect(
        method = "generate",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/init/Blocks;sand:Lnet/minecraft/block/Block;",
            opcode = Opcodes.GETSTATIC))
    private Block drobiazgi$resolveSandSafely() {
        return OceanCraftWorldgenCompat.getVanillaSandBlock();
    }
}
