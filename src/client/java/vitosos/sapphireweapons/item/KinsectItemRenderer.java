package vitosos.sapphireweapons.item;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import net.minecraft.util.math.RotationAxis;

public class KinsectItemRenderer extends GeoItemRenderer<KinsectItem> {

    public KinsectItemRenderer() {
        super(new KinsectItemModel());
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode transformType, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        poseStack.push();

        // 1. Scale up
        poseStack.scale(2.0f, 2.0f, 2.0f);

        // 2. Adjust position if it's held in the off-hand
        if (transformType == ModelTransformationMode.THIRD_PERSON_LEFT_HAND) {
            poseStack.translate(0.72, -0.38, -0.2);
            poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90f));  // Rolls it like a barrel
        }
        else if (transformType == ModelTransformationMode.FIRST_PERSON_LEFT_HAND) {
            poseStack.translate(0.0, -0.2, 0.0);
        }

        super.render(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.pop();
    }
}