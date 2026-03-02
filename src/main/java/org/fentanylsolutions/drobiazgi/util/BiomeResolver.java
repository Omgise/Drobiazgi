package org.fentanylsolutions.drobiazgi.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.world.biome.BiomeGenBase;

import org.fentanylsolutions.drobiazgi.Drobiazgi;

public final class BiomeResolver {

    private final Map<Integer, BiomeGenBase> biomesById;
    private final Map<String, BiomeGenBase> biomesByName;

    private BiomeResolver(Map<Integer, BiomeGenBase> biomesById, Map<String, BiomeGenBase> biomesByName) {
        this.biomesById = biomesById;
        this.biomesByName = biomesByName;
    }

    public static BiomeResolver create() {
        Map<Integer, BiomeGenBase> biomesById = new LinkedHashMap<>();
        Map<String, BiomeGenBase> biomesByName = new LinkedHashMap<>();

        BiomeGenBase[] baseBiomes = BiomeGenBase.getBiomeGenArray();
        if (baseBiomes != null) {
            for (BiomeGenBase biome : baseBiomes) {
                addBiome(biomesById, biomesByName, biome);
            }
        }

        addLotrBiomesReflective(biomesById, biomesByName);
        return new BiomeResolver(biomesById, biomesByName);
    }

    public BiomeGenBase resolveBiome(String selector) {
        ParsedSelector parsedSelector = parseSelector(selector);
        if (parsedSelector == null) {
            return null;
        }

        if (!parsedSelector.forceName) {
            try {
                int id = Integer.parseInt(parsedSelector.idSelector);
                if (biomesById.containsKey(id)) {
                    return biomesById.get(id);
                }
            } catch (NumberFormatException ignored) {
                // Selector is not a numeric ID.
            }
        }

        return biomesByName.get(normalizeName(parsedSelector.nameSelector));
    }

    public Integer resolveBiomeId(String selector) {
        ParsedSelector parsedSelector = parseSelector(selector);
        if (parsedSelector == null) {
            return null;
        }

        if (!parsedSelector.forceName) {
            try {
                int id = Integer.parseInt(parsedSelector.idSelector);
                if (biomesById.containsKey(id)) {
                    return id;
                }
            } catch (NumberFormatException ignored) {
                // Selector is not a numeric ID.
            }
        }

        BiomeGenBase biome = biomesByName.get(normalizeName(parsedSelector.nameSelector));
        return biome == null ? null : biome.biomeID;
    }

    private static ParsedSelector parseSelector(String selector) {
        if (selector == null) {
            return null;
        }

        String normalizedSelector = selector.trim();
        if (normalizedSelector.isEmpty()) {
            return null;
        }

        String idSelector = normalizedSelector;
        String nameSelector = normalizedSelector;
        boolean forceName = false;

        if (normalizedSelector.regionMatches(true, 0, "id:", 0, 3)) {
            idSelector = normalizedSelector.substring(3)
                .trim();
        } else if (normalizedSelector.regionMatches(true, 0, "name:", 0, 5)) {
            nameSelector = normalizedSelector.substring(5)
                .trim();
            forceName = true;
        }

        return new ParsedSelector(idSelector, nameSelector, forceName);
    }

    private static void addLotrBiomesReflective(Map<Integer, BiomeGenBase> biomesById,
        Map<String, BiomeGenBase> biomesByName) {
        try {
            Class<?> lotrDimensionClass = Class.forName("lotr.common.LOTRDimension");
            Field biomeListField = lotrDimensionClass.getField("biomeList");
            biomeListField.setAccessible(true);

            for (Field field : lotrDimensionClass.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || !lotrDimensionClass.isAssignableFrom(field.getType())) {
                    continue;
                }

                field.setAccessible(true);
                Object dimension = field.get(null);
                if (dimension == null) {
                    continue;
                }

                Object biomeList = biomeListField.get(dimension);
                addBiomeContainer(biomesById, biomesByName, biomeList);
            }
        } catch (ClassNotFoundException ignored) {
            // LOTR is not present.
        } catch (Exception e) {
            if (Drobiazgi.isDebugMode()) {
                Drobiazgi.debug("Failed to collect LOTR biomes reflectively: " + e.getMessage());
            }
        }
    }

    private static void addBiomeContainer(Map<Integer, BiomeGenBase> biomesById, Map<String, BiomeGenBase> biomesByName,
        Object container) {
        if (container == null) {
            return;
        }

        if (container instanceof Iterable<?>) {
            for (Object entry : (Iterable<?>) container) {
                if (entry instanceof BiomeGenBase) {
                    addBiome(biomesById, biomesByName, (BiomeGenBase) entry);
                }
            }
            return;
        }

        if (container.getClass()
            .isArray()) {
            int length = Array.getLength(container);
            for (int i = 0; i < length; i++) {
                Object entry = Array.get(container, i);
                if (entry instanceof BiomeGenBase) {
                    addBiome(biomesById, biomesByName, (BiomeGenBase) entry);
                }
            }
        }
    }

    private static void addBiome(Map<Integer, BiomeGenBase> biomesById, Map<String, BiomeGenBase> biomesByName,
        BiomeGenBase biome) {
        if (biome == null || biome.biomeName == null) {
            return;
        }

        biomesById.putIfAbsent(biome.biomeID, biome);
        biomesByName.putIfAbsent(normalizeName(biome.biomeName), biome);
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return "";
        }

        String trimmed = name.trim();
        StringBuilder normalized = new StringBuilder(trimmed.length());
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                normalized.append(Character.toLowerCase(c));
            }
        }
        return normalized.toString();
    }

    private static final class ParsedSelector {

        private final String idSelector;
        private final String nameSelector;
        private final boolean forceName;

        private ParsedSelector(String idSelector, String nameSelector, boolean forceName) {
            this.idSelector = idSelector;
            this.nameSelector = nameSelector;
            this.forceName = forceName;
        }
    }
}
