package vitosos.sapphireweapons.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vitosos.sapphireweapons.SapphireStarArmaments;
import vitosos.sapphireweapons.item.MonsterMaterialItem;
import vitosos.sapphireweapons.mixin.SlotAccessor;
import vitosos.sapphireweapons.network.ServerNetworking;
import vitosos.sapphireweapons.util.ISapphirePlayerData;

import java.util.ArrayList;
import java.util.List;

public class ItemBoxScreen extends HandledScreen<ItemBoxScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(SapphireStarArmaments.MOD_ID, "textures/gui/item_box.png");

    // Client-side Tags for the filters
    private static final TagKey<net.minecraft.item.Item> WEAPONS_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "sapphire_workshop"));
    private static final TagKey<net.minecraft.item.Item> MATS_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier(SapphireStarArmaments.MOD_ID, "forging_materials"));

    private int currentPage = 0;
    private int filterMode = 0; // 0 = All, 1 = Weapons, 2 = Materials

    public ItemBoxScreen(ItemBoxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 330;
        this.backgroundHeight = 225;
        this.playerInventoryTitleY = 128;
        this.playerInventoryTitleX = 80;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        // --- PAGE BUTTONS ---
        this.addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> setPage(currentPage - 1))
                .dimensions(x + 10, y + 40, 20, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> setPage(currentPage + 1))
                .dimensions(x + 50, y + 40, 20, 20).build());

        // --- FILTER BUTTONS ---
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Weapons"), button -> {
            this.filterMode = 1;
            this.currentPage = 0;
            this.updateSlots();
        }).dimensions(x + 10, y + 70, 60, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Mats"), button -> {
            this.filterMode = 2;
            this.currentPage = 0;
            this.updateSlots();
        }).dimensions(x + 10, y + 95, 60, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("All"), button -> {
            this.filterMode = 0;
            this.currentPage = 0;
            this.updateSlots();
        }).dimensions(x + 10, y + 120, 60, 20).build());

        // --- SELL BUTTON ---
        this.addDrawableChild(ButtonWidget.builder(Text.literal("SELL"), button -> {
            ClientPlayNetworking.send(ServerNetworking.SELL_ITEMS_PACKET, PacketByteBufs.empty());
        }).dimensions(x + 261, y + 110, 50, 20).build());

        this.updateSlots();
    }

    private void setPage(int newPage) {
        if (newPage >= 0 && newPage <= 2) {
            this.currentPage = newPage;
            updateSlots();
        }
    }

    // Dynamically packs and pages the inventory!
    private void updateSlots() {
        List<Slot> validSlots = new ArrayList<>();

        // 1. Gather all slots that match the current filter
        for (int i = 0; i < 162; i++) {
            Slot slot = this.handler.slots.get(i);

            if (this.filterMode == 0) {
                validSlots.add(slot); // Show everything, including empty slots
            } else {
                if (slot.hasStack()) {
                    ItemStack stack = slot.getStack();
                    net.minecraft.item.Item item = stack.getItem();

                    // Code-based filter for Weapons!
                    if (this.filterMode == 1 && (item instanceof vitosos.sapphireweapons.item.InsectGlaiveItem || item instanceof vitosos.sapphireweapons.item.KinsectItem)) {
                        validSlots.add(slot);
                    }
                    // Code-based filter for Materials!
                    else if (this.filterMode == 2 && item instanceof vitosos.sapphireweapons.item.MonsterMaterialItem) {
                        validSlots.add(slot);
                    }
                }
            }
        }

        // 2. Banish ALL 162 slots off-screen first
        for (int i = 0; i < 162; i++) {
            ((SlotAccessor) this.handler.slots.get(i)).setX(-2000);
        }

        // 3. Bring back only the 54 slots for the current page
        int startIndex = this.currentPage * 54;
        int endIndex = Math.min(startIndex + 54, validSlots.size());

        for (int i = startIndex; i < endIndex; i++) {
            Slot slot = validSlots.get(i);
            int visualIndex = i - startIndex;

            int row = visualIndex / 9;
            int col = visualIndex % 9;

            ((SlotAccessor) slot).setX(80 + col * 18);
            ((SlotAccessor) slot).setY(18 + row * 18);
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 512, 256);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        ISapphirePlayerData playerData = (ISapphirePlayerData) this.client.player;

        // Custom text elements
        context.drawText(this.textRenderer, "Pts: " + playerData.getSapphirePoints(), 261, 20, 0x55FF55, true);
        context.drawText(this.textRenderer, "Page: " + (currentPage + 1) + "/3", 10, 25, 0xFFFFFF, true);

        // Calculate Sell Grid Value dynamically
        int gridValue = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = this.handler.getSellGrid().getStack(i);
            if (stack.getItem() instanceof MonsterMaterialItem mat) {
                gridValue += (mat.getSellValue() * stack.getCount());
            }
        }
        context.drawText(this.textRenderer, "Value: " + gridValue, 261, 95, 0xFFAA00, true);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}