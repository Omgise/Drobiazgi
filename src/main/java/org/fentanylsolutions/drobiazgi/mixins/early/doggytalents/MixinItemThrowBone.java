package org.fentanylsolutions.drobiazgi.mixins.early.doggytalents;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import doggytalents.item.ItemThrowBone;

@Mixin(value = ItemThrowBone.class)
public class MixinItemThrowBone {

    // Add upward arc like vanilla throwables (snowballs, eggs, etc.)
    @ModifyArg(
        method = "onItemRightClick",
        at = @At(
            value = "INVOKE",
            target = "Ldoggytalents/item/ItemThrowBone;setHeadingFromThrower(Lnet/minecraft/entity/item/EntityItem;Lnet/minecraft/entity/Entity;FFFFF)V",
            remap = false),
        index = 4)
    private float drobiazgi$addPitchOffset(float pitchOffset) {
        return -20.0f;
    }

    // Boost throw force from 1.2 to 1.5 (matches vanilla snowball)
    @ModifyArg(
        method = "onItemRightClick",
        at = @At(
            value = "INVOKE",
            target = "Ldoggytalents/item/ItemThrowBone;setHeadingFromThrower(Lnet/minecraft/entity/item/EntityItem;Lnet/minecraft/entity/Entity;FFFFF)V",
            remap = false),
        index = 5)
    private float drobiazgi$boostForce(float force) {
        return 1.5f;
    }
}
