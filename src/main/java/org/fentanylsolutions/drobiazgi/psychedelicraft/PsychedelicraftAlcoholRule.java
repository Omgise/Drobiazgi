package org.fentanylsolutions.drobiazgi.psychedelicraft;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import ivorius.psychedelicraft.entities.drugs.DrugInfluence;

public final class PsychedelicraftAlcoholRule {

    private final String id;
    private final boolean enabled;
    private final String drugName;
    private final Set<Item> items;
    private final Set<Integer> metadata;
    private final int delay;
    private final double influenceSpeed;
    private final double influenceSpeedPlus;
    private final double maxInfluence;
    private final double lotrAlcoholicityScale;

    public PsychedelicraftAlcoholRule(String id, boolean enabled, String drugName, Set<Item> items,
        Set<Integer> metadata, int delay, double influenceSpeed, double influenceSpeedPlus, double maxInfluence,
        double lotrAlcoholicityScale) {
        this.id = id;
        this.enabled = enabled;
        this.drugName = drugName;
        this.items = items.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(new LinkedHashSet<>(items));
        this.metadata = metadata.isEmpty() ? Collections.emptySet()
            : Collections.unmodifiableSet(new LinkedHashSet<>(metadata));
        this.delay = Math.max(0, delay);
        this.influenceSpeed = Math.max(0.0D, influenceSpeed);
        this.influenceSpeedPlus = Math.max(0.0D, influenceSpeedPlus);
        this.maxInfluence = maxInfluence;
        this.lotrAlcoholicityScale = Math.max(0.0D, lotrAlcoholicityScale);
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDrugName() {
        return drugName;
    }

    public Set<Item> getItems() {
        return items;
    }

    public boolean matches(ItemStack stack) {
        if (stack == null) {
            return false;
        }

        Item item = stack.getItem();
        if (item == null || !items.contains(item)) {
            return false;
        }

        return metadata.isEmpty() || metadata.contains(stack.getItemDamage());
    }

    public DrugInfluence createInfluence(ItemStack stack) {
        double resolvedMaxInfluence = resolveMaxInfluence(stack);
        if (resolvedMaxInfluence <= 0.0D) {
            return null;
        }

        return new DrugInfluence(drugName, delay, influenceSpeed, influenceSpeedPlus, resolvedMaxInfluence);
    }

    public String getDebugSummary() {
        return "id=" + id
            + ", enabled="
            + enabled
            + ", drug="
            + drugName
            + ", items="
            + items.size()
            + ", meta="
            + (metadata.isEmpty() ? "*" : metadata)
            + ", delay="
            + delay
            + ", speed="
            + influenceSpeed
            + ", speedPlus="
            + influenceSpeedPlus
            + ", maxInfluence="
            + maxInfluence
            + ", lotrAlcoholicityScale="
            + lotrAlcoholicityScale;
    }

    private double resolveMaxInfluence(ItemStack stack) {
        if (lotrAlcoholicityScale > 0.0D) {
            double scaledInfluence = LotrAlcoholHelper.getScaledAlcoholInfluence(stack, lotrAlcoholicityScale);
            if (scaledInfluence > 0.0D) {
                return scaledInfluence;
            }
        }

        return maxInfluence;
    }
}
