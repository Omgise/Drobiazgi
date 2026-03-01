package org.fentanylsolutions.drobiazgi.psychedelicraft;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ivorius.psychedelicraft.entities.drugs.DrugInfluence;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;

public final class PsychedelicraftAlcoholManager {

    private static final PsychedelicraftAlcoholManager INSTANCE = new PsychedelicraftAlcoholManager();

    private boolean registered = false;

    private PsychedelicraftAlcoholManager() {}

    public static synchronized void register() {
        if (INSTANCE.registered) {
            return;
        }

        MinecraftForge.EVENT_BUS.register(INSTANCE);
        INSTANCE.registered = true;
    }

    @SubscribeEvent
    public void onPlayerUseItemFinish(PlayerUseItemEvent.Finish event) {
        if (!Config.isPsychedelicraftAlcoholEnabled() || event == null
            || event.entityPlayer == null
            || event.item == null) {
            return;
        }
        if (event.entityPlayer.worldObj == null || event.entityPlayer.worldObj.isRemote) {
            return;
        }

        PsychedelicraftAlcoholRule rule = PsychedelicraftAlcoholRules.findMatchingRule(event.item);
        if (rule == null) {
            return;
        }

        DrugInfluence influence = rule.createInfluence(event.item);
        if (influence == null) {
            if (Drobiazgi.isDebugMode()) {
                Drobiazgi.debug(
                    "Skipped Psychedelicraft alcohol rule '" + rule.getId()
                        + "' for "
                        + describeItem(event.item)
                        + ": resolved max influence was not positive.");
            }
            return;
        }

        DrugProperties properties = DrugProperties.getDrugProperties(event.entityPlayer);
        if (properties == null) {
            DrugProperties.initInEntity(event.entityPlayer);
            properties = DrugProperties.getDrugProperties(event.entityPlayer);
        }
        if (properties == null) {
            Drobiazgi.LOG.warn(
                "Failed to initialize Psychedelicraft drug properties for player '{}'.",
                event.entityPlayer.getCommandSenderName());
            return;
        }

        properties.addToDrug(influence);

        if (Drobiazgi.isDebugMode()) {
            Drobiazgi.debug(
                "Applied Psychedelicraft effect rule '" + rule.getId()
                    + "' to "
                    + event.entityPlayer.getCommandSenderName()
                    + " using "
                    + describeItem(event.item)
                    + ", drug="
                    + rule.getDrugName()
                    + ", maxInfluence="
                    + influence.getMaxInfluence()
                    + ", delay="
                    + influence.getDelay()
                    + ", speed="
                    + influence.getInfluenceSpeed()
                    + ", speedPlus="
                    + influence.getInfluenceSpeedPlus());
        }
    }

    private static String describeItem(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return "<null>";
        }

        Item item = stack.getItem();
        Object name = Item.itemRegistry.getNameForObject(item);
        String itemName = name == null ? String.valueOf(Item.getIdFromItem(item)) : name.toString();
        return itemName + "@" + stack.getItemDamage();
    }
}
