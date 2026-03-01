package org.fentanylsolutions.drobiazgi.leafregrowth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;

public final class LeafRegrowthRules {

    private static volatile List<LeafRegrowthRule> rules = Collections.emptyList();
    private static volatile Map<String, LeafRegrowthRule> rulesById = Collections.emptyMap();
    private static volatile Map<Block, List<LeafRegrowthRule>> rulesByLeafBlock = Collections.emptyMap();

    private LeafRegrowthRules() {}

    public static synchronized void reloadFromConfig() {
        String[] configuredRules = Config.getLeafRegrowthRules();
        if (configuredRules == null || configuredRules.length == 0) {
            rules = Collections.emptyList();
            rulesById = Collections.emptyMap();
            rulesByLeafBlock = Collections.emptyMap();
            Drobiazgi.LOG.info("Loaded 0 leaf regrowth rule(s).");
            return;
        }

        List<LeafRegrowthRule> parsedRules = new ArrayList<>();
        Map<String, LeafRegrowthRule> parsedById = new LinkedHashMap<>();
        Map<Block, List<LeafRegrowthRule>> parsedByLeaf = new HashMap<>();

        for (int i = 0; i < configuredRules.length; i++) {
            LeafRegrowthRule rule = parseRule(configuredRules[i], i);
            if (rule == null) {
                continue;
            }

            if (parsedById.containsKey(rule.getId())) {
                Drobiazgi.LOG.warn("Ignoring duplicate leaf regrowth rule id '{}'.", rule.getId());
                continue;
            }

            parsedRules.add(rule);
            parsedById.put(rule.getId(), rule);
            parsedByLeaf.computeIfAbsent(rule.getLeafBlock(), block -> new ArrayList<>())
                .add(rule);
        }

        for (Map.Entry<Block, List<LeafRegrowthRule>> entry : parsedByLeaf.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }

        rules = Collections.unmodifiableList(parsedRules);
        rulesById = Collections.unmodifiableMap(parsedById);
        rulesByLeafBlock = Collections.unmodifiableMap(parsedByLeaf);

        long enabledCount = parsedRules.stream()
            .filter(LeafRegrowthRule::isEnabled)
            .count();
        Drobiazgi.LOG.info("Loaded {} leaf regrowth rule(s), {} enabled.", parsedRules.size(), enabledCount);
        if (Drobiazgi.isDebugMode()) {
            for (LeafRegrowthRule rule : parsedRules) {
                Drobiazgi.debug("  rule: " + rule.getDebugSummary());
            }
        }
    }

    public static LeafRegrowthRule findMatchingRule(Block leafBlock, int metadata) {
        List<LeafRegrowthRule> candidates = rulesByLeafBlock.get(leafBlock);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        for (LeafRegrowthRule rule : candidates) {
            if (!rule.isEnabled()) {
                continue;
            }
            if (rule.matchesLeafForTracking(leafBlock, metadata)) {
                return rule;
            }
        }
        return null;
    }

