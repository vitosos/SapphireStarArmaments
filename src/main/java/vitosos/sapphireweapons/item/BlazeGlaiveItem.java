package vitosos.sapphireweapons.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;

public class BlazeGlaiveItem extends InsectGlaiveItem {

    public BlazeGlaiveItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public void applyCustomEffects(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.getWorld().isClient()) {
            // Set the target on fire for 4 seconds
            target.setOnFireFor(4);
        }
    }
}