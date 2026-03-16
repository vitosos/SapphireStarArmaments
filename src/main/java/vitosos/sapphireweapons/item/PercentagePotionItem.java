package vitosos.sapphireweapons.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;



public class PercentagePotionItem extends Item {
    private final float healPercentage;
    private final int useTicks;
    private final boolean restoresHunger;
    private final String animationName;

    // useTicks: 40 = 2 seconds. 0 = Instant.
    public PercentagePotionItem(Settings settings, float healPercentage, int useTicks, boolean restoresHunger, String animationName) {
        super(settings);
        this.healPercentage = healPercentage;
        this.useTicks = useTicks;
        this.restoresHunger = restoresHunger;
        this.animationName = animationName;
    }

    public String getAnimationName() {
        return this.animationName;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Simply start the use action, do NOT apply effects here!
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player) {
            this.applyEffects(player);
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            // NEW: Add a 1-second (20 tick) cooldown to the item!
            player.getItemCooldownManager().set(this, 20);
        }
        return stack;
    }

    private void applyEffects(PlayerEntity player) {
        if (!player.getWorld().isClient) {
            float maxHealth = player.getMaxHealth();
            player.heal(maxHealth * this.healPercentage);

            if (this.restoresHunger) {
                // Fills the food bar (20) and saturation completely
                player.getHungerManager().add(20, 20.0f);
            }
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return this.useTicks;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    // --- MUTE THE VANILLA SOUNDS ---
    @Override
    public net.minecraft.sound.SoundEvent getDrinkSound() {
        return net.minecraft.sound.SoundEvents.INTENTIONALLY_EMPTY;
    }

    @Override
    public net.minecraft.sound.SoundEvent getEatSound() {
        return net.minecraft.sound.SoundEvents.INTENTIONALLY_EMPTY;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable(this.getTranslationKey() + ".desc.1").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable(this.getTranslationKey() + ".desc.2").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable(this.getTranslationKey() + ".desc.3").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable(this.getTranslationKey() + ".desc.4").formatted(Formatting.GRAY));
    }
}