    public static LeafRegrowthRule getRuleById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return rulesById.get(id);
    }

    public static boolean hasRules() {
        return !rules.isEmpty();
    }

    private static LeafRegrowthRule parseRule(String line, int index) {
        if (line == null) {
            return null;
        }

        String raw = line.trim();
        if (raw.isEmpty() || raw.startsWith("#")) {
            return null;
        }

        String id = "leaf_rule_" + (index + 1);
        boolean enabled = true;
        String leafSelector = "";
        String logSelector = "";
        Set<Integer> metadata = Collections.emptySet();
        double respawnChance = 1.0D;

        String[] entries = raw.split(";");
        for (String entry : entries) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            int separator = trimmed.indexOf('=');
            if (separator <= 0 || separator >= trimmed.length() - 1) {
                Drobiazgi.LOG.warn("Ignoring malformed leaf regrowth rule entry '{}'.", trimmed);
                continue;
            }

            String key = trimmed.substring(0, separator)
                .trim()
                .toLowerCase(Locale.ROOT);
            String value = trimmed.substring(separator + 1)
                .trim();

            switch (key) {
                case "id":
                    id = value.isEmpty() ? id : value;
                    break;
                case "enabled":
                    enabled = parseBoolean(value, enabled);
                    break;
                case "leaf":
                case "leafblock":
                    leafSelector = value;
                    break;
                case "log":
                case "logblock":
                    logSelector = value;
                    break;
                case "leafmeta":
                case "leafmetadata":
                case "meta":
                    metadata = parseMetadataSet(value, id);
                    break;
                case "chance":
                case "respawnchance":
                case "regrowthchance":
                    respawnChance = clamp(parseDouble(value, respawnChance), 0.0D, 1.0D);
                    break;
                default:
                    Drobiazgi.LOG.warn("Unknown leaf regrowth rule key '{}', value '{}'.", key, value);
                    break;
            }
        }

        if (leafSelector.isEmpty() || logSelector.isEmpty()) {
            Drobiazgi.LOG.warn("Skipping leaf regrowth rule '{}' because leaf/log block selector is missing.", id);
            return null;
        }

        Block leafBlock = resolveBlock(leafSelector);
        if (leafBlock == null || leafBlock == Blocks.air) {
            Drobiazgi.LOG.warn(
                "Skipping leaf regrowth rule '{}' due to unknown leaf block '{}'. {}",
                id,
                leafSelector,
                suggestBlocksForDomain(leafSelector));
            return null;
        }

        Block logBlock = resolveBlock(logSelector);
        if (logBlock == null || logBlock == Blocks.air) {
            Drobiazgi.LOG.warn(
                "Skipping leaf regrowth rule '{}' due to unknown log block '{}'. {}",
                id,
                logSelector,
                suggestBlocksForDomain(logSelector));
            return null;
        }

        return new LeafRegrowthRule(id, enabled, leafBlock, logBlock, metadata, respawnChance);
    }

    @SuppressWarnings("unchecked")
    private static String suggestBlocksForDomain(String selector) {
        int colonIndex = selector.indexOf(':');
        if (colonIndex <= 0) {
            return "";
        }

        String domain = selector.substring(0, colonIndex)
            .toLowerCase(Locale.ROOT) + ":";
        List<String> matches = new ArrayList<>();
        for (String key : (Set<String>) Block.blockRegistry.getKeys()) {
            if (key.toLowerCase(Locale.ROOT)
                .startsWith(domain)) {
                matches.add(key);
            }
        }

        if (matches.isEmpty()) {
            return "No blocks registered under domain '" + selector.substring(0, colonIndex) + "'.";
        }

        Collections.sort(matches);
        if (matches.size() > 30) {
            return "Found " + matches.size()
                + " blocks for '"
                + selector.substring(0, colonIndex)
                + "'. First 30: "
                + matches.subList(0, 30);
        }
        return "Available blocks for '" + selector.substring(0, colonIndex) + "': " + matches;
    }

    @SuppressWarnings("unchecked")
    private static Block resolveBlock(String selector) {
        String normalized = selector == null ? "" : selector.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        Block byName = Block.getBlockFromName(normalized);
        if (byName != null) {
            return byName;
        }

        // Try with tile. prefix (some mods register blocks as "modid:tile.name")
        int colonIndex = normalized.indexOf(':');
        if (colonIndex > 0) {
            String domain = normalized.substring(0, colonIndex);
            String path = normalized.substring(colonIndex + 1);
            String tileVariant = domain + ":tile." + path;
            Block byTile = (Block) Block.blockRegistry.getObject(tileVariant);
            if (byTile != null) {
                return byTile;
            }
        }

        // Case-insensitive fallback through the registry
        for (String key : (Set<String>) Block.blockRegistry.getKeys()) {
            if (key.equalsIgnoreCase(normalized)) {
                Block match = (Block) Block.blockRegistry.getObject(key);
                if (match != null) {
                    return match;
                }
            }
        }

        return null;
    }

    private static Set<Integer> parseMetadataSet(String value, String ruleId) {
        if (value == null) {
            return Collections.emptySet();
        }

        String normalized = value.trim();
        if (normalized.isEmpty() || normalized.equals("*") || normalized.equalsIgnoreCase("any")) {
            return Collections.emptySet();
        }

        Set<Integer> metadata = new LinkedHashSet<>();
        String[] parts = normalized.split(",");
        for (String part : parts) {
            String token = part.trim();
            if (token.isEmpty()) {
                continue;
            }

            if (token.contains("..")) {
                addRange(metadata, token.split("\\.\\.", 2), ruleId, token);
                continue;
            }
            if (token.contains(":")) {
                addRange(metadata, token.split(":", 2), ruleId, token);
                continue;
            }
            if (token.contains("-")) {
                addRange(metadata, token.split("-", 2), ruleId, token);
                continue;
            }

            Integer parsedValue = parseMetadataValue(token);
            if (parsedValue == null) {
                Drobiazgi.LOG.warn("Rule '{}' uses invalid leaf metadata '{}'.", ruleId, token);
                continue;
            }
            metadata.add(parsedValue);
        }

        return Collections.unmodifiableSet(metadata);
    }

    private static void addRange(Set<Integer> metadata, String[] bounds, String ruleId, String originalToken) {
        if (bounds.length != 2) {
            Drobiazgi.LOG.warn("Rule '{}' uses invalid metadata range '{}'.", ruleId, originalToken);
            return;
        }

        Integer min = parseMetadataValue(bounds[0].trim());
        Integer max = parseMetadataValue(bounds[1].trim());
        if (min == null || max == null) {
            Drobiazgi.LOG.warn("Rule '{}' uses invalid metadata range '{}'.", ruleId, originalToken);
            return;
        }

        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }

        for (int value = min; value <= max; value++) {
            metadata.add(value);
        }
    }

    private static Integer parseMetadataValue(String token) {
        try {
            int value = Integer.parseInt(token);
            if (value < 0 || value > 15) {
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            return null;
        }
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

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
