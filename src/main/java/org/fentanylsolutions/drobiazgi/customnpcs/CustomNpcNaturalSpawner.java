package org.fentanylsolutions.drobiazgi.customnpcs;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.NBTJsonUtil;

public final class CustomNpcNaturalSpawner {

    private static final CustomNpcNaturalSpawner INSTANCE = new CustomNpcNaturalSpawner();

    private final Set<String> missingCloneWarnings = new HashSet<>();
    private boolean registered = false;

    private CustomNpcNaturalSpawner() {}

    public static synchronized void register() {
        if (INSTANCE.registered) {
            return;
        }

        FMLCommonHandler.instance()
            .bus()
            .register(INSTANCE);
        INSTANCE.registered = true;
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.world == null || event.world.isRemote) {
            return;
        }
        if (!(event.world instanceof WorldServer) || !Config.isCustomNpcsSpawningEnabled()) {
            return;
        }

        WorldServer world = (WorldServer) event.world;
        int tickInterval = Math.max(1, Config.getCustomNpcsSpawnTickInterval());
        if (world.getWorldInfo()
            .getWorldTotalTime() % tickInterval != 0L) {
            return;
        }

        int maxNpcsPerDimension = Math.max(1, Config.getCustomNpcsMaxNpcsPerDimension());
        int npcCount = countCustomNpcs(world);
        if (npcCount >= maxNpcsPerDimension) {
            if (Drobiazgi.isDebugMode()) {
                Drobiazgi.debug(
                    "CustomNPC spawn cycle skipped in dim " + world.provider.dimensionId
                        + ": npc cap reached ("
                        + npcCount
                        + "/"
                        + maxNpcsPerDimension
                        + ").");
            }
            return;
        }

        if (world.playerEntities == null || world.playerEntities.isEmpty()) {
            if (Drobiazgi.isDebugMode()) {
                Drobiazgi.debug(
                    "CustomNPC spawn cycle skipped in dim " + world.provider.dimensionId + ": no players in world.");
            }
            return;
        }

        int attemptsPerPlayer = Math.max(1, Config.getCustomNpcsAttemptsPerPlayerPerCycle());
        int minPlayerDistance = Math.max(0, Config.getCustomNpcsMinPlayerDistance());
        int maxPlayerDistance = Math.max(1, Config.getCustomNpcsMaxPlayerDistance());
        if (minPlayerDistance > maxPlayerDistance) {
            int tmp = minPlayerDistance;
            minPlayerDistance = maxPlayerDistance;
            maxPlayerDistance = tmp;
        }

        int maxSpawnsPerCycle = Math.max(1, Config.getCustomNpcsMaxSpawnsPerCycle());
        int successfulSpawns = 0;
        Map<String, NBTTagCompound> cloneTemplateCache = new HashMap<>();

        boolean debug = Drobiazgi.isDebugMode();
        int attempts = 0;
        int noSpawnPosition = 0;
        int noBiome = 0;
        int noMatchingRule = 0;
        int chanceRejected = 0;
        int missingClone = 0;
        int invalidSpawnPosition = 0;
        int offsetNoBiome = 0;
        int offsetRuleRejected = 0;
        int nullEntitySpawn = 0;
        int caveNotFound = 0;
        int waterRejected = 0;
        int detailedNoMatchLogsLeft = 4;
        int detailedInvalidPosLogsLeft = 4;
        int detailedChanceLogsLeft = 4;

        if (debug) {
            Drobiazgi.debug(
                "CustomNPC spawn cycle start: dim=" + world.provider.dimensionId
                    + ", day="
                    + world.isDaytime()
                    + ", players="
                    + world.playerEntities.size()
                    + ", npcCount="
                    + npcCount
                    + "/"
                    + maxNpcsPerDimension
                    + ", rules="
                    + CustomNpcSpawnRules.getRules()
                        .size()
                    + ", interval="
                    + tickInterval
                    + ", attemptsPerPlayer="
                    + attemptsPerPlayer
                    + ", maxSpawnsPerCycle="
                    + maxSpawnsPerCycle);
        }

