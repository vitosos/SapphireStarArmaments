package vitosos.sapphireweapons.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import vitosos.sapphireweapons.item.InsectGlaiveItem;
import vitosos.sapphireweapons.registry.ModScreenHandlers;

public class GuildStockBoxScreenHandler extends ScreenHandler {
    private final PlayerInventory playerInventory;
    public final SimpleInventory repairSlot = new SimpleInventory(1);

    public GuildStockBoxScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ModScreenHandlers.GUILD_STOCK_BOX_SCREEN_HANDLER, syncId);
        this.playerInventory = playerInventory;

        int xOffset = 8;
        int yOffset = 140;

        // Player Inventory (Slots 0 - 26)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, xOffset + col * 18, yOffset + row * 18));
            }
        }

        // Player Hotbar (Slots 27 - 35)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, xOffset + col * 18, yOffset + 58));
        }

        // THE NEW REPAIR SLOT (Slot 36)
        // We spawn it way off-screen at -2000. The Client UI will teleport it in when needed!
        this.addSlot(new Slot(repairSlot, 0, -2000, -2000) {
            @Override
            public boolean canInsert(ItemStack stack) {
                // Only allow damaged Insect Glaives!
                return stack.getItem() instanceof InsectGlaiveItem && stack.isDamaged();
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

            if (invSlot < 36) {
                // If they shift-click a damaged weapon in their inventory, try to send it to the repair slot
                if (originalStack.getItem() instanceof InsectGlaiveItem && originalStack.isDamaged()) {
                    if (!this.insertItem(originalStack, 36, 37, false)) {
                        // If repair slot is full, just do standard inventory swap
                        if (invSlot < 27) { if (!this.insertItem(originalStack, 27, 36, false)) return ItemStack.EMPTY; }
                        else { if (!this.insertItem(originalStack, 0, 27, false)) return ItemStack.EMPTY; }
                    }
                } else {
                    // Standard inventory swap
                    if (invSlot < 27) { if (!this.insertItem(originalStack, 27, 36, false)) return ItemStack.EMPTY; }
                    else { if (!this.insertItem(originalStack, 0, 27, false)) return ItemStack.EMPTY; }
                }
            } else if (invSlot == 36) {
                // Shift-clicking the repair slot sends it back to the player
                if (!this.insertItem(originalStack, 0, 36, true)) return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) { slot.setStack(ItemStack.EMPTY); }
            else { slot.markDirty(); }

            if (originalStack.getCount() == newStack.getCount()) return ItemStack.EMPTY;
            slot.onTakeItem(player, originalStack);
        }
        return newStack;
    }

    // Safely eject the weapon if the player closes the UI!
    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.dropInventory(player, this.repairSlot);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}