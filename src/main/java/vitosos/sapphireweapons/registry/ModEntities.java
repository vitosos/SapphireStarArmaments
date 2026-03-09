package vitosos.sapphireweapons.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import vitosos.sapphireweapons.SapphireStarArmaments;
import vitosos.sapphireweapons.entity.KinsectEntity;

public class ModEntities {

    public static final EntityType<KinsectEntity> KINSECT_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(SapphireStarArmaments.MOD_ID, "kinsect"),
            FabricEntityTypeBuilder.<KinsectEntity>create(SpawnGroup.MISC, KinsectEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(10)
                    .build()
    );

    public static void register() {
        // The registry call happens in the variable declaration above,
        // but we keep this method so the Main class can force the class to load!
    }
}