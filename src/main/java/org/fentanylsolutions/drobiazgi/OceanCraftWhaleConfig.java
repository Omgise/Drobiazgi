package org.fentanylsolutions.drobiazgi;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultInt;
import com.gtnewhorizon.gtnhlib.config.Config.RangeInt;
import com.gtnewhorizon.gtnhlib.config.Config.Sync;

@Config(modid = Drobiazgi.MODID, category = "oceancraft_whales")
public final class OceanCraftWhaleConfig {

    @Comment("Split OceanCraft whales into separate humpback, narwhal, and blue whale entities.")
    @DefaultBoolean(false)
    @Sync
    public static boolean enableSeparateWhales = false;

    @Comment("Relative weight for humpback whales when the split is enabled. 0 disables natural humpback spawns.")
    @DefaultInt(2)
    @RangeInt(min = 0, max = 1000)
    @Sync
    public static int humpbackWeight = 2;

    @Comment("Relative weight for narwhals when the split is enabled. 0 disables natural narwhal spawns.")
    @DefaultInt(1)
    @RangeInt(min = 0, max = 1000)
    @Sync
    public static int narwhalWeight = 1;

    @Comment("Relative weight for blue whales when the split is enabled. 0 disables natural blue whale spawns.")
    @DefaultInt(1)
    @RangeInt(min = 0, max = 1000)
    @Sync
    public static int blueWhaleWeight = 1;

    private OceanCraftWhaleConfig() {}
}
