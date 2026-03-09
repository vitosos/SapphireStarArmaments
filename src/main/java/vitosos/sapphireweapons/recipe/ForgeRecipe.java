package vitosos.sapphireweapons.recipe;

import net.minecraft.item.Item;
import java.util.Map;

public class ForgeRecipe {
    private final Item resultWeapon;
    private final String weaponName; // Custom name for the UI list
    private final Item displayIcon; // The mob/material icon for the list (e.g., Spider Eye)
    private final int pointCost;
    private final Map<Item, Integer> requiredMaterials; // The Items and how many are needed

    public ForgeRecipe(Item resultWeapon, String weaponName, Item displayIcon, int pointCost, Map<Item, Integer> requiredMaterials) {
        this.resultWeapon = resultWeapon;
        this.weaponName = weaponName;
        this.displayIcon = displayIcon;
        this.pointCost = pointCost;
        this.requiredMaterials = requiredMaterials;
    }

    public Item getResultWeapon() { return resultWeapon; }
    public String getWeaponName() { return weaponName; }
    public Item getDisplayIcon() { return displayIcon; }
    public int getPointCost() { return pointCost; }
    public Map<Item, Integer> getRequiredMaterials() { return requiredMaterials; }
}