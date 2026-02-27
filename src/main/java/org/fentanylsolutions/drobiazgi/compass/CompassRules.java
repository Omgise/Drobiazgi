package org.fentanylsolutions.drobiazgi.compass;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.World;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;
import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.util.DimensionUtil;
import org.fentanylsolutions.fentlib.util.DimensionUtil.SimpleDimensionObj;

public final class CompassRules {

    private static volatile Set<Integer> parsedDimensionIds = Collections.emptySet();
    private static volatile int parsedConfigHash = Integer.MIN_VALUE;
    private static volatile boolean parsedWithProviders = false;

    private CompassRules() {}

    public static boolean isCompassEnabledForWorld(World world) {
        if (!Config.isCompassModificationEnabled()) {
            return false;
        }

        if (world == null || world.provider == null) {
            return true;
        }

        return isCompassEnabledForDimension(world.provider.dimensionId);
    }

    public static boolean isCompassEnabledForDimension(int dimensionId) {
        if (!Config.isCompassModificationEnabled()) {
            return false;
        }

        ensureParsedDimensions();
        boolean listed = parsedDimensionIds.contains(Integer.valueOf(dimensionId));
        return Config.isCompassUseDimensionBlacklist() ? !listed : listed;
    }

    public static synchronized void reloadFromConfig() {
        String[] configuredDimensions = Config.getCompassDimensions();
        if (configuredDimensions == null) {
            configuredDimensions = new String[0];
        }
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
        parsedConfigHash = computeConfigHash(
            configuredDimensions,
            Config.isCompassUseDimensionBlacklist(),
            Config.isCompassModificationEnabled());
    }

    private static void ensureParsedDimensions() {
        String[] configuredDimensions = Config.getCompassDimensions();
        if (configuredDimensions == null) {
            configuredDimensions = new String[0];
        }
        int configHash = computeConfigHash(
            configuredDimensions,
            Config.isCompassUseDimensionBlacklist(),
            Config.isCompassModificationEnabled());
        boolean providersReady = areDimensionProvidersReady();
        if (configHash != parsedConfigHash || (!parsedWithProviders && providersReady)) {
            reloadFromConfig();
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
            // Not an id, try by name.
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

    private static int computeConfigHash(String[] configuredDimensions, boolean useBlacklist, boolean enabled) {
        int hash = Arrays.hashCode(configuredDimensions);
        hash = (31 * hash) + (useBlacklist ? 1 : 0);
        hash = (31 * hash) + (enabled ? 1 : 0);
        return hash;
    }
}
