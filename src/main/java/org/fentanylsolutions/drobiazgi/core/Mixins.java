package org.fentanylsolutions.drobiazgi.core;

import org.fentanylsolutions.fentlib.core.FentMixins;
import org.fentanylsolutions.fentlib.util.MiscUtil;
import org.fentanylsolutions.fentlib.util.MixinUtil;

public class Mixins extends FentMixins {

    private static final Mixins INSTANCE = new Mixins();

    @Override
    protected void registerMixins(MixinUtil.Registry registry) {
        // Minecraft Mixins
        registry.mixin("MixinBiomeDictionary")
            .modid("minecraftforge")
            .phase(MixinUtil.Phase.EARLY)
            .build();

        registry.mixin("MixinTextureCompass")
            .phase(MixinUtil.Phase.EARLY)
            .side(MiscUtil.Side.CLIENT)
            .build();

        // Doggy Talents Mixins
        registry.mixin("MixinEntityAIFetch")
            .modid("doggytalents")
            .phase(MixinUtil.Phase.LATE)
            .build();

        registry.mixin("MixinItemThrowBone")
            .modid("doggytalents")
            .phase(MixinUtil.Phase.LATE)
            .build();

        // CustomNPCs Mixins
        registry.mixin("MixinNPCSpawning")
            .modid("customnpcs")
            .phase(MixinUtil.Phase.LATE)
            .build();

        // LOTR Mixins
        registry.mixin("MixinLOTRItemMug")
            .modid("lotr")
            .phase(MixinUtil.Phase.LATE)
            .build();

        // OceanCraft Mixins
        registry.mixin("MixinEntityRegisterOceancraft")
            .modid("Oceancraft")
            .phase(MixinUtil.Phase.LATE)
            .build();

        registry.mixin("MixinEntityWhaleItem")
            .modid("Oceancraft")
            .phase(MixinUtil.Phase.LATE)
            .build();

        registry.mixin("MixinWorldGenShells")
            .modid("Oceancraft")
            .phase(MixinUtil.Phase.LATE)
            .build();

        registry.mixin("MixinClientProxyOceancraft")
            .modid("Oceancraft")
            .phase(MixinUtil.Phase.LATE)
            .side(MiscUtil.Side.CLIENT)
            .build();

        // Other Mixins
    }

    public static java.util.List<String> getEarlyMixinsForLoader() {
        return INSTANCE.getEarlyMixins();
    }

    public static java.util.List<String> getLateMixinsForLoader(java.util.Set<String> loadedCoreMods) {
        return INSTANCE.getLateMixins(loadedCoreMods);
    }
}
