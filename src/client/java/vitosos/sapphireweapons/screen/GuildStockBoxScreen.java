package vitosos.sapphireweapons.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import vitosos.sapphireweapons.SapphireStarArmaments;
import vitosos.sapphireweapons.config.SapphireConfigManager;
import vitosos.sapphireweapons.mixin.SlotAccessor;
import vitosos.sapphireweapons.network.ServerNetworking;
import vitosos.sapphireweapons.recipe.ForgeRecipe;
import vitosos.sapphireweapons.recipe.ForgeRecipeRegistry;
import vitosos.sapphireweapons.util.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuildStockBoxScreen extends HandledScreen<GuildStockBoxScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(SapphireStarArmaments.MOD_ID, "textures/gui/guild_stock_box.png");

    private ShopCategory currentCategory = ShopCategory.CONSUMABLES;

    private int scrollOffset = 0;
    private final int VISIBLE_ENTRIES = 9;
    private int levelsToBuy = 1;
    private CantineDish selectedDish = null;

    public GuildStockBoxScreen(GuildStockBoxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.backgroundWidth = 450;
        this.backgroundHeight = 230;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - 256) / 2 - 40;
        updateTabState(); // Teleport the slot initially
    }

    // Teleports the Repair Slot based on the active tab!
    private void updateTabState() {
        net.minecraft.screen.slot.Slot repair = this.handler.slots.get(36);
        if (this.currentCategory == ShopCategory.MAINTENANCE) {
            // Slots use relative coordinates, so we don't add this.x or this.y!
            ((SlotAccessor) repair).setX(185);
            ((SlotAccessor) repair).setY(50);
        } else {
            // -2000 is still fine, it just pushes it infinitely far away
            ((SlotAccessor) repair).setX(-2000);
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, 512, 512);

        int tabStartX = this.x + 8;
        int tabStartY = this.y + 37;

        int index = 0;
        for (ShopCategory category : ShopCategory.values()) {
            int currentY = tabStartY + (index * 17);

            if (category == this.currentCategory) {
                context.drawTexture(TEXTURE, tabStartX, currentY, 0, 300, 16, 16, 512, 512);
            } else {
                context.drawTexture(TEXTURE, tabStartX, currentY, 16, 300, 16, 16, 512, 512);
            }

            if (mouseX >= tabStartX && mouseX <= tabStartX + 16 && mouseY >= currentY && mouseY <= currentY + 16) {
                context.drawTooltip(this.textRenderer, Text.literal(category.getDisplayName()), mouseX, mouseY);
            }
            index++;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        context.drawText(this.textRenderer, "The Guild Stock Box", this.x + 15, this.y + 15, 0xFFAA00, true);

        ISapphirePlayerData playerData = (ISapphirePlayerData) this.client.player;
        int points = playerData.getSapphirePoints();
        String pointsText = points + " Pts" ;

        int textWidth = this.textRenderer.getWidth(pointsText);
        int pointsX = this.x + 165 - textWidth;
        int pointsY = this.y + 127;
        context.drawText(this.textRenderer, pointsText, pointsX, pointsY, 0x55FF55, true);

        drawCategoryDescriptionBox(context);
        drawDynamicContent(context, mouseX, mouseY, playerData);
    }

    private void drawCategoryDescriptionBox(DrawContext context) {
        int boxX = this.x + 30;
        int boxY = this.y + 35;
        int boxWidth = 140;
        int boxHeight = 80;

        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xAA000000);

        String desc = "";
        switch (this.currentCategory) {
            case CONSUMABLES: desc = "Stock up on essential potions, powders, and hunting supplies. You can earn points by selling materials using your Item Box."; break;
            case MAINTENANCE: desc = "Place a damaged weapon in the slot. Bring the monster parts from its forging recipe for a huge discount on the repair cost!"; break;
            case EXPERIENCE:  desc = "Exchange your Points for experience levels."; break;
            case CANTINE:     desc = "Purchase long-lasting meals to buff your next hunt."; break;
            case COMING_SOON: desc = "More guild services will be available soon!"; break;
        }

        List<OrderedText> wrappedLines = this.textRenderer.wrapLines(Text.literal(desc), boxWidth - 8);
        for (int i = 0; i < wrappedLines.size(); i++) {
            context.drawText(this.textRenderer, wrappedLines.get(i), boxX + 4, boxY + 4 + (i * 10), 0xAAAAAA, false);
        }
    }

    private void drawDynamicContent(DrawContext context, int mouseX, int mouseY, ISapphirePlayerData playerData) {
        int contentX = this.x + 185;
        int contentY = this.y + 14;

        context.drawText(this.textRenderer, this.currentCategory.getDisplayName(), contentX, contentY, 0xFFAA00, true);

        switch (this.currentCategory) {
            case CONSUMABLES: {
                List<ShopEntry> entries = ShopRegistry.getEntriesForCategory(this.currentCategory);
                if (entries.isEmpty()) return;

                int maxIndex = Math.min(this.scrollOffset + VISIBLE_ENTRIES, entries.size());
                for (int i = this.scrollOffset; i < maxIndex; i++) {
                    ShopEntry entry = entries.get(i);
                    int rowY = contentY + 20 + ((i - this.scrollOffset) * 20);

                    if (mouseX >= contentX && mouseX <= contentX + 160 && mouseY >= rowY && mouseY <= rowY + 18) {
                        context.drawTexture(TEXTURE, contentX, rowY, 0, 250, 160, 18, 512, 512);
                    } else {
                        context.drawTexture(TEXTURE, contentX, rowY, 0, 230, 160, 18, 512, 512);
                    }

                    context.drawItem(entry.getDisplayIcon(), contentX + 2, rowY + 1);
                    context.drawText(this.textRenderer, entry.getDisplayName(), contentX + 22, rowY + 5, 0xFFFFFF, false);

                    int cost = entry.getPointCost();
                    int color = (playerData.getSapphirePoints() >= cost || this.client.player.isCreative()) ? 0x55FF55 : 0xFF5555;
                    String costText = cost + " Pts";
                    int costWidth = this.textRenderer.getWidth(costText);
                    context.drawText(this.textRenderer, costText, contentX + 156 - costWidth, rowY + 5, color, false);
                }
                break;
            }
            case MAINTENANCE: {
                // Draw the specific background panel for this tab
                context.drawTexture(TEXTURE, contentX - 5, this.y + 10, 0, 320, 170, 190, 512, 512);

                int slotBgX = this.x + 184;
                int slotBgY = this.y + 49;

                // Draw a solid dark square and gold border
                context.fill(slotBgX, slotBgY, slotBgX + 18, slotBgY + 18, 0xAA000000);
                context.drawBorder(slotBgX - 1, slotBgY - 1, 20, 20, 0xFFAA00);

                ItemStack repairStack = this.handler.repairSlot.getStack(0);

                // --- Draw the Weapon Name Box! ---
                if (!repairStack.isEmpty()) {
                    int nameBoxX = slotBgX + 22; // Pushed right next to the 18px slot
                    int nameBoxY = slotBgY;
                    int nameBoxWidth = 140;

                    // Center the text horizontally and vertically
                    Text weaponName = repairStack.getName();
                    int textWidth = this.textRenderer.getWidth(weaponName);
                    int textX = nameBoxX + (nameBoxWidth / 2) - (textWidth / 2); // Perfect horizontal center
                    int textY = nameBoxY + 5; // Centers the standard font vertically in an 18px box

                    // Draw it White if damaged, Green if repaired!
                    int nameColor = repairStack.isDamaged() ? 0xFFFFFF : 0x55FF55;
                    context.drawText(this.textRenderer, weaponName, textX, textY, nameColor, false);
                }

                if (repairStack.isEmpty()) {
                    context.drawText(this.textRenderer, "Place a Damaged Weapon Below!", contentX, contentY + 20, 0xAAAAAA, false);
                } else if (!repairStack.isDamaged()) {
                    context.drawText(this.textRenderer, "Weapon is fully repaired!", contentX, contentY + 20, 0x55FF55, false);
                } else {
                    context.drawText(this.textRenderer, "Mending your weapon...", contentX, contentY + 20, 0xFF8000, false);
                    // It is a damaged weapon! Find its recipe.
                    ForgeRecipe targetRecipe = null;
                    for (List<ForgeRecipe> catList : ForgeRecipeRegistry.RECIPES.values()) {
                        for (ForgeRecipe recipe : catList) {
                            if (recipe.getResultWeapon() == repairStack.getItem()) targetRecipe = recipe;
                        }
                    }

                    if (targetRecipe != null) {
                        int baseCost = targetRecipe.getPointCost() * 30;
                        double totalWeight = 0;
                        Map<Item, Integer> validMats = new HashMap<>();

                        for (Map.Entry<Item, Integer> entry : targetRecipe.getRequiredMaterials().entrySet()) {
                            Item mat = entry.getKey();
                            if (mat instanceof SwordItem) continue; // Ignore Glaives!
                            validMats.put(mat, entry.getValue());

                            Rarity r = mat.getDefaultStack().getRarity();
                            totalWeight += (r == Rarity.RARE || r == Rarity.EPIC) ? 50 : (r == Rarity.UNCOMMON ? 35 : 15);
                        }

                        double totalDiscount = 0;
                        int listY = this.y + 90;
                        int index = 0;

                        context.drawText(this.textRenderer, "Materials Used:", contentX, listY - 12, 0xFFFFFF, false);

                        for (Map.Entry<Item, Integer> entry : validMats.entrySet()) {
                            Item mat = entry.getKey();
                            int needed = entry.getValue();
                            int playerHas = countItemAcrossInventories(mat);
                            int consumed = Math.min(needed, playerHas);

                            Rarity r = mat.getDefaultStack().getRarity();
                            double weight = (r == Rarity.RARE || r == Rarity.EPIC) ? 50 : (r == Rarity.UNCOMMON ? 35 : 15);
                            double weightFraction = weight / totalWeight;
                            totalDiscount += weightFraction * ((double) consumed / needed);

                            int yPos = listY + (index * 20);
                            context.drawTexture(TEXTURE, contentX, yPos - 2, 0, 270, 160, 20, 512, 512);

                            context.drawItem(new ItemStack(mat), contentX + 2, yPos);

                            // --- Draw the material name! ---
                            // Placed 22 pixels to the right so it sits perfectly next to the icon
                            context.drawText(this.textRenderer, mat.getName(), contentX + 22, yPos + 4, 0xFFFFFF, false);

                            int color = playerHas >= needed ? 0x55FF55 : 0xFF5555;
                            String countText = playerHas + " / " + needed;
                            int countWidth = this.textRenderer.getWidth(countText);

                            context.drawText(this.textRenderer, countText, contentX + 156 - countWidth, yPos + 4, color, false);
                            index++;
                        }

                        int finalCost = (int) Math.max(0, baseCost * (1.0 - totalDiscount));


                        int bottomSectionY = this.y + 195;

                        // Draw Cost
                        context.drawText(this.textRenderer, "Original Cost: " + baseCost, contentX, bottomSectionY - 12, 0xAAAAAA, false);
                        context.drawText(this.textRenderer, "Final Cost: " + finalCost, contentX, bottomSectionY, 0x55FFFF, false);

                        // Draw "REPAIR" Button Background
                        int btnX = contentX + 120;
                        int btnY = bottomSectionY - 6;

                        boolean hoverBtn = mouseX >= btnX && mouseX <= btnX + 40 && mouseY >= btnY && mouseY <= btnY + 18;


                        context.drawTexture(TEXTURE, btnX, btnY, 170, hoverBtn ? 250 : 230, 40, 18, 512, 512);

                        int repairTextWidth = this.textRenderer.getWidth("REPAIR");
                        // Centering math changed to 20 (half of the new 40px width)
                        context.drawText(this.textRenderer, "REPAIR", btnX + (20 - repairTextWidth / 2), btnY + 5, 0xFFFFFF, false);
                    }
                }
                break;
            }
            case EXPERIENCE: {
                // Draw the specific background panel for this tab
                context.drawTexture(TEXTURE, contentX - 5, this.y + 10, 170, 320, 170, 190, 512, 512);

                int currentLevel = this.client.player.experienceLevel;
                int cost = calculateXpCost(currentLevel, this.levelsToBuy);

                int uiCenterY = this.y + 60;

                // --- 1. CURRENT STATS ---
                context.drawText(this.textRenderer, "Current Level: " + currentLevel, contentX + 35, uiCenterY, 0x55FF55, false);
                context.drawText(this.textRenderer, "Target Level: " + (currentLevel + this.levelsToBuy), contentX + 35, uiCenterY + 15, 0xAAAAAA, false);

                // --- 2. THE SELECTOR BUTTONS (- / +) ---
                int minusX = contentX + 25;
                int plusX = contentX + 115;
                int btnY = uiCenterY + 40;

                // Minus Button
                boolean hoverMinus = mouseX >= minusX && mouseX <= minusX + 20 && mouseY >= btnY && mouseY <= btnY + 20;
                context.drawTexture(TEXTURE, minusX, btnY, 170, hoverMinus ? 290 : 270, 20, 20, 512, 512);

                // Text in the middle
                String buyText = "+" + this.levelsToBuy + " Lvl";
                int buyWidth = this.textRenderer.getWidth(buyText);
                context.drawText(this.textRenderer, buyText, contentX + 80 - (buyWidth / 2), btnY + 6, 0xFFFFFF, false);

                // Plus Button
                boolean hoverPlus = mouseX >= plusX && mouseX <= plusX + 20 && mouseY >= btnY && mouseY <= btnY + 20;
                context.drawTexture(TEXTURE, plusX, btnY, 190, hoverPlus ? 290 : 270, 20, 20, 512, 512);

                // --- 3. THE CHECKOUT SECTION ---
                // Anchor the text and button dynamically beneath the + / - selectors
                int costY = btnY + 30;
                String costText = "Cost: " + cost + " Pts";
                int costWidth = this.textRenderer.getWidth(costText);

                // Centered perfectly below the Lvl text
                context.drawText(this.textRenderer, costText, contentX + 80 - (costWidth / 2), costY, 0x55FFFF, false);

                // "EXCHANGE" Button (Centers the 80px button by subtracting half its width from the 80 midpoint)
                int exBtnX = contentX + 40;
                int exBtnY = costY + 15;
                boolean hoverEx = mouseX >= exBtnX && mouseX <= exBtnX + 80 && mouseY >= exBtnY && mouseY <= exBtnY + 18;

                context.drawTexture(TEXTURE, exBtnX, exBtnY, 210, hoverEx ? 250 : 230, 80, 18, 512, 512);

                int exTextWidth = this.textRenderer.getWidth("EXCHANGE");
                context.drawText(this.textRenderer, "EXCHANGE", exBtnX + (40 - exTextWidth / 2), exBtnY + 5, 0xFFFFFF, false);
                break;
            }
            case CANTINE: {
                // Draw a blank background panel for the Cantine
                context.drawTexture(TEXTURE, contentX - 5, this.y + 10, 350, 320, 160, 190, 512, 512);

                int infoStartY = contentY + 20;

                Text title = this.selectedDish == null ? Text.literal("Choose your Dish") : Text.translatable(this.selectedDish.getTranslationKey());
                context.drawText(this.textRenderer, title, contentX, infoStartY, 0x55FFFF, false);

                // --- 1. DRAW DESCRIPTION & ORDER SECTION (TOP) ---
                if (this.selectedDish != null) {
                    int descY = infoStartY + 14;
                    List<OrderedText> wrappedLines = this.textRenderer.wrapLines(Text.translatable(this.selectedDish.getDescKey()), 160);

                    // Expanded to allow up to 7 lines of description!
                    for (int i = 0; i < Math.min(wrappedLines.size(), 7); i++) {
                        context.drawText(this.textRenderer, wrappedLines.get(i), contentX, descY + (i * 10), 0xAAAAAA, false);
                    }

                    // TPushed the Order Button down by ~32 pixels
                    int orderBtnY = contentY + 108;
                    int orderBtnX = contentX;

                    long lastTime = playerData.getLastCantineTime();
                    long currentTime = this.client.world.getTime();
                    long maxCooldown = vitosos.sapphireweapons.config.SapphireConfigManager.CONFIG.cantineCooldownTicks;
                    long ticksLeft = maxCooldown - (currentTime - lastTime);

                    if (ticksLeft > 0 && !this.client.player.isCreative()) {
                        // COOLDOWN ACTIVE
                        context.drawTexture(TEXTURE, orderBtnX, orderBtnY, 270, 270, 60, 18, 512, 512);
                        context.fill(orderBtnX, orderBtnY, orderBtnX + 60, orderBtnY + 18, 0xAA000000);

                        long secondsLeft = ticksLeft / 20;
                        String timeText = String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60);
                        int timeWidth = this.textRenderer.getWidth(timeText);
                        context.drawText(this.textRenderer, timeText, orderBtnX + (30 - timeWidth / 2), orderBtnY + 5, 0xFF5555, false);
                    } else {
                        // READY TO ORDER
                        boolean hoverOrder = mouseX >= orderBtnX && mouseX <= orderBtnX + 60 && mouseY >= orderBtnY && mouseY <= orderBtnY + 18;
                        context.drawTexture(TEXTURE, orderBtnX, orderBtnY, 210, hoverOrder ? 290 : 270, 60, 18, 512, 512);
                        int orderWidth = this.textRenderer.getWidth("ORDER");
                        context.drawText(this.textRenderer, "ORDER", orderBtnX + (30 - orderWidth / 2), orderBtnY + 5, 0xFFFFFF, false);
                    }

                    // Cost next to button
                    int cost = this.selectedDish.getCost();
                    int color = (playerData.getSapphirePoints() >= cost || this.client.player.isCreative()) ? 0x55FF55 : 0xFF5555;
                    context.drawText(this.textRenderer, cost + " Pts", orderBtnX + 65, orderBtnY + 5, color, false);
                }

                // --- 2. DRAW THE SCROLLABLE LIST (BOTTOM) ---
                // Pushed the list down by 32 pixels!
                int listY = contentY + 137;
                CantineDish[] dishes = CantineDish.values();

                int maxIndex = Math.min(this.scrollOffset + 3, dishes.length);

                for (int i = this.scrollOffset; i < maxIndex; i++) {
                    CantineDish dish = dishes[i];
                    int rowY = listY + ((i - this.scrollOffset) * 20);

                    boolean isHovered = mouseX >= contentX && mouseX <= contentX + 160 && mouseY >= rowY && mouseY <= rowY + 18;
                    boolean isSelected = this.selectedDish == dish;

                    // Button Background
                    context.drawTexture(TEXTURE, contentX, rowY, 0, isSelected ? 250 : 230, 160, 18, 512, 512);
                    if (isHovered && !isSelected) context.fill(contentX, rowY, contentX + 160, rowY + 18, 0x33FFFFFF);

                    // Name (Left) and Cost (Right)
                    context.drawText(this.textRenderer, Text.translatable(dish.getTranslationKey()), contentX + 4, rowY + 5, 0xFFFFFF, false);

                    int cost = dish.getCost();
                    int color = (playerData.getSapphirePoints() >= cost || this.client.player.isCreative()) ? 0x55FF55 : 0xFF5555;
                    String costText = cost + " Pts";
                    int costWidth = this.textRenderer.getWidth(costText);
                    context.drawText(this.textRenderer, costText, contentX + 156 - costWidth, rowY + 5, color, false);
                }
                break;
            }
        }
    }

    private int countItemAcrossInventories(Item item) {
        int count = 0;
        PlayerInventory pockets = this.client.player.getInventory();
        net.minecraft.inventory.Inventory box = ((ISapphirePlayerData) this.client.player).getBoxInventory();
        for (int i = 0; i < pockets.size(); i++) if (!pockets.getStack(i).isEmpty() && pockets.getStack(i).getItem() == item) count += pockets.getStack(i).getCount();
        for (int i = 0; i < box.size(); i++) if (!box.getStack(i).isEmpty() && box.getStack(i).getItem() == item) count += box.getStack(i).getCount();
        return count;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int tabStartX = this.x + 8;
            int tabStartY = this.y + 37;

            int index = 0;
            for (ShopCategory category : ShopCategory.values()) {
                int currentY = tabStartY + (index * 17);
                if (mouseX >= tabStartX && mouseX <= tabStartX + 16 && mouseY >= currentY && mouseY <= currentY + 16) {
                    this.currentCategory = category;
                    this.scrollOffset = 0;
                    this.updateTabState(); // Update slot positions when clicking tabs!
                    this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
                index++;
            }

            int contentX = this.x + 185;
            int contentY = this.y + 14;

            if (this.currentCategory == ShopCategory.CONSUMABLES) {
                List<ShopEntry> entries = ShopRegistry.getEntriesForCategory(this.currentCategory);
                int maxIndex = Math.min(this.scrollOffset + VISIBLE_ENTRIES, entries.size());

                for (int i = this.scrollOffset; i < maxIndex; i++) {
                    int rowY = contentY + 20 + ((i - this.scrollOffset) * 20); // Removed +20 to align hitboxes perfectly with rendering
                    if (mouseX >= contentX && mouseX <= contentX + 160 && mouseY >= rowY && mouseY <= rowY + 18) {
                        ShopEntry entry = entries.get(i);
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeString(this.currentCategory.name());
                        buf.writeString(entry.getId());
                        ClientPlayNetworking.send(ServerNetworking.BUY_SHOP_ITEM_PACKET, buf);
                        this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return true;
                    }
                }
            } else if (this.currentCategory == ShopCategory.MAINTENANCE) {
                ItemStack repairStack = this.handler.repairSlot.getStack(0);

                if (!repairStack.isEmpty() && repairStack.isDamaged()) {
                    ForgeRecipe targetRecipe = null;
                    for (List<ForgeRecipe> catList : ForgeRecipeRegistry.RECIPES.values()) {
                        for (ForgeRecipe recipe : catList) {
                            if (recipe.getResultWeapon() == repairStack.getItem()) targetRecipe = recipe;
                        }
                    }

                    if (targetRecipe != null) {
                        int bottomSectionY = this.y + 195;
                        int btnX = contentX + 120;
                        int btnY = bottomSectionY - 6;

                        if (mouseX >= btnX && mouseX <= btnX + 40 && mouseY >= btnY && mouseY <= btnY + 18) {
                            ClientPlayNetworking.send(new Identifier(SapphireStarArmaments.MOD_ID, "repair_weapon"), PacketByteBufs.empty());
                            this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            return true;
                        }
                    }
                }
            } else if (this.currentCategory == ShopCategory.EXPERIENCE) {
                int uiCenterY = this.y + 60;
                int minusX = contentX + 25;
                int plusX = contentX + 115;
                int btnY = uiCenterY + 40;

                // Clicked Minus
                if (mouseX >= minusX && mouseX <= minusX + 20 && mouseY >= btnY && mouseY <= btnY + 20) {
                    if (this.levelsToBuy > 1) {
                        this.levelsToBuy--;
                        this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    }
                    return true;
                }

                // Clicked Plus
                if (mouseX >= plusX && mouseX <= plusX + 20 && mouseY >= btnY && mouseY <= btnY + 20) {
                    if (this.levelsToBuy < 50) { // Capped at buying 50 levels at once to prevent integer overflow!
                        this.levelsToBuy++;
                        this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    }
                    return true;
                }

                // Clicked Exchange
                int costY = btnY + 30;
                int exBtnX = contentX + 40; // Updated to match the centered X coordinate
                int exBtnY = costY + 15;    // Updated to match the new Y coordinate

                if (mouseX >= exBtnX && mouseX <= exBtnX + 80 && mouseY >= exBtnY && mouseY <= exBtnY + 18) {
                    int currentLevel = this.client.player.experienceLevel;
                    int cost = calculateXpCost(currentLevel, this.levelsToBuy);

                    // Verify they actually have the points on the client side before bothering the server
                    if (this.client.player.isCreative() || ((ISapphirePlayerData) this.client.player).getSapphirePoints() >= cost) {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeString(this.currentCategory.name());
                        buf.writeInt(this.levelsToBuy);
                        ClientPlayNetworking.send(ServerNetworking.BUY_EXPERIENCE_PACKET, buf);

                        this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F));
                    } else {
                        this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.ENTITY_VILLAGER_NO, 1.0F));
                    }
                    return true;
                }
            } else if (this.currentCategory == ShopCategory.CANTINE) {
                // Matched the new list render Y-coordinate
                int listY = contentY + 137;
                int dishIndex = 0;
                CantineDish[] dishes = CantineDish.values();
                int maxIndex = Math.min(this.scrollOffset + 3, dishes.length);

                // Click a dish in the list
                for (int i = this.scrollOffset; i < maxIndex; i++) {
                    int rowY = listY + ((i - this.scrollOffset) * 20);
                    if (mouseX >= contentX && mouseX <= contentX + 160 && mouseY >= rowY && mouseY <= rowY + 18) {
                        this.selectedDish = dishes[i];
                        this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return true;
                    }
                }

                // Click the ORDER button
                if (this.selectedDish != null) {
                    // Matched the new Order Button render Y-coordinate
                    int orderBtnY = contentY + 108;

                    int btnX = contentX;
                    int btnY = orderBtnY;

                    if (mouseX >= btnX && mouseX <= btnX + 60 && mouseY >= btnY && mouseY <= btnY + 18) {
                        long lastTime = ((ISapphirePlayerData) this.client.player).getLastCantineTime();
                        long currentTime = this.client.world.getTime();
                        long maxCooldown = vitosos.sapphireweapons.config.SapphireConfigManager.CONFIG.cantineCooldownTicks;
                        long ticksLeft = maxCooldown - (currentTime - lastTime);

                        if (this.client.player.isCreative() || ticksLeft <= 0) {
                            int cost = this.selectedDish.getCost();
                            if (this.client.player.isCreative() || ((ISapphirePlayerData) this.client.player).getSapphirePoints() >= cost) {
                                PacketByteBuf buf = PacketByteBufs.create();
                                buf.writeString(this.selectedDish.name());
                                ClientPlayNetworking.send(ServerNetworking.BUY_MEAL_PACKET, buf);

                                this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(vitosos.sapphireweapons.registry.ModSounds.HUNTER_PREPARE, 1.0F));
                            } else {
                                this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.ENTITY_VILLAGER_NO, 1.0F));
                            }
                        } else {
                            this.client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.ENTITY_VILLAGER_NO, 1.0F));
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int maxScroll = 0;

        if (this.currentCategory == ShopCategory.CONSUMABLES) {
            List<ShopEntry> entries = ShopRegistry.getEntriesForCategory(this.currentCategory);
            maxScroll = Math.max(0, entries.size() - VISIBLE_ENTRIES);
        } else if (this.currentCategory == ShopCategory.CANTINE) {
            maxScroll = Math.max(0, CantineDish.values().length - 3);
        } else {
            // Other tabs don't scroll
            return super.mouseScrolled(mouseX, mouseY, amount);
        }

        if (amount > 0 && this.scrollOffset > 0) {
            this.scrollOffset--;
        } else if (amount < 0 && this.scrollOffset < maxScroll) {
            this.scrollOffset++;
        }

        return true;
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    }

    // Replicates Minecraft's exact vanilla XP curve!
    private int calculateTotalXpForLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    // Calculates the Sapphire Point cost to bridge the gap
    private int calculateXpCost(int currentLevel, int levelsToAdd) {
        int currentXp = calculateTotalXpForLevel(currentLevel);
        int targetXp = calculateTotalXpForLevel(currentLevel + levelsToAdd);
        int xpDifference = targetXp - currentXp;

        // THE CONVERSION RATE: 3 Points per 1 XP Point.
        int ptsPerXp = SapphireConfigManager.CONFIG.xpToPointsConversionRate;
        return xpDifference * ptsPerXp;
    }
}