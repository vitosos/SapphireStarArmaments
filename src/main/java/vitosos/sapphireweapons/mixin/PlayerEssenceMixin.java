package vitosos.sapphireweapons.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vitosos.sapphireweapons.util.IInsectGlaiveUser;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEssenceMixin implements IInsectGlaiveUser {

    @Unique private boolean isKinsectDeployed = false;
    @Unique private int redEssenceTicks = 0;
    @Unique private int whiteEssenceTicks = 0;
    @Unique private int orangeEssenceTicks = 0;
    @Unique private int tripleBuffTicks = 0;

    @Unique private static final UUID RED_DMG_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    @Unique private static final UUID WHITE_SPD_UUID = UUID.fromString("66666666-7777-8888-9999-000000000000");

    @Override public boolean isKinsectDeployed() { return this.isKinsectDeployed; }
    @Override public void setKinsectDeployed(boolean deployed) { this.isKinsectDeployed = deployed; }
    @Override public int getRedEssenceTicks() { return this.redEssenceTicks; }
    @Override public void setRedEssenceTicks(int ticks) { this.redEssenceTicks = ticks; }
    @Override public int getWhiteEssenceTicks() { return this.whiteEssenceTicks; }
    @Override public void setWhiteEssenceTicks(int ticks) { this.whiteEssenceTicks = ticks; }
    @Override public int getOrangeEssenceTicks() { return this.orangeEssenceTicks; }
    @Override public void setOrangeEssenceTicks(int ticks) { this.orangeEssenceTicks = ticks; }
    @Override public int getTripleBuffTicks() { return this.tripleBuffTicks; }
    @Override public void setTripleBuffTicks(int ticks) { this.tripleBuffTicks = ticks; }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onEssenceTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        boolean isServer = !player.getWorld().isClient();

        if (this.tripleBuffTicks > 0) {
            this.tripleBuffTicks--;
            if (this.tripleBuffTicks == 0) {
                this.redEssenceTicks = 0;
                this.whiteEssenceTicks = 0;
                this.orangeEssenceTicks = 0;
            }
        } else {
            if (this.redEssenceTicks > 0) this.redEssenceTicks--;
            if (this.whiteEssenceTicks > 0) this.whiteEssenceTicks--;
            if (this.orangeEssenceTicks > 0) this.orangeEssenceTicks--;
        }

        if (isServer) {
            EntityAttributeInstance dmgAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            EntityAttributeInstance spdAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);

            if (dmgAttr != null) dmgAttr.removeModifier(RED_DMG_UUID);
            if (spdAttr != null) spdAttr.removeModifier(WHITE_SPD_UUID);

            if (this.tripleBuffTicks > 0) {
                if (dmgAttr != null) dmgAttr.addTemporaryModifier(new EntityAttributeModifier(RED_DMG_UUID, "Triple Red", 0.20, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                if (spdAttr != null) spdAttr.addTemporaryModifier(new EntityAttributeModifier(WHITE_SPD_UUID, "Triple White", 0.50, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
            } else {
                if (this.redEssenceTicks > 0 && dmgAttr != null) dmgAttr.addTemporaryModifier(new EntityAttributeModifier(RED_DMG_UUID, "Red Buff", 0.10, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                if (this.whiteEssenceTicks > 0 && spdAttr != null) spdAttr.addTemporaryModifier(new EntityAttributeModifier(WHITE_SPD_UUID, "White Buff", 0.25, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float modifyEssenceDamageAmount(float amount, DamageSource source) {
        if (this.tripleBuffTicks > 0) return amount * 0.8f;
        if (this.orangeEssenceTicks > 0) return amount * 0.9f;
        return amount;
    }

    @Override
    public void grantEssence(int type) {
        if (this.tripleBuffTicks > 0) return;

        if (type == 0) this.redEssenceTicks = 1200;
        else if (type == 1) this.whiteEssenceTicks = 1200;
        else if (type == 2) this.orangeEssenceTicks = 1200;

        PlayerEntity player = (PlayerEntity) (Object) this;
        if (this.redEssenceTicks > 0 && this.whiteEssenceTicks > 0 && this.orangeEssenceTicks > 0) {
            this.tripleBuffTicks = 1800;
            if (!player.getWorld().isClient()) {
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_SQUID_SQUIRT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        } else {
            if (!player.getWorld().isClient()) {
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 1.0f, 1.5f);
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_SLIME_JUMP, SoundCategory.PLAYERS, 1.0f, 1.2f);
            }
        }

        if (!player.getWorld().isClient() && player instanceof ServerPlayerEntity serverPlayer) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(this.redEssenceTicks);
            buf.writeInt(this.whiteEssenceTicks);
            buf.writeInt(this.orangeEssenceTicks);
            buf.writeInt(this.tripleBuffTicks);
            ServerPlayNetworking.send(serverPlayer, new Identifier("sapphire-star-armaments", "essence_sync"), buf);
        }
    }
}