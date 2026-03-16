package vitosos.sapphireweapons.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            // 1. Initialize the Screen Builder
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("Sapphire Star Armaments Config"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // 2. General Category
            ConfigCategory general = builder.getOrCreateCategory(Text.literal("General Options"));

            // 3. Points death tax
            general.addEntry(entryBuilder.startDoubleField(Text.literal("Death Tax Percentage"), SapphireConfigManager.CONFIG.deathTaxPercentage)
                    .setDefaultValue(0.10)
                    .setMin(0.0).setMax(1.0)
                    .setTooltip(Text.literal("Percentage of points lost on death (0.10 = 10%)"))
                    .setSaveConsumer(newValue -> SapphireConfigManager.CONFIG.deathTaxPercentage = newValue) // Saves to our active RAM
                    .build());

            // 4. XP Points to Points rate
            general.addEntry(entryBuilder.startIntField(Text.literal("XP to Points Rate"), SapphireConfigManager.CONFIG.xpToPointsConversionRate)
                    .setDefaultValue(3)
                    .setTooltip(Text.literal("How many points 1 XP level is worth in the Guild Stock Box"))
                    .setSaveConsumer(newValue -> SapphireConfigManager.CONFIG.xpToPointsConversionRate = newValue)
                    .build());

            // 5. Cantine Cooldown Timer
            general.addEntry(entryBuilder.startLongField(Text.literal("Cantine Cooldown (Ticks)"), SapphireConfigManager.CONFIG.cantineCooldownTicks)
                    .setDefaultValue(18000L)
                    .setTooltip(Text.literal("Cooldown between meals. (20 ticks = 1 second. 18000 = 15 mins)"))
                    .setSaveConsumer(newValue -> SapphireConfigManager.CONFIG.cantineCooldownTicks = newValue)
                    .build());

            // 6. Cantine Buffs Timers
            general.addEntry(entryBuilder.startIntField(Text.literal("Cantine Buff Duration (Ticks)"), SapphireConfigManager.CONFIG.cantineBuffTicks)
                    .setDefaultValue(48000)
                    .setTooltip(Text.literal("All meal buffs duration. (20 ticks = 1 second. 48000 = 40 mins)"))
                    .setSaveConsumer(newValue -> SapphireConfigManager.CONFIG.cantineBuffTicks = newValue)
                    .build());

            // 7. Define what happens when they click "Save & Quit"
            builder.setSavingRunnable(() -> {
                // Takes the updated variables from RAM and overwrites our .json file!
                SapphireConfigManager.save();
            });

            return builder.build();
        };
    }
}