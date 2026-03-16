package vitosos.sapphireweapons.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vitosos.sapphireweapons.util.IInsectGlaiveUser;

@Mixin(PlayerEntity.class)
public abstract class PlayerMobilityMixin implements IInsectGlaiveUser {

    @Unique private boolean isVaulting = false;
    @Unique private int vaultLandingGrace = 0;
    @Unique private boolean canAirDodge = false;
    @Unique private int vaultWindupTicks = 0;

    @Override public boolean isVaulting() { return this.isVaulting; }
    @Override public void setVaulting(boolean vaulting) { this.isVaulting = vaulting; }
    @Override public int getVaultWindupTicks() { return this.vaultWindupTicks; }
    @Override public void setVaultWindupTicks(int ticks) { this.vaultWindupTicks = ticks; }
    @Override public boolean canAirDodge() { return this.canAirDodge; }
    @Override public void setCanAirDodge(boolean canAirDodge) { this.canAirDodge = canAirDodge; }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onMobilityTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (this.isVaulting) this.vaultLandingGrace = 2;
        if (this.vaultLandingGrace > 0) this.vaultLandingGrace--;

        if (player.isOnGround() || player.isTouchingWater()) {
            if (this.vaultWindupTicks == 0) {
                if ((player.isOnGround() && player.getVelocity().y <= 0) || player.isTouchingWater()) {
                    this.isVaulting = false;
                    if (!player.getWorld().isClient() && player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                        vitosos.sapphireweapons.network.ServerNetworking.broadcastAnimation(serverPlayer, "clear");
                    }
                }
            }
        }
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void cancelVanillaJump(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.isUsingItem() && player.getActiveItem().getItem() instanceof vitosos.sapphireweapons.item.InsectGlaiveItem) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float modifyFallDamageAmount(float amount, DamageSource source) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (source.isOf(DamageTypes.FALL)) {
            boolean holdsGlaive = player.getMainHandStack().getItem() instanceof vitosos.sapphireweapons.item.InsectGlaiveItem;
            boolean holdsKinsect = player.getOffHandStack().getItem() instanceof vitosos.sapphireweapons.item.KinsectItem;
            if (holdsGlaive && holdsKinsect) {
                return amount * 0.25f;
            }
        }
        return amount;
    }
}