package vitosos.sapphireweapons.config;

public class SapphireConfig {
    // --- ECONOMY ---
    public double deathTaxPercentage = 0.10; // 10% lost on death
    public int xpToPointsConversionRate = 3; // 3 Points per 1 XP Level

    // --- TIMERS ---
    public long cantineCooldownTicks = 18000L; // 15 Minutes (20 ticks * 60 seconds * 15)
    public int cantineBuffTicks = 48000;
}