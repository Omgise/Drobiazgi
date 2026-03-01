package org.fentanylsolutions.drobiazgi.leafregrowth;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public final class LeafRegrowthManager {

    private static final int[] OFFSETS = { 1, 0, 0, -1, 0, 0, 0, 1, 0, 0, -1, 0, 0, 0, 1, 0, 0, -1 };
    private static final LeafRegrowthManager INSTANCE = new LeafRegrowthManager();

    private boolean registered = false;

    private LeafRegrowthManager() {}

    public static synchronized void register() {
        if (INSTANCE.registered) {
            return;
        }

        MinecraftForge.EVENT_BUS.register(INSTANCE);
        FMLCommonHandler.instance()
            .bus()
            .register(INSTANCE);
        INSTANCE.registered = true;
        Drobiazgi.LOG.info(
            "Leaf regrowth registered: enabled={}, rulesLoaded={}",
            Config.isLeafRegrowthEnabled(),
            LeafRegrowthRules.hasRules());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLeafBroken(BlockEvent.BreakEvent event) {
        if (!Config.isLeafRegrowthEnabled() || event == null || event.isCanceled()) {
            return;
        }
        if (event.world == null || event.world.isRemote || !(event.world instanceof WorldServer)) {
            return;
        }

        Block brokenBlock = event.block;
        if (brokenBlock == null) {
            return;
        }

        LeafRegrowthRule rule = LeafRegrowthRules.findMatchingRule(brokenBlock, event.blockMetadata);
        if (rule == null) {
            return;
        }

        WorldServer world = (WorldServer) event.world;
        if (world.rand.nextDouble() > rule.getRespawnChance()) {
            return;
        }

        long dueTick = world.getTotalWorldTime() + randomDelay(world.rand);
        LeafRegrowthChunkStore chunkStore = LeafRegrowthChunkStore.get(world);
        chunkStore.trackLeaf(
            world,
            event.x,
            event.y,
            event.z,
            rule.getId(),
            rule.normalizeLeafMeta(event.blockMetadata),
            dueTick);

        if (Drobiazgi.isDebugMode()) {
            Drobiazgi.debug(
                "Leaf regrowth: tracked rule=" + rule
                    .getId() + " at [" + event.x + ", " + event.y + ", " + event.z + "]" + ", dueTick=" + dueTick);
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world == null || event.world.isRemote) {
            return;
        }
        if (!Config.isLeafRegrowthEnabled() || !(event.world instanceof WorldServer) || !LeafRegrowthRules.hasRules()) {
            return;
        }

        WorldServer world = (WorldServer) event.world;
        int interval = Math.max(1, Config.getLeafRegrowthProcessingIntervalTicks());
        if (world.getTotalWorldTime() % interval != 0L) {
            return;
        }

        LeafRegrowthChunkStore chunkStore = LeafRegrowthChunkStore.get(world);
        if (chunkStore.isEmpty()) {
            return;
        }

        long worldTime = world.getTotalWorldTime();
        int budget = Math.max(1, Config.getLeafRegrowthChecksPerCycle());
        int processed = 0;
        int regrown = 0;

        while (processed < budget) {
            LeafRegrowthChunkStore.PendingLeaf pendingLeaf = chunkStore.pollDue(worldTime);
            if (pendingLeaf == null) {
                break;
            }

            processed++;
            if (processPendingLeaf(world, chunkStore, pendingLeaf, worldTime)) {
                regrown++;
            }
        }

        if (Drobiazgi.isDebugMode() && processed > 0) {
            Drobiazgi.debug(
                "Leaf regrowth tick dim=" + world.provider.dimensionId
                    + ", processed="
                    + processed
                    + ", regrown="
                    + regrown
                    + ", trackedTotal="
                    + chunkStore.size());
        }
    }

    @SubscribeEvent
    public void onChunkDataLoad(ChunkDataEvent.Load event) {
        if (event == null || event.world == null || event.world.isRemote) {
            return;
        }
        if (!(event.world instanceof WorldServer) || event.getChunk() == null || event.getData() == null) {
            return;
        }

        WorldServer world = (WorldServer) event.world;
        LeafRegrowthChunkStore.get(world)
            .onChunkLoaded(event.getChunk(), event.getData());
    }

    @SubscribeEvent
    public void onChunkDataSave(ChunkDataEvent.Save event) {
        if (event == null || event.world == null || event.world.isRemote) {
            return;
        }
        if (!(event.world instanceof WorldServer) || event.getChunk() == null || event.getData() == null) {
            return;
        }

        WorldServer world = (WorldServer) event.world;
        LeafRegrowthChunkStore.get(world)
            .onChunkSaved(event.getChunk(), event.getData());
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (event == null || event.world == null || event.world.isRemote || event.getChunk() == null) {
            return;
        }
        if (!(event.world instanceof WorldServer)) {
            return;
        }

        WorldServer world = (WorldServer) event.world;
        LeafRegrowthChunkStore.get(world)
            .onChunkUnloaded(event.getChunk());
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event == null) {
            return;
        }

        World world = event.world;
        if (world == null || world.isRemote || !(world instanceof WorldServer)) {
            return;
        }

        LeafRegrowthChunkStore.clearWorld((WorldServer) world);
    }

    private boolean processPendingLeaf(WorldServer world, LeafRegrowthChunkStore chunkStore,
        LeafRegrowthChunkStore.PendingLeaf pendingLeaf, long worldTime) {
        LeafRegrowthRule rule = LeafRegrowthRules.getRuleById(pendingLeaf.ruleId);
        if (rule == null || !rule.isEnabled()) {
            chunkStore.remove(world, pendingLeaf);
            return false;
        }

        int chunkX = pendingLeaf.x >> 4;
        int chunkZ = pendingLeaf.z >> 4;
        if (!world.getChunkProvider()
            .chunkExists(chunkX, chunkZ)) {
            chunkStore.reschedule(
                world,
                pendingLeaf,
                worldTime + Math.max(1, Config.getLeafRegrowthUnloadedChunkRetryTicks()));
            return false;
        }

        if (!world.blockExists(pendingLeaf.x, pendingLeaf.y, pendingLeaf.z)) {
            chunkStore.remove(world, pendingLeaf);
            return false;
        }

        if (!world.isAirBlock(pendingLeaf.x, pendingLeaf.y, pendingLeaf.z)) {
            chunkStore.remove(world, pendingLeaf);
            return false;
        }

        ConnectionResult connection = findLogConnection(world, pendingLeaf.x, pendingLeaf.y, pendingLeaf.z, rule);
        if (connection == ConnectionResult.UNKNOWN) {
            chunkStore.reschedule(
                world,
                pendingLeaf,
                worldTime + Math.max(1, Config.getLeafRegrowthUnloadedChunkRetryTicks()));
            return false;
        }
        if (connection == ConnectionResult.DISCONNECTED) {
            chunkStore.remove(world, pendingLeaf);
            return false;
        }

        boolean placed = world
            .setBlock(pendingLeaf.x, pendingLeaf.y, pendingLeaf.z, rule.getLeafBlock(), pendingLeaf.leafMeta, 3);
        chunkStore.remove(world, pendingLeaf);
        return placed;
    }

    private ConnectionResult findLogConnection(WorldServer world, int startX, int startY, int startZ,
        LeafRegrowthRule rule) {
        int maxDepth = Math.max(0, Config.getLeafRegrowthMaxConnectionDepth());
        ArrayDeque<SearchNode> queue = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();
        boolean missingChunks = false;

        for (int i = 0; i < OFFSETS.length; i += 3) {
            int nx = startX + OFFSETS[i];
            int ny = startY + OFFSETS[i + 1];
            int nz = startZ + OFFSETS[i + 2];

            if (!world.blockExists(nx, ny, nz)) {
                missingChunks = true;
                continue;
            }

            Block neighborBlock = world.getBlock(nx, ny, nz);
            if (rule.matchesLog(neighborBlock)) {
                return ConnectionResult.CONNECTED;
            }

            if (maxDepth == 0) {
                continue;
            }

            int neighborMeta = world.getBlockMetadata(nx, ny, nz);
            if (!rule.matchesLeafForPath(neighborBlock, neighborMeta)) {
                continue;
            }

            long packed = packPos(nx, ny, nz);
            if (visited.add(packed)) {
                queue.addLast(new SearchNode(nx, ny, nz, 1));
            }
        }

        while (!queue.isEmpty()) {
            SearchNode node = queue.removeFirst();
            if (node.depth >= maxDepth) {
                continue;
            }

            for (int i = 0; i < OFFSETS.length; i += 3) {
                int nx = node.x + OFFSETS[i];
                int ny = node.y + OFFSETS[i + 1];
                int nz = node.z + OFFSETS[i + 2];

                if (!world.blockExists(nx, ny, nz)) {
                    missingChunks = true;
                    continue;
                }

                Block neighborBlock = world.getBlock(nx, ny, nz);
                if (rule.matchesLog(neighborBlock)) {
                    return ConnectionResult.CONNECTED;
                }

                int neighborMeta = world.getBlockMetadata(nx, ny, nz);
                if (!rule.matchesLeafForPath(neighborBlock, neighborMeta)) {
                    continue;
                }

                long packed = packPos(nx, ny, nz);
                if (visited.add(packed)) {
                    queue.addLast(new SearchNode(nx, ny, nz, node.depth + 1));
                }
            }
        }

        return missingChunks ? ConnectionResult.UNKNOWN : ConnectionResult.DISCONNECTED;
    }

    private static long randomDelay(Random random) {
        int min = Math.max(1, Config.getLeafRegrowthMinDelayTicks());
        int max = Math.max(min, Config.getLeafRegrowthMaxDelayTicks());
        return min == max ? min : min + random.nextInt(max - min + 1);
    }

    private static long packPos(int x, int y, int z) {
        long packedX = ((long) x & 0x3FFFFFFL) << 38;
        long packedZ = ((long) z & 0x3FFFFFFL) << 12;
        long packedY = y & 0xFFFL;
        return packedX | packedZ | packedY;
    }

    private enum ConnectionResult {
        CONNECTED,
        DISCONNECTED,
        UNKNOWN
    }

    private static final class SearchNode {

        final int x;
        final int y;
        final int z;
        final int depth;

        SearchNode(int x, int y, int z, int depth) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.depth = depth;
        }
    }
}
