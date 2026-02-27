package org.fentanylsolutions.drobiazgi;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultInt;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultStringList;
import com.gtnewhorizon.gtnhlib.config.Config.RangeInt;
import com.gtnewhorizon.gtnhlib.config.Config.Sync;

@Config(modid = Drobiazgi.MODID, category = "custom_npcs_spawning")
public final class CustomNpcsSpawningConfig {

    @Comment("Enable Drobiazgi natural spawning for CustomNPC clones.")
    @DefaultBoolean(false)
    @Sync
    public static boolean enabled = false;

    @Comment("Disable CustomNPCs built-in WIP natural spawner while this system is enabled.")
    @DefaultBoolean(true)
    @Sync
    public static boolean replaceCustomNpcsWipSpawner = true;

    @Comment("Ticks between spawn cycles.")
    @DefaultInt(100)
    @RangeInt(min = 20, max = 24000)
    @Sync
    public static int tickInterval = 100;

    @Comment("Spawn attempts per player each cycle.")
    @DefaultInt(2)
    @RangeInt(min = 1, max = 64)
    @Sync
    public static int attemptsPerPlayerPerCycle = 2;

    @Comment("Hard cap of loaded CustomNPC entities per dimension.")
    @DefaultInt(80)
    @RangeInt(min = 1, max = 4096)
    @Sync
    public static int maxNpcsPerDimension = 80;

    @Comment("Minimum distance from players for spawn attempts.")
    @DefaultInt(24)
    @RangeInt(min = 0, max = 512)
    @Sync
    public static int minPlayerDistance = 24;

    @Comment("Maximum distance from players for spawn attempts.")
    @DefaultInt(96)
    @RangeInt(min = 1, max = 1024)
    @Sync
    public static int maxPlayerDistance = 96;

    @Comment("Maximum successful spawns in one world cycle.")
    @DefaultInt(16)
    @RangeInt(min = 1, max = 512)
    @Sync
    public static int maxSpawnsPerCycle = 16;

    @Comment({ "Spawn rules. One rule per line. Keys are key=value separated by ';'.",
        "Required keys: clone, tab. Optional keys: id, enabled, weight, chance, group, dims, dimMode, biomes,",
        "biomeMode, time, light, y.",
        "Biome selectors in 'biomes' support both numeric IDs and names, including LOTR biome names when LOTR is loaded.",
        "Example:", "id=bandit_plains;clone=Bandit;tab=1;weight=10;chance=0.35;group=1-2;dims=0;dimMode=whitelist;",
        "biomes=Plains,Forest,4;biomeMode=whitelist;time=day;light=7-15;y=62-120" })
    @DefaultStringList({})
    @Sync
    public static String[] rules = {};

    private CustomNpcsSpawningConfig() {}
}
