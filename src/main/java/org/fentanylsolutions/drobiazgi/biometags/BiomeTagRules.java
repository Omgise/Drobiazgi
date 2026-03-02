package org.fentanylsolutions.drobiazgi.biometags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;
import org.fentanylsolutions.drobiazgi.util.BiomeResolver;

import cpw.mods.fml.common.Loader;

public final class BiomeTagRules {

    private static volatile List<BiomeTagRule> rules = Collections.emptyList();

    private BiomeTagRules() {}

    public static synchronized void reloadFromConfig() {
        List<BiomeTagRule> parsedRules = new ArrayList<>();

        String[] configuredRules = Config.getBiomeTagRules();
        if (configuredRules == null) {
            rules = Collections.emptyList();
            return;
        }

        for (int i = 0; i < configuredRules.length; i++) {
            BiomeTagRule rule = parseRule(configuredRules[i], i);
            if (rule != null) {
                parsedRules.add(rule);
            }
        }

        rules = Collections.unmodifiableList(parsedRules);
        Drobiazgi.LOG.info("Loaded {} biome tag rule(s).", rules.size());
        if (Drobiazgi.isDebugMode()) {
            for (BiomeTagRule rule : rules) {
                Drobiazgi.debug("Biome tag rule loaded: " + rule.getDebugSummary());
            }
        }
    }

    public static synchronized void applyConfiguredTags() {
        if (!Config.isBiomeTaggingEnabled()) {
            Drobiazgi.LOG.info("Biome tag overrides are disabled.");
            return;
        }

        List<BiomeTagRule> currentRules = rules;
        if (currentRules.isEmpty()) {
            Drobiazgi.LOG.info("No biome tag rules to apply.");
            return;
        }

        BiomeResolver biomeResolver = BiomeResolver.create();
        int appliedTagAssignments = 0;
        int matchedBiomes = 0;

        for (BiomeTagRule rule : currentRules) {
            if (!rule.isEnabled()) {
                continue;
            }
            String requiredModId = rule.getRequiredModId();
            if (!requiredModId.isEmpty() && !Loader.isModLoaded(requiredModId)) {
                continue;
            }

            List<BiomeDictionary.Type> ruleTypes = resolveTypes(rule);
            if (ruleTypes.isEmpty()) {
                Drobiazgi.LOG.warn("Skipping biome tag rule '{}' because it has no valid tags.", rule.getId());
                continue;
            }

            Set<BiomeGenBase> resolvedBiomes = resolveBiomes(rule, biomeResolver);
            if (resolvedBiomes.isEmpty()) {
                Drobiazgi.LOG.warn("Biome tag rule '{}' did not match any biome.", rule.getId());
                continue;
            }

            matchedBiomes += resolvedBiomes.size();
            for (BiomeGenBase biome : resolvedBiomes) {
                List<BiomeDictionary.Type> missingTypes = new ArrayList<>();
                for (BiomeDictionary.Type type : ruleTypes) {
                    if (!BiomeDictionary.isBiomeOfType(biome, type)) {
                        missingTypes.add(type);
                    }
                }
                if (missingTypes.isEmpty()) {
                    continue;
                }

                if (BiomeDictionary.registerBiomeType(biome, missingTypes.toArray(new BiomeDictionary.Type[0]))) {
                    appliedTagAssignments += missingTypes.size();
                    if (Drobiazgi.isDebugMode()) {
                        Drobiazgi.debug(
                            "Applied biome tags " + missingTypes
                                + " to "
                                + biome.biomeName
                                + " ("
                                + biome.biomeID
                                + ") via rule "
                                + rule.getId());
                    }
                } else {
                    Drobiazgi.LOG.warn(
                        "Failed to apply biome tags {} to {} ({}) via rule '{}'.",
                        missingTypes,
                        biome.biomeName,
                        biome.biomeID,
                        rule.getId());
                }
            }
        }

        Drobiazgi.LOG.info(
            "Applied {} biome tag assignment(s) across {} matched biome(s).",
            appliedTagAssignments,
            matchedBiomes);
    }

