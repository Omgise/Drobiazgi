package org.fentanylsolutions.drobiazgi.oceancraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;

import com.Oceancraft.common.EntityWhale;
import com.Oceancraft.common.Oceancraft;

import cpw.mods.fml.common.registry.EntityRegistry;

public final class OceanCraftWhaleManager {

    private static final String HUMPBACK_ENTITY_NAME = "OCHumpbackWhale";
    private static final String NARWHAL_ENTITY_NAME = "OCNarwhal";
    private static final String BLUE_WHALE_ENTITY_NAME = "OCBlueWhale";
    private static final String HUMPBACK_GLOBAL_NAME = Drobiazgi.MODID + "." + HUMPBACK_ENTITY_NAME;
    private static final String NARWHAL_GLOBAL_NAME = Drobiazgi.MODID + "." + NARWHAL_ENTITY_NAME;
    private static final String BLUE_WHALE_GLOBAL_NAME = Drobiazgi.MODID + "." + BLUE_WHALE_ENTITY_NAME;
    private static final int HUMPBACK_ENTITY_ID = 0;
    private static final int NARWHAL_ENTITY_ID = 1;
    private static final int BLUE_WHALE_ENTITY_ID = 2;
    private static final int TRACKING_RANGE = 80;
    private static final int UPDATE_FREQUENCY = 3;
    private static final int HUMPBACK_EGG_PRIMARY_COLOR = 2446425;
    private static final int HUMPBACK_EGG_SECONDARY_COLOR = 8960703;
    private static final int NARWHAL_EGG_PRIMARY_COLOR = 6842472;
    private static final int NARWHAL_EGG_SECONDARY_COLOR = 15592941;
    private static final int BLUE_WHALE_EGG_PRIMARY_COLOR = 2504262;
    private static final int BLUE_WHALE_EGG_SECONDARY_COLOR = 10334174;
    private static boolean entitiesRegistered;
    private static int humpbackGlobalEntityId = -1;
    private static int narwhalGlobalEntityId = -1;
    private static int blueWhaleGlobalEntityId = -1;

    private OceanCraftWhaleManager() {}

    public static synchronized void registerCustomEntities() {
        if (entitiesRegistered) {
            return;
        }

        humpbackGlobalEntityId = registerGlobalEntityId(EntityHumpbackWhale.class, HUMPBACK_GLOBAL_NAME);
        narwhalGlobalEntityId = registerGlobalEntityId(EntityNarwhal.class, NARWHAL_GLOBAL_NAME);
        blueWhaleGlobalEntityId = registerGlobalEntityId(EntityBlueWhale.class, BLUE_WHALE_GLOBAL_NAME);
        EntityRegistry.registerModEntity(
            EntityHumpbackWhale.class,
            HUMPBACK_ENTITY_NAME,
            HUMPBACK_ENTITY_ID,
            Drobiazgi.MODID,
            TRACKING_RANGE,
            UPDATE_FREQUENCY,
            false);
        EntityRegistry.registerModEntity(
            EntityNarwhal.class,
            NARWHAL_ENTITY_NAME,
            NARWHAL_ENTITY_ID,
            Drobiazgi.MODID,
            TRACKING_RANGE,
            UPDATE_FREQUENCY,
            false);
        EntityRegistry.registerModEntity(
            EntityBlueWhale.class,
            BLUE_WHALE_ENTITY_NAME,
            BLUE_WHALE_ENTITY_ID,
            Drobiazgi.MODID,
            TRACKING_RANGE,
            UPDATE_FREQUENCY,
            false);
        entitiesRegistered = true;
    }

    public static synchronized void applySpawnEggs() {
        if (!entitiesRegistered) {
            registerCustomEntities();
        }

        removeSpawnEgg(humpbackGlobalEntityId);
        removeSpawnEgg(narwhalGlobalEntityId);
        removeSpawnEgg(blueWhaleGlobalEntityId);

        if (!Config.isOceanCraftWhaleSplitEnabled()) {
            return;
        }

        EntityList.entityEggs.remove(Integer.valueOf(Oceancraft.WhaleId));
        addSpawnEgg(humpbackGlobalEntityId, HUMPBACK_EGG_PRIMARY_COLOR, HUMPBACK_EGG_SECONDARY_COLOR);
        addSpawnEgg(narwhalGlobalEntityId, NARWHAL_EGG_PRIMARY_COLOR, NARWHAL_EGG_SECONDARY_COLOR);
        addSpawnEgg(blueWhaleGlobalEntityId, BLUE_WHALE_EGG_PRIMARY_COLOR, BLUE_WHALE_EGG_SECONDARY_COLOR);
    }

