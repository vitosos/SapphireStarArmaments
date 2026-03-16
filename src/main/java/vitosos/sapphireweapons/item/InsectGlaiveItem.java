package vitosos.sapphireweapons.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import vitosos.sapphireweapons.util.IInsectGlaiveUser;
import vitosos.sapphireweapons.entity.KinsectEntity;
import vitosos.sapphireweapons.item.KinsectItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class InsectGlaiveItem extends SwordItem {

    // Make sure your class definition looks like this:
    // public class InsectGlaiveItem extends SwordItem {

    public InsectGlaiveItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    // Disable Vanilla Anvil Repairs
    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return false;
    }

    // Tells the game how long the right-click action can be held (72000 is standard max)
    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    // Defines the animation. "BOW" makes the player hold it up while right-clicking.
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand); // Starts the visual "using" state
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    // --- 1. ADD THIS NEW METHOD ---
    public void applyCustomEffects(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // The base glaive doesn't have any special elemental effects, so it stays empty!
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {

        // Adds a blank space between the attack damage stats and your custom lore
        tooltip.add(Text.empty());

        // Adds Line 1
        tooltip.add(Text.translatable(this.getTranslationKey() + ".desc.1").formatted(Formatting.GRAY, Formatting.ITALIC));

        // Adds Line 2
        tooltip.add(Text.translatable(this.getTranslationKey() + ".desc.2").formatted(Formatting.GRAY, Formatting.ITALIC));

        // Adds Line 3
        tooltip.add(Text.translatable(this.getTranslationKey() + ".desc.3").formatted(Formatting.GRAY, Formatting.ITALIC));

        // Adds Line 4
        tooltip.add(Text.translatable(this.getTranslationKey() + ".desc.4").formatted(Formatting.GRAY, Formatting.ITALIC));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        // Check if the target is a Hostile Mob OR a Player!
        boolean isValidTarget = target instanceof HostileEntity || target instanceof PlayerEntity;

        if (!attacker.getWorld().isClient() && isValidTarget && attacker instanceof IInsectGlaiveUser glaiveUser) {
            glaiveUser.grantEssence(2); // Grants Orange
        }

        return super.postHit(stack, target, attacker);
    }
}