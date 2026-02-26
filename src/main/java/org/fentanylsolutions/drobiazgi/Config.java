package org.fentanylsolutions.drobiazgi;

import org.fentanylsolutions.drobiazgi.compass.CompassRules;

import com.gtnewhorizon.gtnhlib.config.Config.Comment;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultBoolean;
import com.gtnewhorizon.gtnhlib.config.Config.DefaultStringList;
import com.gtnewhorizon.gtnhlib.config.Config.Sync;

@com.gtnewhorizon.gtnhlib.config.Config(modid = Drobiazgi.MODID)
public final class Config {

    @Comment({ "Dimension list used by compass filtering.",
        "Accepted values: numeric IDs like \"0\" or exact names resolved by FentLib DimensionUtil." })
    @DefaultStringList({ "0" })
    @Sync
    public static String[] compassDimensions = new String[] { "0" };

    @Comment({ "If true, listed dimensions are blocked from north mode.",
        "If false, north mode is allowed only in listed dimensions." })
    @DefaultBoolean(false)
    @Sync
    public static boolean compassUseDimensionBlacklist = false;

    @Comment("Enable compass north modification.")
    @DefaultBoolean(false)
    @Sync
    public static boolean enableCompassModification = false;

    @Comment("Enable debug logging for this mod.")
    @DefaultBoolean(false)
    public static boolean debugMode = false;

    private Config() {}

    public static void postConfiguration() {
        CompassRules.reloadFromConfig();
    }
}
