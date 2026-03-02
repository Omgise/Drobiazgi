package org.fentanylsolutions.drobiazgi.mixins.late.Oceancraft;

import org.fentanylsolutions.drobiazgi.oceancraft.EntityBlueWhale;
import org.fentanylsolutions.drobiazgi.oceancraft.EntityHumpbackWhale;
import org.fentanylsolutions.drobiazgi.oceancraft.EntityNarwhal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Oceancraft.client.ClientProxyOceancraft;
import com.Oceancraft.common.ModelWhale;
import com.Oceancraft.common.RenderWhale;

import cpw.mods.fml.client.registry.RenderingRegistry;

@Mixin(value = ClientProxyOceancraft.class, remap = false)
public class MixinClientProxyOceancraft {

    // OceanCraft only registers a renderer for the original whale class, so add handlers for the split classes too.
    @Inject(method = "registerRenderThings", at = @At("TAIL"))
    private void drobiazgi$registerSplitWhaleRenderers(CallbackInfo ci) {
        RenderingRegistry
            .registerEntityRenderingHandler(EntityHumpbackWhale.class, new RenderWhale(new ModelWhale(), 0.6F, 7.0F));
        RenderingRegistry
            .registerEntityRenderingHandler(EntityNarwhal.class, new RenderWhale(new ModelWhale(), 0.6F, 7.0F));
        RenderingRegistry
            .registerEntityRenderingHandler(EntityBlueWhale.class, new RenderWhale(new ModelWhale(), 0.6F, 7.0F));
    }
}
