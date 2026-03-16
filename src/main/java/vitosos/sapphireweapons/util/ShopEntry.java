package vitosos.sapphireweapons.util; // Adjust package name to your preference!

import net.minecraft.item.ItemStack;

public class ShopEntry {
    private final String id;            // The unique identifier the server uses to know WHAT to do
    private final ItemStack displayIcon; // The icon shown in the UI
    private final String displayName;   // The text shown in the UI
    private final int pointCost;        // How much it costs

    public ShopEntry(String id, ItemStack displayIcon, String displayName, int pointCost) {
        this.id = id;
        this.displayIcon = displayIcon;
        this.displayName = displayName;
        this.pointCost = pointCost;
    }

    public String getId() { return this.id; }
    public ItemStack getDisplayIcon() { return this.displayIcon; }
    public String getDisplayName() { return this.displayName; }
    public int getPointCost() { return this.pointCost; }
}