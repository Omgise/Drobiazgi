package org.fentanylsolutions.drobiazgi.psychedelicraft;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.fentanylsolutions.drobiazgi.Drobiazgi;

import cpw.mods.fml.common.Loader;

final class LotrAlcoholHelper {

    private static boolean checked = false;
    private static boolean available = false;
    private static boolean readFailureLogged = false;
    private static Class<?> lotrItemMugClass;
    private static Field alcoholicityField;
    private static Method getStrengthMethod;

    private LotrAlcoholHelper() {}

    static boolean isLotrMug(ItemStack stack) {
        if (stack == null || !Loader.isModLoaded("lotr") || !ensureLoaded()) {
            return false;
        }

        Object item = stack.getItem();
        return item != null && lotrItemMugClass.isInstance(item);
    }

    static double getScaledAlcoholInfluence(ItemStack stack, double scale) {
        if (stack == null || scale <= 0.0D || !isLotrMug(stack)) {
            return -1.0D;
        }

        double alcoholicity = getAlcoholicity(stack.getItem());
        if (alcoholicity <= 0.0D) {
            return -1.0D;
        }

        try {
            double strength = ((Float) getStrengthMethod.invoke(null, stack)).doubleValue();
            return Math.max(0.0D, alcoholicity) * Math.max(0.0D, strength) * scale;
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            if (!readFailureLogged) {
                Drobiazgi.LOG.warn("Failed to read LOTR mug alcohol data for Psychedelicraft compat.", e);
                readFailureLogged = true;
            }
            return -1.0D;
        }
    }

    @SuppressWarnings("unchecked")
    static Set<Item> collectAlcoholicMugs() {
        if (!Loader.isModLoaded("lotr") || !ensureLoaded()) {
            return Collections.emptySet();
        }

        Set<Item> matches = new LinkedHashSet<>();
        for (String key : (Set<String>) Item.itemRegistry.getKeys()) {
            Object itemObj = Item.itemRegistry.getObject(key);
            if (!(itemObj instanceof Item)) {
                continue;
            }

            Item item = (Item) itemObj;
            if (!lotrItemMugClass.isInstance(item)) {
                continue;
            }
            if (getAlcoholicity(item) <= 0.0D) {
                continue;
            }

            matches.add(item);
        }

        return Collections.unmodifiableSet(matches);
    }

    private static double getAlcoholicity(Item item) {
        if (item == null || !lotrItemMugClass.isInstance(item)) {
            return -1.0D;
        }

        try {
            return alcoholicityField.getFloat(item);
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            if (!readFailureLogged) {
                Drobiazgi.LOG.warn("Failed to read LOTR mug alcohol data for Psychedelicraft compat.", e);
                readFailureLogged = true;
            }
            return -1.0D;
        }
    }

    private static boolean ensureLoaded() {
        if (checked) {
            return available;
        }

        checked = true;
        try {
            lotrItemMugClass = Class.forName("lotr.common.item.LOTRItemMug");
            alcoholicityField = lotrItemMugClass.getField("alcoholicity");
            getStrengthMethod = lotrItemMugClass.getMethod("getStrength", ItemStack.class);
            available = true;
        } catch (ReflectiveOperationException | LinkageError e) {
            Drobiazgi.LOG.warn("Failed to initialize LOTR mug reflection for Psychedelicraft alcohol compat.", e);
            available = false;
        }

        return available;
    }
}
