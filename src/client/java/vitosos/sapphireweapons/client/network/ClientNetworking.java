package vitosos.sapphireweapons.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import vitosos.sapphireweapons.client.animation.ClientAnimationHelper;
import vitosos.sapphireweapons.network.ServerNetworking;
import vitosos.sapphireweapons.util.IInsectGlaiveUser;

public class ClientNetworking {

    public static void registerReceivers() {

        // 1. Glaive Hit Sync
        ClientPlayNetworking.registerGlobalReceiver(new Identifier("sapphire-star-armaments", "glaive_hit_sync"),
                (client, handler, buf, responseSender) -> {
                    client.execute(() -> {
                        if (MinecraftClient.getInstance().player != null) {
                            IInsectGlaiveUser glaiveUser = (IInsectGlaiveUser) MinecraftClient.getInstance().player;
                            glaiveUser.setHasUsedAerialAttack(false);
                            glaiveUser.setCanAirDodge(true);
                            glaiveUser.setAerialChainCount(glaiveUser.getAerialChainCount() + 1);

                            // NEW: Play the vault animation locally!
                            vitosos.sapphireweapons.client.animation.ClientAnimationHelper.playCustomAnimation(MinecraftClient.getInstance().player, "vault");
                        }
                    });
                });

        // 2. Essence Sync
        ClientPlayNetworking.registerGlobalReceiver(new Identifier("sapphire-star-armaments", "essence_sync"),
                (client, handler, buf, responseSender) -> {
                    int red = buf.readInt();
                    int white = buf.readInt();
                    int orange = buf.readInt();
                    int triple = buf.readInt();

                    client.execute(() -> {
                        if (MinecraftClient.getInstance().player != null) {
                            IInsectGlaiveUser glaiveUser = (IInsectGlaiveUser) MinecraftClient.getInstance().player;
                            glaiveUser.setRedEssenceTicks(red);
                            glaiveUser.setWhiteEssenceTicks(white);
                            glaiveUser.setOrangeEssenceTicks(orange);
                            glaiveUser.setTripleBuffTicks(triple);
                        }
                    });
                });

        // 3. Multiplayer Animation Sync
        ClientPlayNetworking.registerGlobalReceiver(new Identifier("sapphire-star-armaments", "play_animation"),
                (client, handler, buf, responseSender) -> {
                    int targetEntityId = buf.readInt();
                    String animationName = buf.readString();

                    client.execute(() -> {
                        if (client.world != null) {
                            net.minecraft.entity.Entity entity = client.world.getEntityById(targetEntityId);
                            if (entity instanceof AbstractClientPlayerEntity targetPlayer) {
                                ClientAnimationHelper.playCustomAnimation(targetPlayer, animationName);
                            }
                        }
                    });
                });

        // --- SYNC PLAYER POINTS ---
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                vitosos.sapphireweapons.network.ServerNetworking.SYNC_POINTS_PACKET,
                (client, handler, buf, responseSender) -> {
                    // 1. Read the new points total sent by the server
                    int newPoints = buf.readInt();

                    // 2. Update the local player's data on the main client thread
                    client.execute(() -> {
                        if (client.player != null) {
                            vitosos.sapphireweapons.util.ISapphirePlayerData playerData =
                                    (vitosos.sapphireweapons.util.ISapphirePlayerData) client.player;
                            playerData.setSapphirePoints(newPoints);
                        }
                    });
                });

        ClientPlayNetworking.registerGlobalReceiver(ServerNetworking.SYNC_BOX_PACKET, (client, handler, buf, responseSender) -> {
            // 1. Read the size of the inventory
            int size = buf.readInt();

            // 2. Read all the item stacks from the buffer
            java.util.List<net.minecraft.item.ItemStack> stacks = new java.util.ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                stacks.add(buf.readItemStack());
            }

            // 3. Unpack them into the Client's Mixin Item Box
            client.execute(() -> {
                if (client.player != null) {
                    vitosos.sapphireweapons.util.ISapphirePlayerData playerData = (vitosos.sapphireweapons.util.ISapphirePlayerData) client.player;
                    net.minecraft.inventory.Inventory box = playerData.getBoxInventory();

                    for (int i = 0; i < size; i++) {
                        box.setStack(i, stacks.get(i));
                    }
                }
            });
        });
    }
}