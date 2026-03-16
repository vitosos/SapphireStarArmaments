package vitosos.sapphireweapons.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import vitosos.sapphireweapons.network.ServerNetworking;
import vitosos.sapphireweapons.util.ISapphirePlayerData;

public class ModCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerSSACommands(dispatcher);
        });
    }

    private static void registerSSACommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("ssa")
                // Removed the global .requires() here so normal players can use /ssa points check!

                // --- 1. /ssa clear-box [player] ---
                .then(CommandManager.literal("clear-box")
                        .requires(source -> source.hasPermissionLevel(2)) // Cheat Required
                        .executes(context -> executeClearBox(context.getSource(), context.getSource().getPlayerOrThrow()))
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(context -> executeClearBox(context.getSource(), EntityArgumentType.getPlayer(context, "target")))
                        )
                )

                // --- 2. /ssa cantine ... ---
                .then(CommandManager.literal("cantine")
                        .requires(source -> source.hasPermissionLevel(2)) // Cheat Required
                        .then(CommandManager.literal("refresh")
                                .then(CommandManager.argument("target", EntityArgumentType.player())
                                        .executes(context -> executeCantineRefresh(context.getSource(), EntityArgumentType.getPlayer(context, "target")))
                                )
                        )
                        .then(CommandManager.literal("time")
                                .then(CommandManager.argument("target", EntityArgumentType.player())
                                        .executes(context -> executeCantineTime(context.getSource(), EntityArgumentType.getPlayer(context, "target")))
                                )
                        )
                )

                // --- 3. /ssa points ... ---
                .then(CommandManager.literal("points")

                        // --- /ssa points check [player] ---
                        .then(CommandManager.literal("check")
                                // Default: Check Self (No Cheats Required!)
                                .executes(context -> executePointsCheck(context.getSource(), context.getSource().getPlayerOrThrow()))

                                // With Target: Check Others (Cheat Required!)
                                .then(CommandManager.argument("target", EntityArgumentType.player())
                                        .requires(source -> source.hasPermissionLevel(2))
                                        .executes(context -> executePointsCheck(context.getSource(), EntityArgumentType.getPlayer(context, "target")))
                                )
                        )

                        // --- /ssa points set <player> <amount> ---
                        .then(CommandManager.literal("set")
                                .requires(source -> source.hasPermissionLevel(2)) // Cheat Required!
                                .then(CommandManager.argument("target", EntityArgumentType.player())
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(context -> executePointsSet(context.getSource(), EntityArgumentType.getPlayer(context, "target"), IntegerArgumentType.getInteger(context, "amount")))
                                        )
                                )
                        )
                )
        );
    }

    // --- EXECUTION LOGIC ---

    private static int executeClearBox(ServerCommandSource source, ServerPlayerEntity targetPlayer) {
        ISapphirePlayerData playerData = (ISapphirePlayerData) targetPlayer;
        playerData.getBoxInventory().clear();
        source.sendFeedback(() -> Text.literal("Cleared the Item Box for " + targetPlayer.getName().getString()), true);
        return 1;
    }

    private static int executePointsCheck(ServerCommandSource source, ServerPlayerEntity targetPlayer) {
        ISapphirePlayerData playerData = (ISapphirePlayerData) targetPlayer;
        int points = playerData.getSapphirePoints();

        // Send a different message depending on if they are checking themselves or someone else
        if (source.getEntity() == targetPlayer) {
            source.sendFeedback(() -> Text.literal("§bYou have " + points + " Sapphire Points."), false);
        } else {
            source.sendFeedback(() -> Text.literal("§b" + targetPlayer.getName().getString() + " has " + points + " Sapphire Points."), false);
        }
        return 1;
    }

    private static int executePointsSet(ServerCommandSource source, ServerPlayerEntity targetPlayer, int amount) {
        ISapphirePlayerData playerData = (ISapphirePlayerData) targetPlayer;
        playerData.setSapphirePoints(amount);

        // SYNC TO CLIENT!
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(amount);
        ServerPlayNetworking.send(targetPlayer, ServerNetworking.SYNC_POINTS_PACKET, buf);

        source.sendFeedback(() -> Text.literal("Set Sapphire Points to " + amount + " for " + targetPlayer.getName().getString()), true);
        return 1;
    }

    private static int executeCantineRefresh(ServerCommandSource source, ServerPlayerEntity targetPlayer) {
        ISapphirePlayerData playerData = (ISapphirePlayerData) targetPlayer;
        playerData.setLastCantineTime(0L);

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeLong(0L);
        ServerPlayNetworking.send(targetPlayer, ServerNetworking.SYNC_CANTINE_PACKET, buf);

        source.sendFeedback(() -> Text.literal("§aSuccessfully reset Cantine timer for " + targetPlayer.getName().getString() + "."), false);
        return 1;
    }

    private static int executeCantineTime(ServerCommandSource source, ServerPlayerEntity targetPlayer) {
        ISapphirePlayerData playerData = (ISapphirePlayerData) targetPlayer;
        long lastTime = playerData.getLastCantineTime();
        long currentTime = targetPlayer.getWorld().getTime();
        long maxCooldown = vitosos.sapphireweapons.config.SapphireConfigManager.CONFIG.cantineCooldownTicks;
        long ticksLeft = maxCooldown - (currentTime - lastTime);

        if (ticksLeft <= 0) {
            source.sendFeedback(() -> Text.literal("§e" + targetPlayer.getName().getString() + " can eat right now!"), false);
        } else {
            long secondsLeft = ticksLeft / 20;
            String timeText = String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60);
            source.sendFeedback(() -> Text.literal("§c" + targetPlayer.getName().getString() + " must wait " + timeText + " before eating again."), false);
        }
        return 1;
    }
}