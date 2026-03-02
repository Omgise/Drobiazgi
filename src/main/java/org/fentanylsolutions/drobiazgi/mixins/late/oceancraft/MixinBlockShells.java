package org.fentanylsolutions.drobiazgi.mixins.late.Oceancraft;

import net.minecraft.block.Block;

import org.fentanylsolutions.drobiazgi.oceancraft.OceanCraftWorldgenCompat;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.Oceancraft.common.BlockShell;
import com.Oceancraft.common.BlockShell2;
import com.Oceancraft.common.BlockShell3;
import com.Oceancraft.common.BlockShell4;
import com.Oceancraft.common.BlockShell5;
import com.Oceancraft.common.BlockShell6;

@Mixin(
    value = { BlockShell.class, BlockShell2.class, BlockShell3.class, BlockShell4.class, BlockShell5.class,
        BlockShell6.class },
    remap = false)
public class MixinBlockShells {

    // canBlockStay can run often, so reuse the cached sand block.
    @Redirect(
        method = "canBlockStay",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/init/Blocks;sand:Lnet/minecraft/block/Block;",
            opcode = Opcodes.GETSTATIC))
    private Block drobiazgi$resolveSandSafely() {
        return OceanCraftWorldgenCompat.getVanillaSandBlock();
    }
}
