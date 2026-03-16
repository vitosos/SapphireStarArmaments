package vitosos.sapphireweapons.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import net.minecraft.particle.DustParticleEffect;
import org.joml.Vector3f;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.List;

public class AreaPowderItem extends Item {
    private final float aoeHealPercentage;
    private final String powderType; // "demon", "hardshell", "life", or "dust_of_life"

    public AreaPowderItem(Settings settings, float aoeHealPercentage, String powderType) {
        super(settings);
        this.aoeHealPercentage = aoeHealPercentage;
        this.powderType = powderType;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && user instanceof PlayerEntity player) {
            // Create a 20x20x20 box around the player
            Box area = player.getBoundingBox().expand(20.0);
            List<PlayerEntity> playersInRadius = world.getEntitiesByClass(PlayerEntity.class, area, EntityPredicates.VALID_ENTITY);

            for (PlayerEntity target : playersInRadius) {
                // 1. Apply Healing (if any)
                if (this.aoeHealPercentage > 0.0f) {
                    target.heal(target.getMaxHealth() * this.aoeHealPercentage);
                }

                // 2. Apply Buffs based on powder type
                // 1800 ticks = 1 minute and 30 seconds. Amplifier 0 = Level 1.
                if (this.powderType.equals("demon")) {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1800, 0));
                } else if (this.powderType.equals("hardshell")) {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 1800, 0));
                }
            }

            // Spawn some cool particles in the world (optional, but highly recommended!)
            world.syncWorldEvent(2005, player.getBlockPos(), 0);

            if (!player.isCreative()) {
                stack.decrement(1);
            }
            player.getItemCooldownManager().set(this, 20);
        }
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 30; // 1.5 Seconds
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
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        // Calculate how many ticks have passed since they started holding right-click
        int elapsed = this.getMaxUseTime(stack) - remainingUseTicks;

        // 1. SOUNDS (Played on the Server so everyone nearby hears the powder being prepped!)
        if (!world.isClient) {
            if (elapsed == 2 || elapsed == 6) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_GRAVEL_HIT, SoundCategory.PLAYERS, 0.25f, 0.5f + (elapsed * 0.05f));
            }
            else if (elapsed == 10 || elapsed == 20) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.PLAYERS, 1.0f, 1.0f + (elapsed * 0.05f));
            }
        }

        // 2. PARTICLES (Client-Side Only, starts after 0.5 seconds / 10 ticks)
        if (world.isClient && elapsed >= 10) {
            Vector3f color;

            // Assign RGB values based on powder type
            if (this.powderType.equals("demon")) {
                color = new Vector3f(0.9f, 0.2f, 0.2f); // Vibrant Red
            } else if (this.powderType.equals("hardshell")) {
                color = new Vector3f(0.9f, 0.5f, 0.1f); // Vibrant Orange
            } else {
                color = new Vector3f(0.2f, 0.9f, 0.2f); // Vibrant Green (Life / Dust of Life)
            }

            // Spawn 3 particles per tick to create a thick, dense cloud around the player
            for (int i = 0; i < 3; i++) {
                world.addParticle(new DustParticleEffect(color, 1.5f), // 1.5f scales the dust up so it's chunky!
                        user.getX() + (world.random.nextDouble() - 0.5) * 1.5,
                        user.getRandomBodyY() + 0.5,
                        user.getZ() + (world.random.nextDouble() - 0.5) * 1.5,
                        0.0, 0.02, 0.0);
            }
        }
    }

    public String getAnimationName() {
        return "hunter_dust";
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable(this.getTranslationKey() + ".desc.1").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable(this.getTranslationKey() + ".desc.2").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable(this.getTranslationKey() + ".desc.3").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable(this.getTranslationKey() + ".desc.4").formatted(Formatting.GRAY));
    }
}