package vitosos.sapphireweapons.client.event;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import vitosos.sapphireweapons.SapphireStarArmaments;
import vitosos.sapphireweapons.client.ClientKeybinds;
import vitosos.sapphireweapons.client.animation.ClientAnimationHelper;
import vitosos.sapphireweapons.client.animation.ISapphireAnimatedPlayer;
import vitosos.sapphireweapons.item.InsectGlaiveItem;
import vitosos.sapphireweapons.item.KinsectItem;
import vitosos.sapphireweapons.util.IInsectGlaiveUser;

public class ClientTickHandler {

    private static boolean wasJumpPressedLastTick = false;
    private static boolean wasAttackPressedLastTick = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            boolean isJumpCurrentlyPressed = client.options.jumpKey.isPressed();
            boolean jumpJustPressed = isJumpCurrentlyPressed && !wasJumpPressedLastTick;
            wasJumpPressedLastTick = isJumpCurrentlyPressed;

            boolean isAttackCurrentlyPressed = client.options.attackKey.isPressed();
            boolean attackJustPressed = isAttackCurrentlyPressed && !wasAttackPressedLastTick;
            wasAttackPressedLastTick = isAttackCurrentlyPressed;

            IInsectGlaiveUser glaiveUser = (IInsectGlaiveUser) client.player;
            boolean holdingGlaiveInHand = client.player.getMainHandStack().getItem() instanceof InsectGlaiveItem;
            boolean isActivelyUsingGlaive = client.player.isUsingItem() && client.player.getActiveItem().getItem() instanceof InsectGlaiveItem;

            // --- BLAZE GLAIVE PARTICLES ---
            if (holdingGlaiveInHand && client.player.getMainHandStack().getItem() == vitosos.sapphireweapons.registry.ModItems.BLAZE_GLAIVE) {
                // 15% chance to spawn particles every tick to prevent screen clutter
                if (client.world.random.nextFloat() < 0.15f) {
                    client.world.addParticle(net.minecraft.particle.ParticleTypes.LARGE_SMOKE,
                            client.player.getX() + (client.world.random.nextDouble() - 0.5),
                            client.player.getRandomBodyY(),
                            client.player.getZ() + (client.world.random.nextDouble() - 0.5),
                            0.0D, 0.05D, 0.0D);
                    client.world.addParticle(net.minecraft.particle.ParticleTypes.FLAME,
                            client.player.getX() + (client.world.random.nextDouble() - 0.5),
                            client.player.getRandomBodyY(),
                            client.player.getZ() + (client.world.random.nextDouble() - 0.5),
                            0.0D, 0.02D, 0.0D);
                }
            }

            // --- JUMP / VAULT LOGIC ---
            if (jumpJustPressed) {
                if (isActivelyUsingGlaive && !glaiveUser.isVaulting() && glaiveUser.getVaultWindupTicks() == 0) {
                    glaiveUser.setVaultWindupTicks(6);
                    glaiveUser.setVaulting(true);
                    ClientAnimationHelper.playCustomAnimation(client.player, "vault");
                }
                else if (glaiveUser.isVaulting() && glaiveUser.canAirDodge()) {
                    glaiveUser.setCanAirDodge(false);
                    glaiveUser.setGlaiveInvulnTicks(8);
                    ClientAnimationHelper.playCustomAnimation(client.player, "air_dodge");

                    Vec3d look = client.player.getRotationVector();
                    Vec3d flatLook = new Vec3d(look.x, 0, look.z).normalize();
                    client.player.setVelocity(flatLook.x * 1.0, 0.6, flatLook.z * 1.0);
                    ClientPlayNetworking.send(vitosos.sapphireweapons.network.ServerNetworking.DODGE_PACKET, PacketByteBufs.empty());
                }
            }

            // --- ATTACK LOGIC ---
            int maxAerialChains = client.player.getMainHandStack().getItem() == vitosos.sapphireweapons.registry.ModItems.PHANTOM_GLAIVE ? 6 : 3;

