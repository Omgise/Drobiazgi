package org.fentanylsolutions.drobiazgi.customnpcs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import net.minecraft.world.WorldServer;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;
import org.fentanylsolutions.fentlib.util.DimensionUtil;

public final class CustomNpcSpawnRules {

    private static volatile List<CustomNpcSpawnRule> rules = Collections.emptyList();

    private CustomNpcSpawnRules() {}

    public static synchronized void reloadFromConfig() {
        BiomeResolver biomeResolver = BiomeResolver.create();
        List<CustomNpcSpawnRule> parsedRules = new ArrayList<>();

        String[] configuredRules = Config.getCustomNpcsSpawnRules();
        if (configuredRules == null) {
            rules = Collections.emptyList();
            return;
        }

        for (int i = 0; i < configuredRules.length; i++) {
            String line = configuredRules[i];
            CustomNpcSpawnRule rule = parseRule(line, i, biomeResolver);
            if (rule != null) {
                parsedRules.add(rule);
            }
        }

        rules = Collections.unmodifiableList(parsedRules);
        Drobiazgi.LOG.info("Loaded {} CustomNPC natural spawn rule(s).", rules.size());
        if (Drobiazgi.isDebugMode()) {
            for (CustomNpcSpawnRule rule : rules) {
                Drobiazgi.debug("CustomNPC spawn rule loaded: " + rule.getDebugSummary());
            }
        }
    }

    public static List<CustomNpcSpawnRule> getRules() {
        return rules;
    }

    public static CustomNpcSpawnRule pickRule(WorldServer world, int biomeId, int y, int lightLevel, Random random) {
        List<CustomNpcSpawnRule> currentRules = rules;
        if (currentRules.isEmpty()) {
            return null;
        }

        int totalWeight = 0;
        List<CustomNpcSpawnRule> matchingRules = new ArrayList<>();
        for (CustomNpcSpawnRule rule : currentRules) {
            if (!rule.isEnabled()) {
                continue;
            }
            if (rule.getWeight() <= 0) {
                continue;
            }
            boolean matched = rule.isCaveRule() ? rule.matchesIgnoringPosition(world, biomeId)
                : rule.matches(world, biomeId, y, lightLevel);
            if (!matched) {
                continue;
            }

            matchingRules.add(rule);
            totalWeight += rule.getWeight();
        }

        if (matchingRules.isEmpty() || totalWeight <= 0) {
            return null;
        }

        int pickedWeight = random.nextInt(totalWeight);
        for (CustomNpcSpawnRule rule : matchingRules) {
            pickedWeight -= rule.getWeight();
            if (pickedWeight < 0) {
                return rule;
            }
        }

        return matchingRules.get(matchingRules.size() - 1);
    }

    public static String describeNoMatch(WorldServer world, int biomeId, int y, int lightLevel) {
        List<CustomNpcSpawnRule> currentRules = rules;
        if (currentRules.isEmpty()) {
            return "no rules loaded";
        }

        int total = 0;
        int enabled = 0;
        int positiveWeight = 0;
        int dimMatch = 0;
        int biomeMatch = 0;
        int timeMatch = 0;
        int yMatch = 0;
        int lightMatch = 0;
        int fullMatch = 0;

        int dimensionId = world.provider.dimensionId;
        for (CustomNpcSpawnRule rule : currentRules) {
            total++;
            if (!rule.isEnabled()) {
                continue;
            }
            enabled++;
            if (rule.getWeight() > 0) {
                positiveWeight++;
            }
            if (rule.matchesDimension(dimensionId)) {
                dimMatch++;
            }
            if (rule.matchesBiome(biomeId)) {
                biomeMatch++;
            }
            if (rule.matchesTime(world)) {
                timeMatch++;
            }
            if (rule.matchesY(y)) {
                yMatch++;
            }
            if (rule.matchesLight(lightLevel)) {
                lightMatch++;
            }
            boolean matched = rule.isCaveRule() ? rule.matchesIgnoringPosition(world, biomeId)
                : rule.matches(world, biomeId, y, lightLevel);
            if (rule.getWeight() > 0 && matched) {
                fullMatch++;
            }
        }

        return "rules total=" + total
            + ", enabled="
            + enabled
            + ", weight>0="
            + positiveWeight
            + ", dimMatch="
            + dimMatch
            + ", biomeMatch="
            + biomeMatch
            + ", timeMatch="
            + timeMatch
            + ", yMatch="
            + yMatch
            + ", lightMatch="
            + lightMatch
            + ", fullMatch="
            + fullMatch;
    }

