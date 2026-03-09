package vitosos.sapphireweapons.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import vitosos.sapphireweapons.SapphireStarArmaments;

public class ModSounds {

    // Create the identifier and sound event
    public static final Identifier ITEM_BOX_OPEN_ID = new Identifier(SapphireStarArmaments.MOD_ID, "item_box_open");
    public static SoundEvent ITEM_BOX_OPEN = SoundEvent.of(ITEM_BOX_OPEN_ID);

    public static void register() {
        Registry.register(Registries.SOUND_EVENT, ITEM_BOX_OPEN_ID, ITEM_BOX_OPEN);
    }
}