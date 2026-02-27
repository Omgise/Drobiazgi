package org.fentanylsolutions.drobiazgi;

import org.fentanylsolutions.drobiazgi.compass.CompassRules;

import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;

@com.gtnewhorizon.gtnhlib.config.Config(modid = Drobiazgi.MODID)
public final class Config {

    @Comment("Enable debug logging for this mod.")
    @DefaultBoolean(false)
    public static boolean debugMode = false;

    private Config() {}

    public static void postConfiguration() {
        CompassRules.reloadFromConfig();
    }

    public static String[] getCompassDimensions() {
        return CompassConfig.compassDimensions;
    }

    public static boolean isCompassUseDimensionBlacklist() {
        return CompassConfig.compassUseDimensionBlacklist;
    }

    public static boolean isCompassModificationEnabled() {
        return CompassConfig.enableCompassModification;
    }

    public static float getDoggyThrowPitchOffset() {
        return DoggyTalentsConfig.throwPitchOffset;
    }

    public static float getDoggyThrowForce() {
        return DoggyTalentsConfig.throwForce;
    }

    public static boolean isDoggyThrowFixEnabled() {
        return DoggyTalentsConfig.enableThrowTuningFix;
    }
}