    private static CustomNpcSpawnRule parseRule(String line, int index, BiomeResolver biomeResolver) {
        if (line == null) {
            return null;
        }

        String rawLine = line.trim();
        if (rawLine.isEmpty() || rawLine.startsWith("#")) {
            return null;
        }

        String id = "rule_" + (index + 1);
        boolean enabled = true;
        int cloneTab = 1;
        String cloneName = "";
        int weight = 10;
        double chance = 1.0D;
        int minGroupSize = 1;
        int maxGroupSize = 1;
        Set<Integer> dimensions = new LinkedHashSet<>();
        CustomNpcSpawnRule.FilterMode dimensionMode = CustomNpcSpawnRule.FilterMode.WHITELIST;
        Set<Integer> biomes = new LinkedHashSet<>();
        CustomNpcSpawnRule.FilterMode biomeMode = CustomNpcSpawnRule.FilterMode.WHITELIST;
        CustomNpcSpawnRule.TimeMode timeMode = CustomNpcSpawnRule.TimeMode.ANY;
        int minLight = 0;
        int maxLight = 15;
        int minY = 0;
        int maxY = 255;
        boolean allowWater = false;
        boolean allowCave = false;

        String[] entries = rawLine.split(";");
        for (String entry : entries) {
            String trimmedEntry = entry.trim();
            if (trimmedEntry.isEmpty()) {
                continue;
            }

            int separatorIndex = trimmedEntry.indexOf('=');
            if (separatorIndex <= 0 || separatorIndex >= trimmedEntry.length() - 1) {
                Drobiazgi.LOG.warn("Ignoring malformed spawn rule entry '{}'.", trimmedEntry);
                continue;
            }

            String key = trimmedEntry.substring(0, separatorIndex)
                .trim()
                .toLowerCase(Locale.ROOT);
            String value = trimmedEntry.substring(separatorIndex + 1)
                .trim();

            switch (key) {
                case "id":
                    id = value.isEmpty() ? id : value;
                    break;
                case "enabled":
                    enabled = parseBoolean(value, enabled);
                    break;
                case "tab":
                    cloneTab = Math.max(0, parseInt(value, cloneTab));
                    break;
                case "clone":
                    cloneName = value;
                    break;
                case "weight":
                    weight = Math.max(1, parseInt(value, weight));
                    break;
                case "chance":
                    chance = clamp(parseDouble(value, chance), 0.0D, 1.0D);
                    break;
                case "group":
                    int[] groupRange = parseRange(value, minGroupSize, maxGroupSize);
                    minGroupSize = Math.max(1, groupRange[0]);
                    maxGroupSize = Math.max(1, groupRange[1]);
                    break;
                case "dims":
                case "dimensions":
                    dimensions = parseDimensions(value, id);
                    break;
                case "dimmode":
                case "dimensionmode":
                    dimensionMode = parseFilterMode(value, dimensionMode);
                    break;
                case "biomes":
                    biomes = parseBiomes(value, biomeResolver, id);
                    break;
                case "biomemode":
                    biomeMode = parseFilterMode(value, biomeMode);
                    break;
                case "time":
                    timeMode = parseTimeMode(value, timeMode);
                    break;
                case "light":
                    int[] lightRange = parseRange(value, minLight, maxLight);
                    minLight = clamp(lightRange[0], 0, 15);
                    maxLight = clamp(lightRange[1], 0, 15);
                    break;
                case "y":
                    int[] yRange = parseRange(value, minY, maxY);
                    minY = clamp(yRange[0], 0, 255);
                    maxY = clamp(yRange[1], 0, 255);
                    break;
                case "water":
                    allowWater = parseBoolean(value, allowWater);
                    break;
                case "cave":
                case "underground":
                    allowCave = parseBoolean(value, allowCave);
                    break;
                default:
                    Drobiazgi.LOG.warn("Unknown CustomNPC spawn rule key '{}', value '{}'.", key, value);
                    break;
            }
        }

        if (cloneName.isEmpty()) {
            Drobiazgi.LOG.warn("Skipping spawn rule '{}' because clone name is empty.", id);
            return null;
        }

        if (minGroupSize > maxGroupSize) {
            int tmp = minGroupSize;
            minGroupSize = maxGroupSize;
            maxGroupSize = tmp;
        }
        if (minLight > maxLight) {
            int tmp = minLight;
            minLight = maxLight;
            maxLight = tmp;
        }
        if (minY > maxY) {
            int tmp = minY;
            minY = maxY;
            maxY = tmp;
        }

        return new CustomNpcSpawnRule(
            id,
            enabled,
            cloneTab,
            cloneName,
            weight,
            chance,
            minGroupSize,
            maxGroupSize,
            dimensions,
            dimensionMode,
            biomes,
            biomeMode,
            timeMode,
            minLight,
            maxLight,
            minY,
            maxY,
            allowWater,
            allowCave);
    }

