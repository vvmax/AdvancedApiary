package ru.advancedapiary;
import java.io.File;
import net.minecraftforge.common.config.Configuration;
public final class Settings {
    public static boolean disableBeeAI = false;
    public static int cycleTicks = 550;
    public static boolean allGenesPure = false;
    public static boolean specialties = true;
    public static double production = 1.0;
    public static void load(File file) {
        Configuration c = new Configuration(file); c.load();
        disableBeeAI = c.getBoolean("disableBeeAI", "bees", false, "Default false enables bee AI. Set true to disable bee effects, pollination, flower growth and external flower searches. Climate, weather and lighting still apply. Queens never age in either mode. Restart required.");
        cycleTicks = c.getInt("cycleTicks", "production", 550, 20, 72000, "Ticks per queen production cycle before speed upgrades.");
        production = c.getFloat("productionMultiplier", "production", 1F, 0.01F, 100F, "Multiplies species product chances and genetic speed.");
        specialties = c.getBoolean("specialties", "production", true, "Enable specialty products when the queen can work in current conditions.");
        allGenesPure = c.getBoolean("requireAllGenesPure", "genetics", false, "False: pure species required. True: every chromosome must be homozygous.");
        if(c.hasChanged()) c.save();
    }
    private Settings() {}
}
