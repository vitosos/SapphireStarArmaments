package vitosos.sapphireweapons.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import vitosos.sapphireweapons.SapphireStarArmaments;
import vitosos.sapphireweapons.entity.KinsectEntity;
import vitosos.sapphireweapons.item.InsectGlaiveItem;
import vitosos.sapphireweapons.item.KinsectItem;
import vitosos.sapphireweapons.util.IInsectGlaiveUser;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class ServerNetworking {

    // --- PACKET IDENTIFIERS ---
    //General
    public static final Identifier ANIMATION_SYNC_PACKET = new Identifier(SapphireStarArmaments.MOD_ID, "play_animation");

    public static final Identifier SELL_ITEMS_PACKET = new Identifier(SapphireStarArmaments.MOD_ID, "sell_items");
    public static final Identifier SYNC_POINTS_PACKET = new Identifier(SapphireStarArmaments.MOD_ID, "sync_points");
    public static final Identifier SYNC_BOX_PACKET = new Identifier(SapphireStarArmaments.MOD_ID, "sync_box");
    public static final Identifier FORGE_WEAPON_PACKET = new Identifier(SapphireStarArmaments.MOD_ID, "forge_weapon");

    //Glaive
    public static final Identifier VAULT_PACKET = new Identifier(SapphireStarArmaments.MOD_ID, "vault_action");
    public static final Identifier DODGE_PACKET = new Identifier(SapphireStarArmaments.MOD_ID, "dodge_action");
    public static final Identifier NUKE_PACKET = new Identifier(SapphireStarArmaments.MOD_ID, "nuke_action");

    public static void registerReceivers() {

        // 1. AERIAL ATTACK WINDUP
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(SapphireStarArmaments.MOD_ID, "glaive_attack_sync"),
                (server, player, handler, buf, responseSender) -> {
                    server.execute(() -> {
                        IInsectGlaiveUser glaiveUser = (IInsectGlaiveUser) player;
                        if (!glaiveUser.hasUsedAerialAttack()) {
                            glaiveUser.setHasUsedAerialAttack(true);
                            glaiveUser.setCanAirDodge(false);
                            glaiveUser.setGlaiveAttackTicks(5);

                            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_IN, SoundCategory.PLAYERS, 1.0f, 1.0f);
                            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);

                            broadcastAnimation(player, "aerial_attack");
                        }
                    });
                });

        // 2. UNIFIED VAULT PACKET
        ServerPlayNetworking.registerGlobalReceiver(VAULT_PACKET, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                IInsectGlaiveUser glaiveUser = (IInsectGlaiveUser) player;
                glaiveUser.setVaulting(true);
                glaiveUser.setGlaiveInvulnTicks(8);

                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_IN, SoundCategory.PLAYERS, 1.0f, 1.0f);
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);

                broadcastAnimation(player, "vault");
            });
        });

        // 3. UNIFIED DODGE PACKET
        ServerPlayNetworking.registerGlobalReceiver(DODGE_PACKET, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                IInsectGlaiveUser glaiveUser = (IInsectGlaiveUser) player;
                glaiveUser.setCanAirDodge(false);
                glaiveUser.setGlaiveInvulnTicks(8);

                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.2f);
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);

                broadcastAnimation(player, "air_dodge");
            });
        });

        // 4. KINSECT LAUNCH PACKET
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(SapphireStarArmaments.MOD_ID, "kinsect_launch_sync"),
                (server, player, handler, buf, responseSender) -> {
                    server.execute(() -> {
                        IInsectGlaiveUser glaiveUser = (IInsectGlaiveUser) player;
                        ItemStack offHandStack = player.getOffHandStack();

                        if (player.isUsingItem() && player.getActiveItem().getItem() instanceof InsectGlaiveItem
                                && offHandStack.getItem() instanceof KinsectItem kinsectItem
                                && !glaiveUser.isKinsectDeployed()) {

                            glaiveUser.setKinsectDeployed(true);
                            KinsectEntity kinsect = new KinsectEntity(player.getWorld(), player, kinsectItem.getKinsectDamage());
                            kinsect.setItem(offHandStack);
                            kinsect.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, 0.6f, 0.0f);
                            player.getWorld().spawnEntity(kinsect);

                            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.2f);
                            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEEHIVE_WORK, SoundCategory.PLAYERS, 1.2f, 1.0f);
                        }
                    });
                });

        // 5. ULTIMATE NUKE PACKET
        ServerPlayNetworking.registerGlobalReceiver(NUKE_PACKET, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                IInsectGlaiveUser glaiveUser = (IInsectGlaiveUser) player;
                if (glaiveUser.getTripleBuffTicks() > 0) {
                    glaiveUser.setNukeWindupTicks(20);
                    broadcastAnimation(player, "kinsect_nuke");
                }
            });
        });

        // 6. ITEM BOX SELL LOGIC
        ServerPlayNetworking.registerGlobalReceiver(SELL_ITEMS_PACKET, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                if (player.currentScreenHandler instanceof vitosos.sapphireweapons.screen.ItemBoxScreenHandler boxHandler) {
                    int totalPointsGained = 0;
                    net.minecraft.inventory.Inventory grid = boxHandler.getSellGrid();

                    // Calculate value and empty the slots
                    for (int i = 0; i < grid.size(); i++) {
                        ItemStack stack = grid.getStack(i);
                        if (stack.getItem() instanceof vitosos.sapphireweapons.item.MonsterMaterialItem material) {
                            totalPointsGained += (material.getSellValue() * stack.getCount());
                            grid.setStack(i, ItemStack.EMPTY);
                        }
                    }

                    if (totalPointsGained > 0) {
                        // Grant points
                        vitosos.sapphireweapons.util.ISapphirePlayerData playerData = (vitosos.sapphireweapons.util.ISapphirePlayerData) player;
                        playerData.addSapphirePoints(totalPointsGained);

                        // Play a satisfying Ka-Ching sound
                        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);

                        // Sync the new point total back to the client
                        net.minecraft.network.PacketByteBuf syncBuf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                        syncBuf.writeInt(playerData.getSapphirePoints());
                        ServerPlayNetworking.send(player, SYNC_POINTS_PACKET, syncBuf);

                        // THE FIX: Sync the box so the client knows the sold items are gone!
                        syncBoxToClient(player);
                    }
                }
            });
        });

        // --- SYNC POINTS & BOX ON LOGIN ---
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            vitosos.sapphireweapons.util.ISapphirePlayerData playerData = (vitosos.sapphireweapons.util.ISapphirePlayerData) player;

            // Grab the points the server just loaded from the save file
            int currentPoints = playerData.getSapphirePoints();

            // Send points to the client!
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(currentPoints);
            ServerPlayNetworking.send(player, SYNC_POINTS_PACKET, buf);

            // THE FIX: Send the Item Box to the client!
            syncBoxToClient(player);
        });

        // --- 7. FORGE WEAPON LOGIC ---
        ServerPlayNetworking.registerGlobalReceiver(FORGE_WEAPON_PACKET, (server, player, handler, buf, responseSender) -> {
            String categoryName = buf.readString();
            int recipeIndex = buf.readInt();

            server.execute(() -> {
                vitosos.sapphireweapons.recipe.WeaponCategory category = vitosos.sapphireweapons.recipe.WeaponCategory.valueOf(categoryName);
                java.util.List<vitosos.sapphireweapons.recipe.ForgeRecipe> currentList = vitosos.sapphireweapons.recipe.ForgeRecipeRegistry.RECIPES.get(category);

                if (currentList == null || recipeIndex < 0 || recipeIndex >= currentList.size()) return;

                vitosos.sapphireweapons.recipe.ForgeRecipe recipe = currentList.get(recipeIndex);
                vitosos.sapphireweapons.util.ISapphirePlayerData playerData = (vitosos.sapphireweapons.util.ISapphirePlayerData) player;
                net.minecraft.inventory.Inventory boxInv = playerData.getBoxInventory();
                net.minecraft.entity.player.PlayerInventory pockets = player.getInventory();

                boolean isCreative = player.isCreative();

                // --- ONLY CHECK & CONSUME IF NOT IN CREATIVE MODE ---
                if (!isCreative) {
                    // 1. CHECK POINTS
                    if (playerData.getSapphirePoints() < recipe.getPointCost()) return;

                    // --- 2. CHECK MATERIALS ACROSS BOTH INVENTORIES ---
                    for (java.util.Map.Entry<net.minecraft.item.Item, Integer> entry : recipe.getRequiredMaterials().entrySet()) {
                        net.minecraft.item.Item requiredItem = entry.getKey();
                        int amountNeeded = entry.getValue();
                        int amountFound = 0;

                        // Scan pockets
                        for (int i = 0; i < pockets.size(); i++) {
                            ItemStack stack = pockets.getStack(i);
                            if (!stack.isEmpty() && stack.getItem() == requiredItem && !stack.hasEnchantments()) {
                                amountFound += stack.getCount();
                            }
                        }
                        // Scan Box
                        for (int i = 0; i < boxInv.size(); i++) {
                            ItemStack stack = boxInv.getStack(i);
                            if (!stack.isEmpty() && stack.getItem() == requiredItem && !stack.hasEnchantments()) {
                                amountFound += stack.getCount();
                            }
                        }

                        if (amountFound < amountNeeded) return; // Transaction cancelled
                    }

                    // --- 3. CONSUME POINTS & MATERIALS ---
                    playerData.setSapphirePoints(playerData.getSapphirePoints() - recipe.getPointCost());

                    for (java.util.Map.Entry<net.minecraft.item.Item, Integer> entry : recipe.getRequiredMaterials().entrySet()) {
                        net.minecraft.item.Item itemToConsume = entry.getKey();
                        int amountLeftToTake = entry.getValue();

                        // Take from pockets first
                        for (int i = 0; i < pockets.size() && amountLeftToTake > 0; i++) {
                            ItemStack stack = pockets.getStack(i);
                            if (!stack.isEmpty() && stack.getItem() == itemToConsume && !stack.hasEnchantments()) {
                                int take = Math.min(stack.getCount(), amountLeftToTake);
                                stack.decrement(take);
                                amountLeftToTake -= take;
                            }
                        }
                        // Take remainder from Box
                        for (int i = 0; i < boxInv.size() && amountLeftToTake > 0; i++) {
                            ItemStack stack = boxInv.getStack(i);
                            if (!stack.isEmpty() && stack.getItem() == itemToConsume && !stack.hasEnchantments()) {
                                int take = Math.min(stack.getCount(), amountLeftToTake);
                                stack.decrement(take);
                                amountLeftToTake -= take;
                            }
                        }
                    }
                } // --- END SURVIVAL CHECKS ---

                // 4. GIVE THE FORGED WEAPON (Place it on the shelf!)
                if (player.currentScreenHandler instanceof vitosos.sapphireweapons.screen.ForgeScreenHandler forgeHandler) {
                    forgeHandler.output.setStack(0, new ItemStack(recipe.getResultWeapon()));
                }

                // 5. PLAY SOUND & SYNC POINTS
                player.getWorld().playSound(null, player.getBlockPos(), net.minecraft.sound.SoundEvents.BLOCK_ANVIL_USE, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);

                // Only sync points and box if they were actually consumed
                if (!isCreative) {
                    net.minecraft.network.PacketByteBuf syncBuf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                    syncBuf.writeInt(playerData.getSapphirePoints());
                    ServerPlayNetworking.send(player, SYNC_POINTS_PACKET, syncBuf);

                    // THE FIX: Sync the newly updated Box to the client!
                    syncBoxToClient(player);
                }
            });
        });
    }

    // A helper method to keep your receivers clean!
    public static void broadcastAnimation(ServerPlayerEntity player, String animationName) {
        PacketByteBuf animBuf = PacketByteBufs.create();
        animBuf.writeInt(player.getId());
        animBuf.writeString(animationName);

        for (ServerPlayerEntity tracker : PlayerLookup.tracking(player)) {
            if (tracker != player) {
                ServerPlayNetworking.send(tracker, ANIMATION_SYNC_PACKET, animBuf);
            }
        }
    }

    public static void syncBoxToClient(ServerPlayerEntity player) {
        vitosos.sapphireweapons.util.ISapphirePlayerData playerData = (vitosos.sapphireweapons.util.ISapphirePlayerData) player;
        net.minecraft.inventory.Inventory box = playerData.getBoxInventory();

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(box.size());

        for (int i = 0; i < box.size(); i++) {
            buf.writeItemStack(box.getStack(i));
        }

        ServerPlayNetworking.send(player, SYNC_BOX_PACKET, buf);
    }
}