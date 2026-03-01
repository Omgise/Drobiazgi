package org.fentanylsolutions.drobiazgi.psychedelicraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;

import cpw.mods.fml.common.Loader;

public final class PsychedelicraftAlcoholRules {

    private static volatile List<PsychedelicraftAlcoholRule> rules = Collections.emptyList();
    private static volatile Map<Item, List<PsychedelicraftAlcoholRule>> rulesByItem = Collections.emptyMap();

    private PsychedelicraftAlcoholRules() {}

    public static synchronized void reloadFromConfig() {
        String[] configuredRules = Config.getPsychedelicraftAlcoholRules();
        if (configuredRules == null || configuredRules.length == 0) {
            rules = Collections.emptyList();
            rulesByItem = Collections.emptyMap();
            Drobiazgi.LOG.info("Loaded 0 Psychedelicraft effect rule(s).");
            return;
        }

        List<PsychedelicraftAlcoholRule> parsedRules = new ArrayList<>();
        Map<String, PsychedelicraftAlcoholRule> parsedById = new LinkedHashMap<>();
        Map<Item, List<PsychedelicraftAlcoholRule>> parsedByItem = new HashMap<>();

        for (int i = 0; i < configuredRules.length; i++) {
            PsychedelicraftAlcoholRule rule = parseRule(configuredRules[i], i);
            if (rule == null) {
                continue;
            }

            if (parsedById.containsKey(rule.getId())) {
                Drobiazgi.LOG.warn("Ignoring duplicate Psychedelicraft alcohol rule id '{}'.", rule.getId());
                continue;
            }

            parsedRules.add(rule);
            parsedById.put(rule.getId(), rule);
            for (Item item : rule.getItems()) {
                parsedByItem.computeIfAbsent(item, ignored -> new ArrayList<>())
                    .add(rule);
            }
        }

        for (Map.Entry<Item, List<PsychedelicraftAlcoholRule>> entry : parsedByItem.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }

        rules = Collections.unmodifiableList(parsedRules);
        rulesByItem = Collections.unmodifiableMap(parsedByItem);

        long enabledCount = parsedRules.stream()
            .filter(PsychedelicraftAlcoholRule::isEnabled)
            .count();
        Drobiazgi.LOG.info("Loaded {} Psychedelicraft effect rule(s), {} enabled.", parsedRules.size(), enabledCount);
        if (Drobiazgi.isDebugMode()) {
            for (PsychedelicraftAlcoholRule rule : parsedRules) {
                Drobiazgi.debug("  rule: " + rule.getDebugSummary());
            }
        }
    }

    public static PsychedelicraftAlcoholRule findMatchingRule(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return null;
        }

        List<PsychedelicraftAlcoholRule> candidates = rulesByItem.get(stack.getItem());
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        for (PsychedelicraftAlcoholRule rule : candidates) {
            if (!rule.isEnabled()) {
                continue;
            }
            if (rule.matches(stack)) {
                return rule;
            }
        }