    public static void addNaturalWhaleSpawns(int baseWeight, int minGroupCount, int maxGroupCount,
        EnumCreatureType creatureType, BiomeGenBase... biomes) {
        if (!Config.isOceanCraftWhaleSplitEnabled()) {
            EntityRegistry.addSpawn(EntityWhale.class, baseWeight, minGroupCount, maxGroupCount, creatureType, biomes);
            return;
        }

        int totalRelativeWeight = getTotalRelativeWeight();
        if (totalRelativeWeight <= 0) {
            EntityRegistry.addSpawn(EntityWhale.class, baseWeight, minGroupCount, maxGroupCount, creatureType, biomes);
            return;
        }

        addScaledSpawn(
            EntityHumpbackWhale.class,
            baseWeight,
            Config.getOceanCraftHumpbackWeight(),
            totalRelativeWeight,
            minGroupCount,
            maxGroupCount,
            creatureType,
            biomes);
        addScaledSpawn(
            EntityNarwhal.class,
            baseWeight,
            Config.getOceanCraftNarwhalWeight(),
            totalRelativeWeight,
            minGroupCount,
            maxGroupCount,
            creatureType,
            biomes);
        addScaledSpawn(
            EntityBlueWhale.class,
            baseWeight,
            Config.getOceanCraftBlueWhaleWeight(),
            totalRelativeWeight,
            minGroupCount,
            maxGroupCount,
            creatureType,
            biomes);
    }

    public static Entity getConfiguredSpawnReplacement(World world, Entity originalEntity) {
        if (!Config.isOceanCraftWhaleSplitEnabled() || !(originalEntity instanceof EntityWhale)) {
            return originalEntity;
        }

        Entity replacement = createWeightedWhale(world);
        if (replacement == null) {
            return originalEntity;
        }

        replacement.setLocationAndAngles(
            originalEntity.posX,
            originalEntity.posY,
            originalEntity.posZ,
            originalEntity.rotationYaw,
            originalEntity.rotationPitch);
        return replacement;
    }

    private static void addScaledSpawn(Class<? extends EntityWhale> entityClass, int baseWeight, int relativeWeight,
        int totalRelativeWeight, int minGroupCount, int maxGroupCount, EnumCreatureType creatureType,
        BiomeGenBase... biomes) {
        int scaledWeight = getScaledSpawnWeight(baseWeight, relativeWeight, totalRelativeWeight);
        if (scaledWeight <= 0) {
            return;
        }

        EntityRegistry.addSpawn(entityClass, scaledWeight, minGroupCount, maxGroupCount, creatureType, biomes);
    }

    private static int getScaledSpawnWeight(int baseWeight, int relativeWeight, int totalRelativeWeight) {
        if (baseWeight <= 0 || relativeWeight <= 0 || totalRelativeWeight <= 0) {
            return 0;
        }

        int scaledWeight = (baseWeight * relativeWeight) / totalRelativeWeight;
        return Math.max(1, scaledWeight);
    }

    private static Entity createWeightedWhale(World world) {
        int totalRelativeWeight = getTotalRelativeWeight();
        if (totalRelativeWeight <= 0) {
            return null;
        }

        int pick = world.rand.nextInt(totalRelativeWeight);
        pick -= Config.getOceanCraftHumpbackWeight();
        if (pick < 0 && Config.getOceanCraftHumpbackWeight() > 0) {
            return new EntityHumpbackWhale(world);
        }

        pick -= Config.getOceanCraftNarwhalWeight();
        if (pick < 0 && Config.getOceanCraftNarwhalWeight() > 0) {
            return new EntityNarwhal(world);
        }

        if (Config.getOceanCraftBlueWhaleWeight() > 0) {
            return new EntityBlueWhale(world);
        }

        if (Config.getOceanCraftNarwhalWeight() > 0) {
            return new EntityNarwhal(world);
        }
        if (Config.getOceanCraftHumpbackWeight() > 0) {
            return new EntityHumpbackWhale(world);
        }
        return null;
    }

    private static int getTotalRelativeWeight() {
        return Math.max(0, Config.getOceanCraftHumpbackWeight()) + Math.max(0, Config.getOceanCraftNarwhalWeight())
            + Math.max(0, Config.getOceanCraftBlueWhaleWeight());
    }

    private static int registerGlobalEntityId(Class<? extends Entity> entityClass, String globalName) {
        int entityId = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(entityClass, globalName, entityId);
        return entityId;
    }

    private static void addSpawnEgg(int entityId, int primaryColor, int secondaryColor) {
        if (entityId < 0) {
            return;
        }

        EntityList.entityEggs
            .put(Integer.valueOf(entityId), new EntityList.EntityEggInfo(entityId, primaryColor, secondaryColor));
    }

    private static void removeSpawnEgg(int entityId) {
        if (entityId < 0) {
            return;
        }

        EntityList.entityEggs.remove(Integer.valueOf(entityId));
    }
}
