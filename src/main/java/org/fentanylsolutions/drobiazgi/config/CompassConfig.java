package org.fentanylsolutions.drobiazgi.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.World;

import org.fentanylsolutions.drobiazgi.Drobiazgi;
import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.util.DimensionUtil;
import org.fentanylsolutions.fentlib.util.DimensionUtil.SimpleDimensionObj;

import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = Drobiazgi.MODID, category = "compass")
public final class CompassConfig {

    @Config.Comment({ "Dimension list used by the compass filter.",
        "Accepted values: numeric IDs (example: \"0\", \"-1\") or exact dimension names from FentLib's DimensionUtil.",
        "Name values are resolved once dimension providers are available." })
    @Config.DefaultStringList({ "0" })
    @Config.Sync
    public static String[] dimensions = new String[] { "0" };

    @Config.Comment({
        "If true, the dimension list acts as a blacklist and compass north mode is disabled in listed dimensions.",
        "If false, the list acts as a whitelist and compass north mode is enabled only in listed dimensions." })
    @Config.DefaultBoolean(false)
    @Config.Sync
    public static boolean useDimensionBlacklist = false;

    @Config.Comment("Enable compass north modification. If false, vanilla compass behavior is used.")
    @Config.DefaultBoolean(false)
    @Config.Sync
    public static boolean enableCompassModification = false;

    @Config.Ignore
    private static volatile Set<Integer> parsedDimensionIds = Collections.emptySet();

    @Config.Ignore
    private static volatile int parsedConfigHash = Integer.MIN_VALUE;

    @Config.Ignore
    private static volatile boolean parsedWithProviders = false;

    private CompassConfig() {}

    public static void postConfiguration() {
        reloadParsedDimensions();
    }

    public static boolean isCompassEnabledForWorld(World world) {
        if (!enableCompassModification) {
            return false;
        }

        if (world == null || world.provider == null) {
            return true;
        }

        return isCompassEnabledForDimension(world.provider.dimensionId);
    }

    public static boolean isCompassEnabledForDimension(int dimensionId) {
        if (!enableCompassModification) {
            return false;
        }

        ensureParsedDimensions();
        boolean listed = parsedDimensionIds.contains(Integer.valueOf(dimensionId));
        return useDimensionBlacklist ? !listed : listed;
    }

    public static Set<Integer> getParsedDimensionIds() {
        ensureParsedDimensions();
        return parsedDimensionIds;
    }

    public static synchronized void reloadParsedDimensions() {
        String[] configuredDimensions = dimensions != null ? dimensions : new String[0];
        boolean providersReady = areDimensionProvidersReady();
        Set<Integer> resolvedDimensionIds = new HashSet<Integer>();

        for (String dimensionEntry : configuredDimensions) {
            if (dimensionEntry == null) {
                continue;
            }

            String trimmedEntry = dimensionEntry.trim();
            if (trimmedEntry.isEmpty()) {
                continue;
            }

            Integer dimensionId = resolveDimensionId(trimmedEntry, providersReady);
            if (dimensionId != null) {
                resolvedDimensionIds.add(dimensionId);
            }
        }

        parsedDimensionIds = Collections.unmodifiableSet(resolvedDimensionIds);
        parsedWithProviders = providersReady;
        parsedConfigHash = computeConfigHash(configuredDimensions, useDimensionBlacklist);
    }

    private static void ensureParsedDimensions() {
        String[] configuredDimensions = dimensions != null ? dimensions : new String[0];
        int configHash = computeConfigHash(configuredDimensions, useDimensionBlacklist);
        boolean providersReady = areDimensionProvidersReady();
        if (configHash != parsedConfigHash || (!parsedWithProviders && providersReady)) {
            reloadParsedDimensions();
        }
    }

    private static Integer resolveDimensionId(String value, boolean providersReady) {
        try {
            int dimensionId = Integer.parseInt(value);
            if (providersReady && DimensionUtil.getSimpleDimensionObj(dimensionId) == null) {
                Drobiazgi.LOG.warn("Compass config dimension id '{}' is not currently registered", value);
            }

            return Integer.valueOf(dimensionId);
        } catch (NumberFormatException ignored) {
            // Not an id, fall through to name lookup.
        }

        if (!providersReady) {
            return null;
        }

        SimpleDimensionObj dimension = DimensionUtil.getSimpleDimensionObj(value);
        if (dimension != null) {
            return Integer.valueOf(dimension.getId());
        }

        Drobiazgi.LOG.warn("Compass config dimension '{}' could not be resolved by FentLib DimensionUtil", value);
        return null;
    }

    private static boolean areDimensionProvidersReady() {
        return FentLib.varInstanceCommon != null && FentLib.varInstanceCommon.providers != null;
    }

    private static int computeConfigHash(String[] configuredDimensions, boolean isBlacklist) {
        return (31 * Arrays.hashCode(configuredDimensions)) + (isBlacklist ? 1 : 0);
    }
}
