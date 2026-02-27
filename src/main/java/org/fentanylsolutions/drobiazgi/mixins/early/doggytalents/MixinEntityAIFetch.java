package org.fentanylsolutions.drobiazgi.mixins.early.doggytalents;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import doggytalents.ModItems;
import doggytalents.entity.ai.EntityAIFetch;

@Mixin(value = EntityAIFetch.class)
public class MixinEntityAIFetch {

    // Doggy Talents expects this hit to consume the fetched throw item. ItemPhysic can leave it alive.
    @Redirect(
        method = "updateTask",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/item/EntityItem;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private boolean drobiazgi$forceConsumeFetchedThrowItem(EntityItem entityItem, DamageSource source, float amount) {
        boolean handled = entityItem.attackEntityFrom(source, amount);
        if (!Config.isDoggyFetchLoopCompatFixEnabled()) {
            return handled;
        }

        if (entityItem.isDead) {
            return handled;
        }

        ItemStack stack = entityItem.getEntityItem();
        if (stack != null && stack.getItem() == ModItems.THROW_BONE) {
            entityItem.setDead();
            if (Drobiazgi.isDebugMode()) {
                Drobiazgi.debug(
                    "Forced consume of fetched throw item after damage hook left it alive: meta="
                        + stack.getItemDamage());
            }
        }

        return handled;
    }
}
