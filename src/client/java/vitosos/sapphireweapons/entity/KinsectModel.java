package vitosos.sapphireweapons.entity;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import vitosos.sapphireweapons.SapphireStarArmaments;

public class KinsectModel extends GeoModel<KinsectEntity> {

    @Override
    public Identifier getModelResource(KinsectEntity animatable) {
        return new Identifier(SapphireStarArmaments.MOD_ID, "geo/kinsect.geo.json");
    }

    @Override
    public Identifier getTextureResource(KinsectEntity animatable) {
        return new Identifier(SapphireStarArmaments.MOD_ID, "textures/entity/kinsect.png");
    }

    @Override
    public Identifier getAnimationResource(KinsectEntity animatable) {
        return new Identifier(SapphireStarArmaments.MOD_ID, "animations/kinsect.animation.json");
    }
}