        for (Object playerObj : world.playerEntities) {
            if (!(playerObj instanceof EntityPlayer)) {
                continue;
            }
            EntityPlayer player = (EntityPlayer) playerObj;
            if (player.isDead) {
                continue;
            }

            for (int attempt = 0; attempt < attemptsPerPlayer; attempt++) {
                attempts++;
                if (npcCount >= maxNpcsPerDimension || successfulSpawns >= maxSpawnsPerCycle) {
                    if (debug) {
                        Drobiazgi.debug(
                            "CustomNPC spawn cycle early-stop: npcCount=" + npcCount
                                + "/"
                                + maxNpcsPerDimension
                                + ", spawnedThisCycle="
                                + successfulSpawns
                                + "/"
                                + maxSpawnsPerCycle
                                + ", attempts="
                                + attempts
                                + ", noSpawnPos="
                                + noSpawnPosition
                                + ", noBiome="
                                + noBiome
                                + ", noRule="
                                + noMatchingRule
                                + ", chanceRejected="
                                + chanceRejected
                                + ", missingClone="
                                + missingClone
                                + ", invalidPos="
                                + invalidSpawnPosition
                                + ", offsetNoBiome="
                                + offsetNoBiome
                                + ", offsetRuleRejected="
                                + offsetRuleRejected
                                + ", nullEntitySpawn="
                                + nullEntitySpawn
                                + ", caveNotFound="
                                + caveNotFound
                                + ", waterRejected="
                                + waterRejected);
                    }
                    return;
                }

                SpawnPosition spawnPosition = getSpawnPosition(world, player, minPlayerDistance, maxPlayerDistance);
                if (spawnPosition == null) {
                    noSpawnPosition++;
                    continue;
                }

                BiomeGenBase biome = world.getBiomeGenForCoords(spawnPosition.x, spawnPosition.z);
                if (biome == null) {
                    noBiome++;
                    continue;
                }

                int lightLevel = world.getBlockLightValue(spawnPosition.x, spawnPosition.y, spawnPosition.z);
                CustomNpcSpawnRule rule = CustomNpcSpawnRules
                    .pickRule(world, biome.biomeID, spawnPosition.y, lightLevel, world.rand);
                if (rule == null) {
                    noMatchingRule++;
                    if (debug && detailedNoMatchLogsLeft-- > 0) {
                        Drobiazgi.debug(
                            "No matching spawn rule at dim=" + world.provider.dimensionId
                                + ", biome="
                                + biome.biomeName
                                + " ("
                                + biome.biomeID
                                + "), y="
                                + spawnPosition.y
                                + ", light="
                                + lightLevel
                                + ", "
                                + CustomNpcSpawnRules
                                    .describeNoMatch(world, biome.biomeID, spawnPosition.y, lightLevel));
                    }
                    continue;
                }

                double chanceRoll = world.rand.nextDouble();
                if (chanceRoll > rule.getChance()) {
                    chanceRejected++;
                    if (debug && detailedChanceLogsLeft-- > 0) {
                        Drobiazgi.debug(
                            "Rule '" + rule
                                .getId() + "' rejected by chance: roll=" + chanceRoll + ", chance=" + rule.getChance());
                    }
                    continue;
                }

                NBTTagCompound cloneTemplate = getCloneTemplate(rule, cloneTemplateCache);
                if (cloneTemplate == null) {
                    missingClone++;
                    continue;
                }

                int groupSize = rule.getGroupSize(world.rand);
                for (int groupIndex = 0; groupIndex < groupSize; groupIndex++) {
                    if (npcCount >= maxNpcsPerDimension || successfulSpawns >= maxSpawnsPerCycle) {
                        return;
                    }

                    int offsetX = world.rand.nextInt(5) - world.rand.nextInt(5);
                    int offsetZ = world.rand.nextInt(5) - world.rand.nextInt(5);
                    int x = spawnPosition.x + offsetX;
                    int z = spawnPosition.z + offsetZ;
                    int y;
                    if (rule.isCaveRule()) {
                        y = findCaveY(world, x, z, rule.getMinY(), rule.getMaxY());
                        if (y < 0) {
                            caveNotFound++;
                            continue;
                        }
                    } else {
                        y = world.getTopSolidOrLiquidBlock(x, z);
                    }
                    if (isLiquidAt(world, x, y - 1, z)) {
                        if (!rule.isWaterAllowed()) {
                            waterRejected++;
                            continue;
                        }
                        y--;
                    }
                    if (!isValidSpawnPosition(world, x, y, z, minPlayerDistance)) {
                        invalidSpawnPosition++;
                        if (debug && detailedInvalidPosLogsLeft-- > 0) {
                            Drobiazgi.debug(
                                "Rejected spawn position for rule '" + rule.getId()
                                    + "': x="
                                    + x
                                    + ", y="
                                    + y
                                    + ", z="
                                    + z
                                    + ", minPlayerDistance="
                                    + minPlayerDistance);
                        }
                        continue;
                    }

                    BiomeGenBase offsetBiome = world.getBiomeGenForCoords(x, z);
                    if (offsetBiome == null) {
                        offsetNoBiome++;
                        continue;
                    }

                    int offsetLightLevel = world.getBlockLightValue(x, y, z);
                    if (!rule.matches(world, offsetBiome.biomeID, y, offsetLightLevel)) {
                        offsetRuleRejected++;
                        continue;
                    }

                    if (trySpawnClone(world, x, y, z, rule, cloneTemplate)) {
                        successfulSpawns++;
                        npcCount++;
                        if (debug) {
                            Drobiazgi.debug(
                                "Spawned clone for rule '" + rule.getId()
                                    + "' at dim="
                                    + world.provider.dimensionId
                                    + ", x="
                                    + x
                                    + ", y="
                                    + y
                                    + ", z="
                                    + z
                                    + ", biome="
                                    + offsetBiome.biomeName
                                    + " ("
                                    + offsetBiome.biomeID
                                    + ")");
                        }
                    } else {
                        nullEntitySpawn++;
                    }
                }
            }
        }

