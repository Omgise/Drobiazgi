package org.fentanylsolutions.drobiazgi.mixins.late.MoCreatures;

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

import drzhark.mocreatures.entity.MoCEntityAnimal;

@Mixin(value = MoCEntityAnimal.class)
public abstract class MixinMoCEntityAnimal extends EntityAnimal {

    @Shadow(remap = false)
    public abstract boolean getIsAdult();

    protected MixinMoCEntityAnimal(World world) {
        super(world);
    }

    // MoCreatures uses the old entityToAttack system, not the vanilla AI task system.
    // The TerritorialPlayerTargetGoal added by TerritorialAggressionManager won't run
    // because isAIEnabled() returns false. Set entityToAttack directly instead.
    @Inject(method = "onLivingUpdate", at = @At("TAIL"))
    private void drobiazgi$territorialAggression(CallbackInfo ci) {
        if (worldObj.isRemote || !Config.isTerritorialAggressionEnabled()
            || !getIsAdult()
            || !TerritorialAggressionRules.matches(this)
            || entityToAttack != null
            || rand.nextInt(20) != 0) {
            return;
        }

        EntityPlayer player = worldObj.getClosestVulnerablePlayerToEntity(this, 8.0D);
        if (player != null) {
            entityToAttack = player;

            if (Drobiazgi.isDebugMode()) {
                Drobiazgi.debug("Territorial aggression targeting player for MoC entity " + getEntityId() + ".");
            }
        }
    }
}
