package org.fentanylsolutions.drobiazgi.mixins.late.Oceancraft;

import net.minecraftforge.event.entity.player.AchievementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Oceancraft.common.OceancraftGoDeepEvent;

@Mixin(value = OceancraftGoDeepEvent.class, remap = false)
public class MixinOceancraftGoDeepEvent {

    // OceanCraft listens to the base PlayerEvent and awards an achievement, which fires another PlayerEvent.
    @Inject(method = "onCookItem", at = @At("HEAD"), cancellable = true)
    private void drobiazgi$skipRecursiveAchievementHandling(PlayerEvent event, CallbackInfo ci) {
        if (event instanceof AchievementEvent) {
            ci.cancel();
        }
    }
}