        return null;
    }

    public static boolean hasRules() {
        return !rules.isEmpty();
    }

    private static PsychedelicraftAlcoholRule parseRule(String line, int index) {
        if (line == null) {
            return null;
        }

        String raw = line.trim();
        if (raw.isEmpty() || raw.startsWith("#")) {
            return null;
        }

        String id = "effect_rule_" + (index + 1);
        boolean enabled = true;
        String requiredModId = "";
        String drugName = "Alcohol";
        List<String> itemSelectors = new ArrayList<>();
        boolean includeLotrAlcoholicMugs = false;
        Set<Integer> metadata = Collections.emptySet();
        int delay = Math.max(0, Config.getPsychedelicraftAlcoholDefaultDelayTicks());
        double influenceSpeed = Math.max(0.0D, Config.getPsychedelicraftAlcoholDefaultInfluenceSpeed());
        double influenceSpeedPlus = Math.max(0.0D, Config.getPsychedelicraftAlcoholDefaultInfluenceSpeedPlus());
        double maxInfluence = -1.0D;
        double lotrAlcoholicityScale = 0.0D;

        String[] entries = raw.split(";");
        for (String entry : entries) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            int separator = trimmed.indexOf('=');
            if (separator <= 0 || separator >= trimmed.length() - 1) {
                Drobiazgi.LOG.warn("Ignoring malformed Psychedelicraft alcohol rule entry '{}'.", trimmed);
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
                case "requiremod":
                case "mod":
                    requiredModId = value.trim()
                        .toLowerCase(Locale.ROOT);
                    break;
                case "drug":
                    if (!value.isEmpty()) {
                        drugName = value;
                    }
                    break;
                case "item":
                    if (!value.isEmpty()) {
                        itemSelectors.add(value);
                    }
                    break;
                case "items":
                    addItemSelectors(itemSelectors, value);
                    break;
                case "lotralcoholicmugs":
                    includeLotrAlcoholicMugs = parseBoolean(value, includeLotrAlcoholicMugs);
                    break;
                case "meta":
                case "metadata":
                    metadata = parseMetadataSet(value, id);
                    break;
                case "delay":
                    delay = Math.max(0, parseInt(value, delay));
                    break;
                case "speed":
                case "influencespeed":
                    influenceSpeed = Math.max(0.0D, parseDouble(value, influenceSpeed));
                    break;
                case "speedplus":
                case "influencespeedplus":
                    influenceSpeedPlus = Math.max(0.0D, parseDouble(value, influenceSpeedPlus));
                    break;
                case "max":
                case "maxinfluence":
                case "potency":
                    maxInfluence = parseDouble(value, maxInfluence);
                    break;
                case "lotralcoholicityscale":
                case "lotrscale":
                    lotrAlcoholicityScale = Math.max(0.0D, parseDouble(value, lotrAlcoholicityScale));
                    break;
                default:
                    Drobiazgi.LOG.warn("Unknown Psychedelicraft effect rule key '{}', value '{}'.", key, value);
                    break;
            }
        }

        if (!requiredModId.isEmpty() && !Loader.isModLoaded(requiredModId)) {
            if (Drobiazgi.isDebugMode()) {
                Drobiazgi.debug(
                    "Skipping Psychedelicraft effect rule '" + id
                        + "' because required mod '"
                        + requiredModId
                        + "' is not loaded.");
            }
            return null;
        }

        if (drugName.trim()
            .isEmpty()) {
            Drobiazgi.LOG.warn("Skipping Psychedelicraft effect rule '{}' because drug name is empty.", id);
            return null;
        }

        if (itemSelectors.isEmpty() && !includeLotrAlcoholicMugs) {
            Drobiazgi.LOG.warn("Skipping Psychedelicraft effect rule '{}' because no item selector was provided.", id);
            return null;
        }

        Set<Item> items = resolveItems(itemSelectors, includeLotrAlcoholicMugs, id);
        if (items.isEmpty()) {
            Drobiazgi.LOG.warn("Skipping Psychedelicraft effect rule '{}' because no items resolved.", id);
            return null;
        }

        if (maxInfluence <= 0.0D && lotrAlcoholicityScale <= 0.0D) {
            Drobiazgi.LOG
                .warn("Skipping Psychedelicraft effect rule '{}' because no usable potency was configured.", id);
            return null;
        }

        return new PsychedelicraftAlcoholRule(
            id,
            enabled,
            drugName.trim(),
            items,
            metadata,
            delay,
            influenceSpeed,
            influenceSpeedPlus,
            maxInfluence,
            lotrAlcoholicityScale);
    }

    private static void addItemSelectors(List<String> itemSelectors, String value) {
        if (value == null) {
            return;
        }

        String[] parts = value.split(",");
        for (String part : parts) {
            String selector = part.trim();
            if (!selector.isEmpty()) {
                itemSelectors.add(selector);
            }
        }
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
                Drobiazgi.LOG.warn("Rule '{}' uses invalid item metadata '{}'.", ruleId, token);
                continue;
            }
            metadata.add(parsedValue);
        }

        return Collections.unmodifiableSet(metadata);
    }

    private static void addRange(Set<Integer> metadata, String[] bounds, String ruleId, String originalToken) {
        if (bounds.length != 2) {
            Drobiazgi.LOG.warn("Rule '{}' uses invalid item metadata range '{}'.", ruleId, originalToken);
            return;
        }

        Integer min = parseMetadataValue(bounds[0].trim());
        Integer max = parseMetadataValue(bounds[1].trim());
        if (min == null || max == null) {
            Drobiazgi.LOG.warn("Rule '{}' uses invalid item metadata range '{}'.", ruleId, originalToken);
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

    @SuppressWarnings("unchecked")
    private static Set<Item> resolveItems(List<String> selectors, boolean includeLotrAlcoholicMugs, String ruleId) {
        Set<Item> items = new LinkedHashSet<>();
        for (String selector : selectors) {
            Item item = resolveItem(selector);
            if (item == null) {
                Drobiazgi.LOG.warn(
                    "Rule '{}' references unknown item '{}'. {}",
                    ruleId,
                    selector,
                    suggestItemsForDomain(selector));
                continue;
            }
            items.add(item);
        }

        if (includeLotrAlcoholicMugs) {
            items.addAll(LotrAlcoholHelper.collectAlcoholicMugs());
        }

        return Collections.unmodifiableSet(items);
    }

    @SuppressWarnings("unchecked")
    private static String suggestItemsForDomain(String selector) {
        int colonIndex = selector.indexOf(':');
        if (colonIndex <= 0) {
            return "";
        }

        String domain = selector.substring(0, colonIndex)
            .toLowerCase(Locale.ROOT) + ":";
        List<String> matches = new ArrayList<>();
        for (String key : (Set<String>) Item.itemRegistry.getKeys()) {
            if (key.toLowerCase(Locale.ROOT)
                .startsWith(domain)) {
                matches.add(key);
            }
        }

        if (matches.isEmpty()) {
            return "No items registered under domain '" + selector.substring(0, colonIndex) + "'.";
        }

        Collections.sort(matches);
        if (matches.size() > 30) {
            return "Found " + matches
                .size() + " items for '" + selector.substring(0, colonIndex) + "'. First 30: " + matches.subList(0, 30);
        }

        return "Available items for '" + selector.substring(0, colonIndex) + "': " + matches;
    }

    @SuppressWarnings("unchecked")
    private static Item resolveItem(String selector) {
        String normalized = selector == null ? "" : selector.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        Item byName = (Item) Item.itemRegistry.getObject(normalized);
        if (byName != null) {
            return byName;
        }

        for (String key : (Set<String>) Item.itemRegistry.getKeys()) {
            if (key.equalsIgnoreCase(normalized)) {
                Item match = (Item) Item.itemRegistry.getObject(key);
                if (match != null) {
                    return match;
                }
            }
        }

        return null;
    }

    private static Integer parseMetadataValue(String token) {
        try {
            int value = Integer.parseInt(token);
            if (value < 0 || value > Short.MAX_VALUE) {
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
}
