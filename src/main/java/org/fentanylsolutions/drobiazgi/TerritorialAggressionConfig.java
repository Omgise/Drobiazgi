package org.fentanylsolutions.drobiazgi;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultStringList;
import com.gtnewhorizon.gtnhlib.config.Config.Sync;

@Config(modid = Drobiazgi.MODID, category = "territorial_aggression")
public final class TerritorialAggressionConfig {

    @Comment("Enable territorial player aggression for listed entities.")
    @DefaultBoolean(true)
    @Sync
    public static boolean enabled = true;

    @Comment({ "Entities that should sometimes target nearby players in a territorial way.",
        "Accepted values: exact entity IDs from EntityList or fully qualified class names.",
        "Adults only. Matching babies stay passive.",
        "The goal uses a periodic target check and half of normal follow range." })
    @DefaultStringList({ "lotr.common.entity.animal.LOTREntityBear", "lotr.common.entity.animal.LOTREntityLionBase",
        "lotr.common.entity.animal.LOTREntityWildBoar", "lotr.common.entity.animal.LOTREntityAurochs",
        "drzhark.mocreatures.entity.animal.MoCEntityBear" })
    @Sync
    public static String[] entities = { "lotr.common.entity.animal.LOTREntityBear",
        "lotr.common.entity.animal.LOTREntityLionBase", "lotr.common.entity.animal.LOTREntityWildBoar",
        "lotr.common.entity.animal.LOTREntityAurochs", "drzhark.mocreatures.entity.animal.MoCEntityBear" };

    private TerritorialAggressionConfig() {}
}
