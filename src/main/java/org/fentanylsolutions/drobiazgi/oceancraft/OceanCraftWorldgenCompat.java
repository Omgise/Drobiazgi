package org.fentanylsolutions.drobiazgi.oceancraft;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import com.Oceancraft.common.Oceancraft;

public final class OceanCraftWorldgenCompat {

    private OceanCraftWorldgenCompat() {}

    public static void generateShell(Object generator, World world, int x, int y, int z) {
        Block shellBlock = getShellBlock(generator);
        if (shellBlock == null) {
            return;
        }

        Block sandBlock = getVanillaSandBlock();
        if (sandBlock == null) {
            return;
        }

        if (world.isAirBlock(x, y, z) && world.getBlock(x, y - 1, z) == sandBlock) {
            world.setBlock(x, y, z, shellBlock);
        }
    }

    private static Block getShellBlock(Object generator) {
        String generatorClassName = generator.getClass()
            .getName();
        if ("com.Oceancraft.common.WorldGenShell".equals(generatorClassName)) {
            return Oceancraft.BlockShell;
        }
        if ("com.Oceancraft.common.WorldGenShell2".equals(generatorClassName)) {
            return Oceancraft.BlockShell2;
        }
        if ("com.Oceancraft.common.WorldGenShell3".equals(generatorClassName)) {
            return Oceancraft.BlockShell3;
        }
        if ("com.Oceancraft.common.WorldGenShell4".equals(generatorClassName)) {
            return Oceancraft.BlockShell4;
        }
        if ("com.Oceancraft.common.WorldGenShell5".equals(generatorClassName)) {
            return Oceancraft.BlockShell5;
        }
        if ("com.Oceancraft.common.WorldGenShell6".equals(generatorClassName)) {
            return Oceancraft.BlockShell6;
        }
        return null;
    }

    private static Block getVanillaSandBlock() {
        Block sandBlock = Block.getBlockFromName("minecraft:sand");
        if (sandBlock != null) {
            return sandBlock;
        }

        return Block.getBlockFromName("sand");
    }
}