        if (debug) {
            Drobiazgi.debug(
                "CustomNPC spawn cycle complete: attempts=" + attempts
                    + ", spawned="
                    + successfulSpawns
                    + ", npcCountEnd="
                    + npcCount
                    + "/"
                    + maxNpcsPerDimension
                    + ", noSpawnPos="
                    + noSpawnPosition
                    + ", noBiome="
                    + noBiome
                    + ", noRule="
                    + noMatchingRule
                    + ", chanceRejected="
                    + chanceRejected
                    + ", missingClone="
                    + missingClone
                    + ", invalidPos="
                    + invalidSpawnPosition
                    + ", offsetNoBiome="
                    + offsetNoBiome
                    + ", offsetRuleRejected="
                    + offsetRuleRejected
                    + ", nullEntitySpawn="
                    + nullEntitySpawn
                    + ", caveNotFound="
                    + caveNotFound
                    + ", waterRejected="
                    + waterRejected);
        }
    }

    private static SpawnPosition getSpawnPosition(WorldServer world, EntityPlayer player, int minDistance,
        int maxDistance) {
        double angle = world.rand.nextDouble() * Math.PI * 2.0D;
        double distance = minDistance + (maxDistance - minDistance) * world.rand.nextDouble();

        int x = MathHelper.floor_double(player.posX + Math.cos(angle) * distance);
        int z = MathHelper.floor_double(player.posZ + Math.sin(angle) * distance);
        int y = world.getTopSolidOrLiquidBlock(x, z);
        if (y <= 0 || y >= world.getActualHeight()) {
            return null;
        }

        return new SpawnPosition(x, y, z);
    }

    private static boolean isValidSpawnPosition(WorldServer world, int x, int y, int z, int minPlayerDistance) {
        if (y <= 0 || y >= world.getActualHeight()) {
            return false;
        }
        if (!world.blockExists(x, y, z)) {
            return false;
        }

        return world.getClosestPlayer(x + 0.5D, y, z + 0.5D, minPlayerDistance) == null;
    }

    private NBTTagCompound getCloneTemplate(CustomNpcSpawnRule rule, Map<String, NBTTagCompound> cloneTemplateCache) {
        if (ServerCloneController.Instance == null) {
            return null;
        }

        String cacheKey = rule.getCloneTab() + ":" + rule.getCloneName();
        if (cloneTemplateCache.containsKey(cacheKey)) {
            return cloneTemplateCache.get(cacheKey);
        }

        NBTTagCompound cloneData = ServerCloneController.Instance
            .getCloneData(null, rule.getCloneName(), rule.getCloneTab());
        if (cloneData == null) {
            cloneData = loadGlobalCloneTemplate(rule);
        }

        if (cloneData == null) {
            cloneTemplateCache.put(cacheKey, null);
            String warningKey = rule.getCloneTab() + ":" + rule.getCloneName();
            if (missingCloneWarnings.add(warningKey)) {
                Drobiazgi.LOG.warn(
                    "CustomNPC spawn rule '{}' points to missing clone '{}' in tab {}.",
                    rule.getId(),
                    rule.getCloneName(),
                    rule.getCloneTab());
            }
            if (Drobiazgi.isDebugMode()) {
                Drobiazgi.debug(
                    "Missing clone for rule '" + rule
                        .getId() + "': name=" + rule.getCloneName() + ", tab=" + rule.getCloneTab());
            }
            return null;
        }

        cloneTemplateCache.put(cacheKey, cloneData);
        return cloneData;
    }

    private NBTTagCompound loadGlobalCloneTemplate(CustomNpcSpawnRule rule) {
        if (CustomNpcs.Dir == null) {
            return null;
        }

        File cloneFile = new File(
            new File(new File(CustomNpcs.Dir, "clones"), String.valueOf(rule.getCloneTab())),
            rule.getCloneName() + ".json");
        if (!cloneFile.exists() || !cloneFile.isFile()) {
            return null;
        }

        try {
            NBTTagCompound cloneData = NBTJsonUtil.LoadFile(cloneFile);
            if (Drobiazgi.isDebugMode()) {
                Drobiazgi.debug(
                    "Loaded clone fallback from global path for rule '" + rule.getId()
                        + "': "
                        + cloneFile.getAbsolutePath());
            }
            return cloneData;
        } catch (Exception e) {
            Drobiazgi.LOG.warn(
                "Failed to load global clone fallback '{}' for rule '{}': {}",
                cloneFile.getAbsolutePath(),
                rule.getId(),
                e.getMessage());
            return null;
        }
    }

    private boolean trySpawnClone(WorldServer world, int x, int y, int z, CustomNpcSpawnRule rule,
        NBTTagCompound cloneTemplate) {
        NBTTagCompound cloneCopy = (NBTTagCompound) cloneTemplate.copy();
        Entity entity = NoppesUtilServer.spawnClone(cloneCopy, x, y, z, world);
        if (entity == null) {
            if (Drobiazgi.isDebugMode()) {
                Drobiazgi
                    .debug("Spawn attempt failed for rule '" + rule.getId() + "' at " + x + ", " + y + ", " + z + ".");
            }
            return false;
        }
        return true;
    }

    private static int countCustomNpcs(WorldServer world) {
        int count = 0;
        for (Object entityObj : world.loadedEntityList) {
            if (entityObj instanceof EntityNPCInterface) {
                count++;
            }
        }
        return count;
    }

    private static int findCaveY(WorldServer world, int x, int z, int minY, int maxY) {
        int range = maxY - minY;
        if (range <= 0) {
            return checkCaveAt(world, x, z, minY) ? minY : -1;
        }

        for (int attempt = 0; attempt < 3; attempt++) {
            int startY = minY + world.rand.nextInt(range + 1);
            for (int y = startY; y >= minY; y--) {
                if (checkCaveAt(world, x, z, y)) {
                    return y;
                }
            }
        }
        return -1;
    }

    private static boolean checkCaveAt(WorldServer world, int x, int z, int y) {
        if (y <= 0 || y + 1 >= world.getActualHeight()) {
            return false;
        }
        if (!world.blockExists(x, y, z)) {
            return false;
        }
        Block below = world.getBlock(x, y - 1, z);
        if (!below.getMaterial()
            .isSolid()) {
            return false;
        }
        return world.isAirBlock(x, y, z) && world.isAirBlock(x, y + 1, z);
    }

    private static boolean isLiquidAt(WorldServer world, int x, int y, int z) {
        if (y < 0 || !world.blockExists(x, y, z)) {
            return false;
        }
        return world.getBlock(x, y, z)
            .getMaterial()
            .isLiquid();
    }

    private static final class SpawnPosition {

        private final int x;
        private final int y;
        private final int z;

        private SpawnPosition(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
