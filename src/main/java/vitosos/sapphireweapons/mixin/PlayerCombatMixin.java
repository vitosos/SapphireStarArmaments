package vitosos.sapphireweapons.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vitosos.sapphireweapons.util.IInsectGlaiveUser;
import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class PlayerCombatMixin implements IInsectGlaiveUser {

    @Unique private int glaiveAttackTicks = 0;
    @Unique private boolean hasUsedAerialAttack = false;
    @Unique private boolean isGlaiveAttackingActive = false;
    @Unique private int glaiveInvulnTicks = 0;
    @Unique private int aerialChainCount = 0;
    @Unique private int nukeWindupTicks = 0;

    @Override public int getGlaiveAttackTicks() { return this.glaiveAttackTicks; }
    @Override public void setGlaiveAttackTicks(int ticks) { this.glaiveAttackTicks = ticks; }
    @Override public void decrementGlaiveAttackTicks() { if (this.glaiveAttackTicks > 0) this.glaiveAttackTicks--; }
    @Override public boolean hasUsedAerialAttack() { return this.hasUsedAerialAttack; }
    @Override public void setHasUsedAerialAttack(boolean used) { this.hasUsedAerialAttack = used; }
    @Override public boolean isGlaiveAttackingActive() { return this.isGlaiveAttackingActive; }
    @Override public void setGlaiveAttackingActive(boolean active) { this.isGlaiveAttackingActive = active; }
    @Override public int getGlaiveInvulnTicks() { return this.glaiveInvulnTicks; }
    @Override public void setGlaiveInvulnTicks(int ticks) { this.glaiveInvulnTicks = ticks; }
    @Override public int getAerialChainCount() { return this.aerialChainCount; }
    @Override public void setAerialChainCount(int count) { this.aerialChainCount = count; }
    @Override public int getNukeWindupTicks() { return this.nukeWindupTicks; }
    @Override public void setNukeWindupTicks(int ticks) { this.nukeWindupTicks = ticks; }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onCombatTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        boolean isServer = !player.getWorld().isClient();
        IInsectGlaiveUser interfaceUser = (IInsectGlaiveUser) this;

        if (this.glaiveInvulnTicks > 0) this.glaiveInvulnTicks--;

        if (player.isOnGround() || player.isTouchingWater()) {
            this.isGlaiveAttackingActive = false;
            this.glaiveAttackTicks = 0;
            this.hasUsedAerialAttack = false;
        }

        if (this.nukeWindupTicks > 0) {
            this.nukeWindupTicks--;
            player.setVelocity(player.getVelocity().multiply(0.5));
            player.velocityModified = true;

            if (this.nukeWindupTicks == 0 && isServer) {
                interfaceUser.setTripleBuffTicks(0);
                interfaceUser.setRedEssenceTicks(0);
                interfaceUser.setWhiteEssenceTicks(0);
                interfaceUser.setOrangeEssenceTicks(0);

                if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                    net.minecraft.network.PacketByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                    buf.writeInt(0); buf.writeInt(0); buf.writeInt(0); buf.writeInt(0);
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(serverPlayer, new net.minecraft.util.Identifier("sapphire-star-armaments", "essence_sync"), buf);
                }

                net.minecraft.item.ItemStack offHandStack = player.getOffHandStack();
                if (offHandStack.getItem() instanceof vitosos.sapphireweapons.item.KinsectItem kinsectItem) {
                    interfaceUser.setKinsectDeployed(true);
                    vitosos.sapphireweapons.entity.KinsectEntity kinsect = new vitosos.sapphireweapons.entity.KinsectEntity(player.getWorld(), player, kinsectItem.getKinsectDamage() * 2.0f);
                    kinsect.setItem(offHandStack);
                    kinsect.setNuke(true);
                    kinsect.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, 2.5f, 0.0f);
                    player.getWorld().spawnEntity(kinsect);
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sound.SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.5f);
                }
            }
        }

        if (this.glaiveAttackTicks > 0) {
            this.glaiveAttackTicks--;
            if (this.glaiveAttackTicks == 0) this.isGlaiveAttackingActive = true;
        }

        if (this.isGlaiveAttackingActive && isServer) {
            net.minecraft.util.math.Box hitbox = player.getBoundingBox().expand(2.0D);
            java.util.List<Entity> targets = player.getWorld().getOtherEntities(player, hitbox);
            boolean hitConnected = false;

            net.minecraft.item.ItemStack mainHandStack = player.getMainHandStack();

            for (Entity target : targets) {
                if (target instanceof LivingEntity livingTarget) {
                    // 1. Get Base Damage
                    float baseDmg = (float) player.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE);

                    // 2. Add Enchantment Damage (Sharpness, Smite)
                    float enchantDmg = net.minecraft.enchantment.EnchantmentHelper.getAttackDamage(mainHandStack, livingTarget.getGroup());

                    // 3. Combine and apply the 80% aerial modifier
                    float aerialDmg = (baseDmg + enchantDmg) * 0.8f;

                    // THE FIX: Apply custom effects (like Fire) BEFORE dealing damage!
                    if (mainHandStack.getItem() instanceof vitosos.sapphireweapons.item.InsectGlaiveItem glaive) {
                        glaive.applyCustomEffects(mainHandStack, livingTarget, player);
                    }

                    // 4. Deal the damage
                    boolean wasDamaged = livingTarget.damage(player.getDamageSources().playerAttack(player), aerialDmg);

                    // Only trigger on-hit effects if the damage actually went through (ignoring i-frames)
                    if (wasDamaged) {
                        hitConnected = true;

                        // 5. Trigger on-hit enchantments (like Slowness from Bane of Arthropods)
                        net.minecraft.enchantment.EnchantmentHelper.onTargetDamaged(player, livingTarget);

                        if (livingTarget instanceof net.minecraft.entity.mob.HostileEntity || livingTarget instanceof PlayerEntity) {
                            interfaceUser.grantEssence(1);
                        }
                    }
                }
            }

            if (hitConnected) {
                // 6. Consume 1 point of durability if we hit at least one enemy!
                mainHandStack.damage(1, player, (p) -> p.sendToolBreakStatus(net.minecraft.util.Hand.MAIN_HAND));

                Vec3d vel = player.getVelocity();
                player.setVelocity(vel.x, 1.05, vel.z);
                player.velocityModified = true;

                this.isGlaiveAttackingActive = false;
                this.hasUsedAerialAttack = false;
                interfaceUser.setCanAirDodge(true);

                if (player.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getBodyY(0.5D), player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                    serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0f, 0.5f);
                }

                if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                            serverPlayer,
                            new net.minecraft.util.Identifier("sapphire-star-armaments", "glaive_hit_sync"),
                            net.fabricmc.fabric.api.networking.v1.PacketByteBufs.empty()
                    );
                    vitosos.sapphireweapons.network.ServerNetworking.broadcastAnimation(serverPlayer, "vault");
                }
            }
        }
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void onPreGroundAttack(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!player.getWorld().isClient() && target instanceof LivingEntity livingTarget) {
            net.minecraft.item.ItemStack stack = player.getMainHandStack();
            if (stack.getItem() instanceof vitosos.sapphireweapons.item.InsectGlaiveItem glaive) {
                // Apply fire/poison BEFORE the damage math happens!
                glaive.applyCustomEffects(stack, livingTarget, player);
            }
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.isGlaiveAttackingActive || this.glaiveInvulnTicks > 0) {
            cir.setReturnValue(false);
        }
    }
}