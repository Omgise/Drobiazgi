package org.fentanylsolutions.drobiazgi.biometags;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

public final class BiomeDictionaryCompat {

    private static final Object LOCK = new Object();
    private static final Map<BiomeGenBase, Set<BiomeDictionary.Type>> COMPAT_TYPES_BY_BIOME = new IdentityHashMap<>();
    private static final Map<BiomeDictionary.Type, Set<BiomeGenBase>> COMPAT_BIOMES_BY_TYPE = new HashMap<>();

    private BiomeDictionaryCompat() {}

    public static boolean shouldUseCompatStorage(BiomeGenBase biome) {
        if (biome == null) {
            return false;
        }

        BiomeGenBase[] biomeArray = BiomeGenBase.getBiomeGenArray();
        int biomeId = biome.biomeID;
        return biomeId < 0 || biomeId >= biomeArray.length || biomeArray[biomeId] != biome;
    }

    public static boolean registerCompatBiomeType(BiomeGenBase biome, BiomeDictionary.Type... types) {
        if (!shouldUseCompatStorage(biome) || types == null || types.length == 0) {
            return false;
        }

        boolean changed = false;
        synchronized (LOCK) {
            Set<BiomeDictionary.Type> storedTypes = COMPAT_TYPES_BY_BIOME
                .computeIfAbsent(biome, ignored -> new LinkedHashSet<>());
            for (BiomeDictionary.Type type : types) {
                for (BiomeDictionary.Type expandedType : expandType(type)) {
                    if (storedTypes.add(expandedType)) {
                        COMPAT_BIOMES_BY_TYPE.computeIfAbsent(expandedType, ignored -> new LinkedHashSet<>())
                            .add(biome);
                        changed = true;
                    }
                }
            }
        }

        return changed;
    }

    public static boolean isCompatBiomeRegistered(BiomeGenBase biome) {
        if (!shouldUseCompatStorage(biome)) {
            return false;
        }

        synchronized (LOCK) {
            return COMPAT_TYPES_BY_BIOME.containsKey(biome);
        }
    }

    public static BiomeDictionary.Type[] getCompatTypesForBiome(BiomeGenBase biome) {
        if (!shouldUseCompatStorage(biome)) {
            return new BiomeDictionary.Type[0];
        }

        synchronized (LOCK) {
            Set<BiomeDictionary.Type> storedTypes = COMPAT_TYPES_BY_BIOME.get(biome);
            if (storedTypes == null || storedTypes.isEmpty()) {
                return new BiomeDictionary.Type[0];
            }
            return storedTypes.toArray(new BiomeDictionary.Type[0]);
        }
    }

    public static boolean isCompatBiomeOfType(BiomeGenBase biome, BiomeDictionary.Type type) {
        if (!shouldUseCompatStorage(biome) || type == null) {
            return false;
        }

        synchronized (LOCK) {
            Set<BiomeDictionary.Type> storedTypes = COMPAT_TYPES_BY_BIOME.get(biome);
            if (storedTypes == null || storedTypes.isEmpty()) {
                return false;
            }
            for (BiomeDictionary.Type expandedType : expandType(type)) {
                if (storedTypes.contains(expandedType)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static BiomeGenBase[] appendCompatBiomesForType(BiomeDictionary.Type type, BiomeGenBase[] baseBiomes) {
        if (type == null) {
            return baseBiomes == null ? new BiomeGenBase[0] : baseBiomes;
        }

        LinkedHashSet<BiomeGenBase> merged = new LinkedHashSet<>();
        if (baseBiomes != null && baseBiomes.length > 0) {
            merged.addAll(Arrays.asList(baseBiomes));
        }

        synchronized (LOCK) {
            for (BiomeDictionary.Type expandedType : expandType(type)) {
                Set<BiomeGenBase> storedBiomes = COMPAT_BIOMES_BY_TYPE.get(expandedType);
                if (storedBiomes != null && !storedBiomes.isEmpty()) {
                    merged.addAll(storedBiomes);
                }
            }
        }

        return merged.toArray(new BiomeGenBase[0]);
    }

    public static boolean areBiomesEquivalent(BiomeGenBase biomeA, BiomeGenBase biomeB) {
        if (biomeA == null || biomeB == null) {
            return false;
        }

        Set<BiomeDictionary.Type> biomeATypes = getEffectiveTypes(biomeA);
        if (biomeATypes.isEmpty()) {
            return false;
        }

        Set<BiomeDictionary.Type> biomeBTypes = getEffectiveTypes(biomeB);
        for (BiomeDictionary.Type type : biomeATypes) {
            if (biomeBTypes.contains(type)) {
                return true;
            }
        }
        return false;
    }

    private static Set<BiomeDictionary.Type> getEffectiveTypes(BiomeGenBase biome) {
        if (biome == null) {
            return Collections.emptySet();
        }
        if (shouldUseCompatStorage(biome)) {
            synchronized (LOCK) {
                Set<BiomeDictionary.Type> storedTypes = COMPAT_TYPES_BY_BIOME.get(biome);
                return storedTypes == null ? Collections.emptySet() : new LinkedHashSet<>(storedTypes);
            }
        }
        return new LinkedHashSet<>(Arrays.asList(BiomeDictionary.getTypesForBiome(biome)));
    }

    private static BiomeDictionary.Type[] expandType(BiomeDictionary.Type type) {
        if (type == null) {
            return new BiomeDictionary.Type[0];
        }

        if (type == BiomeDictionary.Type.WATER) {
            return new BiomeDictionary.Type[] { BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.RIVER };
        }
        if (type == BiomeDictionary.Type.DESERT) {
            return new BiomeDictionary.Type[] { BiomeDictionary.Type.SANDY };
        }
        if (type == BiomeDictionary.Type.FROZEN) {
            return new BiomeDictionary.Type[] { BiomeDictionary.Type.SNOWY };
        }
        return new BiomeDictionary.Type[] { type };
    }
}
