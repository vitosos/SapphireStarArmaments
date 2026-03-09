package vitosos.sapphireweapons.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import vitosos.sapphireweapons.SapphireStarArmaments;
import vitosos.sapphireweapons.screen.ForgeScreenHandler;
import vitosos.sapphireweapons.screen.ItemBoxScreenHandler;

public class ModScreenHandlers {

    public static final ScreenHandlerType<ItemBoxScreenHandler> ITEM_BOX_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SapphireStarArmaments.MOD_ID, "item_box"),
                    new ScreenHandlerType<>(ItemBoxScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES));

    public static final ScreenHandlerType<ForgeScreenHandler> FORGE_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SapphireStarArmaments.MOD_ID, "forge"),
                    new ScreenHandlerType<>(ForgeScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES));

    public static void register() {
    }
}