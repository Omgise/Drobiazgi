package org.fentanylsolutions.drobiazgi.oceancraft;

import net.minecraft.world.World;

public class EntityNarwhal extends FixedSkinWhale {

    public EntityNarwhal(World world) {
        super(world);
    }

    @Override
    protected int getFixedWhaleSkin() {
        return 2;
    }
}
