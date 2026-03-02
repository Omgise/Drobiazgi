package org.fentanylsolutions.drobiazgi.oceancraft;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.Oceancraft.common.EntityWhale;

public abstract class FixedSkinWhale extends EntityWhale {

    protected FixedSkinWhale(World world) {
        super(world);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        setWhaleSkin(getFixedWhaleSkin());
    }

    @Override
    public IEntityLivingData onSpawnWithEgg(IEntityLivingData entityData) {
        IEntityLivingData result = super.onSpawnWithEgg(entityData);
        setWhaleSkin(getFixedWhaleSkin());
        return result;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompound) {
        super.readEntityFromNBT(tagCompound);
        setWhaleSkin(getFixedWhaleSkin());
    }

    protected abstract int getFixedWhaleSkin();
}
