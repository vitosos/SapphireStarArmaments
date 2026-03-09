package vitosos.sapphireweapons.mixin.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import vitosos.sapphireweapons.SapphireStarArmaments;
import vitosos.sapphireweapons.registry.ModItems;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow @Final private ItemModels models;

    @ModifyVariable(
            method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private BakedModel render2DKinsectInGUI(BakedModel defaultModel, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        // 1. Is the item our Kinsect?
        if (stack.isOf(ModItems.KINSECT_ITEM)) {
            // 2. Is the item being rendered ANYWHERE EXCEPT the Left Hand (Off-Hand)?
            if (renderMode != ModelTransformationMode.FIRST_PERSON_LEFT_HAND && renderMode != ModelTransformationMode.THIRD_PERSON_LEFT_HAND) {
                // 3. Swap the 3D GeckoLib model for our 2D sprite!
                return this.models.getModelManager().getModel(new Identifier(SapphireStarArmaments.MOD_ID, "item/kinsect_item_2d"));
            }
        }
        return defaultModel;
    }
}