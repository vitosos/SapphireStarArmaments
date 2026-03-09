package vitosos.sapphireweapons.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MonsterMaterialItem extends Item {
    private final int sellValue;

    public MonsterMaterialItem(Settings settings, int sellValue) {
        super(settings);
        this.sellValue = sellValue;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // 1. Adds the description line (pulls from en_us.json)
        tooltip.add(Text.translatable(this.getTranslationKey() + ".desc").formatted(Formatting.GRAY, Formatting.ITALIC));

        // 2. Adds a blank space
        tooltip.add(Text.empty());

        // 3. Adds the Value line
        tooltip.add(Text.literal("Value: " + this.sellValue + "pts").formatted(Formatting.GOLD));
    }

    public int getSellValue() {
        return this.sellValue;
    }
}