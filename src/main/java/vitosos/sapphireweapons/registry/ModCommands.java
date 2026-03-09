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
                .requires(source -> source.hasPermissionLevel(2)) // Requires OP / Cheats Enabled

                // --- 1. /ssa clear-box [player] ---
                .then(CommandManager.literal("clear-box")
                        // Default: Self
                        .executes(context -> executeClearBox(context.getSource(), context.getSource().getPlayerOrThrow()))
                        // With Player Argument
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(context -> executeClearBox(context.getSource(), EntityArgumentType.getPlayer(context, "target")))
                        )
                )

                // --- 2. /ssa setpoints <value> [player] ---
                .then(CommandManager.literal("setpoints")
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0)) // Prevents negative points
                                // Default: Self
                                .executes(context -> executeSetPoints(context.getSource(), IntegerArgumentType.getInteger(context, "amount"), context.getSource().getPlayerOrThrow()))
                                // With Player Argument
                                .then(CommandManager.argument("target", EntityArgumentType.player())
                                        .executes(context -> executeSetPoints(context.getSource(), IntegerArgumentType.getInteger(context, "amount"), EntityArgumentType.getPlayer(context, "target")))
                                )
                        )
                )
        );
    }

    // --- EXECUTION LOGIC ---

    private static int executeClearBox(ServerCommandSource source, ServerPlayerEntity targetPlayer) {
        ISapphirePlayerData playerData = (ISapphirePlayerData) targetPlayer;

        // Wipe the 162-slot inventory
        playerData.getBoxInventory().clear();

        source.sendFeedback(() -> Text.literal("Cleared the Item Box for " + targetPlayer.getName().getString()), true);
        return 1; // 1 means success in Brigadier!
    }

    private static int executeSetPoints(ServerCommandSource source, int amount, ServerPlayerEntity targetPlayer) {
        ISapphirePlayerData playerData = (ISapphirePlayerData) targetPlayer;

        // Set the points on the server
        playerData.setSapphirePoints(amount);

        // SYNC TO CLIENT! (Crucial so their UI updates immediately)
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(amount);
        ServerPlayNetworking.send(targetPlayer, ServerNetworking.SYNC_POINTS_PACKET, buf);

        source.sendFeedback(() -> Text.literal("Set Sapphire Points to " + amount + " for " + targetPlayer.getName().getString()), true);
        return 1;
    }
}