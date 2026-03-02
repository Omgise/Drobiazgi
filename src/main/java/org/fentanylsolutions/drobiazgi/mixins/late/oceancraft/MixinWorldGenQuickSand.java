package org.fentanylsolutions.drobiazgi.mixins.late.Oceancraft;

import net.minecraft.block.Block;

import org.fentanylsolutions.drobiazgi.oceancraft.OceanCraftWorldgenCompat;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.Oceancraft.common.WorldGenQuickSand;

@Mixin(value = WorldGenQuickSand.class, remap = false)
public class MixinWorldGenQuickSand {

    // OceanCraft hardcodes Blocks.sand here, which is not stable across all runtimes.
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
