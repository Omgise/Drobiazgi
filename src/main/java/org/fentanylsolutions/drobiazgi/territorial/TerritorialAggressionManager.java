package org.fentanylsolutions.drobiazgi.territorial;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public final class TerritorialAggressionManager {

    private static final TerritorialAggressionManager INSTANCE = new TerritorialAggressionManager();
    private static final int TARGET_TASK_PRIORITY = 4;
    private static boolean registered;

    private final Set<Entity> patchedEntities = Collections.newSetFromMap(new WeakHashMap<>());

    private TerritorialAggressionManager() {}

    public static synchronized void register() {
        if (registered) {
            return;
        }

        MinecraftForge.EVENT_BUS.register(INSTANCE);
        registered = true;
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.world.isRemote || !Config.isTerritorialAggressionEnabled()) {
            return;
        }

        Entity entity = event.entity;
        if (!(entity instanceof EntityCreature) || !TerritorialAggressionRules.matches(entity)
            || !patchedEntities.add(entity)) {
            return;
        }

        EntityCreature creature = (EntityCreature) entity;
        creature.targetTasks.addTask(TARGET_TASK_PRIORITY, new TerritorialPlayerTargetGoal(creature));

        if (Drobiazgi.isDebugMode()) {
            Drobiazgi.debug(
                "Added territorial aggression to " + entity.getClass()
                    .getName() + ".");
        }
    }
}
