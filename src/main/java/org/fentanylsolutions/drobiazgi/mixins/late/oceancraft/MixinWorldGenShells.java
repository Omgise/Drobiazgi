package org.fentanylsolutions.drobiazgi.mixins.late.Oceancraft;

import java.util.Random;

import net.minecraft.world.World;

import org.fentanylsolutions.drobiazgi.oceancraft.OceanCraftWorldgenCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.Oceancraft.common.WorldGenShell;
import com.Oceancraft.common.WorldGenShell2;
import com.Oceancraft.common.WorldGenShell3;
import com.Oceancraft.common.WorldGenShell4;
import com.Oceancraft.common.WorldGenShell5;
import com.Oceancraft.common.WorldGenShell6;

@Mixin(
    value = { WorldGenShell.class, WorldGenShell2.class, WorldGenShell3.class, WorldGenShell4.class,
        WorldGenShell5.class, WorldGenShell6.class },
    remap = false)
public class MixinWorldGenShells {

    // Re-run the tiny generator body without touching the unstable Blocks.sand field name.
    @Inject(method = "generate", at = @At("HEAD"), cancellable = true)
    private void drobiazgi$generateShellSafely(World world, Random random, int x, int y, int z,
        CallbackInfoReturnable<Boolean> cir) {
        OceanCraftWorldgenCompat.generateShell(this, world, x, y, z);
        cir.setReturnValue(Boolean.TRUE);
    }
}
