package vitosos.sapphireweapons.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vitosos.sapphireweapons.util.ISapphirePlayerData;

@Mixin(PlayerEntity.class)
public abstract class PlayerStorageMixin implements ISapphirePlayerData {

    // 162 slots = 54 slots (double chest) * 3 pages
    @Unique
    private final SimpleInventory itemBoxInventory = new SimpleInventory(162);

    @Unique
    private int sapphirePoints = 0;

    @Override
    public SimpleInventory getBoxInventory() { return this.itemBoxInventory; }

    @Override
    public int getSapphirePoints() { return this.sapphirePoints; }

    @Override
    public void setSapphirePoints(int points) { this.sapphirePoints = points; }

    @Override
    public void addSapphirePoints(int amount) { this.sapphirePoints += amount; }

    @Unique
    private long lastCantineTime = 0L;

    @Override
    public long getLastCantineTime() {
        return this.lastCantineTime;
    }

    @Override
    public void setLastCantineTime(long time) {
        this.lastCantineTime = time;
    }

    // --- SAVE TO PLAYER FILE ---
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void saveCustomData(NbtCompound nbt, CallbackInfo ci) {
        // Save Points
        nbt.putInt("SapphirePoints", this.sapphirePoints);

        // Cantine Timer
        nbt.putLong("LastCantineTime", this.lastCantineTime);

        // Save Inventory
        NbtList nbtList = new NbtList();
        for (int i = 0; i < this.itemBoxInventory.size(); i++) {
            ItemStack stack = this.itemBoxInventory.getStack(i);
            if (!stack.isEmpty()) {
                NbtCompound itemTag = new NbtCompound();
                itemTag.putByte("Slot", (byte) i);
                stack.writeNbt(itemTag);
                nbtList.add(itemTag);
            }
        }
        nbt.put("ItemBoxInventory", nbtList);
    }

    // --- LOAD FROM PLAYER FILE ---
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void loadCustomData(NbtCompound nbt, CallbackInfo ci) {
        // Load Points
        if (nbt.contains("SapphirePoints")) {
            this.sapphirePoints = nbt.getInt("SapphirePoints");
        }
        // Cantine Timer
        if (nbt.contains("LastCantineTime")) {
            this.lastCantineTime = nbt.getLong("LastCantineTime");
        }

        // Load Inventory
        if (nbt.contains("ItemBoxInventory", NbtElement.LIST_TYPE)) {
            NbtList nbtList = nbt.getList("ItemBoxInventory", NbtElement.COMPOUND_TYPE);
            this.itemBoxInventory.clear();
            for (int i = 0; i < nbtList.size(); i++) {
                NbtCompound itemTag = nbtList.getCompound(i);
                int slot = itemTag.getByte("Slot") & 255;
                if (slot >= 0 && slot < this.itemBoxInventory.size()) {
                    this.itemBoxInventory.setStack(slot, ItemStack.fromNbt(itemTag));
                }
            }
        }
    }
}