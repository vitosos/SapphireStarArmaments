package vitosos.sapphireweapons.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import vitosos.sapphireweapons.registry.ModScreenHandlers;

public class ForgeScreenHandler extends ScreenHandler {
    private final PlayerInventory playerInventory;

    public final SimpleInventory output = new SimpleInventory(2);

    public ForgeScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ModScreenHandlers.FORGE_SCREEN_HANDLER, syncId);
        this.playerInventory = playerInventory;

        // Based on your mockup, the player inventory is standard layout at the bottom.
        // We will adjust the exact X/Y coordinates in the Screen class later if needed!
        // Snapped to the left (X) and pushed to the bottom (Y)
        int xOffset = 8;
        int yOffset = 138; // Adjust this if it overlaps with your top UI elements!

        // Player Inventory (27 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, xOffset + col * 18, yOffset + row * 18));
            }
        }

        // Player Hotbar (9 slots)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, xOffset + col * 18, yOffset + 58));
        }

        // Add Slot 1: The Main Weapon Shelf
        this.addSlot(new Slot(output, 0, 280, 185) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false; // Output only!
            }
        });

        // Add Slot 2: The Secondary Shelf
        this.addSlot(new Slot(output, 1, 300, 185) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false; // Output only!
            }
        });
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            // Slots 0-26: Player Main Inventory
            // Slots 27-35: Player Hotbar
            // Slots 36-37: The Forge Output Shelf

            if (invSlot < 36) {
                // Clicking inside the player's inventory just swaps between main and hotbar
                if (invSlot < 27) {
                    if (!this.insertItem(originalStack, 27, 36, false)) return ItemStack.EMPTY;
                } else {
                    if (!this.insertItem(originalStack, 0, 27, false)) return ItemStack.EMPTY;
                }
            } else if (invSlot >= 36) {
                // If they shift-click the shelf, move the weapon to the player's inventory (Slots 0 to 36)
                // The 'true' at the end makes it try to fill the hotbar first!
                if (!this.insertItem(originalStack, 0, 36, true)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            // THE CRASH PREVENTER: If the item stack size didn't change, we couldn't move it.
            // We must return EMPTY to tell the game to stop looping!
            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, originalStack);
        }
        return newStack;
    }

    @Override
    public void onClosed(net.minecraft.entity.player.PlayerEntity player) {
        super.onClosed(player);
        // This safely returns/drops the items on the shelf when the GUI closes!
        this.dropInventory(player, this.output);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}