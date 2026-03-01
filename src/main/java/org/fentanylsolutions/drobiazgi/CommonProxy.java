package org.fentanylsolutions.drobiazgi;

import org.fentanylsolutions.drobiazgi.customnpcs.CustomNpcNaturalSpawner;
import org.fentanylsolutions.drobiazgi.leafregrowth.LeafRegrowthManager;
import org.fentanylsolutions.drobiazgi.leafregrowth.LeafRegrowthRules;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        try {
            ConfigurationManager.registerConfig(Config.class);
            ConfigurationManager.registerConfig(CompassConfig.class);
            ConfigurationManager.registerConfig(DoggyTalentsConfig.class);
            ConfigurationManager.registerConfig(CustomNpcsSpawningConfig.class);
            ConfigurationManager.registerConfig(RegrowableLeavesConfig.class);
        } catch (ConfigException e) {
            throw new RuntimeException("Failed to load Drobiazgi config", e);
        }

        Config.postConfiguration();
        Drobiazgi.LOG.info("I am Drobiazgi at version {}", Tags.VERSION);
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        LeafRegrowthManager.register();

        if (Loader.isModLoaded("customnpcs")) {
            CustomNpcNaturalSpawner.register();
        }
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        LeafRegrowthRules.reloadFromConfig();
    }

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}
}
