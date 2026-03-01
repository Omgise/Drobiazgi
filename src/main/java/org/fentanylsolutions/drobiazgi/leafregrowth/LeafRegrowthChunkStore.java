package org.fentanylsolutions.drobiazgi.leafregrowth;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

final class LeafRegrowthChunkStore {

    private static final String NBT_ROOT_TAG = "drobiazgiLeafRegrowth";
    private static final String NBT_ENTRIES_TAG = "entries";
    private static final Map<Integer, LeafRegrowthChunkStore> STORES = new HashMap<>();

    private final Map<Long, ChunkLeafData> loadedChunks = new HashMap<>();
    private final PriorityQueue<ScheduledLeaf> schedule = new PriorityQueue<>(
        Comparator.comparingLong(entry -> entry.dueTick));
    private long tokenCounter = 1L;

    private LeafRegrowthChunkStore() {}

    static synchronized LeafRegrowthChunkStore get(WorldServer world) {
        return STORES.computeIfAbsent(world.provider.dimensionId, ignored -> new LeafRegrowthChunkStore());
    }

    static synchronized void clearWorld(WorldServer world) {
        STORES.remove(world.provider.dimensionId);
    }

    boolean isEmpty() {
        return loadedChunks.isEmpty();
    }

    int size() {
        int size = 0;
        for (ChunkLeafData chunkLeafData : loadedChunks.values()) {
            size += chunkLeafData.entries.size();
        }
        return size;
    }

    void onChunkLoaded(Chunk chunk, NBTTagCompound chunkNbt) {
        long chunkKey = chunkKey(chunk.xPosition, chunk.zPosition);
        ChunkLeafData chunkLeafData = new ChunkLeafData(nextToken(), new HashMap<>());
        loadedChunks.put(chunkKey, chunkLeafData);

        if (chunkNbt == null || !chunkNbt.hasKey(NBT_ROOT_TAG, 10)) {
            return;
        }

        NBTTagCompound root = chunkNbt.getCompoundTag(NBT_ROOT_TAG);
        if (!root.hasKey(NBT_ENTRIES_TAG, 9)) {
            return;
        }

        NBTTagList entries = root.getTagList(NBT_ENTRIES_TAG, 10);
        for (int i = 0; i < entries.tagCount(); i++) {
            NBTTagCompound leafTag = entries.getCompoundTagAt(i);
            String ruleId = leafTag.getString("rule");
            if (ruleId == null || ruleId.isEmpty()) {
                continue;
            }

            int localPos = leafTag.getInteger("p");
            int leafMeta = leafTag.getInteger("meta") & 15;
            long dueTick = leafTag.getLong("due");
            TrackedLeaf trackedLeaf = new TrackedLeaf(ruleId, leafMeta, dueTick);
            chunkLeafData.entries.put(localPos, trackedLeaf);
            schedule.offer(new ScheduledLeaf(chunkKey, localPos, dueTick, trackedLeaf.sequence, chunkLeafData.token));
        }
    }

    void onChunkSaved(Chunk chunk, NBTTagCompound chunkNbt) {
        long chunkKey = chunkKey(chunk.xPosition, chunk.zPosition);
        ChunkLeafData chunkLeafData = loadedChunks.get(chunkKey);
        if (chunkLeafData == null || chunkLeafData.entries.isEmpty()) {
            chunkNbt.removeTag(NBT_ROOT_TAG);
            return;
        }

        NBTTagCompound root = new NBTTagCompound();
        NBTTagList entries = new NBTTagList();
        for (Map.Entry<Integer, TrackedLeaf> leafEntry : chunkLeafData.entries.entrySet()) {
            TrackedLeaf trackedLeaf = leafEntry.getValue();
            NBTTagCompound leafTag = new NBTTagCompound();
            leafTag.setInteger("p", leafEntry.getKey());
            leafTag.setString("rule", trackedLeaf.ruleId);
            leafTag.setInteger("meta", trackedLeaf.leafMeta);
            leafTag.setLong("due", trackedLeaf.dueTick);
            entries.appendTag(leafTag);
        }
        root.setTag(NBT_ENTRIES_TAG, entries);
        chunkNbt.setTag(NBT_ROOT_TAG, root);
    }

    void onChunkUnloaded(Chunk chunk) {
        long chunkKey = chunkKey(chunk.xPosition, chunk.zPosition);
        loadedChunks.remove(chunkKey);
    }

    void trackLeaf(WorldServer world, int x, int y, int z, String ruleId, int leafMeta, long dueTick) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        long chunkKey = chunkKey(chunkX, chunkZ);
        int localPos = packLocalPos(x, y, z);

        ChunkLeafData chunkLeafData = loadedChunks
            .computeIfAbsent(chunkKey, ignored -> new ChunkLeafData(nextToken(), new HashMap<>()));
        TrackedLeaf trackedLeaf = chunkLeafData.entries.get(localPos);
        if (trackedLeaf == null) {
            trackedLeaf = new TrackedLeaf(ruleId, leafMeta, dueTick);
            chunkLeafData.entries.put(localPos, trackedLeaf);
        } else {
            trackedLeaf.ruleId = ruleId;
            trackedLeaf.leafMeta = leafMeta;
            trackedLeaf.dueTick = dueTick;
            trackedLeaf.sequence++;
        }

