package org.fentanylsolutions.drobiazgi;

import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultFloat;
import com.gtnewhorizon.gtnhlib.config.Config.RangeFloat;
import com.gtnewhorizon.gtnhlib.config.Config.Sync;

@com.gtnewhorizon.gtnhlib.config.Config(modid = Drobiazgi.MODID, category = "doggy_talents")
public final class DoggyTalentsConfig {

    @Comment("Enable the Doggy Talents throw tuning fix.")
    @DefaultBoolean(false)
    @Sync
    public static boolean enableThrowTuningFix = false;

    @Comment("Enable compatibility fix for ItemPhysic causing Doggy fetch loops.")
    @DefaultBoolean(true)
    @Sync
    public static boolean enableFetchLoopCompatFix = true;

    @Comment("Pitch offset used for throw bone and throw stick.")
    @DefaultFloat(-20.0f)
    @RangeFloat(min = -180.0f, max = 180.0f)
    @Sync
    public static float throwPitchOffset = -20.0f;

    @Comment("Throw force used for throw bone and throw stick.")
    @DefaultFloat(1.5f)
    @RangeFloat(min = 0.0f, max = 10.0f)
    @Sync
    public static float throwForce = 1.5f;

    private DoggyTalentsConfig() {}
}
