package org.fentanylsolutions.drobiazgi.mixins.late.lotr;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cpw.mods.fml.common.Loader;
import lotr.common.item.LOTRItemMug;

@Mixin(value = LOTRItemMug.class)
public class MixinLOTRItemMug {

    // LOTR adds confusion directly in onEaten. Skipping only this call keeps other nausea sources intact.
    @Redirect(
        method = "onEaten",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;addPotionEffect(Lnet/minecraft/potion/PotionEffect;)V",
            ordinal = 0))
    private void drobiazgi$maybeSkipLotrDrunkNausea(EntityPlayer player, PotionEffect effect) {
        if (!shouldSuppressNausea(effect)) {
            player.addPotionEffect(effect);
            return;
        }

        if (Drobiazgi.isDebugMode()) {
            Drobiazgi.debug("Skipped LOTR mug confusion application for " + player.getCommandSenderName() + ".");
        }
    }

    private static boolean shouldSuppressNausea(PotionEffect effect) {
        if (!Config.isPsychedelicraftAlcoholEnabled() || !Config.shouldSuppressLotrAlcoholNausea()) {
            return false;
        }
        if (!Loader.isModLoaded("psychedelicraft")) {
            return false;
        }
        return effect != null && effect.getPotionID() == Potion.confusion.id;
    }
}
