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
import vitosos.sapphireweapons.config.SapphireConfigManager;
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
    public static final Identifier BUY_SHOP_ITEM_PACKET = new Identifier(SapphireStarArmaments.MOD_ID, "buy_shop_item");
    public static final Identifier REPAIR_WEAPON_PACKET = new Identifier(SapphireStarArmaments.MOD_ID, "repair_weapon");
    public static final Identifier BUY_EXPERIENCE_PACKET = new Identifier(SapphireStarArmaments.MOD_ID, "buy_experience");
    public static final Identifier BUY_MEAL_PACKET = new Identifier(SapphireStarArmaments.MOD_ID, "buy_meal");
    public static final Identifier SYNC_CANTINE_PACKET = new Identifier(SapphireStarArmaments.MOD_ID, "sync_cantine");

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
                        ItemStack mainHandStack = player.getMainHandStack();

                        // Check mainHandStack instead of player.isUsingItem()
                        if (mainHandStack.getItem() instanceof InsectGlaiveItem
                                && offHandStack.getItem() instanceof KinsectItem kinsectItem
                                && !glaiveUser.isKinsectDeployed()) {

                            glaiveUser.setKinsectDeployed(true);
                            KinsectEntity kinsect = new KinsectEntity(player.getWorld(), player, kinsectItem.getKinsectDamage());
                            kinsect.setItem(offHandStack);

                            // Absolute velocity to ignore inertia
                            net.minecraft.util.math.Vec3d look = player.getRotationVector();
                            kinsect.setVelocity(look.x, look.y, look.z, 0.6f, 0.0f);

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

                        // Sync the box so the client knows the sold items are gone!
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

            //Send Item Box to Client
            syncBoxToClient(player);

            // Sync Cantine Timer on Login
            PacketByteBuf cantineBuf = PacketByteBufs.create();
            cantineBuf.writeLong(playerData.getLastCantineTime());
            ServerPlayNetworking.send(player, ServerNetworking.SYNC_CANTINE_PACKET, cantineBuf);
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

                    // Sync the newly updated Box to the client!
                    syncBoxToClient(player);
                }
            });
        });

        // --- 8. SHOP PURCHASE LOGIC ---
        ServerPlayNetworking.registerGlobalReceiver(BUY_SHOP_ITEM_PACKET, (server, player, handler, buf, responseSender) -> {
            // 1. Read the data sent from the client's UI
            String categoryName = buf.readString();
            String shopId = buf.readString();

            server.execute(() -> {
                // 2. Safely parse the Enum category
                vitosos.sapphireweapons.util.ShopCategory category;
                try {
                    category = vitosos.sapphireweapons.util.ShopCategory.valueOf(categoryName);
                } catch (IllegalArgumentException e) {
                    return; // Invalid category string, abort!
                }

                // 3. Find the exact ShopEntry the player is trying to buy
                java.util.List<vitosos.sapphireweapons.util.ShopEntry> entries = vitosos.sapphireweapons.util.ShopRegistry.getEntriesForCategory(category);
                vitosos.sapphireweapons.util.ShopEntry targetEntry = null;
                for (vitosos.sapphireweapons.util.ShopEntry entry : entries) {
                    if (entry.getId().equals(shopId)) {
                        targetEntry = entry;
                        break;
                    }
                }

                if (targetEntry == null) return; // Item not found in registry

                vitosos.sapphireweapons.util.ISapphirePlayerData playerData = (vitosos.sapphireweapons.util.ISapphirePlayerData) player;
                int currentPoints = playerData.getSapphirePoints();
                int cost = targetEntry.getPointCost();

                // 4. Verify funds (unless they are in Creative mode)
                boolean isCreative = player.isCreative();
                if (!isCreative && currentPoints < cost) {
                    // Play a failure sound
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sound.SoundEvents.ENTITY_VILLAGER_NO, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
                    return;
                }

                boolean transactionSuccessful = false;

                // PHYSICAL ITEM PURCHASES (Potions, Powders, etc.)
                if (shopId.startsWith("buy_")) {
                    // Give them a fresh copy of whatever icon is displayed in the shop
                    ItemStack stackToGive = targetEntry.getDisplayIcon().copy();
                    player.getInventory().offerOrDrop(stackToGive);
                    transactionSuccessful = true;
                }

                // --- 6. FINALIZE & SYNC ---
                if (transactionSuccessful) {
                    if (!isCreative) {
                        playerData.setSapphirePoints(currentPoints - cost);

                        // Sync the new point balance back to the client immediately!
                        net.minecraft.network.PacketByteBuf syncBuf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                        syncBuf.writeInt(playerData.getSapphirePoints());
                        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, SYNC_POINTS_PACKET, syncBuf);
                    }

                    // Play a satisfying Ka-Ching sound!
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.2f);
                } else {
                    // Transaction failed (e.g., tried to repair a block of dirt)
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sound.SoundEvents.ENTITY_VILLAGER_NO, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            });
        });

        // --- 9. WEAPON REPAIR LOGIC ---
        ServerPlayNetworking.registerGlobalReceiver(REPAIR_WEAPON_PACKET, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                if (!(player.currentScreenHandler instanceof vitosos.sapphireweapons.screen.GuildStockBoxScreenHandler boxHandler)) return;

                ItemStack repairStack = boxHandler.repairSlot.getStack(0);
                if (repairStack.isEmpty() || !repairStack.isDamaged() || !(repairStack.getItem() instanceof vitosos.sapphireweapons.item.InsectGlaiveItem)) return;

                // 1. Find the Recipe
                vitosos.sapphireweapons.recipe.ForgeRecipe targetRecipe = null;
                for (java.util.List<vitosos.sapphireweapons.recipe.ForgeRecipe> catList : vitosos.sapphireweapons.recipe.ForgeRecipeRegistry.RECIPES.values()) {
                    for (vitosos.sapphireweapons.recipe.ForgeRecipe recipe : catList) {
                        if (recipe.getResultWeapon() == repairStack.getItem()) targetRecipe = recipe;
                    }
                }
                if (targetRecipe == null) return;

                vitosos.sapphireweapons.util.ISapphirePlayerData playerData = (vitosos.sapphireweapons.util.ISapphirePlayerData) player;
                net.minecraft.entity.player.PlayerInventory pockets = player.getInventory();
                net.minecraft.inventory.Inventory boxInv = playerData.getBoxInventory();
                boolean isCreative = player.isCreative();

                // 2. Setup the Math
                int baseCost = targetRecipe.getPointCost() * 30;
                double totalWeight = 0;
                java.util.Map<net.minecraft.item.Item, Integer> validMats = new java.util.HashMap<>();

                for (java.util.Map.Entry<net.minecraft.item.Item, Integer> entry : targetRecipe.getRequiredMaterials().entrySet()) {
                    net.minecraft.item.Item mat = entry.getKey();
                    if (mat instanceof net.minecraft.item.SwordItem) continue; // Skip weapons!

                    validMats.put(mat, entry.getValue());
                    net.minecraft.util.Rarity r = mat.getDefaultStack().getRarity();
                    totalWeight += (r == net.minecraft.util.Rarity.RARE || r == net.minecraft.util.Rarity.EPIC) ? 50 : (r == net.minecraft.util.Rarity.UNCOMMON ? 35 : 15);
                }

                double totalDiscount = 0;
                java.util.Map<net.minecraft.item.Item, Integer> itemsToConsumeMap = new java.util.HashMap<>();

                for (java.util.Map.Entry<net.minecraft.item.Item, Integer> entry : validMats.entrySet()) {
                    net.minecraft.item.Item mat = entry.getKey();
                    int needed = entry.getValue();

                    // Count items
                    int playerHas = 0;
                    for (int i = 0; i < pockets.size(); i++) if (pockets.getStack(i).getItem() == mat) playerHas += pockets.getStack(i).getCount();
                    for (int i = 0; i < boxInv.size(); i++) if (boxInv.getStack(i).getItem() == mat) playerHas += boxInv.getStack(i).getCount();

                    int consumed = Math.min(needed, playerHas);
                    itemsToConsumeMap.put(mat, consumed);

                    net.minecraft.util.Rarity r = mat.getDefaultStack().getRarity();
                    double weight = (r == net.minecraft.util.Rarity.RARE || r == net.minecraft.util.Rarity.EPIC) ? 50 : (r == net.minecraft.util.Rarity.UNCOMMON ? 35 : 15);
                    double weightFraction = weight / totalWeight;
                    totalDiscount += weightFraction * ((double) consumed / needed);
                }

                int finalCost = (int) Math.max(0, baseCost * (1.0 - totalDiscount));

                // 3. Verify Funds
                if (!isCreative && playerData.getSapphirePoints() < finalCost) return;

                // 4. Execute Transaction
                if (!isCreative) {
                    playerData.setSapphirePoints(playerData.getSapphirePoints() - finalCost);

                    for (java.util.Map.Entry<net.minecraft.item.Item, Integer> entry : itemsToConsumeMap.entrySet()) {
                        net.minecraft.item.Item itemToTake = entry.getKey();
                        int leftToTake = entry.getValue();

                        for (int i = 0; i < pockets.size() && leftToTake > 0; i++) {
                            ItemStack stack = pockets.getStack(i);
                            if (stack.getItem() == itemToTake) { int take = Math.min(stack.getCount(), leftToTake); stack.decrement(take); leftToTake -= take; }
                        }
                        for (int i = 0; i < boxInv.size() && leftToTake > 0; i++) {
                            ItemStack stack = boxInv.getStack(i);
                            if (stack.getItem() == itemToTake) { int take = Math.min(stack.getCount(), leftToTake); stack.decrement(take); leftToTake -= take; }
                        }
                    }
                }

                // 5. Repair the item and Sync!
                repairStack.setDamage(0);
                player.getWorld().playSound(null, player.getBlockPos(), net.minecraft.sound.SoundEvents.BLOCK_ANVIL_USE, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);

                if (!isCreative) {
                    net.minecraft.network.PacketByteBuf syncBuf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                    syncBuf.writeInt(playerData.getSapphirePoints());
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, SYNC_POINTS_PACKET, syncBuf);
                    syncBoxToClient(player);
                }
            });
        });

        // --- 10. EXPERIENCE PURCHASE LOGIC ---
        ServerPlayNetworking.registerGlobalReceiver(BUY_EXPERIENCE_PACKET, (server, player, handler, buf, responseSender) -> {

            // 1. Read the data sent from the client
            String categoryName = buf.readString(); // We read this just to clear the buffer
            int levelsToBuy = buf.readInt();

            server.execute(() -> {
                vitosos.sapphireweapons.util.ISapphirePlayerData playerData = (vitosos.sapphireweapons.util.ISapphirePlayerData) player;

                // 2. Server calculates the true cost based on the player's actual level
                int currentLevel = player.experienceLevel;
                int cost = calculateXpCost(currentLevel, levelsToBuy);
                boolean isCreative = player.isCreative();

                // 3. Final verification to prevent cheating
                if (!isCreative && playerData.getSapphirePoints() < cost) return;

                // 4. Deduct the points and sync back to the client!
                if (!isCreative) {
                    playerData.setSapphirePoints(playerData.getSapphirePoints() - cost);

                    net.minecraft.network.PacketByteBuf syncBuf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                    syncBuf.writeInt(playerData.getSapphirePoints());
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, SYNC_POINTS_PACKET, syncBuf);
                }

                // 5. Grant the actual levels!
                player.addExperienceLevels(levelsToBuy);
            });
        });

        // --- 11. CANTINE MEAL LOGIC ---
        ServerPlayNetworking.registerGlobalReceiver(BUY_MEAL_PACKET, (server, player, handler, buf, responseSender) -> {
            String dishName = buf.readString();

            server.execute(() -> {
                vitosos.sapphireweapons.util.CantineDish dish = vitosos.sapphireweapons.util.CantineDish.valueOf(dishName);
                vitosos.sapphireweapons.util.ISapphirePlayerData playerData = (vitosos.sapphireweapons.util.ISapphirePlayerData) player;

                boolean isCreative = player.isCreative();
                long currentTime = player.getWorld().getTime();
                long lastTime = playerData.getLastCantineTime();
                long maxCooldown = vitosos.sapphireweapons.config.SapphireConfigManager.CONFIG.cantineCooldownTicks;
                long ticksLeft = maxCooldown - (currentTime - lastTime);
                int cantineBuffTimer = SapphireConfigManager.CONFIG.cantineBuffTicks;

                if (!isCreative && ticksLeft > 0) return; // Cooldown is still active!
                if (!isCreative && playerData.getSapphirePoints() < dish.getCost()) return; // Too poor!

                if (!isCreative) {
                    playerData.setSapphirePoints(playerData.getSapphirePoints() - dish.getCost());
                    playerData.setLastCantineTime(currentTime);

                    // Sync Points
                    net.minecraft.network.PacketByteBuf syncBuf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                    syncBuf.writeInt(playerData.getSapphirePoints());
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, SYNC_POINTS_PACKET, syncBuf);

                    // Sync Cantine Timer
                    net.minecraft.network.PacketByteBuf cantineBuf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                    cantineBuf.writeLong(currentTime);
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, SYNC_CANTINE_PACKET, cantineBuf);
                }

                // --- APPLY BASE EFFECTS ---
                player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.ABSORPTION, cantineBuffTimer, 1));
                player.getHungerManager().setFoodLevel(20);
                player.getHungerManager().setSaturationLevel(20.0f);
                player.setHealth(player.getMaxHealth());

                // --- APPLY SPECIFIC EFFECTS ---
                switch (dish) {
                    case FIERY_LAVA_CHICKEN:
                        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.FIRE_RESISTANCE, cantineBuffTimer, 0));
                        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.HEALTH_BOOST, cantineBuffTimer, 2));
                        player.setHealth(player.getMaxHealth()); // Heal again to fill the newly granted max health!
                        break;
                    case SUGARY_BEE_APPLES:
                        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.HASTE, cantineBuffTimer, 0));
                        break;
                    case HEARTY_BEEF:
                        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.REGENERATION, cantineBuffTimer, 0));
                        break;
                    case LUCKY_FISH_SALAD:
                        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.LUCK, cantineBuffTimer, 2));
                        break;
                    case SPECIAL_RATION:
                        break; // No extra effects!
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

    // Replicates Minecraft's vanilla XP curve securely on the server
    private static int calculateTotalXpForLevel(int level) {
        if (level <= 16) return level * level + 6 * level;
        if (level <= 31) return (int) (2.5 * level * level - 40.5 * level + 360);
        return (int) (4.5 * level * level - 162.5 * level + 2220);
    }

    private static int calculateXpCost(int currentLevel, int levelsToAdd) {
        int currentXp = calculateTotalXpForLevel(currentLevel);
        int targetXp = calculateTotalXpForLevel(currentLevel + levelsToAdd);
        int ptsPerXp = SapphireConfigManager.CONFIG.xpToPointsConversionRate;
        return (targetXp - currentXp) * ptsPerXp;
    }
}