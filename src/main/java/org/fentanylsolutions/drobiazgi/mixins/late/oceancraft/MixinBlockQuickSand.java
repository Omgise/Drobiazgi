package org.fentanylsolutions.drobiazgi.mixins.late.Oceancraft;

import net.minecraft.block.Block;

import org.fentanylsolutions.drobiazgi.oceancraft.OceanCraftWorldgenCompat;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.Oceancraft.common.BlockQuickSand;
import com.Oceancraft.common.BlockQuickSand2;

@Mixin(value = { BlockQuickSand.class, BlockQuickSand2.class }, remap = false)
public class MixinBlockQuickSand {

    // Keep the drop result stable without paying a registry lookup every block break.
    @Redirect(
        method = "idDropped",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/init/Blocks;sand:Lnet/minecraft/block/Block;",
            opcode = Opcodes.GETSTATIC))
    private Block drobiazgi$resolveSandSafely() {
        return OceanCraftWorldgenCompat.getVanillaSandBlock();
    }
}
