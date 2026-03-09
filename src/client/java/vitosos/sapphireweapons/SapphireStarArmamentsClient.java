package vitosos.sapphireweapons;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import software.bernie.geckolib.animatable.client.RenderProvider;
import vitosos.sapphireweapons.client.ClientKeybinds;
import vitosos.sapphireweapons.client.animation.ISapphireAnimatedPlayer;
import vitosos.sapphireweapons.client.event.ClientTickHandler;
import vitosos.sapphireweapons.client.hud.InsectGlaiveHudOverlay;
import vitosos.sapphireweapons.client.network.ClientNetworking;
import vitosos.sapphireweapons.entity.KinsectRenderer;
import vitosos.sapphireweapons.item.KinsectItem;
import vitosos.sapphireweapons.item.KinsectItemRenderer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import vitosos.sapphireweapons.registry.ModScreenHandlers;
import vitosos.sapphireweapons.screen.ForgeScreen;
import vitosos.sapphireweapons.screen.ItemBoxScreen;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.util.Identifier;

public class SapphireStarArmamentsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {

		// --- 1. ANIMATION REGISTRATION ---
		PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, animationStack) -> {
			if (player instanceof ISapphireAnimatedPlayer sapphirePlayer) {
				ModifierLayer<IAnimation> layer = new ModifierLayer<>();
				animationStack.addAnimLayer(1000, layer);
				sapphirePlayer.setSapphireLayer(layer);
			}
		});

		// --- 2. RENDERERS ---
		EntityRendererRegistry.register(vitosos.sapphireweapons.registry.ModEntities.KINSECT_ENTITY_TYPE, KinsectRenderer::new);

		KinsectItem.renderProviderInjector = consumer -> {
			consumer.accept(new RenderProvider() {
				private KinsectItemRenderer renderer;
				@Override
				public net.minecraft.client.render.item.BuiltinModelItemRenderer getCustomRenderer() {
					if (this.renderer == null) this.renderer = new KinsectItemRenderer();
					return this.renderer;
				}
			});
		};


		ModelLoadingPlugin.register(pluginContext -> {
			// Forces the game to load our hidden 2D sprite model!
			pluginContext.addModels(new Identifier(SapphireStarArmaments.MOD_ID, "item/kinsect_item_2d"));
		});

		HudRenderCallback.EVENT.register(new InsectGlaiveHudOverlay());

		// --- 3. INITIALIZE HANDLERS ---
		ClientKeybinds.register();
		ClientNetworking.registerReceivers();
		ClientTickHandler.register();
		HandledScreens.register(ModScreenHandlers.ITEM_BOX_SCREEN_HANDLER, ItemBoxScreen::new);
		HandledScreens.register(ModScreenHandlers.FORGE_SCREEN_HANDLER, ForgeScreen::new);
	}
}