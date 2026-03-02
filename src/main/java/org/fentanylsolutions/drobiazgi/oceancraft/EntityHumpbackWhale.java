package org.fentanylsolutions.drobiazgi.oceancraft;

import net.minecraft.world.World;

public class EntityHumpbackWhale extends FixedSkinWhale {

    public EntityHumpbackWhale(World world) {
        super(world);
    }

    @Override
    protected int getFixedWhaleSkin() {
        return 0;
    }
}
