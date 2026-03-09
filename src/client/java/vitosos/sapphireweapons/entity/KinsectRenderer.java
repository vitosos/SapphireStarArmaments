package vitosos.sapphireweapons.entity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KinsectRenderer extends GeoEntityRenderer<KinsectEntity> {

    public KinsectRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new KinsectModel());
        this.shadowRadius = 0.3f; // Gives the bug a nice little shadow on the ground!
    }

    @Override
    public void render(KinsectEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        poseStack.push();

        //Quick resize (50% bigger)
        poseStack.scale(1.5f, 1.5f, 1.5f);

        // 1. Interpolate the yaw and pitch so it rotates smoothly between frames
        float yaw = MathHelper.lerp(partialTick, entity.prevYaw, entity.getYaw());
        float pitch = MathHelper.lerp(partialTick, entity.prevPitch, entity.getPitch());

        // 2. Rotate the entire 3D canvas!
        // Note: Projectiles in Minecraft require a -90 degree offset on the Y-axis to face forward correctly.
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw - 0F));
        poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(pitch));

        // 3. Tell GeckoLib to draw the model on our newly rotated canvas
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        poseStack.pop();
    }
}