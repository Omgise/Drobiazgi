package org.fentanylsolutions.drobiazgi;

import org.fentanylsolutions.drobiazgi.biometags.BiomeTagRules;
import org.fentanylsolutions.drobiazgi.compass.CompassRules;
import org.fentanylsolutions.drobiazgi.customnpcs.CustomNpcSpawnRules;

import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;

@com.gtnewhorizon.gtnhlib.config.Config(modid = Drobiazgi.MODID)
public final class Config {

    @Comment("Enable debug logging for this mod.")
    @DefaultBoolean(false)
    public static boolean debugMode = false;

    private Config() {}

    public static void postConfiguration() {
        BiomeTagRules.reloadFromConfig();
        CompassRules.reloadFromConfig();
        CustomNpcSpawnRules.reloadFromConfig();
    }

    public static boolean isBiomeTaggingEnabled() {
        return BiomeTagsConfig.enabled;
    }

    public static String[] getBiomeTagRules() {
        return BiomeTagsConfig.rules;
    }

    public static boolean isOceanCraftWhaleSplitEnabled() {
        return OceanCraftWhaleConfig.enableSeparateWhales;
    }

    public static int getOceanCraftHumpbackWeight() {
        return OceanCraftWhaleConfig.humpbackWeight;
    }

    public static int getOceanCraftNarwhalWeight() {
        return OceanCraftWhaleConfig.narwhalWeight;
    }

    public static int getOceanCraftBlueWhaleWeight() {
        return OceanCraftWhaleConfig.blueWhaleWeight;
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

    public static boolean isDoggyFetchLoopCompatFixEnabled() {
        return DoggyTalentsConfig.enableFetchLoopCompatFix;
    }

    public static boolean isCustomNpcsSpawningEnabled() {
        return CustomNpcsSpawningConfig.enabled;
    }

    public static boolean shouldReplaceCustomNpcsWipSpawner() {
        return CustomNpcsSpawningConfig.replaceCustomNpcsWipSpawner;
    }

    public static int getCustomNpcsSpawnTickInterval() {
        return CustomNpcsSpawningConfig.tickInterval;
    }

    public static int getCustomNpcsAttemptsPerPlayerPerCycle() {
        return CustomNpcsSpawningConfig.attemptsPerPlayerPerCycle;
    }

    public static int getCustomNpcsMaxNpcsPerDimension() {
        return CustomNpcsSpawningConfig.maxNpcsPerDimension;
    }

    public static int getCustomNpcsMinPlayerDistance() {
        return CustomNpcsSpawningConfig.minPlayerDistance;
    }

    public static int getCustomNpcsMaxPlayerDistance() {
        return CustomNpcsSpawningConfig.maxPlayerDistance;
    }

    public static int getCustomNpcsMaxSpawnsPerCycle() {
        return CustomNpcsSpawningConfig.maxSpawnsPerCycle;
    }

    public static String[] getCustomNpcsSpawnRules() {
        return CustomNpcsSpawningConfig.rules;
    }

    public static boolean isLeafRegrowthEnabled() {
        return RegrowableLeavesConfig.enabled;
    }

    public static int getLeafRegrowthProcessingIntervalTicks() {
        return RegrowableLeavesConfig.processingIntervalTicks;
    }

    public static int getLeafRegrowthChecksPerCycle() {
        return RegrowableLeavesConfig.checksPerCycle;
    }

    public static int getLeafRegrowthMinDelayTicks() {
        return RegrowableLeavesConfig.minDelayTicks;
    }

    public static int getLeafRegrowthMaxDelayTicks() {
        return RegrowableLeavesConfig.maxDelayTicks;
    }

    public static int getLeafRegrowthMaxConnectionDepth() {
        return RegrowableLeavesConfig.maxConnectionDepth;
    }

    public static int getLeafRegrowthUnloadedChunkRetryTicks() {
        return RegrowableLeavesConfig.unloadedChunkRetryTicks;
    }

    public static String[] getLeafRegrowthRules() {
        return RegrowableLeavesConfig.rules;
    }

    public static boolean isPsychedelicraftAlcoholEnabled() {
        return PsychedelicraftAlcoholConfig.enabled;
    }

    public static boolean shouldSuppressLotrAlcoholNausea() {
        return PsychedelicraftAlcoholConfig.suppressLotrNausea;
    }

    public static int getPsychedelicraftAlcoholDefaultDelayTicks() {
        return PsychedelicraftAlcoholConfig.defaultDelayTicks;
    }

    public static double getPsychedelicraftAlcoholDefaultInfluenceSpeed() {
        return PsychedelicraftAlcoholConfig.defaultInfluenceSpeed;
    }

    public static double getPsychedelicraftAlcoholDefaultInfluenceSpeedPlus() {
        return PsychedelicraftAlcoholConfig.defaultInfluenceSpeedPlus;
    }

    public static String[] getPsychedelicraftAlcoholRules() {
        return PsychedelicraftAlcoholConfig.rules;
    }
}