            // --- KINSECT LAUNCH LOGIC ---
            if (ClientKeybinds.fireKinsectKey.wasPressed() && holdingGlaiveInHand) {
                ItemStack offHandStack = client.player.getOffHandStack();
                if (offHandStack.getItem() instanceof KinsectItem && !glaiveUser.isKinsectDeployed()) {
                    ClientPlayNetworking.send(new Identifier("sapphire-star-armaments", "kinsect_launch_sync"), PacketByteBufs.empty());
                }
            }
            else if (attackJustPressed && glaiveUser.isVaulting() && holdingGlaiveInHand && glaiveUser.getAerialChainCount() < maxAerialChains) {
                if (!glaiveUser.hasUsedAerialAttack()) {
                    glaiveUser.setHasUsedAerialAttack(true);
                    glaiveUser.setCanAirDodge(false);
                    glaiveUser.setGlaiveAttackTicks(5);
                    ClientAnimationHelper.playCustomAnimation(client.player, "aerial_attack");

                    Vec3d look = client.player.getRotationVector();
                    Vec3d flatLook = new Vec3d(look.x, 0, look.z).normalize();
                    client.player.setVelocity(flatLook.x * 1.0, 0.6, flatLook.z * 1.0);
                    ClientPlayNetworking.send(new Identifier("sapphire-star-armaments", "glaive_attack_sync"), PacketByteBufs.empty());
                }
            }

            // --- ULTIMATE ---
            if (ClientKeybinds.ultimateKey.wasPressed() && isActivelyUsingGlaive) {
                if (client.player.getOffHandStack().getItem() instanceof KinsectItem && !glaiveUser.isKinsectDeployed()) {
                    if (glaiveUser.getTripleBuffTicks() > 0 && glaiveUser.getNukeWindupTicks() == 0) {
                        glaiveUser.setNukeWindupTicks(20);
                        ClientAnimationHelper.playCustomAnimation(client.player, "kinsect_nuke");
                        ClientPlayNetworking.send(vitosos.sapphireweapons.network.ServerNetworking.NUKE_PACKET, PacketByteBufs.empty());
                    }
                }
            }

            // --- PHYSICS & STATE UPDATES ---
            if (glaiveUser.getVaultWindupTicks() > 0) {
                glaiveUser.setVaultWindupTicks(glaiveUser.getVaultWindupTicks() - 1);
                client.player.setVelocity(0, client.player.getVelocity().y, 0);

                if (glaiveUser.getVaultWindupTicks() == 0) {
                    client.player.setVelocity(client.player.getVelocity().x, 1.2, client.player.getVelocity().z);
                    glaiveUser.setCanAirDodge(true);
                    glaiveUser.setGlaiveInvulnTicks(8);
                    client.player.stopUsingItem();
                    ClientPlayNetworking.send(vitosos.sapphireweapons.network.ServerNetworking.VAULT_PACKET, PacketByteBufs.empty());
                }
            }
            else if (glaiveUser.isVaulting()) {
                if (client.player.isOnGround() && client.player.getVelocity().y <= 0 || client.player.isTouchingWater())  {
                    glaiveUser.setVaulting(false);
                    glaiveUser.setGlaiveAttackTicks(0);
                    glaiveUser.setHasUsedAerialAttack(false);
                    glaiveUser.setAerialChainCount(0);

                    // WIPE THE ANIMATION LAYER
                    if (client.player instanceof ISapphireAnimatedPlayer sapphirePlayer) {
                        ModifierLayer<IAnimation> layer = sapphirePlayer.getSapphireLayer();
                        if (layer != null) {
                            layer.setAnimation(null);
                        }
                    }

                } else {
                    Vec3d vel = client.player.getVelocity();
                    if (vel.y < 0) {
                        client.player.setVelocity(vel.x, vel.y + 0.04, vel.z);
                    }
                }
            }
        });
    }
}