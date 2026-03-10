package org.fentanylsolutions.drobiazgi.mixins.late.lotr;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;
import org.fentanylsolutions.drobiazgi.territorial.TerritorialAggressionRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lotr.common.entity.animal.LOTREntityLionBase;

@Mixin(value = LOTREntityLionBase.class)
public abstract class MixinLOTREntityLion extends EntityAnimal {

    @Shadow(remap = false)
    private int hostileTick;

    @Shadow(remap = false)
    public abstract void setHostile(boolean hostile);

    protected MixinLOTREntityLion(World world) {
        super(world);
    }

    // Territorial targeting sets an attack target, but LOTR lions only add their attack AI while hostileTick is active.
    @Inject(method = "onLivingUpdate", at = @At("TAIL"))
    private void drobiazgi$refreshTerritorialHostility(CallbackInfo ci) {
        if (worldObj.isRemote || !Config.isTerritorialAggressionEnabled()
            || isChild()
            || !TerritorialAggressionRules.matches(this)
            || !(getAttackTarget() instanceof EntityPlayer)
            || hostileTick > 0) {
            return;
        }

        hostileTick = 200;
        setHostile(true);

        if (Drobiazgi.isDebugMode()) {
            Drobiazgi.debug("Re-armed territorial hostility for LOTR lion " + getEntityId() + ".");
        }
    }
}
