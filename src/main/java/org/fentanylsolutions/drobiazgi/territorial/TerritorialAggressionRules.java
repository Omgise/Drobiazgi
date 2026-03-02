package org.fentanylsolutions.drobiazgi.territorial;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;

import org.fentanylsolutions.drobiazgi.Config;
import org.fentanylsolutions.drobiazgi.Drobiazgi;

import cpw.mods.fml.common.Loader;

public final class TerritorialAggressionRules {

    private static volatile Set<Class<? extends EntityCreature>> configuredClasses = Collections.emptySet();

    private TerritorialAggressionRules() {}

    public static synchronized void reloadFromConfig() {
        Set<Class<? extends EntityCreature>> resolvedClasses = new LinkedHashSet<>();
        String[] configuredEntries = Config.getTerritorialAggressionEntities();

        if (configuredEntries != null) {
            for (String entry : configuredEntries) {
                Class<? extends EntityCreature> entityClass = resolveEntityClass(entry);
                if (entityClass != null) {
                    resolvedClasses.add(entityClass);
                }
            }
        }

        configuredClasses = Collections.unmodifiableSet(resolvedClasses);
        Drobiazgi.LOG.info("Loaded {} territorial aggression class(es).", configuredClasses.size());
    }

    public static boolean matches(Entity entity) {
        if (!Config.isTerritorialAggressionEnabled() || !(entity instanceof EntityCreature)) {
            return false;
        }

        for (Class<? extends EntityCreature> configuredClass : configuredClasses) {
            if (configuredClass.isInstance(entity)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends EntityCreature> resolveEntityClass(String entry) {
        if (entry == null) {
            return null;
        }

        String selector = entry.trim();
        if (selector.isEmpty()) {
            return null;
        }

        Class<?> resolvedClass = EntityList.stringToClassMapping.get(selector);

        if (resolvedClass == null) {
            if (!Loader.isModLoaded("lotr") && (selector.startsWith("lotr.") || selector.startsWith("lotr.common."))) {
                return null;
            }

            try {
                resolvedClass = Class.forName(selector);
            } catch (ClassNotFoundException e) {
                Drobiazgi.LOG.warn("Territorial aggression entry '{}' could not be resolved.", selector);
                return null;
            }
        }

        if (!EntityCreature.class.isAssignableFrom(resolvedClass)) {
            Drobiazgi.LOG.warn(
                "Territorial aggression entry '{}' resolved to {}, which is not an EntityCreature.",
                selector,
                resolvedClass.getName());
            return null;
        }

        return (Class<? extends EntityCreature>) resolvedClass;
    }
}
