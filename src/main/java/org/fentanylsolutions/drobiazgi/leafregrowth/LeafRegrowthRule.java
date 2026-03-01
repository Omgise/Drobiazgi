package org.fentanylsolutions.drobiazgi.leafregrowth;

import java.util.Collections;
import java.util.Set;

import net.minecraft.block.Block;

final class LeafRegrowthRule {

    private final String id;
    private final boolean enabled;
    private final Block leafBlock;
    private final Block logBlock;
    private final Set<Integer> leafMetadata;
    private final double respawnChance;

    LeafRegrowthRule(String id, boolean enabled, Block leafBlock, Block logBlock, Set<Integer> leafMetadata,
        double respawnChance) {
        this.id = id;
        this.enabled = enabled;
        this.leafBlock = leafBlock;
        this.logBlock = logBlock;
        this.leafMetadata = leafMetadata == null ? Collections.<Integer>emptySet() : leafMetadata;
        this.respawnChance = respawnChance;
    }

    String getId() {
        return id;
    }

    boolean isEnabled() {
        return enabled;
    }

    Block getLeafBlock() {
        return leafBlock;
    }

    double getRespawnChance() {
        return respawnChance;
    }

    boolean matchesLeafForTracking(Block block, int meta) {
        if (block != leafBlock) {
            return false;
        }
        return isMetadataAllowed(meta);
    }

    boolean matchesLeafForPath(Block block, int meta) {
        if (block != leafBlock) {
            return false;
        }
        return isMetadataAllowed(meta);
    }

    boolean matchesLog(Block block) {
        return block == logBlock;
    }

    int normalizeLeafMeta(int meta) {
        // Strip decay flags (bits 2-3), set check_decay (bit 2) so regrown leaves
        // can naturally decay if the tree is later removed.
        return (meta & 3) | 4;
    }

    private boolean isMetadataAllowed(int meta) {
        if (leafMetadata.isEmpty()) {
            return true;
        }
        // Strip decay flags (bits 2-3) so naturally-generated leaves
        // with check_decay (0x4) or player-placed leaves with no_decay (0x8)
        // still match against the base wood type (0-3).
        return leafMetadata.contains(meta & 3);
    }

    String getDebugSummary() {
        return "id=" + id
            + ", enabled="
            + enabled
            + ", leaf="
            + blockName(leafBlock)
            + ", log="
            + blockName(logBlock)
            + ", chance="
            + respawnChance
            + ", leafMeta="
            + (leafMetadata.isEmpty() ? "any" : leafMetadata);
    }

    private static String blockName(Block block) {
        if (block == null) {
            return "<null>";
        }
        String name = (String) Block.blockRegistry.getNameForObject(block);
        return name == null ? block.getClass()
            .getName() : name;
    }
}
