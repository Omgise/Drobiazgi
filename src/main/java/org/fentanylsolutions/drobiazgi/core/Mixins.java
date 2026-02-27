package org.fentanylsolutions.drobiazgi.core;

import org.fentanylsolutions.fentlib.core.FentMixins;
import org.fentanylsolutions.fentlib.util.MiscUtil;
import org.fentanylsolutions.fentlib.util.MixinUtil;

public class Mixins extends FentMixins {

    private static final Mixins INSTANCE = new Mixins();

    @Override
    protected void registerMixins(MixinUtil.Registry registry) {
        // Minecraft Mixins
        registry.mixin("MixinTextureCompass")
            .phase(MixinUtil.Phase.EARLY)
            .side(MiscUtil.Side.CLIENT)
            .build();

        // Doggy Talents Mixins
        registry.mixin("MixinEntityAIFetch")
            .modid("doggytalents")
            .phase(MixinUtil.Phase.EARLY)
            .build();

        registry.mixin("MixinItemThrowBone")
            .modid("doggytalents")
            .phase(MixinUtil.Phase.EARLY)
            .build();

        // CustomNPCs Mixins
        registry.mixin("MixinNPCSpawning")
            .modid("customnpcs")
            .phase(MixinUtil.Phase.EARLY)
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
