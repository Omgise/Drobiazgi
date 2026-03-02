package org.fentanylsolutions.drobiazgi.biometags;

import java.util.Collections;
import java.util.List;

public final class BiomeTagRule {

    private final String id;
    private final boolean enabled;
    private final String requiredModId;
    private final List<String> biomeSelectors;
    private final List<String> tagNames;

    public BiomeTagRule(String id, boolean enabled, String requiredModId, List<String> biomeSelectors,
        List<String> tagNames) {
        this.id = id;
        this.enabled = enabled;
        this.requiredModId = requiredModId;
        this.biomeSelectors = Collections.unmodifiableList(biomeSelectors);
        this.tagNames = Collections.unmodifiableList(tagNames);
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getRequiredModId() {
        return requiredModId;
    }

    public List<String> getBiomeSelectors() {
        return biomeSelectors;
    }

    public List<String> getTagNames() {
        return tagNames;
    }

    public String getDebugSummary() {
        return "id=" + id
            + ", enabled="
            + enabled
            + ", requireMod="
            + requiredModId
            + ", biomes="
            + biomeSelectors
            + ", tags="
            + tagNames;
    }
}
