package org.fentanylsolutions.drobiazgi.mixins.late.Oceancraft;

import net.minecraft.block.Block;

import org.fentanylsolutions.drobiazgi.oceancraft.OceanCraftWorldgenCompat;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.Oceancraft.common.ItemShell;
import com.Oceancraft.common.ItemShell2;
import com.Oceancraft.common.ItemShell3;
import com.Oceancraft.common.ItemShell4;
import com.Oceancraft.common.ItemShell5;
import com.Oceancraft.common.ItemShell6;

@Mixin(
    value = { ItemShell.class, ItemShell2.class, ItemShell3.class, ItemShell4.class, ItemShell5.class,
        ItemShell6.class },
    remap = false)
public class MixinItemShells {

    // Shell placement uses the same check path, so the cached lookup keeps right-click overhead negligible.
    @Redirect(
        method = "onItemRightClick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/init/Blocks;sand:Lnet/minecraft/block/Block;",
            opcode = Opcodes.GETSTATIC))
    private Block drobiazgi$resolveSandSafely() {
        return OceanCraftWorldgenCompat.getVanillaSandBlock();
    }
}
