package org.fentanylsolutions.drobiazgi.mixins.early.doggytalents;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import doggytalents.item.ItemThrowBone;

@Mixin(value = ItemThrowBone.class)
public class MixinItemThrowBone {

    // Use configured pitch offset for Doggy Talents throwables.
    @ModifyArg(
        method = "onItemRightClick",
        at = @At(
            value = "INVOKE",
            target = "Ldoggytalents/item/ItemThrowBone;setHeadingFromThrower(Lnet/minecraft/entity/item/EntityItem;Lnet/minecraft/entity/Entity;FFFFF)V",
            remap = false),
        index = 4)
    private float drobiazgi$addPitchOffset(float pitchOffset) {
        if (!Config.isDoggyThrowFixEnabled()) {
            return pitchOffset;
        }

        float configuredPitchOffset = Config.getDoggyThrowPitchOffset();
        if (Drobiazgi.isDebugMode()) {
            Drobiazgi
                .debug("Doggy throw pitchOffset: vanilla=" + pitchOffset + ", configured=" + configuredPitchOffset);
        }
        return configuredPitchOffset;
    }

    // Use configured throw force for Doggy Talents throwables.
    @ModifyArg(
        method = "onItemRightClick",
        at = @At(
            value = "INVOKE",
            target = "Ldoggytalents/item/ItemThrowBone;setHeadingFromThrower(Lnet/minecraft/entity/item/EntityItem;Lnet/minecraft/entity/Entity;FFFFF)V",
            remap = false),
        index = 5)
    private float drobiazgi$boostForce(float force) {
        if (!Config.isDoggyThrowFixEnabled()) {
            return force;
        }

        float configuredForce = Config.getDoggyThrowForce();
        if (Drobiazgi.isDebugMode()) {
            Drobiazgi.debug("Doggy throw force: vanilla=" + force + ", configured=" + configuredForce);
        }
        return configuredForce;
    }
}
