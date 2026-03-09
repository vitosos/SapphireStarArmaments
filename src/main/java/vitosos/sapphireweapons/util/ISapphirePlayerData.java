package vitosos.sapphireweapons.util;

import net.minecraft.inventory.SimpleInventory;

public interface ISapphirePlayerData {
    // The 162-slot inventory (3 pages of 54)
    SimpleInventory getBoxInventory();

    // The custom points currency
    int getSapphirePoints();
    void setSapphirePoints(int points);
    void addSapphirePoints(int amount);
}