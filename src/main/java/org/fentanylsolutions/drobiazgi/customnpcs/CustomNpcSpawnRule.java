package org.fentanylsolutions.drobiazgi.customnpcs;

import java.util.Set;

import net.minecraft.world.WorldServer;

final class CustomNpcSpawnRule {

    enum FilterMode {
        WHITELIST,
        BLACKLIST
    }

    enum TimeMode {
        ANY,
        DAY,
        NIGHT
    }

    private final String id;
    private final boolean enabled;
    private final int cloneTab;
    private final String cloneName;
    private final int weight;
    private final double chance;
    private final int minGroupSize;
    private final int maxGroupSize;
    private final Set<Integer> dimensions;
    private final FilterMode dimensionMode;
    private final Set<Integer> biomes;
    private final FilterMode biomeMode;
    private final TimeMode timeMode;
    private final int minLight;
    private final int maxLight;
    private final int minY;
    private final int maxY;

    CustomNpcSpawnRule(String id, boolean enabled, int cloneTab, String cloneName, int weight, double chance,
        int minGroupSize, int maxGroupSize, Set<Integer> dimensions, FilterMode dimensionMode, Set<Integer> biomes,
        FilterMode biomeMode, TimeMode timeMode, int minLight, int maxLight, int minY, int maxY) {
        this.id = id;
        this.enabled = enabled;
        this.cloneTab = cloneTab;
        this.cloneName = cloneName;
        this.weight = weight;
        this.chance = chance;
        this.minGroupSize = minGroupSize;
        this.maxGroupSize = maxGroupSize;
        this.dimensions = dimensions;
        this.dimensionMode = dimensionMode;
        this.biomes = biomes;
        this.biomeMode = biomeMode;
        this.timeMode = timeMode;
        this.minLight = minLight;
        this.maxLight = maxLight;
        this.minY = minY;
        this.maxY = maxY;
    }

    String getId() {
        return id;
    }

    boolean isEnabled() {
        return enabled;
    }

    int getCloneTab() {
        return cloneTab;
    }

    String getCloneName() {
        return cloneName;
    }

    int getWeight() {
        return weight;
    }

    double getChance() {
        return chance;
    }

    int getGroupSize(java.util.Random random) {
        if (minGroupSize >= maxGroupSize) {
            return minGroupSize;
        }
        return minGroupSize + random.nextInt(maxGroupSize - minGroupSize + 1);
    }

    boolean matches(WorldServer world, int biomeId, int y, int lightLevel) {
        return matchesDimension(world.provider.dimensionId) && matchesBiome(biomeId)
            && matchesTime(world)
            && matchesY(y)
            && matchesLight(lightLevel);
    }

    boolean matchesDimension(int dimensionId) {
        if (dimensions.isEmpty()) {
            return true;
        }

        boolean contains = dimensions.contains(dimensionId);
        return dimensionMode == FilterMode.BLACKLIST ? !contains : contains;
    }

    boolean matchesBiome(int biomeId) {
        if (biomes.isEmpty()) {
            return true;
        }

        boolean contains = biomes.contains(biomeId);
        return biomeMode == FilterMode.BLACKLIST ? !contains : contains;
    }

    boolean matchesTime(WorldServer world) {
        switch (timeMode) {
            case DAY:
                return world.isDaytime();
            case NIGHT:
                return !world.isDaytime();
            case ANY:
            default:
                return true;
        }
    }

    boolean matchesY(int y) {
        return y >= minY && y <= maxY;
    }

    boolean matchesLight(int lightLevel) {
        return lightLevel >= minLight && lightLevel <= maxLight;
    }

    String getDebugSummary() {
        return "id=" + id
            + ", enabled="
            + enabled
            + ", clone="
            + cloneName
            + ", tab="
            + cloneTab
            + ", weight="
            + weight
            + ", chance="
            + chance
            + ", group="
            + minGroupSize
            + "-"
            + maxGroupSize
            + ", dims="
            + dimensions.size()
            + " ("
            + dimensionMode
            + "), biomes="
            + biomes.size()
            + " ("
            + biomeMode
            + "), time="
            + timeMode
            + ", y="
            + minY
            + "-"
            + maxY
            + ", light="
            + minLight
            + "-"
            + maxLight;
    }
}
