package vitosos.sapphireweapons.recipe;

import net.minecraft.item.Item;
import vitosos.sapphireweapons.registry.ModItems;

public enum WeaponCategory {
    GLAIVE("Insect Glaive", ModItems.IRON_GLAIVE);
    // You will add more here later, like:
    // SWORD_AND_SHIELD("Sword & Shield", ModItems.IRON_SWORD),
    // GREATSWORD("Greatsword", ModItems.BONE_BLADE);

    private final String displayName;
    private final Item iconItem;

    WeaponCategory(String displayName, Item iconItem) {
        this.displayName = displayName;
        this.iconItem = iconItem;
    }

    public String getDisplayName() { return displayName; }
    public Item getIconItem() { return iconItem; }
}