    private static BiomeTagRule parseRule(String line, int index) {
        if (line == null) {
            return null;
        }

        String rawLine = line.trim();
        if (rawLine.isEmpty() || rawLine.startsWith("#")) {
            return null;
        }

        String id = "rule_" + (index + 1);
        boolean enabled = true;
        String requiredModId = "";
        List<String> biomeSelectors = Collections.emptyList();
        List<String> tagNames = Collections.emptyList();

        String[] entries = rawLine.split(";");
        for (String entry : entries) {
            String trimmedEntry = entry.trim();
            if (trimmedEntry.isEmpty()) {
                continue;
            }

            int separatorIndex = trimmedEntry.indexOf('=');
            if (separatorIndex <= 0 || separatorIndex >= trimmedEntry.length() - 1) {
                Drobiazgi.LOG.warn("Ignoring malformed biome tag rule entry '{}'.", trimmedEntry);
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
                case "requiremod":
                case "mod":
                    requiredModId = value.toLowerCase(Locale.ROOT);
                    break;
                case "biome":
                case "biomes":
                    biomeSelectors = parseCsv(value, false);
                    break;
                case "tag":
                case "tags":
                case "type":
                case "types":
                    tagNames = parseCsv(value, true);
                    break;
                default:
                    Drobiazgi.LOG.warn("Unknown biome tag rule key '{}', value '{}'.", key, value);
                    break;
            }
        }

        if (biomeSelectors.isEmpty()) {
            Drobiazgi.LOG.warn("Skipping biome tag rule '{}' because it has no biome selectors.", id);
            return null;
        }
        if (tagNames.isEmpty()) {
            Drobiazgi.LOG.warn("Skipping biome tag rule '{}' because it has no tags.", id);
            return null;
        }

        return new BiomeTagRule(id, enabled, requiredModId, biomeSelectors, tagNames);
    }

    private static Set<BiomeGenBase> resolveBiomes(BiomeTagRule rule, BiomeResolver biomeResolver) {
        Set<BiomeGenBase> resolvedBiomes = new LinkedHashSet<>();
        for (String selector : rule.getBiomeSelectors()) {
            BiomeGenBase biome = biomeResolver.resolveBiome(selector);
            if (biome == null) {
                Drobiazgi.LOG.warn("Biome tag rule '{}' references unknown biome '{}'.", rule.getId(), selector);
                continue;
            }
            resolvedBiomes.add(biome);
        }
        return resolvedBiomes;
    }

    private static List<BiomeDictionary.Type> resolveTypes(BiomeTagRule rule) {
        List<BiomeDictionary.Type> resolvedTypes = new ArrayList<>();
        Set<String> seenTypeNames = new LinkedHashSet<>();
        for (String tagName : rule.getTagNames()) {
            if (!seenTypeNames.add(tagName)) {
                continue;
            }
            resolvedTypes.add(BiomeDictionary.Type.getType(tagName));
        }
        return resolvedTypes;
    }

    private static List<String> parseCsv(String value, boolean upperCase) {
        if (value == null || value.trim()
            .isEmpty()) {
            return Collections.emptyList();
        }

        List<String> values = new ArrayList<>();
        Set<String> seenValues = new LinkedHashSet<>();
        for (String part : value.split(",")) {
            String trimmedPart = part.trim();
            if (trimmedPart.isEmpty()) {
                continue;
            }

            String parsedValue = upperCase ? trimmedPart.toUpperCase(Locale.ROOT) : trimmedPart;
            if (seenValues.add(parsedValue)) {
                values.add(parsedValue);
            }
        }
        return values;
    }

    private static boolean parseBoolean(String value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value) || "0".equalsIgnoreCase(value)) {
            return false;
        }
        return fallback;
    }
}
