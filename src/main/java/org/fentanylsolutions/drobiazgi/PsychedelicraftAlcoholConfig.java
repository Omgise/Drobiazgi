package org.fentanylsolutions.drobiazgi;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultDouble;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultInt;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultStringList;
import com.gtnewhorizon.gtnhlib.config.Config.RangeDouble;
import com.gtnewhorizon.gtnhlib.config.Config.RangeInt;
import com.gtnewhorizon.gtnhlib.config.Config.Sync;

@Config(modid = Drobiazgi.MODID, category = "psychedelicraft_alcohol")
public final class PsychedelicraftAlcoholConfig {

    @Comment("Enable configurable Psychedelicraft drug effects on consumed items.")
    @DefaultBoolean(true)
    @Sync
    public static boolean enabled = true;

    @Comment("Remove LOTR mug nausea while Psychedelicraft alcohol compat is enabled.")
    @DefaultBoolean(true)
    @Sync
    public static boolean suppressLotrNausea = true;

    @Comment("Default delay in ticks before a configured effect starts being added.")
    @DefaultInt(20)
    @RangeInt(min = 0, max = 24000)
    @Sync
    public static int defaultDelayTicks = 20;

    @Comment("Default Psychedelicraft DrugInfluence influenceSpeed value.")
    @DefaultDouble(0.003D)
    @RangeDouble(min = 0.0D, max = 10.0D)
    @Sync
    public static double defaultInfluenceSpeed = 0.003D;

    @Comment("Default Psychedelicraft DrugInfluence influenceSpeedPlus value.")
    @DefaultDouble(0.002D)
    @RangeDouble(min = 0.0D, max = 10.0D)
    @Sync
    public static double defaultInfluenceSpeedPlus = 0.002D;

    @Comment({ "Psychedelicraft effect rules. One rule per line. Keys are key=value separated by ';'.",
        "Required: item=.../items=... or lotrAlcoholicMugs=true, plus potency/maxInfluence or lotrAlcoholicityScale.",
        "Optional: id, enabled, requireMod, drug, meta, delay, speed, speedPlus.",
        "Use registry names like 'minecraft:bread'. Meta supports single values, lists, and ranges.",
        "Use drug to choose the Psychedelicraft drug name, for example Alcohol or Tobacco.",
        "Use lotrAlcoholicityScale to derive potency from LOTR mug alcoholicity and mug strength metadata.",
        "Example: id=lotr_hobbit_pipe;item=lotr:hobbitPipe;drug=Tobacco;potency=0.15;delay=0" })
    @DefaultStringList({
        "id=lotr_alcoholic_mugs;requireMod=lotr;lotrAlcoholicMugs=true;drug=Alcohol;lotrAlcoholicityScale=0.2",
        "id=lotr_hobbit_pipe;requireMod=lotr;item=lotr:hobbitPipe;drug=Tobacco;potency=0.15;delay=0" })
    @Sync
    public static String[] rules = {
        "id=lotr_alcoholic_mugs;requireMod=lotr;lotrAlcoholicMugs=true;drug=Alcohol;lotrAlcoholicityScale=0.2",
        "id=lotr_hobbit_pipe;requireMod=lotr;item=lotr:hobbitPipe;drug=Tobacco;potency=0.15;delay=0" };

    private PsychedelicraftAlcoholConfig() {}
}
