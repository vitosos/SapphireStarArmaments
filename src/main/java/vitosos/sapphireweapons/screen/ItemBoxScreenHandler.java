package vitosos.sapphireweapons.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import vitosos.sapphireweapons.SapphireStarArmaments;
import vitosos.sapphireweapons.registry.ModScreenHandlers;

public class ItemBoxScreenHandler extends ScreenHandler {
    private final Inventory inventory; // The 162-slot box
    private final Inventory sellGrid;  // The 9-slot sell grid
    private final PlayerInventory playerInventory;

    public static boolean isItemAllowed(ItemStack stack) {
        net.minecraft.item.Item item = stack.getItem();
        return item instanceof vitosos.sapphireweapons.item.InsectGlaiveItem ||
                item instanceof vitosos.sapphireweapons.item.KinsectItem ||
                item instanceof vitosos.sapphireweapons.item.MonsterMaterialItem;
    }

    public ItemBoxScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(162));
    }

    public ItemBoxScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.ITEM_BOX_SCREEN_HANDLER, syncId);
        checkSize(inventory, 162);
        this.inventory = inventory;
        this.playerInventory = playerInventory;
        this.sellGrid = new SimpleInventory(9); // Initialize 3x3 grid
        inventory.onOpen(playerInventory.player);

        // 1. Box Slots (Indices 0 - 161)
        for (int page = 0; page < 3; page++) {
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    this.addSlot(new FilteredSlot(inventory, (page * 54) + (row * 9) + col, 80 + col * 18, 18 + row * 18));
                }
            }
        }

        // 2. Sell Grid Slots (Indices 162 - 170)
        // Pushed far to the right side of the UI
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(sellGrid, col + row * 3, 260 + col * 18, 36 + row * 18));
            }
        }

        // 3. Player Inventory (Indices 171 - 197)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 80 + col * 18, 140 + row * 18));
            }
        }

        // 4. Player Hotbar (Indices 198 - 206)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 80 + col * 18, 198));
        }
    }

    public Inventory getSellGrid() {
        return this.sellGrid;
    }

    public Inventory getBoxInventory() {
        return this.inventory;
    }

    // Drops the sell grid items if the player walks away or closes the UI
    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.dropInventory(player, this.sellGrid);

        if (!player.getWorld().isClient() && player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            vitosos.sapphireweapons.network.ServerNetworking.syncBoxToClient(serverPlayer);
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (invSlot < 171) { // Moving from Box or Sell Grid -> Player
                if (!this.insertItem(originalStack, 171, this.slots.size(), true)) return ItemStack.EMPTY;
            } else { // Moving from Player -> Box
                if (!isItemAllowed(originalStack)) return ItemStack.EMPTY;
                if (!this.insertItem(originalStack, 0, 162, false)) return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();
        }
        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) { return this.inventory.canPlayerUse(player); }

    class FilteredSlot extends Slot {
        public FilteredSlot(Inventory inventory, int index, int x, int y) { super(inventory, index, x, y); }

        @Override
        public boolean canInsert(ItemStack stack) {
            return isItemAllowed(stack); // Uses our new foolproof method!
        }
    }


}