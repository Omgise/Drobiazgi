package org.fentanylsolutions.drobiazgi.core;

import java.util.List;
import java.util.Set;

import org.fentanylsolutions.drobiazgi.Drobiazgi;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@SuppressWarnings("unused")
@LateMixin
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins." + Drobiazgi.MODID + ".late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return Mixins.getLateMixinsForLoader(loadedCoreMods);
    }
}
