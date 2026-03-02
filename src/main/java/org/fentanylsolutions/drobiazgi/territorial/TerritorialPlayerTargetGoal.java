package org.fentanylsolutions.drobiazgi.territorial;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;

public class TerritorialPlayerTargetGoal extends EntityAINearestAttackableTarget {

    private static final int TARGET_CHANCE = 20;
    private static final double RANGE_MULTIPLIER = 0.5D;

    public TerritorialPlayerTargetGoal(EntityCreature taskOwner) {
        super(taskOwner, EntityPlayer.class, TARGET_CHANCE, true, true);
    }

    @Override
    public boolean shouldExecute() {
        if (taskOwner instanceof EntityAgeable && ((EntityAgeable) taskOwner).isChild()) {
            return false;
        }

        return super.shouldExecute();
    }

    @Override
    protected double getTargetDistance() {
        return super.getTargetDistance() * RANGE_MULTIPLIER;
    }
}