        schedule.offer(
            new ScheduledLeaf(chunkKey, localPos, trackedLeaf.dueTick, trackedLeaf.sequence, chunkLeafData.token));
        markChunkDirty(world, chunkX, chunkZ);
    }

    PendingLeaf pollDue(long worldTime) {
        while (!schedule.isEmpty()) {
            ScheduledLeaf next = schedule.peek();
            if (next.dueTick > worldTime) {
                return null;
            }
            schedule.poll();

            ChunkLeafData chunkLeafData = loadedChunks.get(next.chunkKey);
            if (chunkLeafData == null || chunkLeafData.token != next.chunkToken) {
                continue;
            }

            TrackedLeaf current = chunkLeafData.entries.get(next.localPos);
            if (current == null) {
                continue;
            }
            if (current.sequence != next.sequence || current.dueTick != next.dueTick) {
                continue;
            }

            int chunkX = chunkXFromKey(next.chunkKey);
            int chunkZ = chunkZFromKey(next.chunkKey);
            int x = (chunkX << 4) | (next.localPos & 15);
            int z = (chunkZ << 4) | ((next.localPos >>> 4) & 15);
            int y = (next.localPos >>> 8) & 255;
            return new PendingLeaf(
                next.chunkKey,
                next.localPos,
                chunkX,
                chunkZ,
                x,
                y,
                z,
                current.ruleId,
                current.leafMeta,
                current.dueTick,
                next.chunkToken);
        }

        return null;
    }

    void remove(WorldServer world, PendingLeaf pendingLeaf) {
        if (pendingLeaf == null) {
            return;
        }

        ChunkLeafData chunkLeafData = loadedChunks.get(pendingLeaf.chunkKey);
        if (chunkLeafData == null || chunkLeafData.token != pendingLeaf.chunkToken) {
            return;
        }

        if (chunkLeafData.entries.remove(pendingLeaf.localPos) == null) {
            return;
        }

        if (chunkLeafData.entries.isEmpty()) {
            loadedChunks.remove(pendingLeaf.chunkKey);
        }
        markChunkDirty(world, pendingLeaf.chunkX, pendingLeaf.chunkZ);
    }

    void reschedule(WorldServer world, PendingLeaf pendingLeaf, long dueTick) {
        if (pendingLeaf == null) {
            return;
        }

        ChunkLeafData chunkLeafData = loadedChunks.get(pendingLeaf.chunkKey);
        if (chunkLeafData == null || chunkLeafData.token != pendingLeaf.chunkToken) {
            return;
        }

        TrackedLeaf trackedLeaf = chunkLeafData.entries.get(pendingLeaf.localPos);
        if (trackedLeaf == null) {
            return;
        }

        trackedLeaf.dueTick = dueTick;
        trackedLeaf.sequence++;
        schedule.offer(
            new ScheduledLeaf(
                pendingLeaf.chunkKey,
                pendingLeaf.localPos,
                trackedLeaf.dueTick,
                trackedLeaf.sequence,
                chunkLeafData.token));
        markChunkDirty(world, pendingLeaf.chunkX, pendingLeaf.chunkZ);
    }

    private long nextToken() {
        return tokenCounter++;
    }

    private static void markChunkDirty(WorldServer world, int chunkX, int chunkZ) {
        if (!world.getChunkProvider()
            .chunkExists(chunkX, chunkZ)) {
            return;
        }

        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        chunk.setChunkModified();
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) ^ (chunkZ & 0xFFFFFFFFL);
    }

    private static int chunkXFromKey(long key) {
        return (int) (key >> 32);
    }

    private static int chunkZFromKey(long key) {
        return (int) key;
    }

    private static int packLocalPos(int x, int y, int z) {
        return ((y & 255) << 8) | ((z & 15) << 4) | (x & 15);
    }

    static final class PendingLeaf {

        final long chunkKey;
        final int localPos;
        final int chunkX;
        final int chunkZ;
        final int x;
        final int y;
        final int z;
        final String ruleId;
        final int leafMeta;
        final long dueTick;
        final long chunkToken;

        PendingLeaf(long chunkKey, int localPos, int chunkX, int chunkZ, int x, int y, int z, String ruleId,
            int leafMeta, long dueTick, long chunkToken) {
            this.chunkKey = chunkKey;
            this.localPos = localPos;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.x = x;
            this.y = y;
            this.z = z;
            this.ruleId = ruleId;
            this.leafMeta = leafMeta;
            this.dueTick = dueTick;
            this.chunkToken = chunkToken;
        }
    }

    private static final class ChunkLeafData {

        final long token;
        final Map<Integer, TrackedLeaf> entries;

        ChunkLeafData(long token, Map<Integer, TrackedLeaf> entries) {
            this.token = token;
            this.entries = entries;
        }
    }

    private static final class TrackedLeaf {

        String ruleId;
        int leafMeta;
        long dueTick;
        int sequence;

        TrackedLeaf(String ruleId, int leafMeta, long dueTick) {
            this.ruleId = ruleId;
            this.leafMeta = leafMeta;
            this.dueTick = dueTick;
            this.sequence = 0;
        }
    }

    private static final class ScheduledLeaf {

        final long chunkKey;
        final int localPos;
        final long dueTick;
        final int sequence;
        final long chunkToken;

        ScheduledLeaf(long chunkKey, int localPos, long dueTick, int sequence, long chunkToken) {
            this.chunkKey = chunkKey;
            this.localPos = localPos;
            this.dueTick = dueTick;
            this.sequence = sequence;
            this.chunkToken = chunkToken;
        }
    }
}
