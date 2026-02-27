package org.fentanylsolutions.drobiazgi.customnpcs;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.world.biome.BiomeGenBase;

import org.fentanylsolutions.drobiazgi.Drobiazgi;

final class BiomeResolver {

    private final Map<Integer, BiomeGenBase> biomesById;
    private final Map<String, Integer> biomeIdsByName;

    private BiomeResolver(Map<Integer, BiomeGenBase> biomesById, Map<String, Integer> biomeIdsByName) {
        this.biomesById = biomesById;
        this.biomeIdsByName = biomeIdsByName;
    }

    static BiomeResolver create() {
        Map<Integer, BiomeGenBase> biomesById = new LinkedHashMap<>();
        Map<String, Integer> biomeIdsByName = new LinkedHashMap<>();

        BiomeGenBase[] baseBiomes = BiomeGenBase.getBiomeGenArray();
        if (baseBiomes != null) {
            for (BiomeGenBase biome : baseBiomes) {
                addBiome(biomesById, biomeIdsByName, biome);
            }
        }

        addLotrBiomesReflective(biomesById, biomeIdsByName);
        return new BiomeResolver(biomesById, biomeIdsByName);
    }

    Integer resolveBiomeId(String selector) {
        if (selector == null) {
            return null;
        }

        String normalizedSelector = selector.trim();
        if (normalizedSelector.isEmpty()) {
            return null;
        }

        String idSelector = normalizedSelector;
        String nameSelector = normalizedSelector;

        if (normalizedSelector.regionMatches(true, 0, "id:", 0, 3)) {
            idSelector = normalizedSelector.substring(3)
                .trim();
        } else if (normalizedSelector.regionMatches(true, 0, "name:", 0, 5)) {
            nameSelector = normalizedSelector.substring(5)
                .trim();
        }

        try {
            int id = Integer.parseInt(idSelector);
            if (biomesById.containsKey(id)) {
                return id;
            }
        } catch (NumberFormatException ignored) {
            // Selector is not a numeric ID.
        }

        return biomeIdsByName.get(normalizeName(nameSelector));
    }

    private static void addLotrBiomesReflective(Map<Integer, BiomeGenBase> biomesById,
        Map<String, Integer> biomeIdsByName) {
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
                addBiomeContainer(biomesById, biomeIdsByName, biomeList);
            }
        } catch (ClassNotFoundException ignored) {
            // LOTR is not present.
        } catch (Exception e) {
            if (Drobiazgi.isDebugMode()) {
                Drobiazgi.debug("Failed to collect LOTR biomes reflectively: " + e.getMessage());
            }
        }
    }

    private static void addBiomeContainer(Map<Integer, BiomeGenBase> biomesById, Map<String, Integer> biomeIdsByName,
        Object container) {
        if (container == null) {
            return;
        }

        if (container instanceof Iterable<?>) {
            for (Object entry : (Iterable<?>) container) {
                if (entry instanceof BiomeGenBase) {
                    addBiome(biomesById, biomeIdsByName, (BiomeGenBase) entry);
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
                    addBiome(biomesById, biomeIdsByName, (BiomeGenBase) entry);
                }
            }
        }
    }

    private static void addBiome(Map<Integer, BiomeGenBase> biomesById, Map<String, Integer> biomeIdsByName,
        BiomeGenBase biome) {
        if (biome == null || biome.biomeName == null) {
            return;
        }

        biomesById.putIfAbsent(biome.biomeID, biome);
        biomeIdsByName.putIfAbsent(normalizeName(biome.biomeName), biome.biomeID);
    }

    private static String normalizeName(String name) {
        return name == null ? ""
            : name.trim()
                .toLowerCase();
    }
}