    private static Set<Integer> parseDimensions(String value, String ruleId) {
        if (value == null || value.trim()
            .isEmpty()) {
            return Collections.emptySet();
        }

        Set<Integer> dimensions = new LinkedHashSet<>();
        String[] entries = value.split(",");
        for (String entry : entries) {
            String selector = entry.trim();
            if (selector.isEmpty()) {
                continue;
            }

            Integer dimensionId = resolveDimension(selector);
            if (dimensionId != null) {
                dimensions.add(dimensionId);
            } else {
                Drobiazgi.LOG.warn("Rule '{}' uses unknown dimension selector '{}'.", ruleId, selector);
            }
        }
        return dimensions;
    }

    private static Integer resolveDimension(String selector) {
        try {
            return Integer.parseInt(selector);
        } catch (NumberFormatException ignored) {
            // The selector is likely a dimension name.
        }

        DimensionUtil.SimpleDimensionObj dimension = DimensionUtil.getSimpleDimensionObj(selector);
        if (dimension != null) {
            return dimension.getId();
        }

        return Arrays.stream(selector.split("\\|"))
            .map(String::trim)
            .map(DimensionUtil::getSimpleDimensionObj)
            .filter(java.util.Objects::nonNull)
            .map(DimensionUtil.SimpleDimensionObj::getId)
            .findFirst()
            .orElse(null);
    }

    private static Set<Integer> parseBiomes(String value, BiomeResolver biomeResolver, String ruleId) {
        if (value == null || value.trim()
            .isEmpty()) {
            return Collections.emptySet();
        }

        Set<Integer> biomes = new LinkedHashSet<>();
        String[] entries = value.split(",");
        for (String entry : entries) {
            String selector = entry.trim();
            if (selector.isEmpty()) {
                continue;
            }

            Integer biomeId = biomeResolver.resolveBiomeId(selector);
            if (biomeId != null) {
                biomes.add(biomeId);
            } else {
                Drobiazgi.LOG.warn("Rule '{}' uses unknown biome selector '{}'.", ruleId, selector);
            }
        }
        return biomes;
    }

    private static CustomNpcSpawnRule.FilterMode parseFilterMode(String value, CustomNpcSpawnRule.FilterMode fallback) {
        String normalized = value.trim()
            .toLowerCase(Locale.ROOT);
        if (normalized.equals("blacklist") || normalized.equals("black")
            || normalized.equals("deny")
            || normalized.equals("exclude")) {
            return CustomNpcSpawnRule.FilterMode.BLACKLIST;
        }
        if (normalized.equals("whitelist") || normalized.equals("white")
            || normalized.equals("allow")
            || normalized.equals("include")) {
            return CustomNpcSpawnRule.FilterMode.WHITELIST;
        }
        return fallback;
    }

    private static CustomNpcSpawnRule.TimeMode parseTimeMode(String value, CustomNpcSpawnRule.TimeMode fallback) {
        String normalized = value.trim()
            .toLowerCase(Locale.ROOT);
        if (normalized.equals("day")) {
            return CustomNpcSpawnRule.TimeMode.DAY;
        }
        if (normalized.equals("night")) {
            return CustomNpcSpawnRule.TimeMode.NIGHT;
        }
        if (normalized.equals("any") || normalized.equals("all")) {
            return CustomNpcSpawnRule.TimeMode.ANY;
        }
        return fallback;
    }

    private static int[] parseRange(String value, int fallbackMin, int fallbackMax) {
        String[] parts;
        if (value.contains("..")) {
            parts = value.split("\\.\\.", 2);
        } else if (value.contains(":")) {
            parts = value.split(":", 2);
        } else if (value.contains("-")) {
            parts = value.split("-", 2);
        } else {
            int single = parseInt(value, fallbackMin);
            return new int[] { single, single };
        }

        int min = parseInt(parts[0].trim(), fallbackMin);
        int max = parseInt(parts[1].trim(), fallbackMax);
        return new int[] { min, max };
    }

    private static boolean parseBoolean(String value, boolean fallback) {
        String normalized = value.trim()
            .toLowerCase(Locale.ROOT);
        if (normalized.equals("true") || normalized.equals("yes") || normalized.equals("1")) {
            return true;
        }
        if (normalized.equals("false") || normalized.equals("no") || normalized.equals("0")) {
            return false;
        }
        return fallback;
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
