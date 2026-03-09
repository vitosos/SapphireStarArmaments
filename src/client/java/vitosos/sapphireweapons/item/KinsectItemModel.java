package vitosos.sapphireweapons.item;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import vitosos.sapphireweapons.SapphireStarArmaments;

public class KinsectItemModel extends GeoModel<KinsectItem> {
    @Override
    public Identifier getModelResource(KinsectItem animatable) {
        return new Identifier(SapphireStarArmaments.MOD_ID, "geo/kinsect.geo.json");
    }

    @Override
    public Identifier getTextureResource(KinsectItem animatable) {
        return new Identifier(SapphireStarArmaments.MOD_ID, "textures/entity/kinsect.png");
    }

    @Override
    public Identifier getAnimationResource(KinsectItem animatable) {
        return new Identifier(SapphireStarArmaments.MOD_ID, "animations/kinsect.animation.json");
    }
}