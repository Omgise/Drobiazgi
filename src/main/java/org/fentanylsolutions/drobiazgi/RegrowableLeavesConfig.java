package org.fentanylsolutions.drobiazgi;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultInt;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultStringList;
import com.gtnewhorizon.gtnhlib.config.Config.RangeInt;
import com.gtnewhorizon.gtnhlib.config.Config.Sync;

@Config(modid = Drobiazgi.MODID, category = "regrowable_leaves")
public final class RegrowableLeavesConfig {

    @Comment("Enable configurable leaf regrowth tracking.")
    @DefaultBoolean(false)
    @Sync
    public static boolean enabled = false;

    @Comment("How often regrowth processing runs, in ticks.")
    @DefaultInt(20)
    @RangeInt(min = 1, max = 24000)
    @Sync
    public static int processingIntervalTicks = 20;

    @Comment("Number of tracked leaves processed each regrowth cycle.")
    @DefaultInt(128)
    @RangeInt(min = 1, max = 4096)
    @Sync
    public static int checksPerCycle = 128;

    @Comment("Minimum delay before regrowth is attempted, in ticks.")
    @DefaultInt(120)
    @RangeInt(min = 1, max = 24000)
    @Sync
    public static int minDelayTicks = 120;

    @Comment("Maximum delay before regrowth is attempted, in ticks.")
    @DefaultInt(600)
    @RangeInt(min = 1, max = 24000)
    @Sync
    public static int maxDelayTicks = 600;

    @Comment("Maximum leaf-to-log search depth. 0 means only directly adjacent logs count.")
    @DefaultInt(6)
    @RangeInt(min = 0, max = 64)
    @Sync
    public static int maxConnectionDepth = 6;

    @Comment("Retry delay in ticks when the tracked position is in an unloaded chunk area.")
    @DefaultInt(100)
    @RangeInt(min = 1, max = 24000)
    @Sync
    public static int unloadedChunkRetryTicks = 100;

    @Comment({ "Leaf regrowth rules. One rule per line, key=value entries separated by ';'.",
        "Required keys: leaf, log. Optional keys: id, enabled, leafMeta, chance.",
        "Block selectors use registry names (example: minecraft:leaves, lotr:leaves).",
        "leafMeta accepts single values, comma lists, or ranges like 0-3. Omit it for any metadata.",
        "chance is a 0..1 value for how often broken leaves are tracked for regrowth.",
        "Example: id=lotr_wood1;leaf=lotr:leaves;log=lotr:wood;leafMeta=0-3;chance=0.75" })
    @DefaultStringList({ "id=vanilla_leaves;leaf=minecraft:leaves;log=minecraft:log;leafMeta=0-3",
        "id=vanilla_leaves2;leaf=minecraft:leaves2;log=minecraft:log2;leafMeta=0-1",
        "id=lotr_wood1;leaf=lotr:leaves;log=lotr:wood;leafMeta=0-3",
        "id=lotr_wood2;leaf=lotr:leaves2;log=lotr:wood2;leafMeta=0-3",
        "id=lotr_wood3;leaf=lotr:leaves3;log=lotr:wood3;leafMeta=0-3",
        "id=lotr_wood4;leaf=lotr:leaves4;log=lotr:wood4;leafMeta=0-3",
        "id=lotr_wood5;leaf=lotr:leaves5;log=lotr:wood5;leafMeta=0-3",
        "id=lotr_wood6;leaf=lotr:leaves6;log=lotr:wood6;leafMeta=0-3",
        "id=lotr_wood7;leaf=lotr:leaves7;log=lotr:wood7;leafMeta=0-3",
        "id=lotr_wood8;leaf=lotr:leaves8;log=lotr:wood8;leafMeta=0-3",
        "id=lotr_wood9;leaf=lotr:leaves9;log=lotr:wood9;leafMeta=0-1",
        "id=lotr_fruit;leaf=lotr:fruitLeaves;log=lotr:fruitWood;leafMeta=0-3" })
    @Sync
    public static String[] rules = {};

    private RegrowableLeavesConfig() {}
}
