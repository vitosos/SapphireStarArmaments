package vitosos.sapphireweapons.mixin.client;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vitosos.sapphireweapons.SapphireStarArmaments;
import vitosos.sapphireweapons.item.AreaPowderItem;
import vitosos.sapphireweapons.item.PercentagePotionItem;
import vitosos.sapphireweapons.client.animation.ISapphireAnimatedPlayer;

import vitosos.sapphireweapons.item.InsectGlaiveItem;
import vitosos.sapphireweapons.util.IInsectGlaiveUser;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class PlayerConsumableAnimMixin {

    @Unique
    private String currentConsumableAnim = "";

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPlayerTick(CallbackInfo ci) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;

        if (player instanceof ISapphireAnimatedPlayer animatedPlayer) {
            ModifierLayer<IAnimation> layer = animatedPlayer.getSapphireLayer();

            if (layer != null) {

                // 1. Check if the player is actively holding right-click on an item
                if (player.isUsingItem()) {
                    ItemStack activeStack = player.getActiveItem();
                    String targetAnim = "";

                    if (activeStack.getItem() instanceof PercentagePotionItem potion) {
                        targetAnim = potion.getAnimationName();
                    } else if (activeStack.getItem() instanceof AreaPowderItem powder) {
                        targetAnim = powder.getAnimationName();
                    } else if (activeStack.getItem() instanceof InsectGlaiveItem) {
                        boolean isVaulting = ((IInsectGlaiveUser) player).isVaulting();

                        // Only play the hold animation if they are on the ground and not actively vaulting!
                        if (player.isOnGround() && !isVaulting) {
                            targetAnim = "glaive_hold";
                        }
                    }

                    if (!targetAnim.isEmpty()) {
                        // 2. If the animation isn't already playing, start it!
                        if (!currentConsumableAnim.equals(targetAnim)) {
                            var anim = PlayerAnimationRegistry.getAnimation(new Identifier(SapphireStarArmaments.MOD_ID, targetAnim));

                            if (anim != null) {
                                layer.setAnimation(new KeyframeAnimationPlayer(anim));
                                currentConsumableAnim = targetAnim;
                            }
                        }
                        return; // Successfully running, skip the cancel logic below
                    }
                }

                // 3. CANCEL LOGIC: If we reach this line, the player is NOT using a consumable or glaive hold.
                if (!currentConsumableAnim.isEmpty()) {

                    // Check if a Glaive Combat Animation is currently taking over!
                    boolean isDoingGlaiveCombat = false;
                    if (player instanceof IInsectGlaiveUser glaiveUser) {
                        // Checks if they are vaulting, winding up a vault, or doing the ultimate nuke
                        isDoingGlaiveCombat = glaiveUser.isVaulting() || glaiveUser.getVaultWindupTicks() > 0 || glaiveUser.getNukeWindupTicks() > 0;
                    }

                    // Only wipe the layer if we aren't doing a combat move!
                    // This allows ClientTickHandler to seamlessly inject "vault" without this Mixin immediately deleting it!
                    if (!isDoingGlaiveCombat) {
                        layer.setAnimation(null);
                    }

                    // Reset our internal tracker either way so it doesn't get stuck
                    currentConsumableAnim = "";
                }
            }
        }
    }
}