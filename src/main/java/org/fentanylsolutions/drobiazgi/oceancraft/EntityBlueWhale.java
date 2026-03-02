package org.fentanylsolutions.drobiazgi.oceancraft;

import net.minecraft.world.World;

public class EntityBlueWhale extends FixedSkinWhale {

    public EntityBlueWhale(World world) {
        super(world);
    }

    @Override
    protected int getFixedWhaleSkin() {
        return 3;
    }
}
