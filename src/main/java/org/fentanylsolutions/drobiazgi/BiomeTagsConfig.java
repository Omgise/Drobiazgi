package org.fentanylsolutions.drobiazgi;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultStringList;

@Config(modid = Drobiazgi.MODID, category = "biome_dictionary_tags")
public final class BiomeTagsConfig {

    @Comment("Enable startup biome tag overrides.")
    @DefaultBoolean(true)
    public static boolean enabled = true;

    @Comment({ "BiomeDictionary tag rules. One rule per line. Changes are applied during startup and need a restart.",
        "Required keys: biomes, tags. Optional keys: id, enabled, requireMod.",
        "Biome selectors accept numeric IDs or names. For names, spacing and case are ignored.",
        "Use names for LOTR biomes. Their local IDs overlap vanilla IDs, so names are the only unambiguous option.",
        "tag/tags/type/types all work. Tags use Forge BiomeDictionary names such as FOREST, PLAINS, SANDY.", "Example:",
        "id=desert_lotr;biomes=Near Harad,Near Harad Red Desert;tags=SANDY,HOT,DRY" })
    @DefaultStringList({
        "id=lotr_forests;requireMod=lotr;biomes=shireWoodlands,oldForest,lothlorien,lothlorienEdge,fangorn,fangornClearing,woodlandRealm,woodlandRealmHills,mirkwoodNorth,lindonWoodlands,gondorWoodlands,rohanWoodlands,farHaradForest,farHaradCloudForest,gulfHaradForest,halfTrollForest,rivendell;tags=FOREST,DENSE",
        "id=lotr_plains;requireMod=lotr;biomes=shire,rohan,gondor,pelennor,lossarnach,lebennin,lamedon,eastBight,anduinVale,wold;tags=PLAINS",
        "id=lotr_mountains;requireMod=lotr;biomes=mistyMountains,mordorMountains,whiteMountains,greyMountains,blueMountains,redMountains,windMountains,ironHills,erebor,angmarMountains,haradMountains,forodwaithMountains,mirkwoodMountains,farHaradJungleMountains;tags=MOUNTAIN",
        "id=lotr_hills;requireMod=lotr;biomes=mistyMountainsFoothills,greyMountainsFoothills,blueMountainsFoothills,redMountainsFoothills,whiteMountainsFoothills,windMountainsFoothills,nearHaradHills,lamedonHills,dorEnErnilHills,dorwinionHills,rivendellHills;tags=HILLS",
        "id=lotr_swamps;requireMod=lotr;biomes=deadMarshes,midgewater,longMarshes,nindalf,nurnMarshes,shireMarshes,farHaradSwamp,farHaradMangrove;tags=SWAMP,WET",
        "id=lotr_cold;requireMod=lotr;biomes=forodwaith,forodwaithCoast,forodwaithGlacier,forodwaithMountains,tundra,taiga,coldfells;tags=COLD,SNOWY",
        "id=lotr_sandy;requireMod=lotr;biomes=nearHarad,nearHaradSemiDesert,nearHaradRedDesert,nearHaradOasis,farHarad,farHaradArid,farHaradAridHills,gulfHarad,lastDesert,beachWhite,beachGravel,farHaradCoast;tags=SANDY,HOT,DRY",
        "id=lotr_jungle;requireMod=lotr;biomes=farHaradJungle,farHaradJungleEdge,farHaradJungleLake,farHaradJungleMountains,farHaradKanuka;tags=JUNGLE,HOT,DENSE",
        "id=lotr_wastelands;requireMod=lotr;biomes=mordor,gorgoroth,udun,morgulVale,dolGuldur,mirkwoodCorrupted,brownLands,easternDesolation,dagorlad,nanUngol,fangornWasteland;tags=DEAD,SPOOKY,WASTELAND",
        "id=lotr_waters;requireMod=lotr;biomes=river,ocean,lake,nurnen,anduinMouth,entwashMouth,nearHaradRiverbank;tags=WATER",
        "id=lotr_beaches;requireMod=lotr;biomes=lindonCoast,forodwaithCoast,farHaradCoast,beachWhite,beachGravel,tolfalas,island,rhunIsland;tags=BEACH" })
    public static String[] rules = {};

    private BiomeTagsConfig() {}
}
